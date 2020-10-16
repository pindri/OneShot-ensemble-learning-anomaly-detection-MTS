import it.units.malelab.jgea.Worker;
import it.units.malelab.jgea.core.Individual;
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
            testEvolution();
        } catch (IOException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    private void testEvolution() throws IOException, ExecutionException, InterruptedException {
        Random r = new Random(11);
        Grammar<String> grammar = Grammar.fromFile(new File("grammar.bnf"));
        FitnessFunction fitnessFunction = new FitnessFunction("data/test_data.csv");
        STLMapper mapper = new STLMapper();

        Map<GeneticOperator<Tree<String>>, Double> operators = new LinkedHashMap<>();
        operators.put(new GrammarBasedSubtreeMutation<>(12, grammar), 0.2d);
        operators.put(new SameRootSubtreeCrossover<>(12), 0.8d);


        StandardWithEnforcedDiversityEvolver<Tree<String>, AbstractSTLNode, Double> evolver = new StandardWithEnforcedDiversityEvolver<>(
                mapper,
                new GrammarRampedHalfAndHalf<>(0, 12, grammar),
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
        Collection<AbstractSTLNode> solutions = evolver.solve(
                Misc.cached(fitnessFunction, 20),
                new Iterations(10),
                r,
                executorService,
                listener(
                        new Basic(),
                        new Population(),
                        new Diversity(),
                        new BestInfo("%5.3f"),
                        new BestPrinter(BestPrinter.Part.SOLUTION)
                ));
        System.out.printf("Found %d solutions with %s.%n", solutions.size(), evolver.getClass().getSimpleName());
        System.out.println(solutions.iterator().next());

        /*
        @SuppressWarnings("unchecked")
        Collection<AbstractSTLNode> solutions = evolver.solve(
                Misc.cached(fitnessFunction, 10),
                new Iterations(1),
                r,
                this.executorService,
                Listener.onExecutor(new PrintStreamListener<>(System.out,
                            false,
                            10,
                            ",",
                            ",",
                            new Basic(),
                            new Population(),
                            new Diversity(),
                            new BestInfo("%5.3f")),
                        this.executorService));

        AbstractSTLNode bestFormula = solutions.iterator().next();
        System.out.println(bestFormula);
         */
    }

}
