package nodes;

import eu.quanticol.moonlight.formula.Interval;
import it.units.malelab.jgea.representation.tree.Tree;
import mapper.Expression;

import java.util.List;
import java.util.stream.Collectors;

public abstract class AbstractTemporalSTLNode extends AbstractSTLNode {

    public final int start;
    public final int end;

    public AbstractTemporalSTLNode(List<Tree<String>> siblings, Expression expression) {
        this.start = (int) this.parseIntervalBound(siblings.get(1).childStream().collect(Collectors.toList()));
        this.end = this.start + (int) Math.max(1.0, this.parseIntervalBound(siblings.get(2).childStream().collect(Collectors.toList())));
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

}