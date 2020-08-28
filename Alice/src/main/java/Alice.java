import akka.actor.Address;
import akka.actor.AddressFromURIString;
import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.actor.typed.receptionist.Receptionist;
import akka.cluster.typed.Cluster;
import akka.cluster.typed.JoinSeedNodes;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;


import java.io.File;
import java.io.FileReader;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;


public class Alice extends AbstractBehavior<Message> {

    private ActorRef<Message> bob = null;
    private ActorRef<Receptionist.Listing> listingResponse ;
    private AlgSelection.Algorithm alg;
    private DBSelection.DB db_name;
    private List<int[]> db = new ArrayList<>();
    private int alice_record_len;
    private double min_supp = 0;
    private List<HashMap<Set<Integer>,Integer>> Fs = new ArrayList<>(); //the result of the algorithm
    private int k = 2;  //for keeping track of which step of the algorithm we are on
    private AtomicInteger freq_count;
    private ResultsPrinter printer;
    private Deserializer deserializer;

    Paillier paillier;

    BigInteger[] R;
    int [][] A;
    long seed;
    private HashMap<Set<Integer>,BigInteger[]> y_vectors = new HashMap<>();


    static Behavior<Message> create() {
        return Behaviors.setup(Alice::new);
    }

    private Alice(ActorContext<Message>  context) {
        super(context);
        this.listingResponse  = context.messageAdapter(Receptionist.Listing.class, Alice.ListingResponse::new);
    }

    public Behavior<Message> shutdown(ShutDown cmd){
        return Behaviors.stopped();
    }

    @Override
    public Receive<Message>  createReceive() {
        return newReceiveBuilder()
                .onMessage(UserInput.class, Alice::db_selection_pred, this::connection_handler)
                .onMessage(UserInput.class, Alice::algorithem_selection_pred, this::alg_select)
                .onMessage(UserInput.class, Alice::min_sup_selection_pred, this::min_sup_selection)
                .onMessage(ListingResponse.class, response -> handel_connect(response.listing))
                .onMessage(StartApriori.class,this::generate_f1_alice)
                .onMessage(CandidateGeneration.class,this::generate_candidates)
                .onMessage(FreqCount.class,this::count_freqs)
                .onMessage(Freq.class,this::count_freq)
                .onMessage(FreqResponse.class,this::update_freq)
                .onMessage(HomomorphicResponse.class,this::decrypt_homomorphic)
                .onMessage(VCytag.class,this::assemble_ytag_vector)
                .onMessage(VCS.class,this::calc_freq_vc)
                .onMessage(ShutDown.class,this::shutdown)
                .build();
    }

    //////////////////////////////input predicates ////////////////////////////////////////////


    public static boolean db_selection_pred(UserInput cmd){
        return cmd.msg.equals("1") || cmd.msg.equals("2");
    }
    public static boolean algorithem_selection_pred(UserInput cmd){
        return cmd.msg.equals("vc") || cmd.msg.equals("h");
    }

    public static boolean min_sup_selection_pred(UserInput cmd){
        return Double.parseDouble(cmd.msg) > 0 && Double.parseDouble(cmd.msg) < 1;
    }


    ////////////////////////////// Apriori Gen ////////////////////////////////////////////////

    /**
     * initialization of Apriori algorithm - finding all frequent-1 ItemSets in the DB
     * @param sa - contains all frequent-1 itemSets in Bob's share of the DB {@link Bob#generate_f1_Bob}
     */
    protected Behavior<Message> generate_f1_alice (StartApriori sa){
        HashMap<Set<Integer>,Integer> bob_f1 = deserializer.des_startApriori(sa);
        int[] counters = new int[alice_record_len];
        HashMap<Set<Integer>,Integer> f1 = new HashMap<>();
        Fs.add(new HashMap<>()); //to sort out indices (so they start at 1 instead of 0)

        for (int[] record : db) {
            for(int i = 1; i<alice_record_len;i++){
                counters[i] += record[i];
            }
        }
        //find which ones pass min support
        for (int i = 1;i<counters.length;i++) {
            double c = ((double)counters[i]) / ((double) db.size());
            if(c>min_supp){
                f1.put(new TreeSet<Integer>(Collections.singleton(i)),counters[i]);
            }
        }

        //merge with bob's result
        bob_f1.forEach((key, value) -> f1.merge(key, value,(v1,v2) -> value));
        Fs.add(f1);
        getContext().getSelf().tell(new CandidateGeneration());
        return this;
    }


