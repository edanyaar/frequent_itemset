import akka.actor.typed.ActorRef;

import java.math.BigInteger;
import java.util.Set;

public class VCS extends Message {

    public BigInteger S;
    public Set<Integer> set;

    VCS(BigInteger S,Set<Integer> set, ActorRef<Message> replyTo){
            super(null,replyTo);
            this.S = S;
            this.set = set;
    }
 }
