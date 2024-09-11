package sheep.parsing;

import sheep.expression.CoreFactory;
import sheep.expression.Expression;
import sheep.expression.ExpressionFactory;
import sheep.expression.InvalidExpression;

import java.util.ArrayList;
import java.util.List;

/**
 * A parser of Basic, Arithmetic, and broader functional components.
 * @stage1
 */
public class ComplexParser implements Parser {
    /**
     *  Factory instance for creating new operators
     */
    private final ExpressionFactory factory;

    /**
     * Construct a new parser.
     * Parsed expressions are constructed using the expression factory.
     *
     * @param factory Factory used to construct parsed expressions.
     */
    public ComplexParser(ExpressionFactory factory) {
        this.factory = factory;
    }

    /**
     * Tries to parse the given inputs into an array of Expression objects.
     *
     * @param inputs The input strings to be parsed.
     * @return An array of Expression objects representing the parsed inputs.
     * @throws ParseException If there is a problem parsing any of the inputs.
     * @throws InvalidExpression If any of the parsed expressions are invalid.
     */
    private Expression[] tryParse(String[] inputs) throws ParseException, InvalidExpression {
        Expression[] expressions = new Expression[inputs.length];
        for (int i = 0; i < inputs.length; i++) {
            expressions[i] = tryParse(inputs[i]);
        }
        return expressions;
    }

    /**
     * Tries to parse the given input string and returns the corresponding expression.
     *
     * @param input the input string to be parsed
     * @return the parsed expression
     * @throws ParseException if the input string cannot be parsed into a valid expression
     * @throws InvalidExpression if the parsed expression is invalid
     */
    private Expression tryParse(String input) throws ParseException, InvalidExpression {
        input = input.strip();
        List<ComplexScanner.Token> tokens = ComplexScanner.tokenize(input);
        return tokenParse(tokens);
    }

    /**
     * Parses a list of tokens into an expression.
     *
     * @param tokens the list of tokens to parse
     * @return the parsed expression
     * @throws ParseException if there is an error parsing the tokens
     * @throws InvalidExpression if the expression is invalid
     */
    private Expression tokenParse(List<ComplexScanner.Token> tokens)
            throws ParseException, InvalidExpression {
        // operators
        String op = "123";
        String[] ops = {"=", "<", "/", "*", "-", "+", ","};
        for (String s : ops) {
            if (tokens.stream().anyMatch(a -> a.name().equals(s))) {
                op = s;
            }
        }
        if (!op.equals("123")) {
            List<Integer> indexs = new ArrayList<>();
            for (int i = 0; i < tokens.size(); i++) {
                if (tokens.get(i).name().equals(op)) {
                    indexs.add(i);
                }
            }
            List<Expression> expressions = new ArrayList<>();
            int leftEndPoint = 0;
            for (Integer i : indexs) {
                List<ComplexScanner.Token> tmp = tokens.subList(leftEndPoint, i);
                expressions.add(tokenParse(tmp));
                leftEndPoint = i + 1;
            }
            expressions.add(tokenParse(tokens.subList(indexs.getLast() + 1, tokens.size())));
            return factory.createOperator(op, expressions.toArray());
        }
        for (ComplexScanner.Token t : tokens) { // parentheses
            if (t.type() == ComplexScanner.TokenType.FUNC && t.name().isEmpty()) {
                Expression expression = tokenParse(ComplexScanner.tokenize(t.contents()));
                return factory.createOperator("", new Expression[]{expression});
            }
        }
        for (ComplexScanner.Token t1 : tokens) { // MEAN and MEDIAN
            if (t1.type() == ComplexScanner.TokenType.FUNC && t1.name().equals("MEAN")) {
                Expression[] expressions = tryParse(t1.contents().split(","));
                return factory.createOperator("MEAN", expressions);
            }
            if (t1.type() == ComplexScanner.TokenType.FUNC && t1.name().equals("MEDIAN")) {
                Expression[] expressions = tryParse(t1.contents().split(","));
                return factory.createOperator("MEDIAN", expressions);
            }
        }
        if (tokens.isEmpty()) { // empty token
            return factory.createEmpty();
        }
        for (ComplexScanner.Token t : tokens.stream().filter(a -> a.type()
                == ComplexScanner.TokenType.CONST).toList()) {
            if (t.type() == ComplexScanner.TokenType.CONST) { // const numbers
                try {
                    long number = Long.parseLong(t.name());
                    return factory.createConstant(number);
                } catch (NumberFormatException ignored) {
                    // ignore unable to parse
                }
            }
        }
        List<ComplexScanner.Token> refs = tokens.stream().filter(a -> a.type()
                == ComplexScanner.TokenType.REFERENCE).toList();
        for (ComplexScanner.Token ref : refs) { // references
            for (char character : ref.name().toCharArray()) {
                if (!(Character.isAlphabetic(character) || Character.isDigit(character))) {
                    throw new ParseException("Unknown input: " + ref.name());
                }
            }
            return factory.createReference(ref.name());
        }
        return null;
    }

    /**
     * Attempt to parse a string expression into an expression.
     * <ul>
     * <li>Leading and trailing whitespaces must not affect parsing.</li>
     * <li>
     * If the string is just whitespace, an empty expression should be constructed
     * with {@link ExpressionFactory#createEmpty()}.
     * </li>
     * <li>
     * Any number that can be parsed as a long by the rules of {@link Long#parseLong(String)}
     * should be constructed as a constant with {@link ExpressionFactory#createConstant(long)}.
     * </li>
     * <li>
     * Any string that contains one of the operator names
     * from the {@link sheep.expression.arithmetic}
     * package should be split on that operator name and the components between should be parsed.
     * The components between should follow the same rules as the top-level expression,
     * e.g. leading and trailing whitespace should be ignored, etc.
     * If any component cannot be parsed, the whole expression cannot be parsed.
     * Arithmetic expressions should be constructed
     * with {@link ExpressionFactory#createOperator(String, Object[])}.
     * You must attempt to parse the arithmetic operators in the following order:
     * <ul>
     *     <li>=</li>
     *     <li>&lt;</li>
     *     <li>+</li>
     *     <li>-</li>
     *     <li>*</li>
     *     <li>/</li>
     * </ul>
     * You must ensure that the maximum amount of operands are used,
     * i.e. do not parse 4 + 5 + 6 as Plus(4, Plus(5, 6)) instead do Plus(4, 5, 6).
     * <p>
     * Note: This does not need to be implemented until stage 2.
     * </li>
     * <li>
     * Any remaining expressions that
     * 1) cannot be parsed as a number or arithmetic expression, and
     * 2) only contains alphabetic {@link Character#isAlphabetic(int)}
     * and digit characters {@link Character#isDigit(char)},
     * should be treated as a reference.
     * </li>
     * </ul>
     *
     * <pre>
     * {@code
     * ExpressionFactory factory = new CoreFactory();
     * Parser parser = new SimpleParser(factory);
     * parser.parse("  42  ");
     * // Constant(42)
     * parser.parse("HEY ");
     * // Reference("HEY")
     * parser.parse("hello + world");
     * // Plus(Reference("hello"), Reference("world"))
     * parser.parse("4 + 5 + 7 * 12 + 3");
     * // Plus(Constant(4), Constant(5), Times(Constant(7), Constant(12)), Constant(3))
     * }</pre>
     *
     * @param input A string to attempt to parse.
     * @return The result of parsing the expression.
     * @throws ParseException If the string input is not recognisable as an expression.
     */
    @Override
    public Expression parse(String input) throws ParseException {
        try {
            return tryParse(input);
        } catch (Exception e) {
            throw new ParseException(e);
        }
    }
}
