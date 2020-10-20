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

public class UnaryTemporalSTLNodeTest {

    @Test
    public void eventuallyTest() {
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

        // Creates monitor with x1 > 3.
        NumericSTLNode firstChild = new NumericSTLNode(siblings);

        // Creating a record to monitor.
        boolean[] boolValues = new boolean[]{};
        Record record = new Record(boolValues, new double[]{1, 2.0, 3.0, 4.0});
        Signal<Record> signal = new Signal<>();
        signal.add(0, record);
        record = new Record(boolValues, new double[]{3.5, 2.1, 3.1, 4.1});
        signal.add(1, record);
        record = new Record(boolValues, new double[]{2, 2.2, 3.2, 4.2});
        signal.add(2, record);
        record = new Record(boolValues, new double[]{2.3, 2.2, 3.2, 4.2});
        signal.add(3, record);
        record = new Record(boolValues, new double[]{2.9, 2.2, 3.2, 4.2});
        signal.add(4, record);

        // Actual monitor.
        int inf = 0;
        int sup = 2;
        int start = inf;
        int end = start +  (int) Math.max(1.0, sup);
        Function<Signal<Record>, TemporalMonitor<Record, Double>> operator;
        operator = x -> TemporalMonitor.eventuallyMonitor(firstChild.getOperator().apply(x),
                                        new DoubleDomain()
//                    new DoubleDomain());
                , new Interval(start, end));


        int min_length = 0 + end;

        System.out.println("Min length: " + min_length + "\tSignal size: " + signal.size());

        Signal<Double> fitness = operator.apply(signal).monitor(signal);
        System.out.println("Monitor signal: " + fitness);

        for (int i = (int) fitness.start(); i <= fitness.end() ; i++) {
            System.out.println("t: " + i + "\t" + fitness.valueAt(i));
        }

        System.out.println("At end: " + fitness.valueAt(fitness.end()));
    }

}
