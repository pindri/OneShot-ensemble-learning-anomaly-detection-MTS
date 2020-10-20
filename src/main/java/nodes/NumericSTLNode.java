package nodes;

import eu.quanticol.moonlight.monitoring.temporal.TemporalMonitor;
import it.units.malelab.jgea.representation.tree.Tree;
import mapper.Comparison;

import java.util.List;

public class NumericSTLNode extends AbstractSTLNode {

    // TODO: Better number parsing.

    private String variable;
    private Double number;
    private Comparison comparisonSymbol;

    public NumericSTLNode(List<Tree<String>> siblings) {
        this.variable = siblings.get(0).child(0).content();
        for (Comparison symbol : Comparison.values()) {
            if (symbol.toString().equals(siblings.get(1).child(0).content())) {
                this.comparisonSymbol = symbol;
                break;
            }
        }
        this.number = Double.parseDouble(siblings.get(2).child(0).content());
        this.symbol = this.variable + " " + this.comparisonSymbol.toString() + " " + this.number;
        // Here y is a signal.Record.
        this.operator = x -> TemporalMonitor.atomicMonitor(y -> this.comparisonSymbol.getValue()
                                                                    .apply(y.getNum(this.variable), this.number));
    }

    @Override
    public int getMinLength() {
        return 0;
    }
}