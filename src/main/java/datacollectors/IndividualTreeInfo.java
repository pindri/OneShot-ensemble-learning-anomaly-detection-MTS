package datacollectors;

import it.units.malelab.jgea.core.Individual;
import it.units.malelab.jgea.core.listener.collector.Item;
import it.units.malelab.jgea.representation.tree.Tree;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

public class IndividualTreeInfo<F> implements Function<Individual<?, ?, ? extends F>, List<Item>> {
    private final Function<? super F, List<Item>> fitnessSplitter;

    public IndividualTreeInfo(Function<? super F, List<Item>> fitnessSplitter) {
        this.fitnessSplitter = fitnessSplitter;
    }

    public List<Item> apply(Individual<?, ?, ? extends F> individual) {
        List<Item> items = new ArrayList<>();
        items.add(new Item("birth.iteration", individual.getBirthIteration(), "%3d"));
        items.add(new Item("height", height(individual.getGenotype()), "%3d"));

        for (Object o : this.fitnessSplitter.apply(individual.getFitness())) {
            Item fitnessItem = (Item) o;
            items.add(fitnessItem.prefixed("fitness"));
        }

        return items;
    }

    public static Integer height(Object o) {
        if (o instanceof Tree) {
            return ((Tree<?>) o).height();
        } else {
            return -1;
        }
    }
}
