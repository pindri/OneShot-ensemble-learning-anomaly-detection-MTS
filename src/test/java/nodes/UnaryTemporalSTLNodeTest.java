package nodes;

import eu.quanticol.moonlight.formula.DoubleDomain;
import eu.quanticol.moonlight.formula.Interval;
import eu.quanticol.moonlight.monitoring.temporal.TemporalMonitor;
import eu.quanticol.moonlight.signal.Signal;
import it.units.malelab.jgea.representation.tree.Tree;
import org.junit.Test;
import signal.Record;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static org.junit.Assert.assertEquals;

@SuppressWarnings("unchecked")
public class UnaryTemporalSTLNodeTest {

    // Test tree.
    Tree<String> v = Tree.of("x1");
    Tree<String> c = Tree.of(">");
    Tree<String> n = Tree.of("3");
    Tree<String> var = Tree.of("<var>", v);
    Tree<String> comp = Tree.of("<comp>", c);
    Tree<String> num = Tree.of("<num>", n);
    List<Tree<String>> siblings = new ArrayList<>() {{
        add(var);
        add(comp);
        add(num);
    }};
    boolean[] boolValues = new boolean[]{};
    Signal<Record> signal = new Signal<>();
    Record record;

    // Creates monitor with x1 > 3.
    NumericSTLNode firstChild = new NumericSTLNode(siblings);

    public void populateSignal() {
        this.record = new Record(this.boolValues, new double[]{1, 2.0, 3.0, 4.0});
        this.signal.add(0, this.record);
        this.record = new Record(this.boolValues, new double[]{3.5, 2.1, 3.1, 4.1});
        this.signal.add(1, this.record);
        this.record = new Record(this.boolValues, new double[]{2, 2.2, 3.2, 4.2});
        this.signal.add(2, this.record);
        this.record = new Record(this.boolValues, new double[]{2.3, 2.2, 3.2, 4.2});
        this.signal.add(3, this.record);
        this.record = new Record(this.boolValues, new double[]{2.9, 2.2, 3.2, 4.2});
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
        // Creating a record to monitor.
        populateSignal();

        // Actual monitor.
        int inf = 0;
        int sup = 2;
        int end = inf + sup;
        Function<Signal<Record>, TemporalMonitor<Record, Double>> operator;
        operator = x -> TemporalMonitor.eventuallyMonitor(this.firstChild.getOperator().apply(x),
                                                          new DoubleDomain(),
                                                          new Interval(inf, end));

        Signal<Double> fitness = operator.apply(this.signal).monitor(this.signal);

//        printFitness(fitness);
        
        assertEquals(0.5, fitness.valueAt(0), 0.01);
        assertEquals(-0.1, fitness.valueAt(2), 0.01);
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
        assertEquals(-1, fitness.valueAt(3), 0.01);
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
        assertEquals(0.5, fitness.valueAt(2), 0.01);
    }


}
