import static org.junit.Assert.assertNotNull;
import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;
import compiler.Lexer.Lexer;

public class TestLexer {
    
    @Test
    public void test() throws IOException {
        String input = "a";
        StringReader reader = new StringReader(input);
        Lexer lexer = new Lexer(reader);
        System.out.println(lexer.getNextSymbol());
        System.out.println(lexer.getNextSymbol());
        System.out.println(lexer.getNextSymbol());
        //assertNotNull(lexer.getNextSymbol());
    }

}
