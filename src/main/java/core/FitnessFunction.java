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

    @Override
    public Double apply(AbstractSTLNode monitor) {
//        System.out.println("\n\nMonitor length: " + monitor.getMinLength());
//        System.out.println("STL tree:");
//        System.out.println(monitor);
//
        double penalty = 10.0;
        double fitness = 0.0;

        if (this.signals.size() <= monitor.getMinLength()) {
//            System.out.println("Signal: " + this.signals.size() + "\t min length: " + monitor.getMinLength());
            return penalty;
        }

//        System.out.println(signals.size() + " " + " " + monitor.getMinLength());

        for (Signal<Record> signal : this.signals) {
//            double test = monitor.getOperator().apply(signal).monitor(signal).valueAt(signal.start());
            Signal<Double> m = monitor.getOperator().apply(signal).monitor(signal);
            fitness += Math.abs(m.valueAt(m.start()));
        }

//        System.out.println("fitness: " + fitness);

        return fitness;
    }
}
