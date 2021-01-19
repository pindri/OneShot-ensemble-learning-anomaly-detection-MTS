package signal;

import core.problem.SingleInvariantsProblem;
import eu.quanticol.moonlight.signal.Signal;
import org.junit.Test;

import java.io.IOException;
import java.util.*;
import java.util.stream.IntStream;

import static org.junit.Assert.*;

public class SignalBuilderTest {


    @Test
    public void buildTest() throws IOException {
        String grammarPath = "grammars/test_grammar.bnf";
        String dataPath = "data/toy_train_data.csv";
        String testPath = "data/toy_test_data.csv";
        String labelsPath = "data/toy_labels.csv";
        new SingleInvariantsProblem(grammarPath, dataPath, testPath, labelsPath, 10, 0);
        SignalBuilder sb = new SignalBuilder(10);
        List<Integer> numIndexes = new ArrayList<>(Arrays.asList(0, 1, 2, 3));
        List<Integer> boolIndexes = new ArrayList<>(Collections.emptyList());
        List<Signal<Record>> signals = sb.build(dataPath, boolIndexes, numIndexes);
        assertFalse(signals.get(0).isEmpty());

        // Asserting values have been correctly read.
        double value = signals.get(0).valueAt(2).getNum(SingleInvariantsProblem.getNumNames()[0]);
        double check = 1.67; // Directly from file.
        assertEquals(value, check, 0.01);
    }

    @Test
    public void buildDenseTest() throws IOException {
        String grammarPath = "grammars/test_grammar.bnf";
        String dataPath = "data/toy_train_data.csv";
        String testPath = "data/toy_test_data.csv";
        String labelsPath = "data/toy_labels.csv";
        new SingleInvariantsProblem(grammarPath, dataPath, testPath, labelsPath, 10, 0);
        SignalBuilder sb = new SignalBuilder(10);
        List<Integer> numIndexes = new ArrayList<>(Arrays.asList(0, 1, 2, 3));
        List<Integer> boolIndexes = new ArrayList<>(Collections.emptyList());
        List<Signal<Record>> signals = sb.build(dataPath, boolIndexes, numIndexes);
        assertFalse(signals.get(0).isEmpty());

        // Asserting values have been correctly read.
        double value = signals.get(0).valueAt(2).getNum(SingleInvariantsProblem.getNumNames()[0]);
        double check = 1.67; // Directly from file.
        assertEquals(value, check, 0.01);
    }

    @Test(expected = IOException.class)
    public void exceptionTest() throws IOException {
        SignalBuilder sb = new SignalBuilder(10);
        List<Integer> numIndexes = new ArrayList<>(Collections.emptyList());
        List<Integer> boolIndexes = new ArrayList<>(Collections.emptyList());
        List<Signal<Record>> signals = sb.build("non_existent_file.csv", boolIndexes, numIndexes);
        assertFalse(signals.isEmpty());
    }

    @Test
    public void signalIterationTest() {
        Signal<Double> s = new Signal<>();
        s.add(0, 0.0);
        s.add(1, 9.0);
        s.add(2, 0.0);
        s.add(3, 0.0);
        s.add(4, 1.0);
        s.add(5, 1.0);
        s.add(6, 2.0);
        s.add(7, 1.0);
        System.out.println("AUTOMATIC");
        System.out.println(s.size());
        s.forEach((t, v) -> System.out.println("t: " + t + " v: " + v));

        System.out.println("MANUAL");
        for (int t = (int) s.start(); t <= s.end(); t++) {
            System.out.println("t: " + t + " v: " + s.valueAt(t));
        }


        System.out.println("ANOTHERRAY");
        double[] range = IntStream.rangeClosed((int) s.start(), (int) s.end()).asDoubleStream().toArray();
        Arrays.stream(range).forEach(System.out::println);
        double[][] t2 = s.arrayOf(range, Double::valueOf);
        System.out.println(Arrays.deepToString(t2));
        double sum = Arrays.stream(t2).mapToDouble(arr -> arr[1]).sum();
        System.out.println(sum);
        System.out.println(range.length);


        double fitness = 0.0;
        for (int t = (int) s.start(); t <= s.end(); t++) {
            fitness += Math.abs(s.valueAt(t));
        }
        System.out.println(fitness);

        assertEquals(fitness, sum, 0.001);

    }

}