    /**
     * generates the set of candidate k-itemsets, from Fk-1, the set of frequent (k-1)-itemsets found in the previous step.
     * if algorithm has completed, display results
     */
    protected Behavior<Message> generate_candidates (CandidateGeneration cg){
        if(Fs.get(k-1) != null) {
            List<Set<Integer>> fk_1 = new ArrayList<>(Fs.get(k - 1).keySet());
            HashMap<Set<Integer>, Integer> fk = new HashMap<>();

            for (int i = 0; i < fk_1.size(); i++) {
                for (int j = 1; j < fk_1.size(); j++) {
                    Set<Integer> candidate = new TreeSet<>(fk_1.get(i));
                    candidate.addAll(fk_1.get(j));
                    if (candidate.size() == k && candidate_pruning(candidate)) {
                        fk.put(candidate, null);
                    }
                }
            }
            if (!fk.isEmpty()) {
                freq_count = new AtomicInteger(fk.size());
                Fs.add(fk);
                getContext().getSelf().tell(new FreqCount());
            } else { //no candidates -> stop
                printer.print_results(Fs);
            }
        }
        else{ // f_k-1 is empty -> stop
            printer.print_results(Fs);
        }
        return this;
    }

    /**
     * prune the given candidates based on the following logic:
     *          a necessary condition of candidate to be frequent is that each of its (k-1)-itemset is frequent
     * @param candidate - a set of candidates to be pruned
     * @return true if the candidate passed pruning, false otherwise.
     */
    protected Boolean candidate_pruning(Set<Integer> candidate){
        List<Integer> c = new ArrayList<>(candidate);
        for(int i = 0; i<candidate.size(); i++){
            Integer temp = c.remove(i);
            if(!Fs.get(k-1).containsKey(new TreeSet<>(c))){
                return false;
            }
            c.add(i,temp);
        }
        return true;
    }

    /**
     * for each itemset in Ck - split it into frequencies that can and cannot be computed locally
     * @param inst - contains the set of candidates
     */
    protected Behavior<Message> count_freqs (FreqCount inst){
        for (Set<Integer> c : Fs.get(k).keySet()) {

            Iterator<Integer> it = c.iterator();

            Set<Integer> local = new TreeSet<>();
            Set<Integer> non_local = new TreeSet<>();

            while(it.hasNext()){
                    Integer i = it.next();
                    if(i >= alice_record_len){
                        non_local.add(i);
                    }
                    else{
                        local.add(i);
                    }
            }
            getContext().getSelf().tell(new Freq(local,non_local,null));
        }
        return this;
    }

    /**
     * count the frequency of the itemset given as input via one of three methods:
     *        [1] if the itemset contains only local vectors - compute it locally
     *        [2] if the itemset contains only remote vectors - send a request for remote computation {@link Bob#count_freq}
     *        [3] if the itemset contains both local as well as remote vectors - invoke the selected privacy preserving communication algorithm
     * @param f - contains an itemset that may or may not be computable locally
     */
    protected Behavior<Message> count_freq (Freq f){
        if(f.local.isEmpty()){
            bob.tell(new Freq(f.non_local,null,getContext().getSelf()));
        }
        else if (f.non_local.isEmpty()){
            int result = VectorOps.sum_vector(VectorOps.generate_x_vector(f.local,db));
            getContext().getSelf().tell(new FreqResponse(result,f.local,getContext().getSelf()));
        }
        else{
            comm_bob(f);
        }
        return this;
    }

    /**
     * add the given itemset to F-k if its frequency is larger than the minimum support
     * once finished checking all candidates, proceed to next step of algorithm - generating C-k+1
     * @param f - contains the candidate itemset and its frequency, received from {@link Bob#count_freq} or {@link #count_freq}
     */
    protected Behavior<Message> update_freq(FreqResponse f){
        double c = ((double)f.result) / ((double) db.size());
        if (c>min_supp) {
            Fs.get(k).put(f.s, f.result);
        }
        else{
            Fs.get(k).remove(f.s);
        }
        int counter = freq_count.decrementAndGet();
        if (counter == 0) {//counting done for all potential candidates.
            k++;
            getContext().getSelf().tell(new CandidateGeneration());
        }
        return this;
    }


    //////////////////////////// Privacy Preserving Communication ///////////////////////////////////

    /**
     * forward the communication request to the correct communication function based on the selected communication algorithm
     */
    private void comm_bob (Freq f){
        if(alg == AlgSelection.Algorithm.CLIFTON){
            comm_clifton(f);
        }
        else{
            comm_homomorphic(f);
        }
    }

    /**
     * generates n/2 random numbers and a matrix of size n x n/2 with linearly independent rows
     */
    private void setup_clifton(){
        R = new BigInteger[db.size()/2];
        Random rand = new Random();
        Arrays.fill(R,new BigInteger(Integer.toString(rand.nextInt())));
        A = generate_A_matrix();
    }

