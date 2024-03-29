package nodes;

import eu.quanticol.moonlight.formula.DoubleDomain;
import eu.quanticol.moonlight.monitoring.temporal.TemporalMonitor;
import it.units.malelab.jgea.representation.tree.Tree;
import mapper.Expression;
import mapper.STLMapper;

import java.util.List;
import java.util.Map;

public class UnaryTemporalSTLNode extends AbstractTemporalSTLNode {
    public UnaryTemporalSTLNode(List<Tree<String>> siblings, List<Tree<String>> ancestors, Expression expression) {
        super(siblings, expression);
        this.firstChild = STLMapper.parseSubtree(siblings.get(0), ancestors);
        switch (expression) {
            case ONCE -> this.operator = x ->
                    TemporalMonitor.onceMonitor(this.firstChild.getOperator().apply(x),
                                                new DoubleDomain(),
                                                this.createInterval());
            case EVENTUALLY -> this.operator = x ->
                    TemporalMonitor.eventuallyMonitor(this.firstChild.getOperator().apply(x),
                                                      new DoubleDomain(),
                                                      this.createInterval());
            case HISTORICALLY -> this.operator = x ->
                    TemporalMonitor.historicallyMonitor(this.firstChild.getOperator().apply(x),
                                                        new DoubleDomain(),
                                                        this.createInterval());
            case GLOBALLY -> this.operator = x ->
                    TemporalMonitor.globallyMonitor(this.firstChild.getOperator().apply(x),
                                                    new DoubleDomain(),
                                                    this.createInterval());
        }
    }

    @Override
    public int getNecessaryLength() {
        return firstChild.getNecessaryLength() + this.end;
    }

    @Override
    public List<String> getVariablesList() {
        return this.firstChild.getVariablesList();
    }

    @Override
    public Map<String, List<Integer>> getGreyAreaCoverageMap() {
        Map<String, List<Integer>> map1 = this.firstChild.getGreyAreaCoverageMap();

        return coverageTemporalIncrease(map1, this.start, this.end);
    }

}
