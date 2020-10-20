package nodes;

import eu.quanticol.moonlight.formula.DoubleDomain;
import eu.quanticol.moonlight.monitoring.temporal.TemporalMonitor;
import it.units.malelab.jgea.representation.tree.Tree;
import mapper.STLMapper;

import java.util.List;

public class NotSTLNode extends AbstractSTLNode {

    public NotSTLNode(List<Tree<String>> siblings, List<Tree<String>> ancestors) {
        this.firstChild = STLMapper.parseSubtree(siblings.get(0), ancestors);
        this.symbol = "NOT";
        this.operator = x -> TemporalMonitor.notMonitor(this.firstChild.getOperator().apply(x), new DoubleDomain());
    }

    @Override
    public int getMinLength() {
        return this.firstChild.getMinLength();
    }
}