    /**
     * Generate a matrix of values that form coefficients of linear independent equations by:
     *          [1] generating a matrix with random numbers - rand_a
     *          [2] For every row i, replace the diagonal element with the sum of the absolute values of elements of the corresponding row in rand_a
     *              (the remaining values remain unchanged).such a matrix is diagonally dominant => is nonsingular => Linearly independent rows
     * @return a matrix of values that form coefficients of linear independent equations.
     */
    private int[][] generate_A_matrix(){
        int [][]rand_a = new int[db.size()][db.size()/2];
        Random temprand = new Random();
        seed = temprand.nextLong();
        Random rand = new Random(seed);

        for(int i = 0 ;i < db.size(); i++){
            for(int j = 0; j<db.size()/2; j++){
                rand_a[i][j] = rand.nextInt();
            }
        }

        int[][] a_matrix = new int[db.size()][db.size()/2];
        for(int i = 0 ;i < db.size(); i++){
            for(int j = 0; j<db.size()/2; j++){
                if(i == j){ //diagonal
                    int new_diagonal = 0;
                    for(int k = 0; k<db.size()/2; k++){
                        new_diagonal += Math.abs(rand_a[i][k]);
                    }
                    a_matrix[i][j] = new_diagonal;
                }
                else a_matrix[i][j] = rand_a[i][j];
            }
        }

        return a_matrix;
    }

    /**
     * Generate each entry of the X' vector and send it to bob {@link Bob#assemble_x_vector} in order to compute the dot product
     * @param f - contains:
     *                      [1] the local part of the itemset being examined - in order to generate local X vector
     *                      [2] the part of the itemset found on Bob's end - so he can compute the dot product
     */
    private void comm_clifton(Freq f){
        int[] x = VectorOps.generate_x_vector(f.local,db);
        for (int i=0; i<x.length; i++) {
            BigInteger xi = new BigInteger(Integer.toString(x[i]));
            for (int j = 0; j< R.length; j++){
                xi = xi.add(R[j].multiply(new BigInteger(Integer.toString(A[i][j]))));
            }
            bob.tell(new XVector(f.non_local, f.local,xi,i,getContext().getSelf()));
        }
    }

    /**
     * Take an entry in a Y' vector sent by bob and place it at the appropriate place
     * (based on index and the key given by the itemsets recieved from Bob) in the y_vector Hashmap.
     * Creates a new vector in the Hashmap if needed.
     * @param yVector - contains an entry in the Y' vector for the given ItemSet at given index
     */
    private Behavior<Message> assemble_ytag_vector(VCytag yVector){
        if(yVector.index == 0){
            BigInteger [] v = new BigInteger[db.size()];
            y_vectors.put(yVector.set,v);
        }
        y_vectors.get(yVector.set)[yVector.index] = yVector.yi;
        return this;
    }

    /**
     * Computes the dot product of the set in vcs.set using the S and Y' values received from Bob and the A Matrix + R Vector,
     * in accordance with the Vaidya-Clifton Algorithm.
     * @param vcs - contains the set being examined and the S value calculated by Bob (Assumes Y' has already been fully delivered)
     */
    private Behavior<Message> calc_freq_vc (VCS vcs){
        BigInteger diff = new BigInteger("0");
        BigInteger[] ytag = y_vectors.get(vcs.set);
        for (int i = 0; i<R.length; i++) {
            diff = diff.add(R[i].multiply(ytag[i]));
        }

        int result = vcs.S.subtract(diff).intValue();

        y_vectors.remove(vcs.set);

        getContext().getSelf().tell(new FreqResponse(result,vcs.set,getContext().getSelf()));
        return this;
    }


    /**
     * saves a local instance of the encrypt/decrypt module, and returns just the encryption module (for sending to Bob)
     * @return - an Encryptor for the local instance of the cryptographic module
     */
    private Encryptor setup_homomorphic(){
        paillier = new Paillier();
        return paillier.generate_encryptor();
    }

    /**
     * Encrypt each entry of the local X vector and send it to bob {@link Bob#assemble_x_vector} in order to compute the dot product
     * @param f - contains:
     *                      [1] the local part of the itemset being examined - in order to generate local X vector
     *                      [2] the part of the itemset found on Bob's end - so he can compute the dot product
     */
    private void comm_homomorphic(Freq f){
        int[] x = VectorOps.generate_x_vector(f.local,db);
        for (int i=0; i<x.length; i++) {
            BigInteger xi = paillier.Encryption(new BigInteger(Integer.toString(x[i])));
            bob.tell(new XVector(f.non_local, f.local,xi,i,getContext().getSelf()));
        }
    }

