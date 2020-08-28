import akka.actor.typed.ActorRef;

import java.math.BigInteger;
import java.util.Set;

public class VCytag extends Message {

    public BigInteger yi;
    public int index;
    public Set<Integer> set;

    VCytag(BigInteger yi,int index,Set<Integer> set, ActorRef<Message> replyTo){
        super(null,replyTo);
        this.yi = yi;
        this.index = index;
        this.set = set;
    }
}
