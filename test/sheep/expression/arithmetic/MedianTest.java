package sheep.expression.arithmetic;

import org.junit.Test;
import sheep.expression.Expression;
import sheep.expression.TypeError;
import sheep.expression.basic.Constant;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MedianTest {
    @Test
    public void testIdentityValue() throws TypeError {
        Function function = new Median(new Expression[]{new Constant(20)});
        Expression result = function.value(new HashMap<>());
        assertTrue("Result of evaluating median is not a constant.",
                result instanceof Constant);
        assertEquals("Result of median with a single Constant is incorrect.",
                20, ((Constant) result).getValue());
    }

    @Test
    public void testIdentityPerform() throws TypeError {
        Function function = new Median(new Expression[]{new Constant(20)});
        long result = function.perform(new long[]{20});
        assertEquals("Result of performing median with a single Constant is incorrect.", 20, result);
    }

    @Test
    public void testTwoValue() throws TypeError {
        Function function = new Median(new Expression[]{new Constant(20), new Constant(10)});
        Expression result = function.value(new HashMap<>());
        assertTrue("Result of evaluating median is not a constant.",
                result instanceof Constant);
        assertEquals("Result of median with two Constants is incorrect.",
                15, ((Constant) result).getValue());
    }

    @Test
    public void testTwoPerform() throws TypeError {
        Function function = new Median(new Expression[]{new Constant(20), new Constant(10)});
        long result = function.perform(new long[]{20, 10});
        assertEquals("Result of performing median with two Constants is incorrect.", 15, result);
    }

    @Test
    public void testNValue() throws TypeError {
        Function function = new Median(
                new Expression[]{new Constant(20), new Constant(2), new Constant(2), new Constant(2)}
        );
        Expression result = function.value(new HashMap<>());
        assertTrue("Result of evaluating median is not a constant.",
                result instanceof Constant);
        assertEquals("Result of median with multiple Constants is incorrect.",
                2, ((Constant) result).getValue());
    }

    @Test
    public void testNPerform() throws TypeError {
        Function function = new Median(
                new Expression[]{new Constant(20), new Constant(2), new Constant(5), new Constant(2)}
        );
        long result = function.perform(new long[]{20, 2, 5, 2});
        assertEquals("Result of performing median with multiple Constants is incorrect",3, result);
    }
}
