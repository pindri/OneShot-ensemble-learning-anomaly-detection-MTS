
import com.google.common.base.Stopwatch;
import core.problem.AbstractInvariantsProblem;
import core.problem.SingleInvariantsProblem;
import it.units.malelab.jgea.Worker;
import it.units.malelab.jgea.core.Individual;
import it.units.malelab.jgea.core.consumer.*;
import it.units.malelab.jgea.core.evolver.Evolver;
import it.units.malelab.jgea.core.evolver.StandardWithEnforcedDiversityEvolver;
import it.units.malelab.jgea.core.evolver.stopcondition.Iterations;
import it.units.malelab.jgea.core.evolver.stopcondition.TargetFitness;
import it.units.malelab.jgea.core.order.PartialComparator;
import it.units.malelab.jgea.core.selector.Tournament;
import it.units.malelab.jgea.core.selector.Worst;
import it.units.malelab.jgea.core.util.Misc;
import it.units.malelab.jgea.core.util.Pair;
import it.units.malelab.jgea.problem.symbolicregression.RealFunction;
import it.units.malelab.jgea.representation.grammar.cfggp.GrammarBasedSubtreeMutation;
import it.units.malelab.jgea.representation.grammar.cfggp.GrammarRampedHalfAndHalf;
import it.units.malelab.jgea.representation.tree.SameRootSubtreeCrossover;
import it.units.malelab.jgea.representation.tree.Tree;
import nodes.AbstractSTLNode;
import org.apache.commons.math3.analysis.function.Abs;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import static it.units.malelab.jgea.core.consumer.NamedFunctions.constant;
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
        int nPop = i(a("nPop", "100"));
        int maxHeight = i(a("maxHeight", "20"));
        int nTournament = 5;
        int diversityMaxAttempts = 100;
//        int nIterations = i(a("nIterations", "250"));
        String evolverNamePattern = a("evolver", ".*Diversity.*");
        int[] seeds = ri(a("seed", "0:1"));
        String trainPath = a("trainPath", "data/SWaT/train_no201.csv");
        String testPath = a("testPath", "data/SWaT/test_no201.csv");
        String labelsPath = a("labelsPath", "data/SWaT/labels.csv");
        String grammarPath = a("grammarPath", "grammar_swat_no201.bnf");
        String testResultsFile = a("testResultsFile", "testResults.txt");
        String validationResultsFile = a("validationResultsFile", "validationResults.txt");
//        String paretoResultsFile = a("paretoResultsFile", "paretoResults.txt");
        int traceLength = i(a("traceLength", "0"));
        double validationFraction = d(a("validationFraction", "0.0"));
//        String magicVariable = a("magicVariable", "X1_AIT_001_PV");


        List<SingleInvariantsProblem> problems = null;
        try {
            problems = List.of(
                    new SingleInvariantsProblem(grammarPath, trainPath, testPath, labelsPath, traceLength,
                                                validationFraction)
            );
        } catch (IOException e) {
            e.printStackTrace();
        }


