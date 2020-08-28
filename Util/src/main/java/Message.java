import akka.actor.typed.ActorRef;

public class Message implements MySerializable {
	public String msg;
	public ActorRef<Message> replyTo;

	public Message(String msg, ActorRef<Message> replyTo) {
		this.msg = msg;
		this.replyTo = replyTo;
	}

}
