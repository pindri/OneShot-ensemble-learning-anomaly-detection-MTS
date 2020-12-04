package datacollectors;

import eu.quanticol.moonlight.util.Pair;
import it.units.malelab.jgea.core.Individual;
import it.units.malelab.jgea.core.listener.Event;
import it.units.malelab.jgea.core.listener.collector.DataCollector;
import it.units.malelab.jgea.core.listener.collector.Item;
import nodes.AbstractSTLNode;
import ordering.ParetoCollection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class ParetoFront implements DataCollector<Object, AbstractSTLNode, Double> {

    public ParetoFront() {

    }

    @Override
    public List<Item> collect(Event<?, ? extends AbstractSTLNode, ? extends Double> event) {

        Collection<? extends Individual<?, ? extends AbstractSTLNode, ? extends Double>>
                all = event.getOrderedPopulation().all();
        // Pair<Solution, Fitness>
        List<Pair<AbstractSTLNode, Double>> solutions = all.stream()
                                                       .map(x -> new Pair<AbstractSTLNode, Double>(x.getSolution(),
                                                                                                   x.getFitness()))
                                                       .collect(Collectors.toList());

        // Coverage ranges [0,inf], larger should be BEFORE, so use negative coverage.
        ParetoCollection<Pair<AbstractSTLNode, Double>, Double>
                pc = new ParetoCollection<>(solutions, Double.class,
                                            x -> List.of(-x.getFirst().getCoverage(), x.getSecond()));

        List<Pair<AbstractSTLNode, Double>> pareto = new ArrayList<>(pc.firsts());

        return null;
    }

}
