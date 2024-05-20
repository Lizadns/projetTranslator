import compiler.ByteCode.ByteCodeGeneration;
import compiler.Lexer.Lexer;
import compiler.Parser.Parser;
import compiler.Parser.Program;
import compiler.SemanticAnalysis.SemanticAnalysis;
import compiler.SemanticAnalysis.SemanticException;
import org.junit.Test;

import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Map;

import static org.junit.Assert.assertNotNull;

public class TestByteCode {
    
    @Test
    public void test() throws IOException, SemanticException {
        Lexer lexer = new Lexer(new FileReader("test/test.lang"));
        Parser parser = new Parser(lexer);
        Program program = parser.getAST();
        SemanticAnalysis sa = new SemanticAnalysis(program);
        sa.analyzeNode(program);
        ByteCodeGeneration cg = new ByteCodeGeneration("test.class",program);
        Map<String, byte[]> test = cg.compile("test.class",program);
    }

}
