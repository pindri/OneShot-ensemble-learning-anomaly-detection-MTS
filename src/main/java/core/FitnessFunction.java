package core;

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
    Signal<Record> signal;

    public FitnessFunction(String path) throws IOException {
        this.signal = this.signalBuilder.build(path, this.boolIndexes, this.numIndexes);
    }


    @Override
    public Double apply(AbstractSTLNode monitor) {
        double count = 0;
//        System.out.println("\n\nMonitor length: " + monitor.getMinLength());
//        System.out.println("STL tree:");
//        System.out.println(monitor);

        if (this.signal.size() < monitor.getMinLength()) {
            System.out.println("Signal: " + this.signal.size() + "\tminLength: " + monitor.getMinLength());
            return 1.0;
        }

        Signal<Double> pointRobustness = monitor.getOperator().apply(this.signal).monitor(this.signal);

        for (int t = (int) pointRobustness.start(); t < pointRobustness.end(); t++) {
            count += Math.abs(pointRobustness.valueAt(t));
        }

//        System.out.println("Fitness: " + count);

        return count/pointRobustness.size();
    }
}
