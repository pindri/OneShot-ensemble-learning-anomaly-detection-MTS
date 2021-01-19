package core.problem;

import core.fitness.AbstractFitnessFunction;
import it.units.malelab.jgea.representation.grammar.Grammar;
import it.units.malelab.jgea.representation.grammar.GrammarBasedProblem;
import mapper.STLMapper;
import nodes.AbstractSTLNode;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class AbstractInvariantsProblem<F> implements GrammarBasedProblem<String, AbstractSTLNode, F> {

    protected static String[] boolNames;
    protected static String[] numNames;
    protected final Grammar<String> grammar;
    protected final STLMapper solutionMapper;
    protected AbstractFitnessFunction<F> fitnessFunction;

    protected AbstractInvariantsProblem(String grammarPath, String trainPath, String testPath, String labelPath,
                                     int traceLength, double validationFraction) throws IOException {
        // Note: names must be initialised first.
        boolNames = new String[]{}; // No boolean variables are used.
        numNames = initialiseNames(trainPath);
        this.grammar = initialiseGrammar(grammarPath, trainPath);
        this.solutionMapper = new STLMapper();
    }

    public String[] initialiseNames(String dataPath) throws IOException {
        return Objects.requireNonNull(Files.lines(Path.of(dataPath)).findFirst().orElse(null))
                      .replace("\"", "").split(",");
    }

    private Grammar<String> initialiseGrammar(String grammarPath, String dataPath) throws IOException {
        String replacement = Objects.requireNonNull(Files.lines(Path.of(dataPath)).findFirst().orElse(null))
                .replace("\"", "").replace(",", " | ");

        try (Stream<String> lines = Files.lines(Path.of(grammarPath))) {
            List<String> replaced = lines
                    .map(line-> line.replaceAll("(?m)^<var>.*", "<var> ::= " + replacement))
                    .collect(Collectors.toList());
            Files.write(Path.of(grammarPath), replaced);
        }

        return Grammar.fromFile(new File(grammarPath));
    }

    @Override
    public Grammar<String> getGrammar() {
        return this.grammar;
    }

    @Override
    public STLMapper getSolutionMapper() {
        return this.solutionMapper;
    }

    @Override
    public AbstractFitnessFunction<F> getFitnessFunction() {
        return this.fitnessFunction;
    }

    public static String[] getBoolNames() {
        return boolNames;
    }

    public static String[] getNumNames() {
        return numNames;
    }

    public static List<String> getVariableList() {
        return Stream.of(numNames, boolNames).flatMap(Stream::of).collect(Collectors.toList());
    }
}
