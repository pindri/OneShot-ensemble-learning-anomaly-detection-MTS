import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class RecordTest {

    @Test
    public void recordTest() {
        // Using 1 num and 2 bool vars.
        String[] boolNames = new String[]{"FIT101", "MV101"};
        boolean[] boolValues = new boolean[]{true, false};
        String[] numNames = new String[]{"LIT101"};
        double[] numValues = new double[]{1.43};
        Record record = new Record(boolValues, numValues);
        assertEquals(record.getNum(numNames[0]).toString(), String.valueOf(numValues[0]));
        assertEquals(record.getBool(boolNames[1]), boolValues[1]);
    }


}
