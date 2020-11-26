package core;

import it.units.malelab.jgea.core.listener.collector.Item;
import nodes.AbstractSTLNode;

import java.io.IOException;
import java.util.List;
import java.util.function.Function;

public abstract class AbstractFitnessFunction implements Function<AbstractSTLNode, Double> {
    @Override
    public abstract Double apply(AbstractSTLNode monitor);

    public abstract List<Item> evaluateSolution(AbstractSTLNode solution);

    public abstract void solutionToFile(AbstractSTLNode solution, String filename) throws IOException;
}
