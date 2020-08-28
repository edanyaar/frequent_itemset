import akka.actor.typed.ActorRef;

public class ServerConnectionResponse extends Message {
	public ServerConnectionResponse(String msg, ActorRef<Message> replyTo) {
		super(msg,replyTo);
	}

}
