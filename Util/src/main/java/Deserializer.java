import java.util.Collections;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

public class Deserializer {

    public HashMap<Set<Integer>,Integer>  des_startApriori(StartApriori msg){
        HashMap<Integer,Integer> original = msg.bob_f1;
        HashMap<Set<Integer>,Integer> output = new HashMap<>();
        for (Integer i:original.keySet()) {
            output.put(new TreeSet<Integer>(Collections.singleton(i)),original.get(i));
        }
        return output;
    }
}
