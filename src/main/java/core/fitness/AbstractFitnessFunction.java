package core.fitness;

import arrayUtilities.ArraysUtilities;
import core.problem.SingleInvariantsProblem;
import eu.quanticol.moonlight.signal.Signal;
import eu.quanticol.moonlight.util.Pair;
import nodes.AbstractSTLNode;
import signal.Record;
import signal.SignalBuilder;
import signal.SignalHandler;

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.zip.GZIPOutputStream;

public abstract class AbstractFitnessFunction<F> implements Function<AbstractSTLNode, F> {

    protected SignalBuilder signalBuilder;
    protected List<Signal<Record>> testSignals;
    protected List<Integer> testLabels;
    protected List<Signal<Record>> signals;
    protected List<Signal<Record>> trainSignals;
    protected List<Signal<Record>> validationSignals;

    // If fitness >= epsilon, record is not anomalous == 0 == NEGATIVE.
    protected double epsilon = -0.0;

    protected AbstractFitnessFunction(String trainPath, String testPath, String labelPath, int traceLength,
                                      double validationFraction) throws IOException {

        this.signalBuilder = new SignalBuilder(traceLength);
        List<Integer> numIndexes = IntStream.range(0, SingleInvariantsProblem.getNumNames().length).boxed()
                                            .collect(Collectors.toList());
        List<Integer> boolIndexes = IntStream.range(0, SingleInvariantsProblem.getBoolNames().length).boxed()
                                             .collect(Collectors.toList());
        this.signals = this.signalBuilder.build(trainPath, boolIndexes, numIndexes);

        this.trainSignals = this.signalBuilder.extractPortion(this.signals, 0, 1 - validationFraction);
        this.validationSignals = this.signalBuilder.extractPortion(this.signals, 1 - validationFraction, 1);

        this.testSignals = this.signalBuilder.build(testPath, boolIndexes, numIndexes);
        this.testLabels = this.signalBuilder.parseLabels(labelPath);

        printInfo(validationFraction > 0);
    }

    protected void printInfo(boolean printValidation) {
        System.out.println("Sizes. Signal: " + this.signals.size()
                                   + ". Train: " + this.trainSignals.size()
                                   + ". Test: " + this.testSignals.size()
                                   + ". Test labels: " + this.testLabels.size()
                                   + ". Validation: " + this.validationSignals.size());
        System.out.print("Element sizes. Signal: " + this.signals.get(0).size()
                                 + ". Train: " + this.trainSignals.get(0).size()
                                 + ". Test: " + this.testSignals.get(0).size() + ".");
        if (printValidation) {
            System.out.print(" Validation: " + this.validationSignals.get(0).size());
        } else {
            System.out.println(" No validation.");
        }
        System.out.println();
    }


    @Override
    public abstract F apply(AbstractSTLNode monitor);

    public double[] applyMonitor(AbstractSTLNode monitor, Signal<Record> signal) {
        Signal<Double> robustness = monitor.getOperator().apply(signal).monitor(signal);

        return Arrays.stream(SignalHandler.toDoubleArray(robustness)).mapToDouble(x -> x[1]).toArray();
    }


