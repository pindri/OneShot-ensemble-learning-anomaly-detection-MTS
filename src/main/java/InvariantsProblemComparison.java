import com.google.common.base.Stopwatch;
import core.InvariantsProblem;
import datacollectors.BestTreeInfo;
import it.units.malelab.jgea.Worker;
import it.units.malelab.jgea.core.Individual;
import it.units.malelab.jgea.core.evolver.Evolver;
import it.units.malelab.jgea.core.evolver.StandardEvolver;
import it.units.malelab.jgea.core.evolver.StandardWithEnforcedDiversityEvolver;
import it.units.malelab.jgea.core.evolver.stopcondition.Iterations;
import it.units.malelab.jgea.core.evolver.stopcondition.TargetFitness;
import it.units.malelab.jgea.core.listener.Listener;
import it.units.malelab.jgea.core.listener.MultiFileListenerFactory;
import it.units.malelab.jgea.core.listener.collector.*;
import it.units.malelab.jgea.core.order.PartialComparator;
import it.units.malelab.jgea.core.selector.Tournament;
import it.units.malelab.jgea.core.selector.Worst;
import it.units.malelab.jgea.core.util.Misc;
import it.units.malelab.jgea.problem.symbolicregression.*;
import it.units.malelab.jgea.representation.grammar.cfggp.GrammarBasedSubtreeMutation;
import it.units.malelab.jgea.representation.grammar.cfggp.GrammarRampedHalfAndHalf;
import it.units.malelab.jgea.representation.tree.SameRootSubtreeCrossover;
import it.units.malelab.jgea.representation.tree.Tree;
import nodes.AbstractSTLNode;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import static it.units.malelab.jgea.core.util.Args.i;
import static it.units.malelab.jgea.core.util.Args.ri;

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
        int maxHeight = i(a("maxHeight", "10"));
        int nTournament = 5;
        int diversityMaxAttempts = 100;
        int nIterations = i(a("nIterations", "100"));
        String evolverNamePattern = a("evolver", ".*Diversity.*");
        int[] seeds = ri(a("seed", "0:1"));
        String trainPath = a("trainPath", "data/SWaT/train_partial.csv");
        String testPath = a("testPath", "data/SWaT/test.csv");
        String labelsPath = a("labelsPath", "data/SWaT/labels.csv");
        String grammarPath = a("grammarPath", "grammar_temporal.bnf");
//        double[] constants = new double[]{0.1, 1d, 10d};


        List<InvariantsProblem> problems = null;
        try {
            problems = List.of(
                    new InvariantsProblem(grammarPath, trainPath, testPath, labelsPath, 12)
            );
        } catch (IOException e) {
            e.printStackTrace();
        }


        MultiFileListenerFactory<Object, RealFunction, Double> listenerFactory = new MultiFileListenerFactory<>(
                a("dir", "."),
                a("file", null)
        );


        Map<String, Function<InvariantsProblem, Evolver<Tree<String>, AbstractSTLNode, Double>>>
                evolvers =new TreeMap<>();

        evolvers.put("Standard", p -> new StandardEvolver<>(
                p.getSolutionMapper(),
                new GrammarRampedHalfAndHalf<>(3, maxHeight, p.getGrammar()),
                PartialComparator.from(Double.class).comparing(Individual::getFitness),
                nPop,
                Map.of(
                    new SameRootSubtreeCrossover<>(maxHeight), 0.8d,
                    new GrammarBasedSubtreeMutation<>(maxHeight, p.getGrammar()), 0.20d
                ),
                new Tournament(nTournament),
                new Worst(),
                500,
                true
        ));

        evolvers.put("StandardDiversity", p -> new StandardWithEnforcedDiversityEvolver<>(
                p.getSolutionMapper(),
                new GrammarRampedHalfAndHalf<>(3, maxHeight, p.getGrammar()),
                PartialComparator.from(Double.class).comparing(Individual::getFitness),
                nPop,
                Map.of(
                        new SameRootSubtreeCrossover<>(maxHeight), 0.8d,
                        new GrammarBasedSubtreeMutation<>(maxHeight, p.getGrammar()), 0.20d
                ),
                new Tournament(nTournament),
                new Worst(),
                500,
                true,
                diversityMaxAttempts
        ));

        // Actual run.

        // Filtering evolvers.
        evolvers = evolvers.entrySet().stream()
                .filter(e -> e.getKey().matches(evolverNamePattern))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        L.info(String.format("Going to test with %d evolver/s: %s%n", evolvers.size(), evolvers.keySet()));


        for (int seed : seeds) {
            assert problems != null;
            for (InvariantsProblem problem : problems) {
                for (Map.Entry<String, Function<InvariantsProblem, Evolver<Tree<String>,
                        AbstractSTLNode, Double>>> evolverEntry : evolvers.entrySet()) {
                    Map<String, String> keys = new TreeMap<>(Map.of(
                            "seed", Integer.toString(seed),
                            "problem", problem.getClass().getSimpleName().toLowerCase(),
                            "evolver", evolverEntry.getKey()
                    ));
                    try {
                        List<DataCollector<? super Tree<String>, ? super AbstractSTLNode, ? super Double>>
                                collectors = List.of(
                                        new Static(keys),
                                        new Basic(),
                                        new Population(),
                                        new Diversity(),
                                        new FunctionOfOneBest<>
                                                (i -> List.of(new Item(
                                                        "temporal.length",
                                                        i.getSolution().getMinLength(),
                                                        "%3d"
                                                ))),
                                        new FunctionOfOneBest<>
                                                (i -> problem.getFitnessFunction().evaluateSolution(i.getSolution())),
                                        new BestTreeInfo("%7.5f")
////                                        , new BestPrinter(BestPrinter.Part.SOLUTION, "%80.80s")
                        );

                        Stopwatch stopwatch = Stopwatch.createStarted();
                        Evolver<Tree<String>, AbstractSTLNode, Double> evolver = evolverEntry.getValue().apply(problem);
                        L.info(String.format("Starting %s", keys));

                        @SuppressWarnings("unchecked")
                        Collection<AbstractSTLNode> solutions = evolver.solve(
                                Misc.cached(problem.getFitnessFunction(), 10000),
                                new TargetFitness<>(0d).or(new Iterations(nIterations)),
                                new Random(seed),
                                executorService,
                                Listener.onExecutor((listenerFactory.getBaseFileName() == null) ?
                                                            listener(collectors.toArray(DataCollector[]::new)) :
                                                            listenerFactory.build(collectors.toArray(DataCollector[]::new)),
                                                    executorService)
                        );


                        L.info(String.format("Done %s: %d solutions in %4.1fs",
                                             keys,
                                             solutions.size(),
                                             (double) stopwatch.elapsed(TimeUnit.MILLISECONDS) / 1000d
                        ));

                    } catch (InterruptedException | ExecutionException e) {
                        L.severe(String.format("Cannot complete %s due to %s", keys, e));
                        e.printStackTrace();
                    }
                }
            }
        }

    }


}

