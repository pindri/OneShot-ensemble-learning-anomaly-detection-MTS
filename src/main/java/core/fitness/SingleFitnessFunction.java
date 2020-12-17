package core.fitness;

import eu.quanticol.moonlight.signal.Signal;
import nodes.AbstractSTLNode;
import signal.Record;

import java.io.IOException;
import java.util.Arrays;

public class SingleFitnessFunction extends AbstractFitnessFunction<Double> {

    public SingleFitnessFunction(String trainPath, String testPath, String labelPath, int traceLength,
                                 double validationFraction) throws IOException {
        super(trainPath, testPath, labelPath, traceLength, validationFraction);
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

            fitnessArray = applyMonitor(monitor, signal);
//            fitness += fitnessArray[fitnessArray.length - 1];
            fitness += Arrays.stream(fitnessArray).map(Math::abs).summaryStatistics().getAverage();
        }

        return fitness/this.trainSignals.size();
    }
}
