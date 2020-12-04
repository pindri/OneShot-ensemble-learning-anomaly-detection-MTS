package datacollectors;

import it.units.malelab.jgea.core.Individual;
import it.units.malelab.jgea.core.listener.Event;
import it.units.malelab.jgea.core.listener.collector.DataCollector;
import it.units.malelab.jgea.core.listener.collector.Item;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;


public class FunctionOfAll<G, S, F> implements DataCollector<G, S, F> {

    private final Function<Collection<Individual<? extends G, ? extends S, ? extends F>>, List<Item>> function;

    public FunctionOfAll(Function<Collection<Individual<? extends G, ? extends S, ? extends F>>, List<Item>> function) {
        this.function = function;
    }

    @Override
    public List<Item> collect(Event<? extends G, ? extends S, ? extends F> event) {
        Collection<Individual<? extends G, ? extends S, ? extends F>>
                all = new ArrayList<>(event.getOrderedPopulation().all());
        return function.apply(all);
    }
}

