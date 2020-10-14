package signal;

import org.junit.Test;
import signal.Record;
import signal.SignalBuilder;

import static org.junit.Assert.assertEquals;

public class RecordTest {

    @Test
    public void recordTest() {
        // Using 4 num vars.
        boolean[] boolValues = new boolean[]{};
        double[] numValues = new double[]{1.43, 12.2, 14.1, 232.3};

        Record record = new Record(boolValues, numValues);
        assertEquals(record.getNum(SignalBuilder.numNames[0]).toString(), String.valueOf(numValues[0]));
    }


}