//        MultiFileListenerFactory<Object, RealFunction, Double> listenerFactory = new MultiFileListenerFactory<>(
//                a("dir", "."),
//                a("file", null)
//        );

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
//        evolvers.put("Speciated", p -> new SpeciatedEvolver<>(
//                p.getSolutionMapper(),
//                new GrammarRampedHalfAndHalf<>(3, maxHeight, p.getGrammar()),
//                PartialComparator.from(Double.class).comparing(Individual::getFitness),
////                new ParetoDominance<>(Double.class).comparing(Individual::getFitness),
//                nPop,
//                Map.of(
//                        new SameRootSubtreeCrossover<>(maxHeight), 0.8d,
//                        new GrammarBasedSubtreeMutation<>(maxHeight, p.getGrammar()), 0.2d
//                      ),
//                5,
//                0.75
//
//        ))

        // Consumers.
        assert problems != null;
        Map<String, Object> keys = new HashMap<>();

        List<NamedFunction<Event<? extends Tree<String>, ? extends AbstractSTLNode, ? extends Double>, ?>>
                functions = List.of(
                        constant("seed", "%2d", keys),
                        constant("problem", NamedFunction.formatOfLongest(
                                problems.stream().map(p -> p.getClass().getSimpleName())
                                .collect(Collectors.toList())),
                                 keys),
                        constant("evolver", "%20.20s", keys));

        List<Consumer.Factory<Tree<String>, AbstractSTLNode, Double, Void>>
                factories = new ArrayList<>();
        factories.add(new TabularPrinter<>(functions, System.out, 10, true));
        if (a("file", null) != null) {
            factories.add(new CSVPrinter<>(functions, new File(a("file", null))));
        }



        /////////////////
        // Actual run. //
        /////////////////

        // Filtering evolvers.
        evolvers = evolvers.entrySet().stream()
                .filter(e -> e.getKey().matches(evolverNamePattern))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        L.info(String.format("Going to test with %d evolver/s: %s%n", evolvers.size(), evolvers.keySet()));

        for (SingleInvariantsProblem problem : problems) {
            for (int seed : seeds) {
                for (Map.Entry<String, Function<SingleInvariantsProblem, Evolver<Tree<String>,
                        AbstractSTLNode, Double>>> evolverEntry : evolvers.entrySet()) {
                    keys.put("seed", Integer.toString(seed));
                    keys.put("problem", problem.getClass().getSimpleName().toLowerCase());
                    keys.put("evolver", evolverEntry.getKey());
                    keys.put("traceLength", String.valueOf(traceLength));
                    keys.put("validationFraction", String.valueOf(validationFraction));
                    Consumer<Tree<String>, AbstractSTLNode, Double, ?>
                            consumer = Consumer.of(factories.stream().map(Consumer.Factory::build)
                                                            .collect(Collectors.toList()))
                                                  .deferred(executorService);
                    try {
                        Stopwatch stopwatch = Stopwatch.createStarted();
                        Evolver<Tree<String>, AbstractSTLNode, Double> evolver = evolverEntry.getValue().apply(problem);
                        L.info(String.format("Starting %s", keys));
                        Collection<AbstractSTLNode> solutions = evolver.solve(
                                Misc.cached(problem.getFitnessFunction(), 10000),
                                new TargetFitness<>(0d),
////                                new Iterations(0),
                                new Random(seed),
                                executorService,
                                consumer
                        );
                        consumer.consume(solutions);
                        L.info(String.format("Done %s: %d solutions in %4.1fs",
                                             keys,
                                             solutions.size(),
                                             (double) stopwatch.elapsed(TimeUnit.MILLISECONDS) / 1000d
                                            ));
                    } catch (InterruptedException | ExecutionException e) {
                        L.severe(String.format("Cannot complete %s due to %s",
                                               keys,
                                               e
                                              ));
                        e.printStackTrace();
                    }
                }

            }

        }


