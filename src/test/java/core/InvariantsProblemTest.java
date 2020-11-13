package core;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

public class InvariantsProblemTest {

    @Test
    public void namesInitialisationTest() throws IOException {
        String grammarPath = "test_grammar.bnf";
        String dataPath = "data/toy_train_data.csv";
        String testPath = "data/toy_test_data.csv";
        String labelsPath = "data/toy_labels.csv";
        // Null before an InvariantsProblem is created.
        new InvariantsProblem(grammarPath, dataPath, testPath, labelsPath, 10);
        assertArrayEquals(new String[]{"x1", "x2", "x3", "x4"}, InvariantsProblem.getNumNames());
        assertArrayEquals(new String[]{}, InvariantsProblem.getBoolNames());
    }
}
