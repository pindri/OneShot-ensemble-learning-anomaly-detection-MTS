import elements.AbstractSTLNode;
import it.units.malelab.jgea.representation.tree.Tree;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class STLMapper implements Function<Tree<String>, AbstractSTLNode> {
    @Override
    public AbstractSTLNode apply(Tree<String> root) {
        return null;
    }

    public static void parseSubtree(Tree<String> root, List<Tree<String>> ancestors) {
        List<Tree<String>> children = root.childStream().collect(Collectors.toList());
        Tree<String> firstChild = children.get(0);
        for (Expression expr : Expression.values()) {
            if (expr.toString().equals(firstChild.content())) {
                ancestors.add(root);
                System.out.println("found");
            }
        }
        ancestors.add(root);
        parseSubtree(firstChild, ancestors);
    }
}