    private double[] getRobustnessArray(AbstractSTLNode monitor, List<Signal<Record>> signals) {
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


    public double[] getTestRobustnessArray(AbstractSTLNode monitor) {

        return getRobustnessArray(monitor, this.testSignals);
    }


    public double[] getValidationRobustnessArray(AbstractSTLNode monitor) {

        return getRobustnessArray(monitor, this.validationSignals);
    }


    // If robustness larger or equal than epsilon, record is labelled as 0 == NEGATIVE.
    public static int[] robustnessToLabel(double[] robustness, double epsilon) {
        return Arrays.stream(robustness).mapToInt(x -> x >= epsilon ? 0 : 1).toArray();
    }


    private Map<String, Number> computeIndices(int[] predictions, int[] labels) {

        Map<String, Number> indices = new HashMap<>();
        long P = Arrays.stream(labels).filter(x -> x > 0).count(); // POSITIVE := (label == 1)
        long N = predictions.length - P;
        int TP = 0;
        int FP = 0;
        int TN = 0;
        int FN = 0;

        for (int i = 0; i < predictions.length; i++) {
            if (predictions[i] == 1) { // If predicted anomaly, with POSITIVE := (prediction == 1).
                if (labels[i] == 1) {
                    TP++;
                } else {
                    FP++;
                }
            } else {
                if (labels[i] == 1) {
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

        double TPR = (TP * 1.0) / (P * 1.0);
        double FPR = (FP * 1.0) / (N * 1.0);
        double FNR = (1.0 * FN) / (P * 1.0);

        indices.put("TPR", TPR);
        indices.put("FPR", FPR);
        indices.put("FNR", FNR);

        return indices;
    }

    public Map<String, Number> evaluateSolution(AbstractSTLNode solution) {

        double[] fitness = getTestRobustnessArray(solution);

        return evaluateSingleSolution(robustnessToLabel(fitness, this.epsilon));
    }


    private Map<String, Number> evaluateSingleSolution(int[] predictions) {

        int from = this.testLabels.size() - predictions.length;
        int to = this.testLabels.size();
        int[] labels = IntStream.range(from, to).map(this.testLabels::get).toArray();

        return computeIndices(predictions, labels);
    }


    public Map<String, Number> evaluateSolutions(List<AbstractSTLNode> solutions, Operator operator) {
        List<int[]> predictions = solutions.stream()
                                           .map(x -> robustnessToLabel(getTestRobustnessArray(x), this.epsilon))
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

        return evaluateSingleSolution(aggregatedPredictions);
    }


    public double validateSolution(AbstractSTLNode solution) {

        long FP = Arrays.stream(getValidationRobustnessArray(solution)).filter(x -> x < 0).count();
        int N = this.validationSignals.size();

        return (FP * 1.0) / (N * 1.0);
    }

    public void solutionToCompressedFile(AbstractSTLNode solution, String filename) throws IOException {

        double[] fitness = getTestRobustnessArray(solution);

        int from = this.testLabels.size() - fitness.length;
        int to = this.testLabels.size();
        int[] labels = IntStream.range(from, to).map(this.testLabels::get).toArray();

        assert fitness.length == labels.length;

        try (FileOutputStream output = new FileOutputStream(filename + ".gz")) {
            try (Writer writer = new OutputStreamWriter(new GZIPOutputStream(output), StandardCharsets.UTF_8)) {
                writer.write("fitness;label\n");
                for (int i = 0; i < fitness.length; i++) {
                    writer.write(roundDouble(fitness[i], 4) + ";" + labels[i] + "\n");
                }
            }
        }

    }

    public int[] ensemblePredictions(Collection<AbstractSTLNode> solutions, double epsilon) {
        List<int[]> predictions = solutions.stream().map(x -> robustnessToLabel(getTestRobustnessArray(x), epsilon))
                                           .collect(Collectors.toList());
        Optional<Integer> opt = predictions.stream().map(x -> x.length).reduce(Math::min);
        assert opt.isPresent();
        int minLength = opt.get();
        int[] ensemble = new int[minLength];
        for (int[] prediction : predictions) {
            for (int i = 0; i < minLength; i++) {
                ensemble[i] += prediction[prediction.length - minLength + i];
            }

        }
        return ensemble;
    }

    public void solutionToFile(AbstractSTLNode solution, String filename) throws IOException {

        double[] fitness = getTestRobustnessArray(solution);

        int from = this.testLabels.size() - fitness.length;
        int to = this.testLabels.size();
        int[] labels = IntStream.range(from, to).map(this.testLabels::get).toArray();

        FileWriter fw = new FileWriter(filename);
        fw.write("fitness;label\n");

        assert fitness.length == labels.length;

        for (int i = 0; i < fitness.length; i++) {
            fw.write(roundDouble(fitness[i], 4) + ";" + labels[i] + "\n");
        }

        fw.close();
    }

    public void collectionToFile(Collection<AbstractSTLNode> solutions, String filename) throws IOException {
        int i = 0;
        for (AbstractSTLNode solution : solutions) {
            solutionToFile(solution, filename + "-" + i);
            i++;
        }
    }

    public void collectionToCompressedFile(Collection<AbstractSTLNode> solutions, String filename) throws IOException {
        int i = 0;
        for (AbstractSTLNode solution : solutions) {
            solutionToCompressedFile(solution, filename + "-" + i);
            i++;
        }
    }

    public void ensembleToFile(Collection<AbstractSTLNode> solutions, String filename) throws IOException {
        int[] predictions = ensemblePredictions(solutions, 0.0);

        int from = this.testLabels.size() - predictions.length;
        int to = this.testLabels.size();
        int[] labels = IntStream.range(from, to).map(this.testLabels::get).toArray();

        FileWriter fw = new FileWriter(filename);
        fw.write("prediction;normalisedPrediction;label\n");

        assert predictions.length == labels.length;

        for (int i = 0; i < predictions.length; i++) {
            fw.write(predictions[i] + ";" + roundDouble(predictions[i]*1.0/solutions.size(), 4)
                             + ";" + labels[i] + "\n");
        }

        fw.close();
    }

    public void paretoToFile(List<Pair<AbstractSTLNode, Double>> solutions, String filename) throws IOException {
        int i = 0;
        for (Pair<AbstractSTLNode, Double> solution : solutions) {
            solutionToFile(solution.getFirst(), filename + "-pareto-" + solution.getSecond() + "-" + i);
            i++;
        }

    }

    public void bestParetoToFile(List<Pair<AbstractSTLNode, Double>> solutions, String filename) throws IOException {
        double min = solutions.get(0).getSecond();
        Pair<AbstractSTLNode, Double> best = new Pair<>(solutions.get(0).getFirst(), solutions.get(0).getSecond());
        for (Pair<AbstractSTLNode, Double> solution : solutions) {
            if (solution.getSecond() <= min) {
                best = new Pair<>(solution.getFirst(), solution.getSecond());
                min = solution.getSecond();
            }
        }
        solutionToFile(best.getFirst(), filename + "-best-pareto-" + best.getSecond());
    }


    public static double roundDouble(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);

        return bd.doubleValue();
    }

}
