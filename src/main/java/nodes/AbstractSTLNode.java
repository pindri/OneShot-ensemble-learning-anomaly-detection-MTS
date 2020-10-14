package nodes;

import eu.quanticol.moonlight.monitoring.temporal.TemporalMonitor;
import eu.quanticol.moonlight.signal.Signal;
import signal.Record;

import java.util.Objects;
import java.util.function.Function;

public abstract class AbstractSTLNode {
    // Abstract node of the STL syntax tree.

    protected Function<Signal<Record>, TemporalMonitor<Record, Double>> operator;
    protected AbstractSTLNode firstChild;
    protected AbstractSTLNode secondChild;
    protected String symbol;

    public Function<Signal<Record>, TemporalMonitor<Record, Double>> getOperator() {
        return this.operator;
    }

    public AbstractSTLNode getFirstChild() {
        return firstChild;
    }

    public AbstractSTLNode getSecondChild() {
        return secondChild;
    }

    public String getSymbol() {
        return symbol;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractSTLNode that = (AbstractSTLNode) o;
        return Objects.equals(operator, that.operator) &&
                Objects.equals(firstChild, that.firstChild) &&
                Objects.equals(secondChild, that.secondChild) &&
                Objects.equals(symbol, that.symbol);
    }

    @Override
    public int hashCode() {
        return Objects.hash(operator, firstChild, secondChild, symbol);
    }

    @Override
    public String toString() {
        // TODO: pretty print.
        return "NoteToString";
    }
}
