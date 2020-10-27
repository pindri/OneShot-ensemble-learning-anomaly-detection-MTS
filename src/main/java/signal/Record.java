package signal;

import core.InvariantsProblem;

import java.util.HashMap;

public class Record {

    private static final HashMap<String, Integer> namesToIdx = new HashMap<>();
    static {
        int i = 0;
        for (String name : InvariantsProblem.getBoolNames()) {
            namesToIdx.put(name, i);
            ++i;
        }
        i = 0;
        for (String name: InvariantsProblem.getNumNames()) {
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

//    public void printMap() {
//        namesToIdx.entrySet().forEach(entry->{
//            System.out.println(entry.getKey() + " " + entry.getValue());
//        });
//    }
}
