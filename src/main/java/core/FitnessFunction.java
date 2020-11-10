package core;

import eu.quanticol.moonlight.monitoring.temporal.TemporalMonitor;
import eu.quanticol.moonlight.signal.Signal;
import nodes.AbstractSTLNode;
import signal.Record;
import signal.SignalBuilder;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class FitnessFunction implements Function<AbstractSTLNode, Double> {

    final SignalBuilder signalBuilder = new SignalBuilder();
    final List<Integer> numIndexes = IntStream.range(0, InvariantsProblem.getNumNames().length).boxed().collect(Collectors.toList());
    final List<Integer> boolIndexes = IntStream.range(0, InvariantsProblem.getBoolNames().length).boxed().collect(Collectors.toList());
    final int traceLength;
    List<Signal<Record>> signals;

    public FitnessFunction(String path, int traceLength) throws IOException {
        this.traceLength = traceLength;
        this.signals = this.signalBuilder.build(path, this.boolIndexes, this.numIndexes, this.traceLength);
    }

    public List<Signal<Record>> getTestSignals (String path) throws IOException {
        return this.signalBuilder.build(path, this.boolIndexes, this.numIndexes, this.traceLength);
    }

    public List<Integer> getTestLabels (String path) throws IOException {
        return this.signalBuilder.parseLabels(path, this.traceLength);
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
            TemporalMonitor<Record, Double> m = monitor.getOperator().apply(signal);
            Signal<Double> s = m.monitor(signal);
            fitness += Math.abs(s.valueAt(s.start()));
        }

        return fitness;
    }

    public void evaluateSolution(AbstractSTLNode solution) throws IOException {
        String testPath = "data/SWaT/test.csv";
        String labelsPath = "data/SWaT/labels.csv";
        List<Signal<Record>> testSignal = this.getTestSignals(testPath);
        List<Integer> labels = this.getTestLabels(labelsPath);

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

        System.out.println("P: " + P + "\t\tN: " + N);
        System.out.println("TP: " + TP + "\tFP: " + FP + "\tTN: " + TN + "\tFN: " + FN);
        System.out.println("TPR: " + (TP*1.0)/(P*1.0) + "\tFPR: " + (FP*1.0)/(N*1.0) + "\tFNR: " + (1.0*FN)/(P*1.0));

    }

}
