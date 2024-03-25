import static org.junit.Assert.assertNotNull;

import compiler.Parser.Parser;
import compiler.Parser.Program;
import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;
import compiler.Lexer.Lexer;

public class TestLexer {
    
    @Test
    public void testArrayVariable() throws IOException {
        String input = "int[] c = int[5];";
        StringReader reader = new StringReader(input);
        Lexer lexer = new Lexer(reader);
        Parser parser = new Parser(lexer);
        Program program = parser.getAST();
        assertNotNull(program);
    }

    @Test
    public void testFinalDeclaration() throws IOException {
        String input = "final float j = parser(3*5,5-9);";
        StringReader reader = new StringReader(input);
        Lexer lexer = new Lexer(reader);
        Parser parser = new Parser(lexer);
        Program program = parser.getAST();
        assertNotNull(program);
    }

    @Test
    public void testMethodDeclaration() throws IOException {
        String input = "def int add(int a, int b) { return a + b; }";
        StringReader reader = new StringReader(input);
        Lexer lexer = new Lexer(reader);
        Parser parser = new Parser(lexer);
        Program program = parser.getAST();
        assertNotNull(program);
    }
    @Test
    public void testStatement() throws IOException {
        String input = "while(j == 9*7 + point(x)){" +
                            "if(i > 0) { " +
                                "for(k=0,k<6,k=k+1){" +
                                    "return ;}" +
                            "} else {" +
                                "return 2;" +
                            "}" +
                        "}";
        StringReader reader = new StringReader(input);
        Lexer lexer = new Lexer(reader);
        Parser parser = new Parser(lexer);
        Program program = parser.getAST();
        assertNotNull(program);
    }

    @Test
    public void testMethod() throws IOException {
        String input = "struct Object {\n" +
                            "string name;\n" +
                            "Objet location;\n" +
                            "int[] history;\n" +
                        "}";
        StringReader reader = new StringReader(input);
        Lexer lexer = new Lexer(reader);
        Parser parser = new Parser(lexer);
        Program program = parser.getAST();
        assertNotNull(program);
    }

}
