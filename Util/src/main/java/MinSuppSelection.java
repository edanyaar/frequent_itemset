import akka.actor.typed.ActorRef;

public class MinSuppSelection extends Message{
    public Double min_supp;

    MinSuppSelection(String msg, Double min_supp, ActorRef<Message> replyTo){
        super(msg,replyTo);
        this.min_supp = min_supp;
    }
}
