
import com.google.common.base.Stopwatch;
import core.problem.AbstractInvariantsProblem;
import core.problem.SingleInvariantsProblem;
import evolution.AttributeSpeciator;
import evolution.SpeciatedRemoveEvolver;
import it.units.malelab.jgea.Worker;
import it.units.malelab.jgea.core.Individual;
import it.units.malelab.jgea.core.evolver.Event;
import it.units.malelab.jgea.core.evolver.Evolver;
import it.units.malelab.jgea.core.evolver.StandardWithEnforcedDiversityEvolver;
import it.units.malelab.jgea.core.evolver.stopcondition.TargetFitness;
import it.units.malelab.jgea.core.listener.*;
import it.units.malelab.jgea.core.order.PartialComparator;
import it.units.malelab.jgea.core.selector.Tournament;
import it.units.malelab.jgea.core.selector.Worst;
import it.units.malelab.jgea.core.util.Misc;
import it.units.malelab.jgea.representation.grammar.cfggp.GrammarBasedSubtreeMutation;
import it.units.malelab.jgea.representation.grammar.cfggp.GrammarRampedHalfAndHalf;
import it.units.malelab.jgea.representation.tree.SameRootSubtreeCrossover;
import it.units.malelab.jgea.representation.tree.Tree;
import nodes.AbstractSTLNode;
import stopcondition.MultiTargetFitness;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static consumers.STLConsumer.*;
import static it.units.malelab.jgea.core.listener.NamedFunctions.*;
import static it.units.malelab.jgea.core.util.Args.*;

public class InvariantsProblemComparison extends Worker {

    public InvariantsProblemComparison(String[] args) {
        super(args);
    }

    public static void main(String[] args) {
        new InvariantsProblemComparison(args);
    }

