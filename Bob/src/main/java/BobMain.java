
import akka.actor.typed.ActorSystem;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.util.Scanner;

public class BobMain {

	public static void main(String[] args) {

		Scanner sc = new Scanner(System.in);
		Config conf = ConfigFactory.load("application.conf");
		final ActorSystem<Message> system = ActorSystem.create(Bob.create(), "PPDM", conf);

		 try {
	            while (true) {
	            	if(sc.nextLine().contains("/shutdown")){
						system.tell(new ShutDown(null,null));
						break;
					}
	            }
	        }
	        catch (Exception e){
	            System.out.println(e.getMessage());
	        }
  }
}
