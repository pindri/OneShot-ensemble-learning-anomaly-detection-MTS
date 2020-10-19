package nodes;

import eu.quanticol.moonlight.formula.DoubleDomain;
import eu.quanticol.moonlight.monitoring.temporal.TemporalMonitor;
import it.units.malelab.jgea.representation.tree.Tree;
import mapper.Expression;
import mapper.STLMapper;

import java.util.List;

public class BinaryTemporalSTLNode extends AbstractTemporalSTLNode {
    public BinaryTemporalSTLNode(Expression expression, List<Tree<String>> siblings, List<Tree<String>> ancestors) {
        super(siblings, expression);
        this.firstChild = STLMapper.parseSubtree(siblings.get(0), ancestors);
        this.secondChild= STLMapper.parseSubtree(siblings.get(3), ancestors);
        switch (expression) {
            case UNTIL -> this.operator = x -> TemporalMonitor.untilMonitor(this.firstChild.getOperator().apply(x),
                                                                            this.createInterval(),
                                                                            this.secondChild.getOperator().apply(x),
                                                                            new DoubleDomain());
        }
    }
}
