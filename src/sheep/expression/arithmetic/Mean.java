package sheep.expression.arithmetic;

import sheep.expression.Expression;

import java.util.Arrays;

/**
 * Calculates the mean (average) of a set of numbers.
 */
public class Mean extends Function {
    /**
     * Constructs a Mean instance with the given arguments.
     *
     * @param arguments the numbers to calculate the mean of
     */
    protected Mean(Expression[] arguments) {
        super("MEAN", arguments);
    }

    /**
     * Calculates the mean of the provided arguments.
     *
     * @param arguments the numbers to average
     * @return the mean of the numbers, truncated to a long value
     */
    protected long perform(long[] arguments) {
        System.out.println("MEAN calcu");
        Double mean = Arrays.stream(arguments).average().orElse(0.0);
        return mean.longValue();
    }
}



