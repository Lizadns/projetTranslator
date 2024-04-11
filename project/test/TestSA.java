import compiler.Lexer.Lexer;
import compiler.Parser.Node;
import compiler.Parser.Parser;
import compiler.Parser.Program;
import compiler.SemanticAnalysis.SemanticAnalysis;
import compiler.SemanticAnalysis.SemanticException;
import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.ExpectedException;

import java.io.IOException;
import java.io.StringReader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class TestSA {

    @Test
    public void testConstantDeclarationLiteral() throws IOException, SemanticException {
        String input = "final int a = 3;";
        StringReader reader = new StringReader(input);
        Lexer lexer = new Lexer(reader);
        Parser parser = new Parser(lexer);
        Program program = parser.getAST();
        SemanticAnalysis sa = new SemanticAnalysis(program);
        String answer = sa.analyzeNode(program);
        assertEquals("Everything is OK!", answer);

    }

    @Test
    public void testVariableDeclaration() throws IOException, SemanticException {
        String input = "int a = 2;";
        StringReader reader = new StringReader(input);
        Lexer lexer = new Lexer(reader);
        Parser parser = new Parser(lexer);

        Program program = parser.getAST();
        SemanticAnalysis sa = new SemanticAnalysis(program);
        String answer = sa.analyzeNode(program);
        assertEquals("Everything is OK!", answer);

    }

    @Test
    public void testFindReturnTypeFunctionCall() throws IOException, SemanticException {
        String input = "def float add(float a, int b) { return a + b; }\n" +
                "float b = add(2.3,3);";
        StringReader reader = new StringReader(input);
        Lexer lexer = new Lexer(reader);
        Parser parser = new Parser(lexer);

        Program program = parser.getAST();
        SemanticAnalysis sa = new SemanticAnalysis(program);
        String answer = sa.analyzeNode(program);
        assertEquals("Everything is OK!", answer);

    }

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Test
    public void testFindReturnTypeFunctionCallFalse() throws IOException, SemanticException {
        String input = "def float add(float a, int b) { return a + b; }\n" +
                "int b = add(2.3,3);";
        StringReader reader = new StringReader(input);
        Lexer lexer = new Lexer(reader);
        Parser parser = new Parser(lexer);

        Program program = parser.getAST();
        SemanticAnalysis sa = new SemanticAnalysis(program);

        // Spécifie que l'exception SemanticException est attendue
        exceptionRule.expect(SemanticException.class);
        // Spécifie que le message de l'exception doit contenir "TypeError"
        exceptionRule.expectMessage("TypeError");

        // Exécute la méthode qui doit lever l'exception
        sa.analyzeNode(program);
    }

    @Test
    public void testVariable() throws IOException, SemanticException {
        String input = "int testV ;\n" +
                "testV = 6 ;";
        StringReader reader = new StringReader(input);
        Lexer lexer = new Lexer(reader);
        Parser parser = new Parser(lexer);

        Program program = parser.getAST();
        SemanticAnalysis sa = new SemanticAnalysis(program);
        String answer = sa.analyzeNode(program);
        assertEquals("Everything is OK!", answer);

    }
    public void testVariableFault() throws IOException, SemanticException {
        String input = "int testV ;\n" +
                "testMTN = 6 ;";
        StringReader reader = new StringReader(input);
        Lexer lexer = new Lexer(reader);
        Parser parser = new Parser(lexer);

        Program program = parser.getAST();
        SemanticAnalysis sa = new SemanticAnalysis(program);

        // Spécifie que l'exception SemanticException est attendue
        exceptionRule.expect(SemanticException.class);
        // Spécifie que le message de l'exception doit contenir "TypeError"
        exceptionRule.expectMessage("No declaration of the variable");

        // Exécute la méthode qui doit lever l'exception
        sa.analyzeNode(program);

    }

}
