package nodes;

import core.problem.SingleInvariantsProblem;
import eu.quanticol.moonlight.formula.DoubleDomain;
import eu.quanticol.moonlight.formula.Interval;
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
public class UnaryTemporalSTLNodeTest {

    // Test tree.
    final Tree<String> v = Tree.of("x1");
    final Tree<String> c = Tree.of(">");
    final Tree<String> n = Tree.of("3");
    final Tree<String> var = Tree.of("<var>", v);
    final Tree<String> comp = Tree.of("<comp>", c);
    final Tree<String> dig = Tree.of("<dig>", n);
    final Tree<String> num = Tree.of("<num>", dig);
    final List<Tree<String>> siblings = new ArrayList<>() {{
        add(var);
        add(comp);
        add(num);
    }};
    final boolean[] boolValues = new boolean[]{};
    final Signal<Record> signal = new Signal<>();
    Record record;

    // Creates monitor with x1 > 3.
    final NumericSTLNode firstChild = new NumericSTLNode(siblings);

    public UnaryTemporalSTLNodeTest() throws IOException {
        // Initialising variables.
        String grammarPath = "test_grammar.bnf";
        String dataPath = "data/toy_train_data.csv";
        String testPath = "data/toy_test_data.csv";
        String labelsPath = "data/toy_labels.csv";
        new SingleInvariantsProblem(grammarPath, dataPath, testPath, labelsPath, 10, 0);
    }

    public void populateSignal() {
        this.record = new Record(this.boolValues, new double[]{0.1, 0.20, 0.30, 0.40});
        this.signal.add(0, this.record);
        this.record = new Record(this.boolValues, new double[]{0.35, 0.21, 0.31, 0.41});
        this.signal.add(1, this.record);
        this.record = new Record(this.boolValues, new double[]{0.2, 0.22, 0.32, 0.42});
        this.signal.add(2, this.record);
        this.record = new Record(this.boolValues, new double[]{0.23, 0.22, 0.32, 0.42});
        this.signal.add(3, this.record);
        this.record = new Record(this.boolValues, new double[]{0.29, 0.22, 0.32, 0.42});
        this.signal.add(4, this.record);
    }

    public void printFitness(Signal<Double> fitness) {
        System.out.println(fitness);
        for (int t = (int) fitness.start(); t <= fitness.end(); t++) {
            System.out.println("t: " + t + "\t" + fitness.valueAt(t));
        }
    }


    @Test
    public void eventuallyTest() {
        populateSignal();

        int inf = 0;
        int sup = 2;
        int end = inf + sup;
        Function<Signal<Record>, TemporalMonitor<Record, Double>> operator;
        operator = x -> TemporalMonitor.eventuallyMonitor(this.firstChild.getOperator().apply(x),
                                                          new DoubleDomain(),
                                                          new Interval(inf, end));

        Signal<Double> fitness = operator.apply(this.signal).monitor(this.signal);

//        printFitness(fitness);
        assertEquals(0.05, fitness.valueAt(0), 0.001);
        assertEquals(-0.01, fitness.valueAt(2), 0.001);
    }

    @Test
    public void historicallyTest() {
        populateSignal();

        int inf = 0;
        int sup = 2;
        int end = inf + sup;
        Function<Signal<Record>, TemporalMonitor<Record, Double>> operator;
        operator = x -> TemporalMonitor.historicallyMonitor(this.firstChild.getOperator().apply(x),
                                                            new DoubleDomain(),
                                                            new Interval(inf, end));

        Signal<Double> fitness = operator.apply(this.signal).monitor(this.signal);

//        printFitness(fitness);
        assertEquals(-0.1, fitness.valueAt(3), 0.001);
    }

    @Test
    public void onceTest() {
        populateSignal();

        int inf = 0;
        int sup = 2;
        int end = inf + sup;
        Function<Signal<Record>, TemporalMonitor<Record, Double>> operator;
        operator = x -> TemporalMonitor.onceMonitor(this.firstChild.getOperator().apply(x),
                                                    new DoubleDomain(),
                                                    new Interval(inf, end));

        Signal<Double> fitness = operator.apply(this.signal).monitor(this.signal);

//        printFitness(fitness);
        assertEquals(0.05, fitness.valueAt(2), 0.001);
    }

    @Test
    public void globallyTest() {
        populateSignal();

        int inf = 0;
        int sup = 2;
        int end = inf + sup;
        Function<Signal<Record>, TemporalMonitor<Record, Double>> operator;
        operator = x -> TemporalMonitor.globallyMonitor(this.firstChild.getOperator().apply(x),
                                                        new DoubleDomain(),
                                                        new Interval(inf, end));

        Signal<Double> fitness = operator.apply(this.signal).monitor(this.signal);

//        printFitness(fitness);
        assertEquals(-0.1, fitness.valueAt(2), 0.001);
    }

    @Test
    public void sizeTest() {
        Signal<Double> s = new Signal<>();
        s.add(0, 3.0);
        s.add(1, 0.0);
        s.add(2, 9.0);
        s.add(3, 9.0);
        assertEquals(3, s.size());
    }
}
