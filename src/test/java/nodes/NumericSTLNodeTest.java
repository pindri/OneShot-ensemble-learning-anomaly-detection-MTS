package nodes;

import core.InvariantsProblem;
import eu.quanticol.moonlight.signal.Signal;
import it.units.malelab.jgea.representation.tree.Tree;
import org.junit.Test;
import signal.Record;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class NumericSTLNodeTest {

    @SuppressWarnings("unchecked")
    @Test
    public void numericOperatorTest() throws IOException {

        // Initialising variables.
        String grammarPath = "test_grammar.bnf";
        String dataPath = "data/test_data.csv";
        new InvariantsProblem(grammarPath, dataPath);

        // Tree to be parsed.
        Tree<String> v = Tree.of("x2");
        Tree<String> c = Tree.of(">");
        Tree<String> n = Tree.of("3");

        Tree<String> var = Tree.of("<var>", v);
        Tree<String> comp = Tree.of("<comp>", c);
        Tree<String> dig = Tree.of("<dig>", n);
        Tree<String> num = Tree.of("<num>", dig);


        List<Tree<String>> siblings = new ArrayList<>() {{
            add(var);
            add(comp);
            add(num);
        }};

        // Creates monitor with x2 > 3.
        NumericSTLNode monitor = new NumericSTLNode(siblings);

        // Creating a record to monitor.
        boolean[] boolValues = new boolean[]{};
        double[] numValues = new double[]{1, 4.5, 3, 4};
        Record record = new Record(boolValues, numValues);
        Signal<Record> signal = new Signal<>();
        signal.add(0, record);
        numValues = new double[]{1, 3.5, 3, 4};
        record = new Record(boolValues, numValues);
        signal.add(1, record);

        double fitness = monitor.getOperator().apply(signal).monitor(signal).valueAt(signal.end());

        assertEquals(fitness, 0.5, 0);
        assertEquals(monitor.getMinLength(), 0, 0);
    }

}
