import eu.quanticol.moonlight.signal.Signal;
import nodes.AbstractSTLNode;
import org.apache.commons.math3.util.IntegerSequence;
import signal.Record;
import signal.SignalBuilder;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;

public class FitnessFunction implements Function<AbstractSTLNode, Double> {

    SignalBuilder signalBuilder = new SignalBuilder();
    List<Integer> numIndexes = new ArrayList<>(Arrays.asList(0, 1, 2, 3));
    List<Integer> boolIndexes = new ArrayList<>(Collections.emptyList());
    Signal<Record> signal;

    public FitnessFunction(String path) throws IOException {
        signal = this.signalBuilder.build(path, this.boolIndexes, this.numIndexes);
    }


    @Override
    public Double apply(AbstractSTLNode monitor) {
        double count = 0;
//        System.out.println("\n\nMonitor length: " + monitor.getMinLength());
//        System.out.println("STL tree:");
//        System.out.println(monitor);
//        if (this.signal.size() < monitor.getMinLength()) {
//            count += this.signal.size();
//        } else {
//            count += Math.abs(monitor.getOperator()
//                    .apply(this.signal)
//                    .monitor(this.signal)
//                    .valueAt(signal.end()));
//        }
        Signal<Double> pointRobustness = monitor.getOperator().apply(this.signal).monitor(this.signal);

        for (int t = (int) pointRobustness.start(); t < pointRobustness.end(); t++) {
            count += Math.abs(pointRobustness.valueAt(t));
        }
//        for (int t = 0; t < this.signal.size(); t++) {
//            count += Math.abs(monitor.getOperator()
//                    .apply(this.signal)
//                    .monitor(this.signal)
//                    .valueAt(t));
//        }
//        System.out.println("Fitness: " + count);
        return count/pointRobustness.size();
    }
}
