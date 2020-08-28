import akka.actor.typed.ActorRef;

public class ShutDown extends Message {

    public ShutDown(String msg, ActorRef<Message> replyTo){
        super(msg,replyTo);
    }
}
