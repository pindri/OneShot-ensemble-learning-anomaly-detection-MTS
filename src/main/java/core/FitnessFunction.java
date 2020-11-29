package core;

import eu.quanticol.moonlight.signal.Signal;
import it.units.malelab.jgea.core.listener.collector.Item;
import nodes.AbstractSTLNode;
import signal.Record;
import signal.SignalBuilder;
import signal.SignalHandler;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class FitnessFunction extends AbstractFitnessFunction {

    final SignalBuilder signalBuilder;
    final List<Integer> numIndexes = IntStream.range(0, InvariantsProblem.getNumNames().length).boxed()
            .collect(Collectors.toList());
    final List<Integer> boolIndexes = IntStream.range(0, InvariantsProblem.getBoolNames().length).boxed()
            .collect(Collectors.toList());
    private final List<Signal<Record>> testSignals;
    private final List<Integer> testLabels;
    List<Signal<Record>> signals;
    List<Signal<Record>> trainSignals;
    List<Signal<Record>> validationSignals;

    public FitnessFunction(String trainPath, String testPath, String labelPath,
                           int traceLength, double validationFraction) throws IOException {
        this.signalBuilder = new SignalBuilder(traceLength);
        this.signals = this.signalBuilder.build(trainPath, this.boolIndexes, this.numIndexes);
        // Splitting into training and validation set.
        this.trainSignals = this.signalBuilder.extractPortion(this.signals, 0, 1-validationFraction);
        this.validationSignals = this.signalBuilder.extractPortion(this.signals, 1-validationFraction, 1);

        this.testSignals = this.signalBuilder.build(testPath, this.boolIndexes, this.numIndexes);
        this.testLabels = this.signalBuilder.parseLabels(labelPath);
        System.out.println("Sig: " + signals.size() + " train: " + trainSignals.size() + " val: "
                                   + validationSignals.size() + " test: " + testSignals.size()
                                   + " labels: " + testLabels.size());
    }


    @Override
    public Double apply(AbstractSTLNode monitor) {

        double penalty = Double.MAX_VALUE;
        double fitness = 0.0;

        for (Signal<Record> signal : this.trainSignals) {
            if (signal.size() <= monitor.getMinLength()) {
                fitness += penalty;
                continue;
            }

            Signal<Double> robustness = monitor.getOperator().apply(signal).monitor(signal);

            // Last element.
//            fitness += Math.abs(robustness.valueAt(robustness.end()));

            // Mean fitness for this signal.
            int range = (int) robustness.end() - (int) robustness.start() + 1;
            fitness += (Arrays.stream(SignalHandler.toDoubleArray(robustness))
                    .mapToDouble(x -> Math.abs(x[1])).sum())/(1.0*range);
        }

        return fitness/this.trainSignals.size();
    }


    @Override
    public List<Item> evaluateSolution(AbstractSTLNode solution) {

        int TP = 0;
        int FP = 0;
        int TN = 0;
        int FN = 0;

        // TODO: first elements will not be considered.
        long P = this.testLabels.stream().filter(x -> x > 0).count();
        long N = this.testLabels.size() - P;
        double fitness;
        int label;
//        int position = 0;

        for (Signal<Record> signal : this.testSignals) {
            Signal<Double> robustness = solution.getOperator().apply(signal).monitor(signal);
            for (int t = (int) robustness.start(); t <= robustness.end(); t++) {
                label = this.testLabels.get(t);
                fitness = robustness.valueAt(t);
//        for (Signal<Record> signal : this.testSignals) {
//            fitness = solution.getOperator().apply(signal).monitor(signal).valueAt(signal.end());
//            label = this.testLabels.get(position);
//            position++;
                if (fitness >= 0) {
                    if (label > 0) {
                        FN++;
                    } else {
                        TN++;
                    }
                } else {
                    if (label > 0) {
                        TP++;
                    } else {
                        FP++;
                    }
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

    @Override
    public void solutionToFile(AbstractSTLNode solution, String filename) throws IOException {

        FileWriter fw = new FileWriter(filename);
        fw.write("fitness;label\n");

        double fitness;
        int label;
//        int position = 0;

        for (Signal<Record> signal : this.testSignals) {
            Signal<Double> robustness = solution.getOperator().apply(signal).monitor(signal);
//            fitness = robustness.valueAt(signal.end());
//            label = this.testLabels.get(position);
//            fw.write(fitness + ";" + label + "\n");
//            System.out.println(position);
//            position++;
            for (int t = (int) robustness.start(); t <= robustness.end(); t++) {
                label = this.testLabels.get(t);
                fitness = robustness.valueAt(t);
                fw.write(fitness + ";" + label + "\n");
            }
        }
        fw.close();
    }

}
