package core.problem;

import core.fitness.AbstractFitnessFunction;
import core.fitness.SingleFitnessFunction;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SingleInvariantsProblem extends AbstractInvariantsProblem<Double> {

    public SingleInvariantsProblem(String grammarPath, String trainPath, String testPath, String labelPath,
                                   int traceLength, double validationFraction, String magicVariable) throws IOException {
        super(grammarPath, trainPath, testPath, labelPath, traceLength, validationFraction);
        super.fitnessFunction = new SingleFitnessFunction(trainPath, testPath, labelPath, traceLength,
                                                          validationFraction, magicVariable);
    }

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
