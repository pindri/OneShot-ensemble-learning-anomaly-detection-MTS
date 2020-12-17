package core.problem;

import core.fitness.AbstractFitnessFunction;
import core.fitness.SingleFitnessFunction;

import java.io.IOException;

public class SingleInvariantsProblem extends AbstractInvariantsProblem<Double> {

    public SingleInvariantsProblem(String grammarPath, String trainPath, String testPath, String labelPath,
                                   int traceLength, double validationFraction) throws IOException {
        super(grammarPath, trainPath, testPath, labelPath, traceLength, validationFraction);
        super.fitnessFunction = new SingleFitnessFunction(trainPath, testPath, labelPath, traceLength,
                                                          validationFraction);
    }

    @Override
    public AbstractFitnessFunction<Double> getFitnessFunction() {
        return super.fitnessFunction;
    }
}