//        List<List<Pair<AbstractSTLNode, Double>>> ensemble = new ArrayList<>();
//
//        assert problems != null;
//        for (SingleInvariantsProblem problem : problems) {
//            for (int seed : seeds) {
//                for (Map.Entry<String, Function<SingleInvariantsProblem, Evolver<Tree<String>,
//                        AbstractSTLNode, Double>>> evolverEntry : evolvers.entrySet()) {
//                    Map<String, String> keys = new TreeMap<>(Map.of(
//                            "seed", Integer.toString(seed),
//                            "problem", problem.getClass().getSimpleName().toLowerCase(),
//                            "evolver", evolverEntry.getKey(),
//                            "traceLength", String.valueOf(traceLength),
//                            "validationFraction", String.valueOf(validationFraction)
////                            ,"magicVariable", String.valueOf(magicVariable)
//                    ));
//                    try {
//                        List<DataCollector<? super Tree<String>, ? super AbstractSTLNode, ? super Double>>
//                                collectors = List.of(
//                                        new Static(keys),
//                                        new Basic(),
//                                        new Population(),
//                                        new Diversity(),
//                                        new FunctionOfOneBest<>
//                                                (i -> List.of(new Item("temporal.length",
//                                                                       i.getSolution().getMinLength(),
//                                                                       "%3d"),
//                                                              new Item("coverage",
//                                                                       i.getSolution().getCoverage(),
//                                                                       "%3f"))),
//                                        new FunctionOfOneBest<>
//                                                (i -> problem.getFitnessFunction().evaluateSolution(i.getSolution(),
//                                                                                                    "test")),
//                                        new BestTreeInfo("%7.5f")
////                                        new FunctionOfAll<>(i -> Pareto.computeIndices(i, problem, Operator.AND)),
////                                        new FunctionOfAll<>(i -> Pareto.computeIndices(i, problem, Operator.OR)),
////                                        new FunctionOfAll<>(i -> Pareto.computeIndices(i, problem, Operator.MAJORITY)),
////                                        new FunctionOfAll<>(i -> Pareto.computeIndices(i, problem, Operator.TWO)),
////                                        new FunctionOfAll<>(i -> List.of(new Item("front.size",
////                                                                                  Pareto.getFront(i).size(),
////                                                                                  "%3d"))),
////                                        new FunctionOfAll<>(
////                                                i -> {
//////                                                    ensemble.add(new ArrayList<>(Pareto.getFront(i)));
////                                                    return List.of(new Item("aux", 0, "%3d"));
////                                                }
////                                        )
////                                        new FunctionOfFirsts<>(
////                                                i -> problem.getFitnessFunction()
////                                                            .evaluateSolutions(i.stream().map(Individual::getSolution)
////                                                                                .collect(Collectors.toList()),
////                                                                               "Ensemble.AND", Operator.AND)),
////                                        new FunctionOfFirsts<>(
////                                                i -> problem.getFitnessFunction()
////                                                            .evaluateSolutions(i.stream().map(Individual::getSolution)
////                                                                                .collect(Collectors.toList()),
////                                                                               "Ensemble.OR", Operator.OR)),
////                                        new FunctionOfFirsts<>(
////                                                i -> problem.getFitnessFunction()
////                                                            .evaluateSolutions(i.stream().map(Individual::getSolution)
////                                                                                .collect(Collectors.toList()),
////                                                                                "Ensemble.M", Operator.MAJORITY)),
////                                        new FunctionOfFirsts<>(
////                                                i -> problem.getFitnessFunction()
////                                                            .evaluateSolutions(i.stream().map(Individual::getSolution)
////                                                                                .collect(Collectors.toList()),
////                                                                               "Ensemble.TWO", Operator.TWO))
//                        );
//
//                        Stopwatch stopwatch = Stopwatch.createStarted();
//                        Evolver<Tree<String>, AbstractSTLNode, Double> evolver = evolverEntry.getValue().apply(problem);
//                        L.info(String.format("Starting %s", keys));
//
//                        @SuppressWarnings("unchecked")
//                        Collection<AbstractSTLNode> solutions = evolver.solve(
//                                Misc.cached(problem.getFitnessFunction(), 10000),
////                                new TargetFitness<>(0d),
////                                new Iterations(0),
//                                new MultiTargetFitness<>(0d, 100),
//                                new Random(seed),
//                                executorService,
//                                Listener.onExecutor((listenerFactory.getBaseFileName() == null) ?
//                                                            listener(collectors.toArray(DataCollector[]::new)) :
//                                                            listenerFactory
//                                                                    .build(collectors.toArray(DataCollector[]::new)),
//                                                    executorService)
//                        );
//
//
//                        // Validation.
///*
//                        if (validationFraction > 0.0) {
//                            // Select solution with smaller FPR.
//                            Optional<AbstractSTLNode> validationSolution = solutions.stream()
//                                    .reduce((a, b) -> problem.getFitnessFunction().validateSolution(a) <=
//                                            problem.getFitnessFunction().validateSolution(b) ? a : b );
//
//                            validationSolution.ifPresent(valSolution -> {
//                                try {
////                                     Validation to file.
//                                    problem.getFitnessFunction().solutionToFile(valSolution, validationResultsFile);
//                                } catch (IOException e) {
//                                    e.printStackTrace();
//                                }
//                            });
//                        }
//*/
//
//                        // Validation.
//                        double threshold = 0.0;
//                        if (validationFraction > 0.0) {
//                            // Filter solutions with large validation FPR;
//                            solutions = solutions.stream().filter(x -> problem.getFitnessFunction()
//                                                                              .validateSolution(x) <= threshold)
//                                                 .collect(Collectors.toList());
//                        }
//
//
//                        // Test to file.
//                        AbstractSTLNode solution = solutions.iterator().next();
//                        System.out.println("\n" + solution);
////                        problem.getFitnessFunction().solutionToFile(solution, testResultsFile);
//                        problem.getFitnessFunction().collectionToFile(solutions, testResultsFile);
//
//
//                        // Pareto ensemble to file.
////                        problem.getFitnessFunction().paretoToFile(ensemble.get(ensemble.size() - 1), testResultsFile);
//
//
//                        L.info(String.format("Done %s: %d solutions in %4.1fs",
//                                             keys,
//                                             solutions.size(),
//                                             (double) stopwatch.elapsed(TimeUnit.MILLISECONDS) / 1000d
//                        ));
//
//                    } catch (InterruptedException | ExecutionException | IOException e) {
//                        L.severe(String.format("Cannot complete %s due to %s", keys, e));
//                        e.printStackTrace();
//                    }
//                }
//            }
//        }
//
    }


}

