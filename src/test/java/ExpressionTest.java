import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ExpressionTest {

    @Test
    public void expressionTest() {
        Expression expression = Expression.NOT;
        assertEquals(expression.toString(), "not");
    }
}
