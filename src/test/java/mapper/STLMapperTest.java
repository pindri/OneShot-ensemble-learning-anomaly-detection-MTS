package mapper;

import it.units.malelab.jgea.representation.tree.Tree;
import mapper.STLMapper;
import nodes.AbstractSTLNode;
import org.junit.Test;

import java.io.PrintStream;

import static org.junit.Assert.assertEquals;

public class STLMapperTest {

    @SuppressWarnings("unchecked")
    @Test
    public void parseTreeTest() {
        // First branch.
        Tree<String> v1 = Tree.of("x1");
        Tree<String> c1 = Tree.of(">");
        Tree<String> n1 = Tree.of("3");

        Tree<String> propSymbol1 = Tree.of("proposition");
        Tree<String> var1 = Tree.of("<var>", v1);
        Tree<String> comp1 = Tree.of("<comp>", c1);
        Tree<String> dig1 = Tree.of("<dig>", n1);
        Tree<String> num1 = Tree.of("<num>", dig1);

        Tree<String> prop1 = Tree.of("<prop>", propSymbol1, var1, comp1, num1);

        // Second branch.
        Tree<String> v2 = Tree.of("x2");
        Tree<String> c2 = Tree.of("<");
        Tree<String> n2 = Tree.of("3");

        Tree<String> propSymbol2 = Tree.of("proposition");
        Tree<String> var2 = Tree.of("<var>", v2);
        Tree<String> comp2 = Tree.of("<comp>", c2);
        Tree<String> dig2 = Tree.of("<dig>", n2);
        Tree<String> num2 = Tree.of("<num>", dig2);

        Tree<String> prop2 = Tree.of("<prop>", propSymbol2, var2, comp2, num2);

        // Top of the Tree.
        Tree<String> andSymbol = Tree.of("and");
        Tree<String> two = Tree.of("<two>", andSymbol, prop1, prop2);
        Tree<String> root = Tree.of("<e>", two);

        root.prettyPrint(new PrintStream(System.out));

        STLMapper mapper = new STLMapper();
        AbstractSTLNode node = mapper.apply(root);
        System.out.println(node);

        assertEquals(node.getSymbol(), "AND");
        assertEquals(node.getFirstChild().getSymbol(), "x1 > 3.0");
    }
}
