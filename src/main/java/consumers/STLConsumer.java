package consumers;

import core.fitness.AbstractFitnessFunction;
import it.units.malelab.jgea.core.Individual;
import it.units.malelab.jgea.core.consumer.Event;
import it.units.malelab.jgea.core.consumer.NamedFunction;
import it.units.malelab.jgea.representation.tree.Tree;
import nodes.AbstractSTLNode;

import java.util.Collection;
import java.util.stream.Collectors;

public class STLConsumer {
    private STLConsumer() {
    }

    public static NamedFunction<AbstractSTLNode, Number> temporalLength() {
        return NamedFunction.build("temporalLength", "%3d", AbstractSTLNode::getMinLength);
    }

    public static NamedFunction<AbstractSTLNode, Number> coverage() {
        return NamedFunction.build("coverage", "%3f", AbstractSTLNode::getCoverage);
    }

    public static NamedFunction<Tree<String>, Number> height() {
        return NamedFunction.build("treeHeight", "%3d", Tree::height);
    }

    public static <F> NamedFunction<AbstractSTLNode, Number> FPR(AbstractFitnessFunction<F> fitnessFunction) {
        return NamedFunction.build("FPR", "%5f", i -> fitnessFunction.evaluateSolution(i).get("FPR"));
    }

    public static <F> NamedFunction<AbstractSTLNode, Number> FNR(AbstractFitnessFunction<F> fitnessFunction) {
        return NamedFunction.build("FNR", "%5f", i -> fitnessFunction.evaluateSolution(i).get("FNR"));
    }

    public static <F> NamedFunction<AbstractSTLNode, Number> TPR(AbstractFitnessFunction<F> fitnessFunction) {
        return NamedFunction.build("TPR", "%5f", i -> fitnessFunction.evaluateSolution(i).get("TPR"));
    }

    public static <G, S, F> NamedFunction<Event<? extends G, ? extends S, ? extends F>,
            Collection<? extends Individual<? extends G, ? extends S, ? extends F>>> zeroFitness() {
        return NamedFunction.build("best", "%s",e -> e.getOrderedPopulation().all().stream()
                                                      .filter(i -> i.getFitness().equals(0.0))
                                                      .collect(Collectors.toList()));
    }

    public static <G, F>
    NamedFunction<Event<? extends G, ? extends AbstractSTLNode, ? extends F>, Number> totalVariableCoverage() {
        return NamedFunction.build("totalVariableCoverage", "%3d",
                                   e -> e.getOrderedPopulation().all().stream().filter(i -> i.getFitness().equals(0.0))
                                         .flatMap(i -> i.getSolution().getVariablesList().stream()).distinct().count());
    }


}
