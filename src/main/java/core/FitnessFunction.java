package core;

import eu.quanticol.moonlight.signal.Signal;
import it.units.malelab.jgea.core.listener.collector.Item;
import nodes.AbstractSTLNode;
import signal.Record;
import signal.SignalBuilder;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class FitnessFunction extends AbstractFitnessFunction {

    final SignalBuilder signalBuilder = new SignalBuilder();
    final List<Integer> numIndexes = IntStream.range(0, InvariantsProblem.getNumNames().length).boxed().collect(Collectors.toList());
    final List<Integer> boolIndexes = IntStream.range(0, InvariantsProblem.getBoolNames().length).boxed().collect(Collectors.toList());
    final int traceLength;
    private final List<Signal<Record>> testSignals;
    private final List<Integer> testLabels;
    List<Signal<Record>> signals;

    public FitnessFunction(String trainPath, String testPath, String labelPath, int traceLength) throws IOException {
        this.traceLength = traceLength;
        this.signals = this.signalBuilder.build(trainPath, this.boolIndexes, this.numIndexes, this.traceLength);
        this.testSignals = this.signalBuilder.build(testPath, this.boolIndexes, this.numIndexes, this.traceLength);
        this.testLabels = this.signalBuilder.parseLabels(labelPath, this.traceLength);
    }


    @Override
    public Double apply(AbstractSTLNode monitor) {
//        System.out.println("\n\nMonitor length: " + monitor.getMinLength());
//        System.out.println("STL tree:");
//        System.out.println(monitor);

        double penalty = 10.0;
        double fitness = 0.0;

        for (Signal<Record> signal : this.signals) {
            if (signal.size() <= monitor.getMinLength()) {
//                System.out.println("Monitor length: " + monitor.getMinLength());
//                System.out.println("LENGTH");
                fitness += penalty;
                continue;
            }
//            Signal<Double> m = monitor.getOperator().apply(signal).monitor(signal);
//            fitness += Math.abs(m.valueAt(m.start()));
            Signal<Double> m = monitor.getOperator().apply(signal).monitor(signal);
            fitness += Math.abs(m.valueAt(m.start()));
        }

        return fitness;
    }

    @Override
    public List<Item> evaluateSolution(AbstractSTLNode solution) {
        List<Signal<Record>> testSignal = this.testSignals;
        List<Integer> labels = this.testLabels;

        int TP = 0;
        int TN = 0;
        int FP = 0;
        int FN = 0;

        long P = labels.stream().filter(x -> x > 0).count();
        long N = labels.size() - P;

        for (int i = 0; i < testSignal.size(); i++) {
            Signal<Double> s = solution.getOperator().apply(testSignal.get(i)).monitor(testSignal.get(i));
            double fitness = s.valueAt(s.start());
            if (fitness == 0) {
                if (labels.get(i) > 0) {
                    FN++;
                } else {
                    TN++;
                }
            } else {
                if (labels.get(i) > 0) {
                    TP++;
                } else {
                    FP++;
                }
            }
        }

        double TPR =(TP*1.0)/(P*1.0);
        double FPR = (FP*1.0)/(N*1.0);
        double FNR = (1.0*FN)/(P*1.0);

        List<Item> items = new ArrayList<>();
        items.add(new Item("test.TPR", TPR, "%7.5f"));
        items.add(new Item("test.FPR", FPR, "%7.5f"));
        items.add(new Item("test.FNR", FNR, "%7.5f"));

        return items;
    }

}
