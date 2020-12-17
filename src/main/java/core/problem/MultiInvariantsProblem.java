package core.problem;

import core.fitness.MultiFitnessFunction;

import java.io.IOException;
import java.util.List;

public class MultiInvariantsProblem extends AbstractInvariantsProblem<List<Double>> {

    public MultiInvariantsProblem(String grammarPath, String trainPath, String testPath, String labelPath,
                                  int traceLength, double validationFraction) throws IOException {
        super(grammarPath, trainPath, testPath, labelPath, traceLength, validationFraction);
        super.fitnessFunction = new MultiFitnessFunction(trainPath, testPath, labelPath, traceLength,
                                                         validationFraction);
    }
}
