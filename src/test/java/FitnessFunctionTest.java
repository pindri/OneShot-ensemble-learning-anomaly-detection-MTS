import it.units.malelab.jgea.representation.tree.Tree;
import mapper.STLMapper;
import nodes.AbstractSTLNode;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertTrue;

public class FitnessFunctionTest {

    @SuppressWarnings({"unchecked", "SpellCheckingInspection"})
    @Test
    public void applyTest() throws IOException {
        // Creating monitor manually.
        STLMapper mapper = new STLMapper();

            // First branch.
            Tree<String> v1 = Tree.of("x1");
            Tree<String> c1 = Tree.of("<");
            Tree<String> n1 = Tree.of("2");

            Tree<String> propSymbol1 = Tree.of("proposition");
            Tree<String> var1 = Tree.of("<var>", v1);
            Tree<String> comp1 = Tree.of("<comp>", c1);
            Tree<String> num1 = Tree.of("<num>", n1);

            Tree<String> prop1 = Tree.of("<prop>", propSymbol1, var1, comp1, num1);

            // Second branch.
            Tree<String> v2 = Tree.of("x3");
            Tree<String> c2 = Tree.of(">");
            Tree<String> n2 = Tree.of("6");

            Tree<String> propSymbol2 = Tree.of("proposition");
            Tree<String> var2 = Tree.of("<var>", v2);
            Tree<String> comp2 = Tree.of("<comp>", c2);
            Tree<String> num2 = Tree.of("<num>", n2);

            Tree<String> prop2 = Tree.of("<prop>", propSymbol2, var2, comp2, num2);

            // Top of the Tree.
            Tree<String> andSymbol = Tree.of("and");
            Tree<String> two = Tree.of("<two>", andSymbol, prop1, prop2);
            Tree<String> root = Tree.of("<e>", two);

        AbstractSTLNode monitor1 = mapper.apply(root);
        System.out.println(monitor1);

            // Second branch (alternative).
            Tree<String> v2alt = Tree.of("x3");
            Tree<String> c2alt = Tree.of("<");
            Tree<String> n2alt = Tree.of("6");

            Tree<String> propSymbol2alt = Tree.of("proposition");
            Tree<String> var2alt = Tree.of("<var>", v2alt);
            Tree<String> comp2alt = Tree.of("<comp>", c2alt);
            Tree<String> num2alt = Tree.of("<num>", n2alt);

            Tree<String> prop2alt = Tree.of("<prop>", propSymbol2alt, var2alt, comp2alt, num2alt);

            // Top of the Tree (alternative).
            Tree<String> andSymbolalt = Tree.of("and");
            Tree<String> twoalt = Tree.of("<two>", andSymbolalt, prop1, prop2alt);
            Tree<String> rootalt = Tree.of("<e>", twoalt);

        AbstractSTLNode monitor2 = mapper.apply(rootalt);
        System.out.println(monitor2);

        // Computing fitness.
        FitnessFunction fitnessFunction = new FitnessFunction("data/test_data.csv");
        assertTrue(fitnessFunction.apply(monitor1) < fitnessFunction.apply(monitor2));

        System.out.println(fitnessFunction.apply(monitor1));
        System.out.println(fitnessFunction.apply(monitor2));

    }
}
