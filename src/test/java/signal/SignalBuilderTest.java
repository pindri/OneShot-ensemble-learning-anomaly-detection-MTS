package signal;

import core.InvariantsProblem;
import eu.quanticol.moonlight.signal.Signal;
import org.junit.Test;

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
        String grammarPath = "test_grammar.bnf";
        String dataPath = "data/toy_train_data.csv";
        new InvariantsProblem(grammarPath, dataPath, 10);
        SignalBuilder sb = new SignalBuilder();
        List<Integer> numIndexes = new ArrayList<>(Arrays.asList(0, 1, 2, 3));
        List<Integer> boolIndexes = new ArrayList<>(Collections.emptyList());
        List<Signal<Record>> signals = sb.build(dataPath, boolIndexes, numIndexes, 10);
        assertFalse(signals.get(0).isEmpty());

        // Asserting values have been correctly read.
        double value = signals.get(0).valueAt(2).getNum(InvariantsProblem.getNumNames()[0]);
        double check = 1.67; // Directly from file.
        assertEquals(value, check, 0.01);
    }

    @Test(expected = IOException.class)
    public void exceptionTest() throws IOException {
        SignalBuilder sb = new SignalBuilder();
        List<Integer> numIndexes = new ArrayList<>(Collections.emptyList());
        List<Integer> boolIndexes = new ArrayList<>(Collections.emptyList());
        List<Signal<Record>> signals = sb.build("non_existent_file.csv", boolIndexes, numIndexes, 10);
        assertFalse(signals.isEmpty());
    }

    @Test
    public void testTest() {
        SignalBuilder sb = new SignalBuilder();

    }

}
