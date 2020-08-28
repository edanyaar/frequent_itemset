import akka.actor.typed.ActorRef;

import java.util.HashMap;
import java.util.Set;

public class StartApriori extends Message {

    HashMap<Integer,Integer> bob_f1;

    public StartApriori(String msg, HashMap<Integer,Integer> bob_f1 ,ActorRef<Message> replyTo) {
        super(msg,replyTo);
        this.bob_f1 = bob_f1;
    }

    /*
       HashMap<Set<Integer>,Integer> bob_f1;

    public StartApriori(String msg, HashMap<Set<Integer>,Integer> bob_f1 ,ActorRef<Message> replyTo) {
        super(msg,replyTo);
        this.bob_f1 = bob_f1;
    }
     */

}
