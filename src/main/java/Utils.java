import mathelement.Inequality;
import mathelement.InequalitySystem;
import mathelement.Operator;

import java.util.ArrayList;
import java.util.Arrays;

public abstract class Utils {
    public static double compressDouble(double value) {
        int valueInt = (int) Math.round(value * 100);
        return ((double) valueInt) / 100;
    }

    public static void addData(InequalitySystem inequalitySystem){
        inequalitySystem.add(new Inequality(new ArrayList<>(Arrays.asList(+3.0, +3.0)), Operator.GREATER_OR_EQUAL, 4));
        inequalitySystem.add(new Inequality(new ArrayList<>(Arrays.asList(+2.0, +4.0)), Operator.LESS_OR_EQUAL, 5));
        inequalitySystem.add(new Inequality(new ArrayList<>(Arrays.asList(+1.0, +3.0)), Operator.LESS_OR_EQUAL, 6));
        inequalitySystem.add(new Inequality(new ArrayList<>(Arrays.asList(+0.0, +1.0)), Operator.LESS_OR_EQUAL, 5));
    }
}