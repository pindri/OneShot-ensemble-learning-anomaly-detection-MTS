import it.units.malelab.jgea.Worker;
import it.units.malelab.jgea.core.Individual;
import it.units.malelab.jgea.core.evolver.StandardEvolver;
import it.units.malelab.jgea.core.evolver.StandardWithEnforcedDiversityEvolver;
import it.units.malelab.jgea.core.evolver.stopcondition.Iterations;
import it.units.malelab.jgea.core.evolver.stopcondition.TargetFitness;
import it.units.malelab.jgea.core.listener.collector.*;
import it.units.malelab.jgea.core.operator.GeneticOperator;
import it.units.malelab.jgea.core.order.PartialComparator;
import it.units.malelab.jgea.core.selector.Tournament;
import it.units.malelab.jgea.core.selector.Worst;
import it.units.malelab.jgea.core.util.Misc;
import it.units.malelab.jgea.representation.grammar.Grammar;
import it.units.malelab.jgea.representation.grammar.cfggp.GrammarBasedSubtreeMutation;
import it.units.malelab.jgea.representation.grammar.cfggp.GrammarRampedHalfAndHalf;
import it.units.malelab.jgea.representation.tree.SameRootSubtreeCrossover;
import it.units.malelab.jgea.representation.tree.Tree;
import mapper.STLMapper;
import nodes.AbstractSTLNode;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;

public class Main extends Worker {

    // TODO: fix 'unchecked or unsafe operations' warning (now suppressed, caused by PrintStreamListener).

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
            nonTemporalRun();
        } catch (IOException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    private void nonTemporalRun() throws IOException, ExecutionException, InterruptedException {
        Random r = new Random(42);
//        Grammar<String> grammar = Grammar.fromFile(new File("grammar.bnf"));
        Grammar<String> grammar = Grammar.fromFile(new File("grammar_temporal.bnf"));
        FitnessFunction fitnessFunction = new FitnessFunction("data/test_data.csv");
        STLMapper mapper = new STLMapper();

        Map<GeneticOperator<Tree<String>>, Double> operators = new LinkedHashMap<>();
        operators.put(new GrammarBasedSubtreeMutation<>(10, grammar), 0.2d);
        operators.put(new SameRootSubtreeCrossover<>(10), 0.8d);

        StandardEvolver<Tree<String>, AbstractSTLNode, Double> evolver = new StandardEvolver<>(
                mapper,
                new GrammarRampedHalfAndHalf<>(3, 12, grammar),
                PartialComparator.from(Double.class).comparing(Individual::getFitness),
                500,
                operators,
                new Tournament(5),
                new Worst(),
                500,
                true
        );

        @SuppressWarnings("unchecked")
        Collection<AbstractSTLNode> solutions = evolver.solve(
                Misc.cached(fitnessFunction, 20),
                new Iterations(50),
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
    }

}
