package nodes;

import eu.quanticol.moonlight.formula.DoubleDomain;
import eu.quanticol.moonlight.monitoring.temporal.TemporalMonitor;
import it.units.malelab.jgea.representation.tree.Tree;
import mapper.STLMapper;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class OrSTLNode extends AbstractSTLNode {

    public OrSTLNode(List<Tree<String>> siblings, List<Tree<String>> ancestors) {
        this.firstChild = STLMapper.parseSubtree(siblings.get(0), ancestors);
        this.secondChild = STLMapper.parseSubtree(siblings.get(1), ancestors);
        this.symbol = "OR";
        this.operator = x -> TemporalMonitor.orMonitor(this.firstChild.getOperator().apply(x),
                                                       new DoubleDomain(),
                                                       this.secondChild.getOperator().apply(x));

    }

    @Override
    public int getNecessaryLength() {
        return Math.max(this.firstChild.getNecessaryLength(), this.secondChild.getNecessaryLength());
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
        return mergeCoverages(map1, map2);
    }
}
