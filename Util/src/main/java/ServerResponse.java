import akka.actor.typed.ActorRef;

public class ServerResponse extends Message {
    public ServerResponse(String msg, ActorRef<Message> replyTo) {
        super(msg,replyTo);
    }
}
