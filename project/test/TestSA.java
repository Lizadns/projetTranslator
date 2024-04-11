import compiler.Lexer.Lexer;
import compiler.Parser.Node;
import compiler.Parser.Parser;
import compiler.Parser.Program;
import compiler.SemanticAnalysis.SemanticAnalysis;
import compiler.SemanticAnalysis.SemanticException;
import org.junit.Test;

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
}
