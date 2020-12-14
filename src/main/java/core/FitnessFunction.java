package core;

import arrayUtilities.ArraysUtilities;
import eu.quanticol.moonlight.signal.Signal;
import it.units.malelab.jgea.core.listener.collector.Item;
import nodes.AbstractSTLNode;
import org.tukaani.xz.DeltaInputStream;
import signal.Record;
import signal.SignalBuilder;
import signal.SignalHandler;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class FitnessFunction extends AbstractFitnessFunction {

    public FitnessFunction(String trainPath, String testPath, String labelPath,
                           int traceLength, double validationFraction) throws IOException {

        this.signalBuilder = new SignalBuilder(traceLength);
        List<Integer> numIndexes = IntStream.range(0, InvariantsProblem.getNumNames().length).boxed()
                                            .collect(Collectors.toList());
        List<Integer> boolIndexes = IntStream.range(0, InvariantsProblem.getBoolNames().length).boxed()
                                             .collect(Collectors.toList());
        this.signals = this.signalBuilder.build(trainPath, boolIndexes, numIndexes);

        this.trainSignals = this.signalBuilder.extractPortion(this.signals, 0, 1-validationFraction);
        this.validationSignals = this.signalBuilder.extractPortion(this.signals, 1-validationFraction, 1);

        this.testSignals = this.signalBuilder.build(testPath, boolIndexes, numIndexes);
        this.testLabels = this.signalBuilder.parseLabels(labelPath);

        printInfo(validationFraction > 0);
    }


    @Override
    public Double apply(AbstractSTLNode monitor) {

        double penalty = Double.MAX_VALUE;
        double fitness = 0.0;
        double[] fitnessArray;

        for (Signal<Record> signal : this.trainSignals) {
            if (signal.size() <= monitor.getMinLength()) {
                fitness += penalty;
                continue;
            }
//            if (monitor.getCoverage() < 50 || monitor.getVariablesList().stream().distinct().count() < 5) {
//                fitness += penalty;
//                continue;
//            }

            // Exclude P201
            if (monitor.getVariablesList().contains("P201")) {
                fitness += penalty;
                continue;
            }
            if (monitor.getCoverage() < 100) {
                fitness += 0.0001*(100 - monitor.getCoverage());
            }
            if (monitor.getVariablesList().stream().distinct().count() < 10) {
                fitness += 0.001*(10 - monitor.getVariablesList().stream().distinct().count());
            }
//            if (monitor.getCoverage() < 100) {
//                fitness += 0.0001*(100 - monitor.getCoverage());
//            }
//            if (monitor.getVariablesList().stream().distinct().count() < 10) {
//                fitness += 0.001*(100 - monitor.getVariablesList().stream().distinct().count());
//            }
//            // Penalty for false positives.
//            if (validateSolution(monitor) > 0) {
//                fitness += penalty;
//                continue;
//            }
            fitnessArray = applyMonitor(monitor, signal);
//            fitness += fitnessArray[fitnessArray.length - 1];
            fitness += Arrays.stream(fitnessArray).map(Math::abs).summaryStatistics().getAverage();
//            fitness += Arrays.stream(fitnessArray).map(x -> x >= epsilon ? Math.abs(x) : 1).summaryStatistics().getAverage();

        }

        return fitness/this.trainSignals.size();
    }


    public double[] applyMonitor(AbstractSTLNode monitor, Signal<Record> signal) {
        Signal<Double> robustness = monitor.getOperator().apply(signal).monitor(signal);

        return Arrays.stream(SignalHandler.toDoubleArray(robustness)).mapToDouble(x -> x[1]).toArray();
    }


    private double[] getFitnessArray(AbstractSTLNode monitor, List<Signal<Record>> signals) {
        ArrayList<double[]> result = new ArrayList<>();
        double[] fitness;
        for (Signal<Record> signal : signals) {
            fitness = applyMonitor(monitor, signal);
//            result.add(new double[] {fitness[fitness.length - 1]});
            result.add(fitness);
        }

        // Flat map: from List<double[]> to double[].
        return result.stream().flatMapToDouble(Arrays::stream).toArray();
    }


    public double[] getTestFitnessArray(AbstractSTLNode monitor) {

        return getFitnessArray(monitor, this.testSignals);
    }


    public double[] getValidationFitnessArray(AbstractSTLNode monitor) {

        return getFitnessArray(monitor, this.validationSignals);
    }


    // If fitness larger or equal than epsilon, record is labelled as 0 == NEGATIVE.
    public static int[] fitnessToLabel(double[] fitness, double epsilon) {
        return Arrays.stream(fitness).mapToInt(x -> x >= epsilon ? 0 : 1).toArray();
    }


    private Map<String, Integer> computeIndices(int[] predictions, int[] label) {

        Map<String, Integer> indices = new HashMap<>();
        int TP = 0;
        int FP = 0;
        int TN = 0;
        int FN = 0;

        for (int i = 0; i < predictions.length; i++) {
            if (predictions[i] == 1) { // If predicted anomaly, with POSITIVE == (prediction == 1).
                if (label[i] == 1) {
                    TP++;
                } else {
                    FP++;
                }
            } else {
                if (label[i] == 1) {
                    FN++;
                } else {
                    TN++;
                }
            }
        }

        indices.put("TP", TP);
        indices.put("FP", FP);
        indices.put("TN", TN);
        indices.put("FN", FN);

        return indices;
    }

    @Override
    public List<Item> evaluateSolution(AbstractSTLNode solution, String prefix) {

        double[] fitness = getTestFitnessArray(solution);

        return evaluateSingleSolution(fitnessToLabel(fitness, this.epsilon), prefix);
    }


    private List<Item> evaluateSingleSolution(int[] predictions, String prefix) {

        Map<String, Integer> indices;

        int from = this.testLabels.size() - predictions.length;
        int to = this.testLabels.size();
        int[] labels = IntStream.range(from, to).map(this.testLabels::get).toArray();

        long P = Arrays.stream(labels).filter(x -> x > 0).count();
        long N = this.testLabels.size() - P;
        int TP = 0;
        int FP = 0;
        int TN = 0;
        int FN = 0;

        indices = computeIndices(predictions, labels);

        TP += indices.get("TP");
        FP += indices.get("FP");
        TN += indices.get("TN");
        FN += indices.get("FN");

        double TPR =(TP*1.0)/(P*1.0);
        double FPR = (FP*1.0)/(N*1.0);
        double FNR = (1.0*FN)/(P*1.0);

        List<Item> items = new ArrayList<>();
        items.add(new Item(prefix + ".TPR", TPR, "%7.5f"));
        items.add(new Item(prefix + ".FPR", FPR, "%7.5f"));
        items.add(new Item(prefix + ".FNR", FNR, "%7.5f"));

        return items;
    }


    @Override
    public List<Item> evaluateSolutions(List<AbstractSTLNode> solutions, String prefix, Operator operator) {
        List<int[]> predictions = solutions.stream().map(x -> fitnessToLabel(getTestFitnessArray(x), this.epsilon))
                                           .collect(Collectors.toList());
        predictions = ArraysUtilities.trimHeadSameSize(predictions);
        int[] aggregatedPredictions;

        switch (operator) {
            case OR -> aggregatedPredictions = ArraysUtilities.labelsOR(predictions);
            case AND -> aggregatedPredictions = ArraysUtilities.labelsAND(predictions);
            case MAJORITY -> aggregatedPredictions = ArraysUtilities.labelsMajority(predictions);
            case TWO -> aggregatedPredictions = ArraysUtilities.labelsTwo(predictions);
            default -> throw new IllegalStateException("Unexpected value: " + operator);
        }

        return evaluateSingleSolution(aggregatedPredictions, prefix);
    }


    @Override
    public double validateSolution(AbstractSTLNode solution) {

        long FP = Arrays.stream(fitnessToLabel(getValidationFitnessArray(solution), this.epsilon))
                        .filter(x -> x < 0).count();
        int N = this.validationSignals.size();

        return (FP*1.0)/(N*1.0);
    }


    @Override
    public void solutionToFile(AbstractSTLNode solution, String filename) throws IOException {

        double[] fitness = getTestFitnessArray(solution);

        int from = this.testLabels.size() - fitness.length;
        int to = this.testLabels.size();
        int[] labels = IntStream.range(from, to).map(this.testLabels::get).toArray();

        FileWriter fw = new FileWriter(filename);
        fw.write("fitness;label\n");

        assert fitness.length == labels.length;

        for (int i = 0; i < fitness.length; i++) {
            fw.write(fitness[i] + ";" + labels[i] + "\n");
        }

        fw.close();
    }

}
