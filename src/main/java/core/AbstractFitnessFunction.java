package core;

import core.single.SingleInvariantsProblem;
import eu.quanticol.moonlight.signal.Signal;
import it.units.malelab.jgea.core.listener.collector.Item;
import nodes.AbstractSTLNode;
import signal.Record;
import signal.SignalBuilder;

import java.io.IOException;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public abstract class AbstractFitnessFunction<F> implements Function<AbstractSTLNode, F> {

    protected SignalBuilder signalBuilder;
    protected List<Signal<Record>> testSignals;
    protected List<Integer> testLabels;
    protected List<Signal<Record>> signals;
    protected List<Signal<Record>> trainSignals;
    protected List<Signal<Record>> validationSignals;

    // If fitness >= epsilon, record is not anomalous == 0 == NEGATIVE.
    protected double epsilon = -0.001;

    protected AbstractFitnessFunction(String trainPath, String testPath, String labelPath, int traceLength,
                                      double validationFraction) throws IOException {

        this.signalBuilder = new SignalBuilder(traceLength);
        List<Integer> numIndexes = IntStream.range(0, SingleInvariantsProblem.getNumNames().length).boxed()
                                            .collect(Collectors.toList());
        List<Integer> boolIndexes = IntStream.range(0, SingleInvariantsProblem.getBoolNames().length).boxed()
                                             .collect(Collectors.toList());
        this.signals = this.signalBuilder.build(trainPath, boolIndexes, numIndexes);

        this.trainSignals = this.signalBuilder.extractPortion(this.signals, 0, 1-validationFraction);
        this.validationSignals = this.signalBuilder.extractPortion(this.signals, 1-validationFraction, 1);

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

    public abstract List<Item> evaluateSolution(AbstractSTLNode solution, String prefix);

    public abstract List<Item> evaluateSolutions(List<AbstractSTLNode> solutions, String prefix, Operator operator);

    public abstract double validateSolution(AbstractSTLNode solution);

    public abstract void solutionToFile(AbstractSTLNode solution, String filename) throws IOException;
}