    /**
     * decrypt the message received from Bob (containing a requested dot product) and forward the result to {@link #update_freq}
     * @param h - contains the encrypted dot product from bob and the itemset it is relevant to
     */
    private Behavior<Message> decrypt_homomorphic (HomomorphicResponse h){
        int result = paillier.Decryption(h.result).intValue();
        getContext().getSelf().tell(new FreqResponse(result,h.s,getContext().getSelf()));
        return this;
    }

    /////////////////////////// Message Handlers /////////////////////////////////////////////


    // Connecting to Bob and Setup


    /***
     * Request Bob ActorRef and load selected db into memory
     */
    private Behavior<Message>  connection_handler(UserInput cmd) {
        List<Address> seedNodes = new ArrayList<>();
        seedNodes.add(AddressFromURIString.parse("akka://MessageApp@127.0.0.1:2551"));
        Cluster.get(getContext().getSystem()).manager().tell(new JoinSeedNodes(seedNodes));
        getContext().getSystem().receptionist().tell(Receptionist.find(Bob.sk ,listingResponse));
        db_selection(cmd);
        return this;
    }

    /**
     * attempt to retrieve bob actorRef, and send him selected db choice
     * @param msg - a Listing that should contain Bob's ActorRef if he is online
     */
    private Behavior<Message> handel_connect(Receptionist.Listing msg){
        Set<ActorRef<Message>> result = msg.getServiceInstances(Bob.sk);
        if (result.size() == 1){
            bob = result.iterator().next();
            bob.tell(new DBSelection(null,db_name,getContext().getSelf()));

            // communication method selection
            System.out.println("Please Select A Privacy Algorithm:");
            System.out.println("   [vc] Vaidya Clifton");
            System.out.println("   [h] Homomorphic");
        }
        return this;
    }


    /**
     * save selected algorithm choice and send it to bob
     */
    private Behavior<Message> alg_select(UserInput cmd){
        if(cmd.msg.equals("vc")){
            alg = AlgSelection.Algorithm.CLIFTON;
            bob.tell(new AlgSelection(null,alg,null,getContext().getSelf()));
            setup_clifton();
            bob.tell(new VCAmatrix(null,seed,getContext().getSelf()));
        }
        else{
            alg = AlgSelection.Algorithm.HOMOMORPHIC;
            Encryptor e = setup_homomorphic();
            bob.tell(new AlgSelection(null,alg,e,getContext().getSelf()));
        }

        // minimum support selection
        System.out.println("Please Select Minimum support [0-1]:");

        return this;
    }

    /**
     * save selected minimum support and send it to bob
     */
    private Behavior<Message> min_sup_selection(UserInput cmd){
        min_supp = Double.parseDouble(cmd.msg);
        System.out.println(LocalDateTime.now());
        bob.tell(new MinSuppSelection(null,min_supp,getContext().getSelf()));
        return this;
    }



    //////////////////////////////// CSV Processing ///////////////////////////////////////////////////////

    private void db_selection (UserInput cmd){
        if(cmd.msg.equals("1")){
            db_name = DBSelection.DB.CMC;
            load_db("cmcAlice.csv");
        }
        else{
            db_name = DBSelection.DB.HD;
            load_db("heartDiseaseDataAlice.csv");
        }
        deserializer = new Deserializer();
        printer = new ResultsPrinter(db_name);
    }

    /**
     * loads db into memory and parses the records from strings to integers
     * @param path - the name of the database file found in resources folder
     */
    private void load_db(String path){
        try {
            ClassLoader classLoader = ClassLoader.getSystemClassLoader();
            File db_file = new File(classLoader.getResource(path).getFile());
            FileReader filereader = new FileReader(db_file);
            CSVReader csvReader = new CSVReaderBuilder(filereader).withSkipLines(1).build();
            List<String[]> db_as_string =  csvReader.readAll();
            for (String[] record: db_as_string) {
                db.add(Arrays.stream(record).mapToInt(Integer::parseInt).toArray());
            }
            alice_record_len = db.get(0).length;
        }
        catch(Exception e){
            System.out.println(e.getMessage());
        }
    }



    /////////////////////////////// Internal Message Types ///////////////////////////////////////////////

    private class ListingResponse extends Message {
        final Receptionist.Listing listing;

        private ListingResponse(Receptionist.Listing listing) {
            super(null,getContext().getSelf());
            this.listing = listing;
        }
    }

    private class CandidateGeneration extends Message {
        private CandidateGeneration() {
            super(null,getContext().getSelf());
        }
    }

    private class FreqCount extends Message {
        private FreqCount() {
            super(null,getContext().getSelf());
        }
    }
}
