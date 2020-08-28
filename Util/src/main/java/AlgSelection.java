import akka.actor.typed.ActorRef;

public class AlgSelection extends Message {
    public enum Algorithm {CLIFTON,HOMOMORPHIC};
    public Algorithm algorithm;
    public Encryptor encryptor;

    AlgSelection(String msg, Algorithm algorithm,Encryptor encryptor, ActorRef<Message> replyTo){
        super(msg,replyTo);
        this.algorithm = algorithm;
        this.encryptor = encryptor;
    }
}
