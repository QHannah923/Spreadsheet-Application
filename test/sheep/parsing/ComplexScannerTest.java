package sheep.parsing;

import org.junit.Before;
import org.junit.Test;
import sheep.expression.Expression;

import java.util.List;
import java.util.Scanner;

import static org.junit.Assert.*;

public class ComplexScannerTest {
    private ComplexScanner scanner;

    @Before
    public void setUp() {
        scanner = new ComplexScanner();
    }

    /**
     * Assert that parsing the empty string returns the
     * result of `createEmpty()` from the parsers factory.
     * Note: This is not necessarily an instance of `Nothing`.
     */
    @Test
    public void testNothing() throws ParseException {
        List<ComplexScanner.Token> result = ComplexScanner.tokenize("");
        assertEquals("Parsing an empty string did not return result of createEmpty().",
                "[]", result.toString());
    }

    @Test
    public void testNothingSpace() throws ParseException {
        List<ComplexScanner.Token> result = ComplexScanner.tokenize(" ");
        assertEquals("Parsing single whitespace did not return result of createEmpty().",
                "[]", result.toString());
    }

    @Test
    public void testNothingWhiteSpace() throws ParseException {
        List<ComplexScanner.Token> result = ComplexScanner.tokenize("\"\\t    \"");
        assertEquals("Parsing multiple whitespaces did not return result of createEmpty().",
                "[Token[type=REFERENCE, name=\"\\t\", contents=null]]"
                , result.toString());
    }

    @Test
    public void testConstant() throws ParseException {
        List<ComplexScanner.Token> result = ComplexScanner.tokenize("42");
        assertEquals("Parsing a constant value did not return result of createConstant(long).",
                "[Token[type=CONST, name=42, contents=null]]", result.toString());
    }

    @Test
    public void testConstantWhitespace() throws ParseException {
        List<ComplexScanner.Token> result = ComplexScanner.tokenize("  42\t");
        assertEquals("Parsing a constant surrounded by whitespace did not return result of createConstant(long).",
                "[Token[type=CONST, name=42, contents=null]]", result.toString());
    }



    @Test
    public void testConstantNegative() throws ParseException {
        List<ComplexScanner.Token> result = ComplexScanner.tokenize("-42");
        assertEquals("Parsing a negative constant did not return result of createConstant(long).",
                "[Token[type=OP, name=-, contents=null], " +
                        "Token[type=CONST, name=42, contents=null]]", result.toString());
    }

    @Test
    public void testConstantStripZeros() throws ParseException {
        List<ComplexScanner.Token> result = ComplexScanner.tokenize("00000");
        assertEquals("Parsing multiple zeros did not return result of createConstant(long).",
                "[Token[type=CONST, name=00000, contents=null]]", result.toString());
    }

    @Test
    public void testConstantNegativeSpace() throws ParseException {
        List<ComplexScanner.Token> result = ComplexScanner.tokenize("   -42");
        assertEquals("Parsing a negative constant with whitespace did not " +
                        "return result of createConstant(long).",
                "[Token[type=OP, name=-, contents=null]," +
                        " Token[type=CONST, name=42, contents=null]]", result.toString());
    }

    @Test
    public void testArithmeticPlus() throws ParseException {
        List<ComplexScanner.Token> result = ComplexScanner.tokenize("3 + 20 + 12 + 100");
        assertEquals("Parsing addition expression with whitespace did not return result of creatOperator.",
                "[Token[type=CONST, name=3, contents=null], " +
                        "Token[type=OP, name=+, contents=null], Token[type=CONST, name=20, contents=null], " +
                        "Token[type=OP, name=+, contents=null], Token[type=CONST, name=12, contents=null], " +
                        "Token[type=OP, name=+, contents=null], Token[type=CONST, name=100, contents=null]]",
                result.toString());
    }

    @Test
    public void testArithmeticPlusNoSpace() throws ParseException {
        List<ComplexScanner.Token> result = ComplexScanner.tokenize("3+20+12+100");
        assertEquals("Parsing addition expression did not return result of creatOperator.",
                "[Token[type=CONST, name=3, contents=null], " +
                        "Token[type=OP, name=+, contents=null], Token[type=CONST, name=20, contents=null], " +
                        "Token[type=OP, name=+, contents=null], Token[type=CONST, name=12, contents=null], " +
                        "Token[type=OP, name=+, contents=null], Token[type=CONST, name=100, contents=null]]",
                result.toString());
    }

    @Test
    public void testArithmeticPlusMixedSpace() throws ParseException {
        List<ComplexScanner.Token> result = ComplexScanner.tokenize("3+ 20 +12+ 100");
        assertEquals("Parsing addition expression with whitespace did not return result of creatOperator.",
                "[Token[type=CONST, name=3, contents=null], " +
                        "Token[type=OP, name=+, contents=null], Token[type=CONST, name=20, contents=null], " +
                        "Token[type=OP, name=+, contents=null], Token[type=CONST, name=12, contents=null], " +
                        "Token[type=OP, name=+, contents=null], Token[type=CONST, name=100, contents=null]]",
                result.toString());
    }

