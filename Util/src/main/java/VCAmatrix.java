import akka.actor.typed.ActorRef;

public class VCAmatrix extends Message {
    public long seed;

    VCAmatrix(String msg,long seed, ActorRef<Message> replyTo){
        super(msg,replyTo);
        this.seed = seed;

    }
}
