package mapper;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ExpressionTest {

    @Test
    public void expressionTest() {
        Expression expression = Expression.AND;
        assertEquals(expression.toString(), "and");
    }
}
