package nodes;

import eu.quanticol.moonlight.formula.DoubleDomain;
import eu.quanticol.moonlight.monitoring.temporal.TemporalMonitor;
import it.units.malelab.jgea.representation.tree.Tree;
import mapper.Expression;
import mapper.STLMapper;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BinaryTemporalSTLNode extends AbstractTemporalSTLNode {
    public BinaryTemporalSTLNode(List<Tree<String>> siblings, List<Tree<String>> ancestors, Expression expression) {
        super(siblings, expression);
        this.firstChild = STLMapper.parseSubtree(siblings.get(0), ancestors);
        this.secondChild = STLMapper.parseSubtree(siblings.get(3), ancestors);
        switch (expression) {
            case UNTIL -> this.operator = x ->
                    TemporalMonitor.untilMonitor(this.firstChild.getOperator().apply(x),
                                                 this.createInterval(),
                                                 this.secondChild.getOperator().apply(x),
                                                 new DoubleDomain());
            case SINCE -> this.operator = x ->
                    TemporalMonitor.sinceMonitor(this.firstChild.getOperator().apply(x),
                                                 this.createInterval(),
                                                 this.secondChild.getOperator().apply(x),
                                                 new DoubleDomain());
        }
    }

    @Override
    public int getNecessaryLength() {
        return Math.max(this.firstChild.getNecessaryLength(), this.secondChild.getNecessaryLength()) + this.end;
    }

    @Override
    public List<String> getVariablesList() {
        return Stream.concat(this.firstChild.getVariablesList().stream(),
                             this.secondChild.getVariablesList().stream()).collect(Collectors.toList());
    }

    @Override
    public Map<String, List<Integer>> getGreyAreaCoverageMap() {
        Map<String, List<Integer>> map1 = this.firstChild.getGreyAreaCoverageMap();
        Map<String, List<Integer>> map2 = this.secondChild.getGreyAreaCoverageMap();
        map1 = coverageTemporalIncrease(map1, 0, this.end);
        map2 = coverageTemporalIncrease(map2, this.start, this.end);

        return(mergeCoverages(map1, map2));

    }
}
