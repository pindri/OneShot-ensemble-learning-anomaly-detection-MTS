package nodes;

import core.problem.SingleInvariantsProblem;
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
        String dataPath = "data/toy_train_data.csv";
        String testPath = "data/toy_test_data.csv";
        String labelsPath = "data/toy_labels.csv";
        new SingleInvariantsProblem(grammarPath, dataPath, testPath, labelsPath, 10, 0);

        // Tree to be parsed.
        Tree<String> v = Tree.of("x2");
        Tree<String> c = Tree.of(">");
        Tree<String> n1 = Tree.of("2"); // Will be 2 * 10^{-1}
        Tree<String> n2 = Tree.of("0"); // Will be 0 * 10^{-2}

        Tree<String> var = Tree.of("<var>", v);
        Tree<String> comp = Tree.of("<comp>", c);
        Tree<String> dig1 = Tree.of("<dig>", n1);
        Tree<String> dig2 = Tree.of("<dig>", n2);
        Tree<String> num = Tree.of("<num>", dig1, dig2); // Represents 0.20


        List<Tree<String>> siblings = new ArrayList<>() {{
            add(var);
            add(comp);
            add(num);
        }};

        // Creates monitor with x2 > 0.2.
        NumericSTLNode monitor = new NumericSTLNode(siblings);

        // Creating a record to monitor.
        boolean[] boolValues = new boolean[]{};
        double[] numValues;
        numValues = new double[]{0.1, 0.30, 0.3, 0.4};
        Record record = new Record(boolValues, numValues);
        Signal<Record> signal = new Signal<>();
        signal.add(0, record);
        numValues = new double[]{0.1, 0.45, 0.3, 0.4};
        record = new Record(boolValues, numValues);
        signal.add(1, record);
        numValues = new double[]{0.1, 0.10, 0.3, 0.4};
        record = new Record(boolValues, numValues);
        signal.add(2, record);

        Signal<Double> m = monitor.getOperator().apply(signal).monitor(signal);
        System.out.println(m.valueAt(0));

        assertEquals(0.1, m.valueAt(0), 0.005);
        assertEquals(-0.1, m.valueAt(2), 0.005);
        assertEquals(monitor.getMinLength(), 0, 0);
    }

}
