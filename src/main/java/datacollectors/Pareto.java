package datacollectors;

import core.InvariantsProblem;
import core.Operator;
import eu.quanticol.moonlight.util.Pair;
import it.units.malelab.jgea.core.Individual;
import it.units.malelab.jgea.core.listener.collector.Item;
import it.units.malelab.jgea.representation.tree.Tree;
import nodes.AbstractSTLNode;
import ordering.ParetoCollection;
import org.apache.commons.math3.analysis.function.Abs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class Pareto {


    public static Collection<Pair<AbstractSTLNode, Double>>
    getFront(Collection<Individual<? extends Tree<String>, ? extends AbstractSTLNode, ? extends Double>> individuals) {

        // Pair<Solution, Fitness>
        List<Pair<AbstractSTLNode, Double>>
                solutions = individuals.stream()
                                       .map(x -> new Pair<AbstractSTLNode, Double>(x.getSolution(),
                                                                                   x.getFitness()))
                                       .collect(Collectors.toList());

        // Coverage ranges [0,inf], larger should be BEFORE, so use negative coverage.
        ParetoCollection<Pair<AbstractSTLNode, Double>, Double>
                pc = new ParetoCollection<>(solutions, Double.class,
                                            x -> List.of(-x.getFirst().getCoverage(), x.getSecond()));

        return new ArrayList<>(pc.firsts());
    }

    public static List<Item> computeIndices(Collection<Individual<? extends Tree<String>, ? extends AbstractSTLNode,
            ? extends Double>> individuals, InvariantsProblem problem, Operator operator) {

        Collection<Pair<AbstractSTLNode, Double>> front = Pareto.getFront(individuals);
        List<AbstractSTLNode> frontSolutions = front.stream().map(Pair::getFirst).collect(Collectors.toList());

        return problem.getFitnessFunction().evaluateSolutions(frontSolutions, "Pareto." + operator.toString(),
                                                              operator);
    }


}
