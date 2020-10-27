package core;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

public class InvariantsProblemTest {

    @Test
    public void namesInitialisationTest() throws IOException {
        String grammarPath = "grammar_temporal.bnf";
        String dataPath = "data/test_data.csv";
        // Null before an InvariantsProblem is created.
        new InvariantsProblem(grammarPath, dataPath);
        assertArrayEquals(new String[]{"x1", "x2", "x3", "x4"}, InvariantsProblem.getNumNames());
        assertArrayEquals(new String[]{}, InvariantsProblem.getBoolNames());
    }
}
