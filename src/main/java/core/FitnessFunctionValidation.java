package core;

import eu.quanticol.moonlight.signal.Signal;
import it.units.malelab.jgea.core.listener.collector.Item;
import nodes.AbstractSTLNode;
import signal.Record;
import signal.SignalBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

//public class FitnessFunctionValidation extends AbstractFitnessFunction{
//
//    final SignalBuilder signalBuilder;
//    final List<Integer> numIndexes = IntStream.range(0, InvariantsProblem.getNumNames().length).boxed().collect(Collectors.toList());
//    final List<Integer> boolIndexes = IntStream.range(0, InvariantsProblem.getBoolNames().length).boxed().collect(Collectors.toList());
//    private final List<Signal<Record>> testSignals;
//    private final List<Integer> testLabels;
//    List<Signal<Record>> signals;
//    List<Signal<Record>> trainSignals;
//    List<Signal<Record>> validationSignals;
//
//    public FitnessFunctionValidation(String trainPath, String testPath, String labelPath, int traceLength)
//            throws IOException {
//        this.signalBuilder = new SignalBuilder(traceLength);
//        this.signals = this.signalBuilder.build(trainPath, this.boolIndexes, this.numIndexes);
//        this.testSignals = this.signalBuilder.build(testPath, this.boolIndexes, this.numIndexes);
//        this.testLabels = this.signalBuilder.parseLabels(labelPath);
//        // Splitting into training and validation set.
//        int index = (int) (0.8 * this.signals.size());
//        this.trainSignals = this.signals.subList(0, index);
//        this.validationSignals = this.signals.subList(index, this.signals.size());
//    }
//    @Override
//    public Double apply(AbstractSTLNode monitor) {
//        double penalty = Double.MAX_VALUE;
//        double fitness = 0.0;
//
//        for (Signal<Record> signal : this.trainSignals) {
//            if (signal.size() <= monitor.getMinLength()) {
//                return penalty;
//            }
//            Signal<Double> m = monitor.getOperator().apply(signal).monitor(signal);
//            fitness += Math.abs(m.valueAt(m.start()));
//        }
//
//        // Limit FPS at 0.10%.
////        if (validateSolution(monitor) > 0.001) {return penalty;}
//        // fitness + alpha * FPR.
////        double alpha = 0.25;
////        return fitness + alpha * validateSolution(monitor);
//        return fitness;
//    }
//
//
//    private double validateSolution(AbstractSTLNode solution) {
//
//        long N = this.validationSignals.size(); // All validation signals are negative.
//        int FP = 0;
//
//        for (Signal<Record> signal : this.validationSignals) {
//            Signal<Double> s = solution.getOperator().apply(signal).monitor(signal);
//            double fitness = s.valueAt(s.start());
//            if (fitness > 0) {
//                FP++;
//            }
//        }
//        return (FP*1.0)/(N*1.0); // FPR
//
//    }
//
//    @Override
//    public List<Item> evaluateSolution(AbstractSTLNode solution) {
//        List<Signal<Record>> testSignal = this.testSignals;
//        List<Integer> labels = this.testLabels;
//
//        int TP = 0;
//        int TN = 0;
//        int FP = 0;
//        int FN = 0;
//
//        long P = labels.stream().filter(x -> x > 0).count();
//        long N = labels.size() - P;
//
//        for (int i = 0; i < testSignal.size(); i++) {
//            Signal<Double> s = solution.getOperator().apply(testSignal.get(i)).monitor(testSignal.get(i));
//            double fitness = s.valueAt(s.start());
//            if (fitness == 0) {
//                if (labels.get(i) > 0) {
//                    FN++;
//                } else {
//                    TN++;
//                }
//            } else {
//                if (labels.get(i) > 0) {
//                    TP++;
//                } else {
//                    FP++;
//                }
//            }
//        }
//
//        double TPR =(TP*1.0)/(P*1.0);
//        double FPR = (FP*1.0)/(N*1.0);
//        double FNR = (1.0*FN)/(P*1.0);
//
//        List<Item> items = new ArrayList<>();
//        items.add(new Item("test.TPR", TPR, "%7.5f"));
//        items.add(new Item("test.FPR", FPR, "%7.5f"));
//        items.add(new Item("test.FNR", FNR, "%7.5f"));
//
//        return items;
//    }
//}
