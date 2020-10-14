import eu.quanticol.moonlight.signal.Signal;
import nodes.AbstractSTLNode;
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

    // test path: "data/test_data.csv"

    public FitnessFunction(String path) throws IOException {
        signal = this.signalBuilder.build(path, this.boolIndexes, this.numIndexes);
    }


    @Override
    public Double apply(AbstractSTLNode monitor) {
        double count;
        count = Math.abs(monitor.getOperator()
                                .apply(this.signal)
                                .monitor(this.signal)
                                .valueAt(this.signal.end()));
        return count;
    }
}
