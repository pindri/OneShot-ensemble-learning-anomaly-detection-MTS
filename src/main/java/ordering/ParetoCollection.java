package ordering;

import it.units.malelab.jgea.core.order.ParetoDominance;
import it.units.malelab.jgea.core.order.PartiallyOrderedCollection;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static it.units.malelab.jgea.core.order.PartialComparator.PartialComparatorOutcome.AFTER;
import static it.units.malelab.jgea.core.order.PartialComparator.PartialComparatorOutcome.BEFORE;

public class ParetoCollection<T, C extends Comparable<C>> implements PartiallyOrderedCollection<T> {

    private final ParetoDominance<C> comparator;
    private Collection<T> candidates;
    private Collection<T> firsts = Collections.emptyList();
    private final Function<T, ? extends List<? extends C>> function;

    public ParetoCollection(Collection<T> candidates, Class<C> comparatorClass,
                            Function<T, ? extends  List<? extends C>> function) {
        this.comparator = new ParetoDominance<>(comparatorClass);
        this.candidates = candidates;
        this.function = function;
        candidates.forEach(this::addToFirst);
    }


    @Override
    public Collection<T> all() {
        return this.candidates;
    }

    @Override
    public Collection<T> firsts() {
        return this.firsts;
    }

    @Override
    public Collection<T> lasts() {
        return null;
    }

    @Override
    public boolean remove(T t) {
        return false;
    }

    private void addToFirst(T t) {
        if (this.firsts.stream()
                       .anyMatch(f -> this.comparator.compare(this.function.apply(t),
                                                              this.function.apply(f)) == AFTER)) {
            return;
        }
        // Exclude first that are after new candidate.
        this.firsts = this.firsts.stream().filter(f -> !(this.comparator.compare(this.function.apply(t),
                                                                                 this.function.apply(f)) == BEFORE))
                                 .collect(Collectors.toList());
        this.firsts.add(t);
    }

    @Override
    public void add(T t) {
        addToFirst(t);
        this.candidates.add(t);
    }
}
