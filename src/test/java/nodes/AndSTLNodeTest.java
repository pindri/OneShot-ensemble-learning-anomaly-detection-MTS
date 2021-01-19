package nodes;

import core.problem.SingleInvariantsProblem;
import eu.quanticol.moonlight.formula.DoubleDomain;
import eu.quanticol.moonlight.monitoring.temporal.TemporalMonitor;
import eu.quanticol.moonlight.signal.Signal;
import it.units.malelab.jgea.representation.tree.Tree;
import org.junit.Test;
import signal.Record;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static org.junit.Assert.assertEquals;

@SuppressWarnings("unchecked")
public class AndSTLNodeTest {

    @Test
    public void andMonitorTest() throws IOException {

        // Initialising variables.
        String grammarPath = "grammars/test_grammar.bnf";
        String dataPath = "data/toy_train_data.csv";
        String testPath = "data/toy_test_data.csv";
        String labelsPath = "data/toy_labels.csv";
        new SingleInvariantsProblem(grammarPath, dataPath, testPath, labelsPath, 10, 0, "");

            // Tree to be parsed.
            Tree<String> v = Tree.of("x1");
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

        // Creates monitor with x2 > 0.3.
        NumericSTLNode node = new NumericSTLNode(siblings);

            // Tree to be parsed.
            Tree<String> v1 = Tree.of("x3");
            Tree<String> c1 = Tree.of("<");
            Tree<String> n1 = Tree.of("5");

            Tree<String> var1 = Tree.of("<var>", v1);
            Tree<String> comp1 = Tree.of("<comp>", c1);
            Tree<String> dig1 = Tree.of("<dig>", n1);
            Tree<String> num1 = Tree.of("<num>", dig1);

        List<Tree<String>> siblings1 = new ArrayList<>() {{
                add(var1);
                add(comp1);
                add(num1);
            }};

        // Creates monitor with x3 < 0.5.
        NumericSTLNode node1 = new NumericSTLNode(siblings1);


        // Creating a record to monitor.
        boolean[] boolValues = new boolean[]{};
        double[] numValues = new double[]{0.35, 0.4, 0.15, 0.4};
        Record record = new Record(boolValues, numValues);
        Signal<Record> signal = new Signal<>();
        signal.add(0, record);

        // Creating AND TemporalMonitor for x1 > 0.3 and x3 < 0.5.
        Function<Signal<Record>, TemporalMonitor<Record, Double>> operator;
        operator = x -> TemporalMonitor.andMonitor(node.getOperator().apply(x),
                                                   new DoubleDomain(),
                                                   node1.getOperator().apply(x));
        Signal<Double> m = operator.apply(signal).monitor(signal);

        double fitness = m.valueAt(m.start());
        assertEquals(0.05, fitness, 0.001);

    }
}
