package mapper;

import nodes.AbstractSTLNode;
import it.units.malelab.jgea.representation.tree.Tree;
import nodes.AndSTLNode;
import nodes.NumericSTLNode;

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

    private static AbstractSTLNode createNode(Expression expr, List<Tree<String>> siblings, List<Tree<String>> ancestors) {
        switch (expr) {
            case PROP:
                return new NumericSTLNode(siblings);
            case AND:
                return new AndSTLNode(siblings, ancestors);
            default:
                return null;
        }
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
