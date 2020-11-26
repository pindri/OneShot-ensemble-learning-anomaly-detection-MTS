package core;

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

public class InvariantsProblem implements GrammarBasedProblem<String, AbstractSTLNode, Double> {

    private static String[] boolNames;
    private static String[] numNames;
    private final Grammar<String> grammar;
    private final STLMapper solutionMapper;
    private final AbstractFitnessFunction fitnessFunction;

    public InvariantsProblem(String grammarPath, String trainPath, String testPath, String labelPath,
                             int traceLength) throws IOException {
        // Note: names must be initialised first.
        boolNames = new String[]{}; // No boolean variables are used.
        numNames = initialiseNames(trainPath);
        this.grammar = initialiseGrammar(grammarPath, trainPath);
        this.solutionMapper = new STLMapper();
//        this.fitnessFunction = new FitnessFunction(trainPath, testPath, labelPath, traceLength);
        this.fitnessFunction = new FitnessFunction(trainPath, testPath, labelPath, traceLength);
    }

    public String[] initialiseNames(String dataPath) throws IOException {
        return Objects.requireNonNull(Files.lines(Path.of(dataPath)).findFirst().orElse(null)).replace("\"", "").split(",");
    }

    private Grammar<String> initialiseGrammar(String grammarPath, String dataPath) throws IOException {
//        String replacement = Objects.requireNonNull(Files.lines(Path.of(dataPath)).findFirst().orElse(null)).replace("\"", "").replace(",", " | ");
//
//        try (Stream<String> lines = Files.lines(Path.of(grammarPath))) {
//            List<String> replaced = lines
//                    .map(line-> line.replaceAll("(?m)^<var>.*", "<var> ::= " + replacement))
//                    .collect(Collectors.toList());
//            Files.write(Path.of(grammarPath), replaced);
//        }

        return Grammar.fromFile(new File(grammarPath ));
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
    public AbstractFitnessFunction getFitnessFunction() {
        return this.fitnessFunction;
    }

    public static String[] getBoolNames() {
        return boolNames;
    }

    public static String[] getNumNames() {
        return numNames;
    }
}
