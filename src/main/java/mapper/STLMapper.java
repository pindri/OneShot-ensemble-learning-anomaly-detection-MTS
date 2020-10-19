package mapper;

import nodes.*;
import it.units.malelab.jgea.representation.tree.Tree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static mapper.Expression.*;

public class STLMapper implements Function<Tree<String>, AbstractSTLNode> {

    @Override
    public AbstractSTLNode apply(Tree<String> root) {
        return parseSubtree(root, new ArrayList<Tree<String>>() {{add(null);}});
    }

    public static AbstractSTLNode parseSubtree(Tree<String> root, List<Tree<String>> ancestors) {
        List<Tree<String>> children = root.childStream().collect(Collectors.toList());
        Tree<String> firstChild = children.get(0);
        for (Expression expr : values()) {
            if (expr.toString().equals(firstChild.content())) {
                ancestors.add(root);
                return createNode(expr, getSiblings(firstChild, ancestors), ancestors);
            }
        }
        ancestors.add(root);
        return parseSubtree(firstChild, ancestors);
    }

    private static AbstractSTLNode createNode(Expression expression, List<Tree<String>> siblings, List<Tree<String>> ancestors) {
        return switch (expression) {
            case PROP -> new NumericSTLNode(siblings);
            case UNTIL -> new BinaryTemporalSTLNode(expression, siblings, ancestors);
            case AND -> new AndSTLNode(siblings, ancestors);
            case NOT -> new NotSTLNode(siblings, ancestors);
        };
    }

    private static List<Tree<String>> getSiblings(Tree<String> node, List<Tree<String>> ancestors) {
        Tree<String> parent = ancestors.get(ancestors.size() - 1);
        if (parent == null) {
            return Collections.emptyList();
        }

        List<Tree<String>> siblings = parent.childStream().collect(Collectors.toList());
        siblings.remove(node);
        if (siblings.isEmpty()) {
            return getSiblings(parent, ancestors.stream().filter(x -> x != parent).collect(Collectors.toList()));
        }

        return siblings;
    }
}
