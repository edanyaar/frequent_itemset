import java.time.LocalDateTime;
import java.util.*;

public class ResultsPrinter {

   DBSelection.DB db;
   float dbSize;

    public ResultsPrinter (DBSelection.DB db){
        this.db = db;
        if(db == DBSelection.DB.CMC) {
            dbSize= 1473;
        }
        else if(db == DBSelection.DB.HD){
            dbSize= 303;
        }
    }

    public void print_results(List<HashMap<Set<Integer>,Integer>> results){
        System.out.println(LocalDateTime.now());
        int i = 0;
        for (HashMap<Set<Integer>,Integer> h:results) {

            Iterator<Set<Integer>> sets = h.keySet().iterator();
            Iterator<Integer> freqs = h.values().iterator();
            if(sets.hasNext()) {
                System.out.println(String.format("F-%d:",i));
                while (sets.hasNext()) {
                    System.out.println(String.format("Set: %-60s   support: %f", set_toString(sets.next()), freqs.next()/dbSize));
                }
            }
            System.out.println();
            i++;
        }
    }

    private String set_toString(Set<Integer> s){
        switch (db){
            case CMC:
                return cols_toString(s, CMC_COLS);
            case HD:
                return cols_toString(s, HD_COLS);
            default:
                return "";
        }
    }

    private String cols_toString(Set<Integer> s, String[] columns){
        String output = "[";
        for (Integer i:s) {
            output = output.concat(columns[i] + ", ");
        }
        output = output.substring(0,output.length()-2);
        output = output.concat("]");
        return output;
    }

    private String [] CMC_COLS = {
            "ID",
            "Wife_Age_Teen",
            "Wife_Age_20's",
            "Wife_Age_30's",
            "Wife_Age_40's",
            "Wife's_Education_Very_Low",
            "Wife's_Education_Low",
            "Wife's_Education_Medium",
            "Wife's_Education_High",
            "Husband's_Education_Very_Low",
            "Husband's_Education_Low",
            "Husband's_Education_Medium",
            "Husband's_Education_High",
            "Children_0to3",
            "Children_4to7",
            "Children_8to11",
            "Children_12to16",
            "Islamic",
            "not_Islamic",
            "Wife unemployed",
            "Wife employed",
            "Living_Standard_Very_Low",
            "Living_Standard_Low",
            "Living_Standard_Medium",
            "Living_Standard_High",
            "no media exposure",
            "media exposure",
            "No_Contraceptive",
            "Long_Term_Contraceptive",
            "Short_Term_Contraceptive"
    };

    private String [] HD_COLS = {
            "ID",
            "Age_20to40",
            "Age_41to50",
            "Age_51to60",
            "Age_61to80",
            "male",
            "female",
            "no_chest_pain",
            "chest_pain_type_1",
            "chest_pain_type_2",
            "chest_pain_type_3",
            "restBps_94-115",
            "restBps_115-136",
            "restBps_136-157",
            "restBps_157-200",
            "chol_126-213",
            "chol_213-301",
            "chol_301-564",
            "blood_sugar>120",
            "blood_sugar<120",
            "max_heartrate_71-123",
            "max_heartrate_123-149",
            "max_heartrate_149-175",
            "max_heartrate_175-202",
            "exercise_induced_angina",
            "no_exercise_induced_angina",
            "no_heart_disease",
            "heart_disease",
    };
}
