import akka.actor.typed.ActorRef;

import java.math.BigInteger;
import java.util.Set;

public class XVector extends Message {
    public Set<Integer> local;
    public Set<Integer> non_local;
    public BigInteger xi;
    public int index;
    public XVector(Set<Integer> local, Set<Integer> non_local, BigInteger xi, int index, ActorRef<Message> replyTo) {
        super(null,replyTo);
        this.local = local;
        this.non_local = non_local;
        this.xi = xi;
        this.index = index;
    }
}
