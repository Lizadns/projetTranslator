import compiler.Lexer.Lexer;
import compiler.Parser.Node;
import compiler.Parser.Parser;
import compiler.Parser.PrintAST;
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
    public void testStructFieldAccess() throws IOException, SemanticException {
        String input = "struct Point {\n" +
                "    int x ;\n" +
                "    int y;\n" +
                "}\n" +
                "Point p;" +
                "p.x = 3;" +
                "int e = p.x;";
        StringReader reader = new StringReader(input);
        Lexer lexer = new Lexer(reader);
        Parser parser = new Parser(lexer);
        Program program = parser.getAST();
        PrintAST p= new PrintAST(program);
        p.print();
        SemanticAnalysis sa = new SemanticAnalysis(program);
        String answer = sa.analyzeNode(program);
        assertEquals("Everything is OK!", answer);

    }





}
