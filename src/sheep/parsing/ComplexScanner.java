package sheep.parsing;

import sheep.expression.Expression;

import java.util.ArrayList;
import java.util.List;

/**
 * First stage of extracting the {@link Expression}s from a String entered into a cell.
 * <p>
 * The tokenize method transforms a String into a list of tokens
 * representing types of {@link Expression} instances.
 * These tokens make it easier for the {@link ComplexParser}
 * to parse the expression into its component parts.
 * @hint {@link ComplexParser} should use {@link #tokenize(String)} to break a String into tokens.
 *       {@link ComplexParser} should then parse the returned list of tokens.
 * @provided
 * @test
 */
public class ComplexScanner {
    // List of all valid arithmetic operators in the order of precedence.
    public static final List<Character> OPERATORS = List.of(
            ',', '=', '<', '+', '-', '*', '/'
    );

    /**
     * Tokenize a string into a list of operator, reference, constant, and function tokens.
     * Only the "bottom" layer of the string is tokenized,
     * function tokens store the contents of their parentheses untokenized.
     *
     * @param input the string to tokenize.
     * @return The list of tokens found in order.
     * @throws ParseException if there are unmatched parentheses.
     */
    public static List<Token> tokenize(String input) throws ParseException {
        TokenBuilder tokenBuilder = new TokenBuilder();
        int level = 0;

        for (var c : input.toCharArray()) {
            if (c == '(') {
                level++;
            }

            if (c == ')') {
                level--;

                // handle closed parenthesis with no open
                if (level < 0) {
                    throw new ParseException("Unmatched \")\" in \"" + input + "\"");
                }

                if (level == 0) {
                    // When we reach the base level the function has ended
                    tokenBuilder.finishToken();
                    continue;
                }
            }

            // If we're an operator unenclosed in parentheses
            if ((level == 0) && OPERATORS.contains(c)) {
                tokenBuilder.finishToken();
                tokenBuilder.append(c);
                tokenBuilder.finishToken();
                continue;
            }

            tokenBuilder.append(c);
        }

        if (level != 0) {
            throw new ParseException("Unmatched \"(\" in \"" + input + "\"");
        }

        tokenBuilder.finishToken();

        return tokenBuilder.getList();
    }

    /**
     * The types of Tokens.
     * @provided
     */
    public enum TokenType {
        /**
         * Operator token.
         */
        OP,

        /**
         * Reference token.
         */
        REFERENCE,

        /**
         * Constant token.
         */
        CONST,

        /**
         * Function token.
         */
        FUNC
    }

    /**
     * An intermediary representation of an Expression.
     * @param type The token's type.
     * @param name The token's name.
     * @param contents The string value that was extracted into this token.
     * @provided
     */
    public record Token(TokenType type, String name, String contents) {
        public Token(TokenType type, String name) {
            this(type, name, null);
        }
    }

    /**
     * A class that helps build a list of Tokens while ignoring whitespace and empty tokens.
     */
    private static class TokenBuilder {
        private final StringBuilder builder = new StringBuilder();
        private final List<Token> list = new ArrayList<>();

        /**
         * Converts a string to a single token
         * <p>
         * Strings with parentheses are tokenized as FUNCs
         * Any string before the open parenthesis is treated as the name,
         * Any string between the open parenthesis and it's matching close parenthesis is treated as the contents.
         * </p>
         * <p>
         * Characters in the list of valid operators are tokenized alone as OPs with the character as the name.
         * </p>
         * <p>
         * Strings that can be parsed as a Long are tokenized as CONSTs with a name equal to the string.
         * </p>
         * <p>
         * All other strings are treated as REFERENCEs with name equal to the whole string.
         * </p>
         * Token types without contents have null as the contents.
         * <p>
         * Examples:
         * "Name(Contents)" is parsed as a FUNC with name: "Name" and contents "Contents"
         * "()" is parsed as a FUNC with name: "" and contents: ""
         * "+" is parsed as an OP with name: "+" and contents: null
         * "0" is parsed as an CONST with name: "0" and contents: null
         * </p>
         *
         * @param input the string to parse.
         * @return The token represented by the input.
         * @requires the string to only contain 1 token
         */
        private static Token toToken(String input) {
            input = input.trim();
            if (OPERATORS.contains(input.charAt(0))) {
                return new Token(TokenType.OP, input);
            }

            if (input.contains("(")) {
                String[] parts = input.split("\\(", 2);
                return new Token(TokenType.FUNC, parts[0], parts[1]);
            }
            try {
                Long.parseLong(input);
                return new Token(TokenType.CONST, input);
            } catch (NumberFormatException nfe) {
                return new Token(TokenType.REFERENCE, input);
            }
        }

        /**
         * Add a character to the buffer
         *
         * @param c The character to add
         */
        public void append(char c) {
            if (!Character.isWhitespace(c)) {
                builder.append(c);
            }
        }

        /**
         * Parse the buffer as a token and
         * add it to the list of finalised tokens
         */
        public void finishToken() {
            if (!builder.isEmpty()) {
                list.add(toToken(builder.toString()));
                builder.setLength(0);
            }
        }

        /**
         * Get all finalised tokens.
         *
         * @return A list of all parsed tokens
         */
        public List<Token> getList() {
            return new ArrayList<>(list);
        }
    }
}
