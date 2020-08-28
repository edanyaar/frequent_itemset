import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.KeyDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Set;

public class SetDeserializer extends KeyDeserializer {

    private static ObjectMapper mapper = new ObjectMapper();

    public Object deserializeKey(String key, DeserializationContext ctxt) throws IOException {
        return mapper.readValue(key, Set.class);
    }
    /*
        try {
            String[] s = key.split(",");
            s[0] = s[0].replace('[', ' ');
            s[s.length - 1] = s[s.length - 1].replace(']', ' ');

            Set<Integer> output = new TreeSet<>();
            for (String i : s) {
                output.add(Integer.parseInt(i));
            }
            return output;
        }
        catch (Exception e){
            throw new IOException("deserializing set failed");
        }
     */
}
