package nodes;

import eu.quanticol.moonlight.formula.DoubleDomain;
import eu.quanticol.moonlight.monitoring.temporal.TemporalMonitor;
import it.units.malelab.jgea.representation.tree.Tree;
import mapper.STLMapper;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AndSTLNode extends AbstractSTLNode {

    public AndSTLNode(List<Tree<String>> siblings, List<Tree<String>> ancestors) {
        this.firstChild = STLMapper.parseSubtree(siblings.get(0), ancestors);
        this.secondChild = STLMapper.parseSubtree(siblings.get(1), ancestors);
        this.symbol = "AND";
        // Returns lowest robustness of the two children.
        this.operator = x -> TemporalMonitor.andMonitor(this.firstChild.getOperator().apply(x),
                                                        new DoubleDomain(),
                                                        this.secondChild.getOperator().apply(x));

    }

    @Override
    public int getMinLength() {
        return Math.max(this.firstChild.getMinLength(), this.secondChild.getMinLength());
    }

    @Override
    public List<String> getVariablesList() {
        return Stream.concat(this.firstChild.getVariablesList().stream(),
                             this.secondChild.getVariablesList().stream()).collect(Collectors.toList());
    }
}
