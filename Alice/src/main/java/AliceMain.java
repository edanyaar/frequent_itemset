import akka.actor.typed.ActorSystem;
import java.util.Scanner;


public class AliceMain {



    public static void main(String [] args){
        Scanner sc = new Scanner(System.in);
        final ActorSystem<String> system = ActorSystem.create(PPDM.create(),"PPDM");

        //DB Selection
        System.out.println("Please Select A DataBase:");
        System.out.println("   [1]  Contraceptive Method Choice In Indonesia");
        System.out.println("   [2]  Cleveland Heart Disease Database");


        try {
            while (true) {
                String next = sc.nextLine();
                system.tell(next);
                if(next.contains("/shutdown")) {
                    break;
                }
            }
        }
        catch (Exception e){
            System.out.println(e);
        }
    }
}
