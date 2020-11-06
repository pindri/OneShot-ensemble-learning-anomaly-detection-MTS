package nodes;

import eu.quanticol.moonlight.formula.DoubleDomain;
import eu.quanticol.moonlight.monitoring.temporal.TemporalMonitor;
import it.units.malelab.jgea.representation.tree.Tree;
import mapper.Expression;
import mapper.STLMapper;

import java.util.List;

public class UnaryTemporalSTLNode extends AbstractTemporalSTLNode {
    public UnaryTemporalSTLNode(List<Tree<String>> siblings, List<Tree<String>> ancestors, Expression expression) {
        super(siblings, expression);
        this.firstChild = STLMapper.parseSubtree(siblings.get(0), ancestors);
        switch (expression) {
            case ONCE:
                this.operator = x ->
                    TemporalMonitor.onceMonitor(this.firstChild.getOperator().apply(x),
                                                new DoubleDomain(),
                                                this.createInterval());
                break;
            case EVENTUALLY:
                this.operator = x ->
                    TemporalMonitor.eventuallyMonitor(this.firstChild.getOperator().apply(x),
                                                      new DoubleDomain(),
                                                      this.createInterval());
                break;
            case HISTORICALLY:
                this.operator = x ->
                    TemporalMonitor.historicallyMonitor(this.firstChild.getOperator().apply(x),
                                                        new DoubleDomain(),
                                                        this.createInterval());
                break;
            case GLOBALLY:
                this.operator = x ->
                    TemporalMonitor.globallyMonitor(this.firstChild.getOperator().apply(x),
                                                    new DoubleDomain(),
                                                    this.createInterval());
                break;
        }
    }

    @Override
    public int getMinLength() {
        return firstChild.getMinLength() + this.end;
    }
}
