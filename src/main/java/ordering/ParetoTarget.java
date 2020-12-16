package ordering;

import it.units.malelab.jgea.core.listener.Event;

import java.util.List;
import java.util.function.Predicate;

public class ParetoTarget implements Predicate<Event<?, ?, List<Double>>> {

    private final List<Double> paretoTarget;

    public ParetoTarget(List<Double> paretoTarget) {
        this.paretoTarget = paretoTarget;
    }

    @Override
    public boolean test(Event<?, ?, List<Double>> event) {
        return event.getOrderedPopulation().all().stream().anyMatch(i -> i.getFitness().get(0)
                                                                          .equals(paretoTarget.get(0)));
    }

    @Override
    public String toString() {
        return "ParetoTarget{" +
                "paretoTarget=" + paretoTarget +
                '}';
    }
}
