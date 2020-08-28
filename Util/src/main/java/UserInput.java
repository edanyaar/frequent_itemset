import akka.actor.typed.ActorRef;

public class UserInput extends Message {

    public ActorRef replyTo;

    public UserInput(String msg, ActorRef<Message> replyTo) {
        super(msg,replyTo);
    }
}
