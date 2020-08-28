import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;


public class PPDM extends AbstractBehavior<String> {

    private  ActorRef<Message> alice;

    static Behavior<String> create() {
        return Behaviors.setup(PPDM::new);
    }

    private PPDM(ActorContext<String> context) {
        super(context);
        alice = getContext().spawn(Alice.create(), "Alice");
    }

    @Override
    public Receive<String> createReceive() {
        return newReceiveBuilder().onAnyMessage(this::onstart).build();
    }

    /***
     * take input string from user and pass it on (wrapped) to the Alice actor
     * @param cmd - input string from user
     */
    private Behavior<String> onstart(String cmd) {
        if(cmd.contains("/shutdown")){
            alice.tell(new ShutDown(null,null));
            return Behaviors.stopped();
        }
        alice.tell(new UserInput(cmd,null));
        return this;
    }
}
