import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class VectorOps {

    public static int[] generate_x_vector (Set<Integer> local, List<int[]> db){
        int [] output = new int[db.size()];
        Arrays.fill(output,1);
        for (int record = 0; record<db.size(); record++) {
            for (Integer col: local) {
                output[record] = output[record]*db.get(record)[col];
            }
        }
        return output;
    }

    public static int sum_vector (int[] x){
        int output = 0;
        for (Integer i : x) {
            output+=i;
        }
        return output;
    }

}
