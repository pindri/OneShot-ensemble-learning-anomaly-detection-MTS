package nodes;

import eu.quanticol.moonlight.formula.DoubleDomain;
import eu.quanticol.moonlight.monitoring.temporal.TemporalMonitor;
import eu.quanticol.moonlight.util.Pair;
import it.units.malelab.jgea.representation.tree.Tree;
import mapper.STLMapper;

import java.util.List;
import java.util.Map;

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

    @Override
    public List<String> getVariablesList() {
       return this.firstChild.getVariablesList();
    }

    @Override
    public Map<String, List<Integer>> getAreaCoverage() {
        return this.firstChild.getAreaCoverage();
    }
}
