import core.FitnessFunction;
import core.InvariantsProblem;
import eu.quanticol.moonlight.signal.Signal;
import it.units.malelab.jgea.Worker;
import it.units.malelab.jgea.core.Individual;
import it.units.malelab.jgea.core.evolver.StandardEvolver;
import it.units.malelab.jgea.core.evolver.StandardWithEnforcedDiversityEvolver;
import it.units.malelab.jgea.core.evolver.stopcondition.Iterations;
import it.units.malelab.jgea.core.listener.collector.*;
import it.units.malelab.jgea.core.operator.GeneticOperator;
import it.units.malelab.jgea.core.order.PartialComparator;
import it.units.malelab.jgea.core.selector.Tournament;
import it.units.malelab.jgea.core.selector.Worst;
import it.units.malelab.jgea.core.util.Misc;
import it.units.malelab.jgea.representation.grammar.cfggp.GrammarBasedSubtreeMutation;
import it.units.malelab.jgea.representation.grammar.cfggp.GrammarRampedHalfAndHalf;
import it.units.malelab.jgea.representation.tree.SameRootSubtreeCrossover;
import it.units.malelab.jgea.representation.tree.Tree;
import nodes.AbstractSTLNode;
import signal.Record;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;

public class Main extends Worker {

    // TODO: fix 'unchecked or unsafe operations' warning (now suppressed, caused by PrintStreamListener).
    // TODO: properly save result of evolution.

    public static void main(String[] args) {
        new Main(args);
    }

    public Main(String[] args) {
        super(args);
    }

    @Override
    public void run() {
        System.out.println("Main");
        try {
            solve();
        } catch (IOException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }


    @SuppressWarnings("RedundantSuppression")
    private void solve() throws IOException, ExecutionException, InterruptedException {
        Random r = new Random(42);
//        String grammarPath = "test_grammar.bnf";
//        String dataPath = "data/toy_train_data.csv";
        String grammarPath = "grammar_temporal.bnf";
        String dataPath = "data/SWaT/full_train_normal_partial.csv";
//        String dataPath = "data/SWaT/8_vars_train_normal_partial.csv";
        InvariantsProblem problem = new InvariantsProblem(grammarPath, dataPath);

        int treeHeight = 35;

        Map<GeneticOperator<Tree<String>>, Double> operators = new LinkedHashMap<>();
        operators.put(new GrammarBasedSubtreeMutation<>(treeHeight, problem.getGrammar()), 0.2d);
        operators.put(new SameRootSubtreeCrossover<>(treeHeight), 0.8d);

        StandardEvolver<Tree<String>, AbstractSTLNode, Double> evolver = new StandardEvolver<>(
                problem.getSolutionMapper(),
                new GrammarRampedHalfAndHalf<>(3, treeHeight, problem.getGrammar()),
                PartialComparator.from(Double.class).comparing(Individual::getFitness),
                500,
                operators,
                new Tournament(5),
                new Worst(),
                500,
                true
        );

        StandardWithEnforcedDiversityEvolver<Tree<String>, AbstractSTLNode, Double>
                evolverDiversity = new StandardWithEnforcedDiversityEvolver<>(
                    problem.getSolutionMapper(),
                    new GrammarRampedHalfAndHalf<>(3, treeHeight, problem.getGrammar()),
                    PartialComparator.from(Double.class).comparing(Individual::getFitness),
                    500,
                    operators,
                    new Tournament(5),
                    new Worst(),
                    500,
                    true,
                    100
        );

        @SuppressWarnings("unchecked")
//        Collection<AbstractSTLNode> solutions = evolver.solve(
        Collection<AbstractSTLNode> solutions = evolverDiversity.solve(
                Misc.cached(problem.getFitnessFunction(), 20),
                new Iterations(40),
                r,
                executorService,
                listener(
                        new Basic(),
                        new Population(),
                        new Diversity(),
                        new BestInfo("%5.3f")
                ));
        System.out.printf("Found %d solutions with %s.%n", solutions.size(), evolver.getClass().getSimpleName());
        System.out.println();
        System.out.println(solutions.iterator().next());
        evaluateSolution(solutions, (FitnessFunction) problem.getFitnessFunction());
    }

    public void evaluateSolution(Collection<AbstractSTLNode> solutions, FitnessFunction fitnessFunction) throws IOException {
        AbstractSTLNode solution = solutions.iterator().next();
//        String testPath = "data/toy_test_data.csv";
        String testPath = "data/SWaT/full_test_normal_partial.csv";
        Signal<Record> testSignal = fitnessFunction.buildTest(testPath);

        Signal<Double> pointRobustness = solution.getOperator().apply(testSignal).monitor(testSignal);
        double rho;

        int count = 0;

        for (int t = (int) pointRobustness.start(); t <= pointRobustness.end(); t++) {
            rho = pointRobustness.valueAt(t);
            if (rho < 0) {
                System.out.println(t+2 + ": " + rho);
                count++;
            }
        }

        System.out.println("FP: " + count/pointRobustness.size());
    }

}
