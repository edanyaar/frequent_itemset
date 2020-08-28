import akka.actor.typed.ActorRef;

import java.util.Set;

public class FreqResponse extends Message {
    public int result;
    public Set<Integer> s;

    FreqResponse(int result,Set<Integer> s, ActorRef<Message> replyTo){
        super(null,replyTo);
        this.result = result;
        this.s = s;
    }
}
