import akka.actor.typed.ActorRef;

import java.math.BigInteger;
import java.util.Set;

public class HomomorphicResponse extends Message{
    public BigInteger result;
    public Set<Integer> s;

    HomomorphicResponse(BigInteger result,Set<Integer> s, ActorRef<Message> replyTo){
        super(null,replyTo);
        this.result = result;
        this.s = s;
    }
}