    @Test
    public void testArithmeticTimesMixedSpace() throws ParseException {
        List<ComplexScanner.Token> result = ComplexScanner.tokenize("3* 20 *12* 100");
        assertEquals("Parsing multiplication expression with whitespace did not return result of creatOperator.",
                "[Token[type=CONST, name=3, contents=null], " +
                        "Token[type=OP, name=*, contents=null], Token[type=CONST, name=20, contents=null], " +
                        "Token[type=OP, name=*, contents=null], Token[type=CONST, name=12, contents=null], " +
                        "Token[type=OP, name=*, contents=null], Token[type=CONST, name=100, contents=null]]",
                result.toString());
    }

    @Test
    public void testArithmeticNested() throws ParseException {
        List<ComplexScanner.Token> result = ComplexScanner.tokenize("3* 2 * 20 - 2/15 +12* 100");
        assertEquals("Parsing mixed expression with whitespace did not return result of creatOperator.",
                "[Token[type=CONST, name=3, contents=null], " +
                        "Token[type=OP, name=*, contents=null], Token[type=CONST, name=2, contents=null], " +
                        "Token[type=OP, name=*, contents=null], Token[type=CONST, name=20, contents=null], " +
                        "Token[type=OP, name=-, contents=null], Token[type=CONST, name=2, contents=null], " +
                        "Token[type=OP, name=/, contents=null], Token[type=CONST, name=15, contents=null], " +
                        "Token[type=OP, name=+, contents=null], Token[type=CONST, name=12, contents=null], " +
                        "Token[type=OP, name=*, contents=null], Token[type=CONST, name=100, contents=null]]",
                result.toString());
    }

    @Test
    public void testReference() throws ParseException {
        List<ComplexScanner.Token> result = ComplexScanner.tokenize("A0");
        assertEquals("Parsing a reference string did not return result of createReference.",
                "[Token[type=REFERENCE, name=A0, contents=null]]", result.toString());
    }

    @Test
    public void testComma() throws ParseException {
        List<ComplexScanner.Token> result = ComplexScanner.tokenize("1,2,3");
        assertEquals("Parsing a reference string did not return result of createReference.",
                "[Token[type=CONST, name=1, contents=null], Token[type=OP, name=,, contents=null], Token[type=CONST, name=2, contents=null], Token[type=OP, name=,, contents=null], Token[type=CONST, name=3, contents=null]]", result.toString());
    }

    @Test
    public void testOddReference() throws ParseException {
        List<ComplexScanner.Token> result = ComplexScanner.tokenize("sd45678fghjk");
        assertEquals("Parsing a reference string did not return result of createReference.",
                "[Token[type=REFERENCE, name=sd45678fghjk, contents=null]]", result.toString());
    }

    @Test
    public void testReferenceSpaces() throws ParseException {
        List<ComplexScanner.Token> result = ComplexScanner.tokenize("   OO  ");
        assertEquals("Parsing a reference string with whitespace did not return result of createReference.",
                "[Token[type=REFERENCE, name=OO, contents=null]]", result.toString());
    }

    @Test
    public void testTokenEmptyName() throws ParseException {
        List<ComplexScanner.Token> result = ComplexScanner.tokenize("  5*(A1+A9+3*5)");
        assertEquals("Parsing a Expressions with parentheses.",
                "[Token[type=CONST, name=5, contents=null], Token[type=OP, name=*, contents=null], Token[type=FUNC, name=, contents=A1+A9+3*5]]", result.toString());
        assertEquals("empty name has type of String","",result.get(2).name());
        assertTrue("isEmpty",result.get(2).name().isEmpty());
    }

    @Test
    public void testMeanExp() throws ParseException {
        List<ComplexScanner.Token> result = ComplexScanner.tokenize("  MEAN(5*(A1+A9+3*5),342)");
        assertEquals("Parsing a Expressions with MEAN function.",
                "[Token[type=FUNC, name=MEAN, contents=5*(A1+A9+3*5),342]]", result.toString());
        assertEquals("",result.get(0).contents().toString());
    }

    @Test
    public void testMedianExp() throws ParseException {
        List<ComplexScanner.Token> result = ComplexScanner.tokenize("  MEDIAN(5*(A1+A9+3*5),342)");
        assertEquals("Parsing a Expressions with MEDIAN function.",
                "[Token[type=FUNC, name=MEDIAN, contents=5*(A1+A9+3*5),342]]", result.toString());
    }

    /**
     * Assert that parsing "(()" throws a ParseException.
     */
    @Test(expected = ParseException.class)
    public void testInvalidParens() throws ParseException {
        List<ComplexScanner.Token> result = ComplexScanner.tokenize("   (()  ");
        System.out.println(result.toString());
    }

    /**
     * Assert that parsing "())" throws a ParseException.
     */
    @Test(expected = ParseException.class)
    public void testInvalidParens2() throws ParseException {
        List<ComplexScanner.Token> result = ComplexScanner.tokenize("())");
        System.out.println(result.toString());
    }
}
