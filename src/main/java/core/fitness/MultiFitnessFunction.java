package core.fitness;

import eu.quanticol.moonlight.signal.Signal;
import nodes.AbstractSTLNode;
import signal.Record;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class MultiFitnessFunction extends AbstractFitnessFunction<List<Double>> {

    public MultiFitnessFunction(String trainPath, String testPath, String labelPath, int traceLength,
                                double validationFraction) throws IOException {
        super(trainPath, testPath, labelPath, traceLength, validationFraction);
    }


    @Override
    public List<Double> apply(AbstractSTLNode monitor) {

        double penalty = Double.MAX_VALUE;
        double fitness = 0.0;
        double coverage = 0.0;
        double[] fitnessArray;

        for (Signal<Record> signal : this.trainSignals) {
            if (signal.size() <= monitor.getMinLength()) {
                fitness += penalty;
                continue;
            }

            fitnessArray = applyMonitor(monitor, signal);
//            fitness += fitnessArray[fitnessArray.length - 1];
            fitness += Arrays.stream(fitnessArray).map(Math::abs).summaryStatistics().getAverage();
            coverage += 1.0/monitor.getCoverage();
        }

        return List.of(fitness/this.trainSignals.size(), coverage/this.trainSignals.size());
    }
}
