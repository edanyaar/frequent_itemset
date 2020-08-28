import akka.actor.typed.ActorRef;

import java.util.EnumMap;

public class DBSelection extends Message {
    public enum DB {CMC,HD};
    public DB db;
    public EnumMap<DB,Integer> alice_db_sizes = new EnumMap<DB, Integer>(DB.class);

    DBSelection(String msg, DB db, ActorRef<Message> replyTo){
        super(msg,replyTo);
        this.db = db;
        alice_db_sizes.put(DB.CMC, 27);
        alice_db_sizes.put(DB.HD, 7);
    }
}
