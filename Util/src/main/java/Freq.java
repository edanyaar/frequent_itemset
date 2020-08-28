import akka.actor.typed.ActorRef;

import java.util.Set;

public class Freq extends Message {
    public Set<Integer> local;
    public Set<Integer> non_local;
    public Freq(Set<Integer> local, Set<Integer> non_local, ActorRef<Message> replyTo) {
        super(null,replyTo);
        this.local = local;
        this.non_local = non_local;
    }
}