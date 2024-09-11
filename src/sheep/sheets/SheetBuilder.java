package sheep.sheets;

import sheep.expression.Expression;
import sheep.expression.TypeError;
import sheep.parsing.ParseException;
import sheep.parsing.Parser;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Builder pattern to construct {@link Sheet} instances.
 * <p>
 * A sheet builder maintains a collection of built-in expressions.
 * These map identifiers to expressions such that any expression
 * within the constructed sheet can reference the identifier and
 * evaluate to the expression.
 * <p>
 * For example, if the identifier 'hundred' was mapped to the number 100,
 * then any cell in the constructed sheet could use 'hundred' in place of
 * 100 in a formula.
 * <pre>
 * {@code
 * SheetBuilder builder = new SheetBuilder(parser, exp);
 * builder.includeBuiltIn("hundred", new Number(100));
 * Sheet sheet = builder.empty(2, 2);
 * sheet.update(new GridLocation(1, 1), new Identifier("hundred"));
 * sheet.valueAt(new GridLocation(1, 1)) // 100
 * }
 * </pre>
 * @stage2
 */
public class SheetBuilder {
    private final Map<String, Expression> builtins = new HashMap<>();
    private final Parser parser;
    private final Expression defaultExpression;

    /**
     * Construct an instance of SheetBuilder that will create
     * Sheet instances using the given {@link Parser} and {@link Expression} instances.
     */
    public SheetBuilder(Parser parser, Expression defaultExpression) {
        this.parser = parser;
        this.defaultExpression = defaultExpression;
    }

    private static int countCols(String row) {
        return Math.toIntExact(row.chars().filter(c -> c == '|').count()) + 1;
    }

    private static Expression parseOr(Parser parser, String input, Expression defaultExpr) {
        try {
            return parser.parse(input);
        } catch (ParseException e) {
            return defaultExpr;
        }
    }

    /**
     * Include a new built-in expression for the given identifier
     * within any sheet constructed by this builder instance.
     *
     * <pre>
     * {@code
     * Sheet sheet = new SheetBuilder(parser, exp)
     *         .includeBuiltIn("cafe", new Constant(3405691582))
     *         .includeBuiltIn("dood", new Constant(3490524077))
     *         .empty(10, 10);
     * }</pre>
     *
     * @param identifier A string identifier to be used in the constructed sheet.
     * @param expression The value that the identifier should resolve to within
     *                   the constructed sheet.
     * @return The current instance of the SheetBuilder.
     * @requires identifier cannot be a valid cell location reference, e.g. A1.
     */
    public SheetBuilder includeBuiltIn(String identifier, Expression expression) {
        this.builtins.put(identifier, expression);
        return this;
    }

    /**
     * Construct a new empty sheet with the given number of rows and columns.
     * <p>
     * If the built-ins are updated (i.e. {@link SheetBuilder#includeBuiltIn(String, Expression)} is called)
     * after a sheet has been constructed,
     * this must not affect the constructed sheet.
     *
     * <pre>
     * {@code
     * SheetBuilder builder = new SheetBuilder(parser, exp);
     * builder.includeBuiltIn("cafe", new Constant(3405691582));
     * Sheet sheet = builder.empty(10, 10);
     * builder.includeBuiltIn("hello", new Constant(20)); // Should not update the built-ins of `sheet`
     * }</pre>
     *
     * @param rows    Amount of rows for the new sheet.
     * @param columns Amount of columns for the sheet.
     * @return A new sheet with the appropriate built-ins and of the specified dimensions.
     */
    public Sheet empty(int rows, int columns) {
        return new Sheet(parser, new HashMap<>(builtins), defaultExpression, rows, columns);
    }

    /**
     * Load a Sheet from file.
     * The format of the file is treated the same as in {@link Sheet encode}.
     * The resulting sheet has as many rows as there are lines in the file,
     * and one more column than there are pipes '|' in the row with the most pipes.
     * Missing trailing columns are filled with the default expression.
     *
     * @param filename The path to file to open.
     * @return The sheet decoded from the file.
     * @throws FileNotFoundException If the file is not found.
     * @throws ParseException        If the sheet or any of its expressions fail to parse.
     * @throws TypeError             If any of the expressions have a type error.
     * @stage2
     */
    public Sheet load(String filename) throws FileNotFoundException, ParseException, TypeError {
        BufferedReader br = new BufferedReader(new FileReader(filename));
        List<String> lines = new ArrayList<>(br.lines().toList());
        int rows = lines.size();
        int cols = lines
                .stream()
                .mapToInt(SheetBuilder::countCols)
                .max()
                .orElseThrow(() -> new ParseException("File had no valid"));

        Sheet sheet = new Sheet(parser, new HashMap<>(builtins), defaultExpression, rows, cols);
        List<List<Expression>> expressions = lines.stream()
                .map(line -> List.of(line.split("\\|")))
                .map(row -> row.stream()
                        .map((e) -> parseOr(parser, e, defaultExpression))
                        .toList())
                .toList();

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                Expression expression = defaultExpression;
                if (i < expressions.size() && j < expressions.get(i).size()) {
                    expression = expressions.get(i).get(j);
                }
                sheet.update(new CellLocation(i, j), expression);
            }
        }
        return sheet;
    }

}