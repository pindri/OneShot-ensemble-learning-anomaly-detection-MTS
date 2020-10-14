package signal;

import eu.quanticol.moonlight.signal.Signal;
import org.junit.Test;
import signal.Record;
import signal.SignalBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class SignalBuilderTest {

    @Test
    public void buildTest() throws IOException {
        SignalBuilder sb = new SignalBuilder();
        List<Integer> numIndexes = new ArrayList<>(Arrays.asList(0, 1, 2, 3));
        List<Integer> boolIndexes = new ArrayList<>(Collections.emptyList());
        Signal<Record> signal = sb.build("data/test_data.csv", boolIndexes, numIndexes);
        assertFalse(signal.isEmpty());

        // Asserting values have been correctly read.
        double value = signal.valueAt(2).getNum(SignalBuilder.numNames[0]);
        double check = 124.4705; // Directly from file.
        assertEquals(value, check, 0);
    }

    @Test(expected = IOException.class)
    public void exceptionTest() throws IOException {
        SignalBuilder sb = new SignalBuilder();
        List<Integer> numIndexes = new ArrayList<>(Collections.emptyList());
        List<Integer> boolIndexes = new ArrayList<>(Collections.emptyList());
        Signal<Record> signal = sb.build("non_existent_file.csv", boolIndexes, numIndexes);
        assertFalse(signal.isEmpty());
    }

}
