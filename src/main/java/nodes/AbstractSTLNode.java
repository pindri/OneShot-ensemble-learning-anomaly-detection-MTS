package nodes;

import eu.quanticol.moonlight.monitoring.temporal.TemporalMonitor;
import eu.quanticol.moonlight.signal.Signal;
import eu.quanticol.moonlight.util.Pair;
import signal.Record;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    public abstract int getMinLength();

    public abstract List<String> getVariablesList();

    public abstract Map<String, List<Integer>> getAreaCoverage();

    public double getCoverage() {
        return this.getMinLength() * this.getVariablesList().stream().distinct().count();
    }

    public Map<String, List<Integer>> mergeCoverages(Map<String, List<Integer>> map1, Map<String, List<Integer>> map2) {
        map2.forEach(
                (key, value) -> map1.merge(key, value, (v1, v2) -> Stream.of(v1, v2).flatMap(Collection::stream)
                                                                         .collect(Collectors.toList()))
                    );
        return map1;
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
        return traversePreOrder(this);
    }

    private static String traversePreOrder(AbstractSTLNode node) {
        if (node == null) {
            return "\n";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(node.getSymbol());
        String pointerRight = "└──";
        boolean hasRightChild = node.getFirstChild() != null;
        String pointerLeft = (hasRightChild) ? "├──" : "└──";
        traverseNodes(sb, "", pointerLeft, node.getSecondChild(), hasRightChild);
        traverseNodes(sb, "", pointerRight, node.getFirstChild(), false);
        sb.append("\n");
        return sb.toString();
    }

    private static void traverseNodes(StringBuilder sb, String padding, String pointer,
                                      AbstractSTLNode node, boolean hasRightSibling) {
        if (node != null) {
            sb.append("\n");
            sb.append(padding);
            sb.append(pointer);
            sb.append(node.getSymbol());
            StringBuilder paddingBuilder = new StringBuilder(padding);
            if (hasRightSibling) {
                paddingBuilder.append("│  ");
            }
            else {
                paddingBuilder.append("   ");
            }
            String paddingForBoth = paddingBuilder.toString();
            String pointerRight = "└──";
            boolean hasRightChild = node.getFirstChild() != null;
            String pointerLeft = (hasRightChild) ? "├──" : "└──";
            traverseNodes(sb, paddingForBoth, pointerLeft, node.getSecondChild(), hasRightChild);
            traverseNodes(sb, paddingForBoth, pointerRight, node.getFirstChild(), false);
        }
    }
}
