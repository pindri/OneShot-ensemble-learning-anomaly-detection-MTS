package signal;

import core.InvariantsProblem;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class RecordTest {

    @Test
    public void recordTest() throws IOException {
        String grammarPath = "test_grammar.bnf";
        String dataPath = "data/toy_train_data.csv";
        // Initialises the variables names taking the first line from the data-path.
        new InvariantsProblem(grammarPath, dataPath);
        boolean[] boolValues = new boolean[]{};
        double[] numValues = new double[]{1.43, 12.2, 14.1, 232.3};

        Record record = new Record(boolValues, numValues);
        assertEquals(record.getNum(InvariantsProblem.getNumNames()[0]).toString(), String.valueOf(numValues[0]));
    }


}
