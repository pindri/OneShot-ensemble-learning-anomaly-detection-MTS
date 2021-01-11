package stopcondition;

import it.units.malelab.jgea.core.consumer.Event;
import it.units.malelab.jgea.representation.tree.Tree;
import nodes.AbstractSTLNode;

import java.util.function.Predicate;

public class SpeciesZero implements Predicate<Event<Tree<String>, AbstractSTLNode, Double>> {

    private final Double targetFitness;
    private final int targetNumber;

    public SpeciesZero(Double targetFitness, int targetNumber) {
        this.targetFitness = targetFitness;
        this.targetNumber = targetNumber;
    }

    public boolean test(Event<Tree<String>, AbstractSTLNode, Double> event) {
        return event.getOrderedPopulation().all().stream()
                    .filter(i -> targetFitness.equals(i.getFitness()))
                    .flatMap(i -> i.getSolution().getVariablesList().stream()).distinct().count() >= targetNumber;
    }

    @Override
    public String toString() {
        return "TargetFitness{" +
                "targetFitness=" + targetFitness +
                '}';
    }
}
