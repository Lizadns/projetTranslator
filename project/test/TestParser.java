import static org.junit.Assert.assertNotNull;

import compiler.Parser.Parser;
import compiler.Parser.PrintAST;
import compiler.Parser.Program;
import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;
import compiler.Lexer.Lexer;

public class TestParser {

    @Test
    public void testReturnNotInMethod() throws IOException {
        String input = "else{return null;}";
        StringReader reader = new StringReader(input);
        Lexer lexer = new Lexer(reader);
        Parser parser = new Parser(lexer);
        Program program = parser.getAST();
        assertNotNull(program);
    }

    @Test
    public void testReturnInMethod() throws IOException {
        String input = "def int blabla(){ return null;}";
        StringReader reader = new StringReader(input);
        Lexer lexer = new Lexer(reader);
        Parser parser = new Parser(lexer);
        Program program = parser.getAST();
        assertNotNull(program);
    }

    @Test
    public void test3() throws IOException {
        String input = "Point p = Point(3,5,8);";
        StringReader reader = new StringReader(input);
        Lexer lexer = new Lexer(reader);
        Parser parser = new Parser(lexer);
        Program program = parser.getAST();
        PrintAST p= new PrintAST(program);
        p.print();
        assertNotNull(program);
    }
}

