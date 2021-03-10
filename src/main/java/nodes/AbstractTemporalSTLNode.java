package nodes;

import eu.quanticol.moonlight.formula.Interval;
import it.units.malelab.jgea.representation.tree.Tree;
import mapper.Expression;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public abstract class AbstractTemporalSTLNode extends AbstractSTLNode {

    public final int start;
    public final int end;

    public AbstractTemporalSTLNode(List<Tree<String>> siblings, Expression expression) {
        this.start = (int) this.parseIntervalBound(siblings.get(1).childStream().collect(Collectors.toList()));
        this.end = this.start + (int) Math.max(1.0, this.parseIntervalBound(siblings.get(2).childStream()
                                                                                    .collect(Collectors.toList())));
        this.symbol = expression.toString() + " [" + this.start + ", " + this.end + "]";
    }

    public Interval createInterval() {
        return new Interval(this.start, this.end);
    }

    private double parseIntervalBound(List<Tree<String>> leaves) {
        int k = 0;
        double value = 0.0;
        for (Tree<String> leaf : leaves) {
            value += Double.parseDouble(leaf.child(0).content()) * Math.pow(10, k);
            k++;
        }
        return value;
    }

    protected Map<String, List<Integer>> coverageTemporalIncrease(Map<String, List<Integer>> map1, int start, int end) {

        List<Integer> temporalRange = IntStream.rangeClosed(start, end)
                                               .boxed().collect(Collectors.toList());

        for (Map.Entry<String, List<Integer>> entry : map1.entrySet()) {

            List<Integer> instants = entry.getValue();
            instants = instants.stream().map(x -> temporalRange.stream().map(y -> y + x).collect(Collectors.toList()))
                               .flatMap(Collection::stream).distinct().collect(Collectors.toList());
            entry.setValue(instants);
        }
        return map1;
    }

}