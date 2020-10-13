import java.util.HashMap;

public class Record {

    private static final HashMap<String, Integer> namesToIdx = new HashMap<>();
    public static final String[] numNames = new String[]{"LIT101"};
    public static final String[] boolNames = new String[]{"FIT101", "MV101"};
    static {
        int i = 0;
        for (String name : boolNames) {
            namesToIdx.put(name, i);
            ++i;
        }
        i = 0;
        for (String name: numNames) {
            namesToIdx.put(name, i);
            ++i;
        }
    }

    private final boolean[] boolVars;
    private final double[] numVars;

    public Record(boolean[] boolValues, double[] numValues) {
        this.boolVars = new boolean[boolValues.length];
        this.numVars = new double[numValues.length];
        System.arraycopy(boolValues, 0, this.boolVars, 0, boolValues.length);
        System.arraycopy(numValues, 0, this.numVars, 0, numValues.length);
    }

    public Boolean getBool(String var) {
        return this.boolVars[namesToIdx.get(var)];
    }

    public Double getNum(String var) {
        return this.numVars[namesToIdx.get(var)];
    }
}
