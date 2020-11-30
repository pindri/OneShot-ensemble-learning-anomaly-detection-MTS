package nodes;

import eu.quanticol.moonlight.monitoring.temporal.TemporalMonitor;
import it.units.malelab.jgea.representation.tree.Tree;
import mapper.Comparison;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class NumericSTLNode extends AbstractSTLNode {

    private final String variable;
    private final Double number;
    private Comparison comparisonSymbol;

    public NumericSTLNode(List<Tree<String>> siblings) {
        this.variable = siblings.get(0).child(0).content();
        for (Comparison symbol : Comparison.values()) {
            if (symbol.toString().equals(siblings.get(1).child(0).content())) {
                this.comparisonSymbol = symbol;
                break;
            }
        }
        this.number = parseNumber(siblings.get(2).childStream().collect(Collectors.toList()));
        this.symbol = this.variable + " " + this.comparisonSymbol.toString() + " " + this.number;
        // Here y is a signal.Record.
        this.operator = x -> TemporalMonitor.atomicMonitor(y -> this.comparisonSymbol.getValue()
                                                                    .apply(y.getNum(this.variable), this.number));
    }

    @Override
    public int getMinLength() {
        return 0;
    }

    @Override
    public List<String> getVariablesList() {
        List<String> list = new ArrayList<>();
        list.add(this.variable);
        return list;
    }

    private double parseNumber(List<Tree<String>> digits) {
        double result = 0.0;
        for (int i = 0; i < digits.size(); i++) {
            result += Double.parseDouble(digits.get(i).child(0).content()) * Math.pow(10, - (i+1));
        }
        return result;
    }
}