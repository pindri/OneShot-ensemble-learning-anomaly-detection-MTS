package core;

import it.units.malelab.jgea.core.listener.collector.Item;
import nodes.AbstractSTLNode;

import java.util.List;
import java.util.function.Function;

public abstract class AbstractFitnessFunction implements Function<AbstractSTLNode, Double> {
    @Override
    public abstract Double apply(AbstractSTLNode monitor);

    public abstract List<Item> evaluateSolution(AbstractSTLNode solution);
}
