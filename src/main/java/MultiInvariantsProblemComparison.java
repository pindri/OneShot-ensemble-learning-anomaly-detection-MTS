//import com.google.common.base.Stopwatch;
//import core.problem.MultiInvariantsProblem;
//import datacollectors.BestTreeInfo;
//import eu.quanticol.moonlight.util.Pair;
//import it.units.malelab.jgea.Worker;
//import it.units.malelab.jgea.core.Individual;
//import it.units.malelab.jgea.core.evolver.Evolver;
//import it.units.malelab.jgea.core.evolver.StandardWithEnforcedDiversityEvolver;
//import it.units.malelab.jgea.core.evolver.stopcondition.Iterations;
//import it.units.malelab.jgea.core.listener.Listener;
//import it.units.malelab.jgea.core.listener.MultiFileListenerFactory;
//import it.units.malelab.jgea.core.listener.collector.*;
//import it.units.malelab.jgea.core.order.ParetoDominance;
//import it.units.malelab.jgea.core.selector.Tournament;
//import it.units.malelab.jgea.core.selector.Worst;
//import it.units.malelab.jgea.core.util.Misc;
//import it.units.malelab.jgea.problem.symbolicregression.RealFunction;
//import it.units.malelab.jgea.representation.grammar.cfggp.GrammarBasedSubtreeMutation;
//import it.units.malelab.jgea.representation.grammar.cfggp.GrammarRampedHalfAndHalf;
//import it.units.malelab.jgea.representation.tree.SameRootSubtreeCrossover;
//import it.units.malelab.jgea.representation.tree.Tree;
//import nodes.AbstractSTLNode;
//import stopcondition.ParetoTarget;
//
//import java.io.IOException;
//import java.util.*;
//import java.util.concurrent.ExecutionException;
//import java.util.concurrent.TimeUnit;
//import java.util.function.Function;
//import java.util.stream.Collectors;
//
//import static it.units.malelab.jgea.core.util.Args.*;
//
//public class MultiInvariantsProblemComparison extends Worker {
//
//    public MultiInvariantsProblemComparison(String[] args) {
//        super(args);
//    }
//
//    public static void main(String[] args) {
//        new MultiInvariantsProblemComparison(args);
//    }
//
//    @Override
//    public void run() {
//        int nPop = i(a("nPop", "100"));
//        int maxHeight = i(a("maxHeight", "20"));
//        int nTournament = 5;
//        int diversityMaxAttempts = 100;
////        int nIterations = i(a("nIterations", "250"));
//        String evolverNamePattern = a("evolver", ".*Diversity.*");
//        int[] seeds = ri(a("seed", "0:1"));
//        String trainPath = a("trainPath", "data/SWaT/train.csv");
//        String testPath = a("testPath", "data/SWaT/test.csv");
//        String labelsPath = a("labelsPath", "data/SWaT/labels.csv");
//        String grammarPath = a("grammarPath", "grammar_temporal.bnf");
//        String testResultsFile = a("testResultsFile", "testResults.txt");
//        String validationResultsFile = a("validationResultsFile", "validationResults.txt");
////        String paretoResultsFile = a("paretoResultsFile", "paretoResults.txt");
//        int traceLength = i(a("traceLength", "0"));
//        double validationFraction = d(a("validationFraction", "0.2"));
//
//
//        List<MultiInvariantsProblem> problems = null;
//        try {
//            problems = List.of(
//                    new MultiInvariantsProblem(grammarPath, trainPath, testPath, labelsPath, traceLength,
//                                               validationFraction)
//            );
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//
//        MultiFileListenerFactory<Object, RealFunction, Double> listenerFactory = new MultiFileListenerFactory<>(
//                a("dir", "."),
//                a("file", null)
//        );
//
//        Map<String, Function<MultiInvariantsProblem, Evolver<Tree<String>, AbstractSTLNode, List<Double>>>>
//                evolvers = new TreeMap<>();
//
//        evolvers.put("StandardDiversity", p -> new StandardWithEnforcedDiversityEvolver<>(
//                p.getSolutionMapper(),
//                new GrammarRampedHalfAndHalf<>(3, maxHeight, p.getGrammar()),
//                new ParetoDominance<>(Double.class).comparing(Individual::getFitness),
//                nPop,
//                Map.of(
//                        new SameRootSubtreeCrossover<>(maxHeight), 0.8d,
//                        new GrammarBasedSubtreeMutation<>(maxHeight, p.getGrammar()), 0.2d
//                ),
//                new Tournament(nTournament),
//                new Worst(),
//                500,
//                true,
//                diversityMaxAttempts
//        ));
//
//        /////////////////
//        // Actual run. //
//        /////////////////
//
//        // Filtering evolvers.
//        evolvers = evolvers.entrySet().stream()
//                .filter(e -> e.getKey().matches(evolverNamePattern))
//                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
//        L.info(String.format("Going to test with %d evolver/s: %s%n", evolvers.size(), evolvers.keySet()));
//
//
//        assert problems != null;
//        for (MultiInvariantsProblem problem : problems) {
//            for (int seed : seeds) {
//                for (Map.Entry<String, Function<MultiInvariantsProblem, Evolver<Tree<String>,
//                        AbstractSTLNode, List<Double>>>> evolverEntry : evolvers.entrySet()) {
//                    Map<String, String> keys = new TreeMap<>(Map.of(
//                            "seed", Integer.toString(seed),
//                            "problem", problem.getClass().getSimpleName().toLowerCase(),
//                            "evolver", evolverEntry.getKey(),
//                            "traceLength", String.valueOf(traceLength),
//                            "validationFraction", String.valueOf(validationFraction)
//                    ));
//                    try {
//                        List<DataCollector<? super Tree<String>, ? super AbstractSTLNode, ? super List<Double>>>
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
////                                        new FunctionOfFirsts<>( // Get front
////                                                i -> {
////                                                    List<Pair<AbstractSTLNode, Double>>
////                                                            l = i.stream().map(x -> new Pair<AbstractSTLNode, Double>(
////                                                                    x.getSolution(),
////                                                                    x.getFitness().get(0)))
////                                                                 .collect(Collectors.toList());
////                                                    ensemble.add(new ArrayList<>(l));
////                                                    return List.of(new Item("aux", 0, "%3d"));
////                                                }
////                                        )
//                        );
//
//                        Stopwatch stopwatch = Stopwatch.createStarted();
//                        Evolver<Tree<String>, AbstractSTLNode, List<Double>> evolver = evolverEntry.getValue().apply(problem);
//                        L.info(String.format("Starting %s", keys));
//
//                        @SuppressWarnings("unchecked")
//                        Collection<AbstractSTLNode> solutions = evolver.solve(
//                                Misc.cached(problem.getFitnessFunction(), 10000),
//                                new ParetoTarget(List.of(0.0, 500.0)).or(new Iterations(50)),
////                                new Iterations(10),
//                                new Random(seed),
//                                executorService,
//                                Listener.onExecutor((listenerFactory.getBaseFileName() == null) ?
//                                                            listener(collectors.toArray(DataCollector[]::new)) :
//                                                            listenerFactory
//                                                                    .build(collectors.toArray(DataCollector[]::new)),
//                                                    executorService)
//                        );
//
//                        // Validation.
//                        double threshold = 0.001;
//                        if (validationFraction > 0.0) {
//                            // Filter solutions with large validation FPR;
//                            solutions = solutions.stream().filter(x -> problem.getFitnessFunction()
//                                                                  .validateSolution(x) <= threshold)
//                                                 .collect(Collectors.toList());
//                        }
//
//                        // Test to file.
//                        AbstractSTLNode solution = solutions.iterator().next();
//                        System.out.println("\n" + solution);
////                        problem.getFitnessFunction().solutionToFile(solution, testResultsFile);
//                        problem.getFitnessFunction().collectionToFile(solutions, testResultsFile);
//
//                        // Pareto ensemble to file.
//                        List<Pair<AbstractSTLNode, Double>> ensemble = new ArrayList<>();
//                        for (AbstractSTLNode s : solutions) {
//                            ensemble.add(new Pair<>(s, problem.getFitnessFunction().apply(s).get(0)));
//                        }
//
//                        problem.getFitnessFunction().paretoToFile(ensemble, testResultsFile);
//                        problem.getFitnessFunction().bestParetoToFile(ensemble, testResultsFile);
//
//
//                        L.info(String.format("Done %s: %d solutions in %4.1fs",
//                                             keys,
//                                             solutions.size(),
//                                             (double) stopwatch.elapsed(TimeUnit.MILLISECONDS) / 1000d
//                                            ));
//
//                    } catch (InterruptedException | ExecutionException | IOException e) {
//                        L.severe(String.format("Cannot complete %s due to %s", keys, e));
//                        e.printStackTrace();
//                    }
//                }
//            }
//        }
//
//    }
//
//
//}
//
