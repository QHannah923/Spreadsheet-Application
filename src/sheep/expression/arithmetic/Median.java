package sheep.expression.arithmetic;

import sheep.expression.Expression;

import java.util.Arrays;

/**
 * Represents a median function that calculates the median of a set of numbers.
 */
public class Median extends Function {

    /**
     * Constructs a Median instance with the given arguments.
     *
     * @param arguments the numbers to calculate the median of
     */
    protected Median(Expression[] arguments) {
        super("MEDIAN", arguments);
    }

    /**
     * Calculates the median of the provided arguments.
     *
     * @param arguments the numbers to calculate the median of
     * @return the median of the numbers
     */
    protected long perform(long[] arguments) {
        Arrays.sort(arguments);
        int middleIndex;
        middleIndex = arguments.length / 2;
        if (arguments.length % 2 != 0) {
            return arguments[middleIndex];
        }
        return (arguments[middleIndex - 1] + arguments[middleIndex]) / 2;
    }

}