    @Override
    public void run() {
        int nPop = i(a("nPop", "200"));
        int maxHeight = i(a("maxHeight", "20"));
        int nTournament = 5;
        int diversityMaxAttempts = 100;
//        String evolverNamePattern = a("evolution", "StandardDiversity");
        String evolverNamePattern = a("evolution", "Speciated");
        int[] seeds = ri(a("seed", "0:1"));
        String trainPath = a("trainPath", "data/SWaT/train_no201.csv");
        String testPath = a("testPath", "data/SWaT/test_no201.csv");
        String labelsPath = a("labelsPath", "data/SWaT/labels.csv");
        String grammarPath = a("grammarPath", "grammars/grammar_swat_no201.bnf");
        String testResultsFile = a("testResultsFile", "results/testResults.txt");
        String topResultsFile = a("topResultsFile", "results/topResults.txt");
        String ensembleResultsFile = a("ensembleResultsFile", "results/ensemble.csv");
        int traceLength = i(a("traceLength", "0"));
        double validationFraction = d(a("validationFraction", "0.2"));


        // Logging level global options.
        Level level = Level.INFO;
        Logger rootLog = Logger.getLogger("");
        rootLog.setLevel(level);
        rootLog.getHandlers()[0].setLevel(level);

        List<SingleInvariantsProblem> problems = null;
        try {
            problems = List.of(
                    new SingleInvariantsProblem(grammarPath, trainPath, testPath, labelsPath, traceLength,
                                                validationFraction)
                              );
        } catch (IOException e) {
            e.printStackTrace();
        }

        Map<String, Function<SingleInvariantsProblem, Evolver<Tree<String>, AbstractSTLNode, Double>>>
                evolvers = new TreeMap<>();

        evolvers.put("StandardDiversity", p -> new StandardWithEnforcedDiversityEvolver<>(
                p.getSolutionMapper(),
                new GrammarRampedHalfAndHalf<>(3, maxHeight, p.getGrammar()),
                PartialComparator.from(Double.class).comparing(Individual::getFitness),
//                new ParetoDominance<>(Double.class).comparing(Individual::getFitness),
                nPop,
                Map.of(
                        new SameRootSubtreeCrossover<>(maxHeight), 0.8d,
                        new GrammarBasedSubtreeMutation<>(maxHeight, p.getGrammar()), 0.2d
                      ),
                new Tournament(nTournament),
                new Worst(),
                500,
                true,
                diversityMaxAttempts
        ));


        evolvers.put("Speciated", p -> new SpeciatedRemoveEvolver<>(
                p.getSolutionMapper(),
                new GrammarRampedHalfAndHalf<>(3, maxHeight, p.getGrammar()),
                PartialComparator.from(Double.class).comparing(Individual::getFitness),
//                new ParetoDominance<>(Double.class).comparing(Individual::getFitness),
                nPop,
                Map.of(
                        new SameRootSubtreeCrossover<>(maxHeight), 0.8d,
                        new GrammarBasedSubtreeMutation<>(maxHeight, p.getGrammar()), 0.2d
                      ),
                new Tournament(nTournament),
                new AttributeSpeciator<>(
                        AbstractInvariantsProblem.getVariableList()
                )
        ));


        // Consumers.
        assert problems != null;

        List<NamedFunction<? super Event<? extends Tree<String>, ? extends AbstractSTLNode, ? extends Double>, ?>>
                functions = List.of(
                eventAttribute("seed", "%2d"),
                eventAttribute("problem", NamedFunction.formatOfLongest(
                        problems.stream().map(p -> p.getClass().getSimpleName())
                                .collect(Collectors.toList()))),
                eventAttribute("evolution", "%20.20s"),
                iterations(),
                births(),
                elapsedSeconds(),
                fitnessEvaluations(),
                size().of(all()),
                size().of(firsts()),
                size().of(lasts()),
                uniqueness().of(each(genotype())).of(all()),
                uniqueness().of(each(solution())).of(all()),
                uniqueness().of(each(fitness())).of(all()),
                size().of(genotype()).of(best()),
                birthIteration().of(best()),
                hist(8).of(each(fitness())).of(all()),

                // TODO: Better handling.
                TPR(problems.get(0).getFitnessFunction()).of(solution()).of(best()),
                FPR(problems.get(0).getFitnessFunction()).of(solution()).of(best()),
                FNR(problems.get(0).getFitnessFunction()).of(solution()).of(best()),

                temporalLength().of(solution()).of(best()),
                coverage().of(solution()).of(best()),
                height().of(genotype()).of(best()),

                size().of(zeroFitness()),
                totalVariableCoverage(),

                fitness().reformat("%7.5f").of(best()));

        // Factories.
        Listener.Factory<Event<? extends Tree<String>, ? extends AbstractSTLNode, ? extends Double>>
                listenerFactory = new TabularPrinter<>(functions);
        if (a("file", null) != null) {
            listenerFactory = Listener.Factory.all(List.of(
                    listenerFactory,
                    new CSVPrinter<>(functions, new File(a("file", null)))
                                                          ));
        }


        /////////////////
        // Actual run. //
        /////////////////

        // Filtering evolvers.
        evolvers = evolvers.entrySet().stream()
                           .filter(e -> e.getKey().matches(evolverNamePattern))
                           .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        L.info(String.format("Going to test with %d evolver/s: %s%n", evolvers.size(), evolvers.keySet()));

        // Run.
        for (SingleInvariantsProblem problem : problems) {
            for (int seed : seeds) {
                for (Map.Entry<String, Function<SingleInvariantsProblem, Evolver<Tree<String>,
                        AbstractSTLNode, Double>>> evolverEntry : evolvers.entrySet()) {
                    Map<String, Object> keys = Map.ofEntries(
                        Map.entry("seed", seed),
                        Map.entry("problem", problem.getClass().getSimpleName().toLowerCase()),
                        Map.entry("evolution", evolverEntry.getKey()),
                        Map.entry("traceLength", String.valueOf(traceLength)),
                        Map.entry("validationFraction", String.valueOf(validationFraction)));
                    Listener<Event<? extends Tree<String>, ? extends AbstractSTLNode, ? extends Double>> listener
                            = Listener.all(List.of(new EventAugmenter(keys),
                                                   listenerFactory.build())).deferred(executorService);
                    try {
                        Stopwatch stopwatch = Stopwatch.createStarted();
                        Evolver<Tree<String>, AbstractSTLNode, Double> evolver = evolverEntry.getValue().apply(problem);
                        L.info(String.format("Starting %s", keys));
                        Collection<AbstractSTLNode> solutions = evolver.solve(
                                Misc.cached(problem.getFitnessFunction(), 10000),
                                new TargetFitness<>(0.0d),
//                                new Iterations(0),
//                                new MultiTargetFitness<>(0d, 20),
//                                new SpeciesZero(0.0, 40),
                                new Random(seed),
                                executorService,
                                listener);
                        L.info(String.format("Done %s: %d solutions in %4.1fs",
                                             keys,
                                             solutions.size(),
                                             (double) stopwatch.elapsed(TimeUnit.MILLISECONDS) / 1000d
                                            ));

                        // Validation.
                        double threshold = 0.0;
                        L.info("Number of solutions pre-validation: " + solutions.size());

                        // Filter solutions with large validation FPR;
                        if (validationFraction > 0.0) {
                            solutions = solutions.stream().filter(x -> problem.getFitnessFunction()
                                                                              .validateSolution(x) <= threshold)
                                                 .collect(Collectors.toList());

                        }
                        L.info("Number of solutions validated: " + solutions.size());

                        Collection<AbstractSTLNode> solutionsTop5;
                        Collection<AbstractSTLNode> solutionsTop10;
                        Collection<AbstractSTLNode> solutionsTop25;
                        Collection<AbstractSTLNode> solutionsTop50;
//                        solutionsTop5 = new ArrayList<>(solutions).subList(0, solutions.size() / 20);
                        solutionsTop10 = new ArrayList<>(solutions).subList(0, solutions.size() / 10);
//                        solutionsTop25 = new ArrayList<>(solutions).subList(0, solutions.size() / 4);
//                        solutionsTop50 = new ArrayList<>(solutions).subList(0, solutions.size() / 2);

//                        solutionsZero = solutions.stream()
//                                                 .filter(x -> problem.getFitnessFunction().apply(x).equals(0.0))
//                                                 .collect(Collectors.toList());

                        // Print solutions.
//                        solutions.forEach(System.out::println);

                        // Test to file.
//                        problem.getFitnessFunction().collectionToFile(solutionsTop5, topResultsFile);
//                        problem.getFitnessFunction().collectionToFile(solutionsTop10, "10top" + testResultsFile);
//                        problem.getFitnessFunction().collectionToFile(solutionsTop10, topResultsFile);
//                        problem.getFitnessFunction().collectionToFile(solutionsTop50, "50top" + testResultsFile);
//                        problem.getFitnessFunction().collectionToCompressedFile(solutions, testResultsFile);
//                        problem.getFitnessFunction().collectionToFile(solutions, testResultsFile);
                        problem.getFitnessFunction().ensembleToFile(solutions, ensembleResultsFile);

                        // Pareto ensemble to file.
//                        problem.getFitnessFunction().paretoToFile(ensemble.get(ensemble.size() - 1), testResultsFile);

                    } catch (InterruptedException | ExecutionException | IOException e) {
                        L.severe(String.format("Cannot complete %s due to %s",
                                               keys,
                                               e
                                              ));
                        e.printStackTrace();
                    }
                }
            }
        }

    }


}

