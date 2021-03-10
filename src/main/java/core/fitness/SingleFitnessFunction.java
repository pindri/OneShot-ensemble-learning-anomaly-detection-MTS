package core.fitness;

import eu.quanticol.moonlight.signal.Signal;
import nodes.AbstractSTLNode;
import signal.Record;

import java.io.IOException;
import java.util.Arrays;

public class SingleFitnessFunction extends AbstractFitnessFunction<Double> {

    public SingleFitnessFunction(String trainPath, String testPath, String labelPath, int traceLength,
                                 double validationFraction, String magicVariable) throws IOException {
        super(trainPath, testPath, labelPath, traceLength, validationFraction);
    }

    public SingleFitnessFunction(String trainPath, String testPath, String labelPath, int traceLength,
                                 double validationFraction) throws IOException {
        super(trainPath, testPath, labelPath, traceLength, validationFraction);
    }


    @Override
    public Double apply(AbstractSTLNode monitor) {

        double penalty = Double.MAX_VALUE;
        double fitness = 0.0;
        double[] fitnessArray;
        int lengthMin = 50;
        int lengthMax = 10;
        int varMin = 10;
        int varMax = 3;

        for (Signal<Record> signal : this.trainSignals) {
            if (signal.size() <= monitor.getNecessaryLength()) {
                fitness += penalty;
                continue;
            }

//            if (monitor.getNecessaryLength() < lengthMin) {
//                fitness += 0.01*(lengthMin - monitor.getNecessaryLength());
//            }
//            if (monitor.getVariablesList().stream().distinct().count() < varMin) {
//                fitness += 0.01*(varMin - monitor.getVariablesList().stream().distinct().count());
//            }
//            if (monitor.getMinLength() > lengthMax) {
//                fitness += 0.1*(monitor.getMinLength() - lengthMax);
//                fitness += penalty;
//            }
//            if (monitor.getVariablesList().stream().distinct().count() > varMax) {
//                fitness += 0.1*(monitor.getVariablesList().stream().distinct().count() - varMax);
//                fitness += penalty;
//            }

            fitnessArray = applyMonitor(monitor, signal);
//            fitness += fitnessArray[fitnessArray.length - 1];
//            fitness += Arrays.stream(fitnessArray).map(Math::abs).summaryStatistics().getAverage();
            fitness += Arrays.stream(fitnessArray).map(x -> x >= 0 ? x : 1).summaryStatistics().getAverage();
        }

        return fitness/this.trainSignals.size();
    }
}
