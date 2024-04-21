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
        String input = "def int add(int a, int b) { return a + b; }\n" +
                "int b = add(2,3);";
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
    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();
    @Test
    public void testFindReturnTypeFunctionCallFalse() throws IOException, SemanticException {
        String input = "def float add(float a, float b) { if(b>true){return a + b;} }\n" +
                "float b = add(2.3,3.5);";
        StringReader reader = new StringReader(input);
        Lexer lexer = new Lexer(reader);
        Parser parser = new Parser(lexer);
        Program program = parser.getAST();
        SemanticAnalysis sa = new SemanticAnalysis(program);
        // Spécifie que l'exception SemanticException est attendue
        exceptionRule.expect(SemanticException.class);
        // Spécifie que le message de l'exception doit contenir "TypeError"
        exceptionRule.expectMessage("OperatorError");
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
    @Test
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
    @Test
    public void testArithmetic() throws IOException, SemanticException {
        String input = "int testV = 2 * 86;";
        StringReader reader = new StringReader(input);
        Lexer lexer = new Lexer(reader);
        Parser parser = new Parser(lexer);
        Program program = parser.getAST();
        SemanticAnalysis sa = new SemanticAnalysis(program);
        String answer = sa.analyzeNode(program);
        assertEquals("Everything is OK!", answer);
    }
    @Test
    public void testConditionStatement1() throws IOException, SemanticException {
        String input = "int a = 5;\n" +
                "int b = 6;" +
                "if(a >= 5){" +
                "if(b<2){" +
                "int c;"+
                "}}" +
                "c = 5;";
        StringReader reader = new StringReader(input);
        Lexer lexer = new Lexer(reader);
        Parser parser = new Parser(lexer);
        Program program = parser.getAST();
        SemanticAnalysis sa = new SemanticAnalysis(program);
        String answer = sa.analyzeNode(program);
        assertEquals("Everything is OK!", answer);
    }
    @Test
    public void testConditionStatement2() throws IOException, SemanticException {
        String input = "bool a = 5<8;\n" +
                "if(a){" +
                " a = false;}";
        StringReader reader = new StringReader(input);
        Lexer lexer = new Lexer(reader);
        Parser parser = new Parser(lexer);
        Program program = parser.getAST();
        SemanticAnalysis sa = new SemanticAnalysis(program);
        String answer = sa.analyzeNode(program);
        assertEquals("Everything is OK!", answer);
    }


    @Test
    public void testArrayAndStructAccess() throws IOException, SemanticException {
        String input = "struct Point {\n" +
                "    int x ;\n" +
                "    int y;\n" +
                "}\n" +
                "Point[] ps = Point[5];" +
                "Point e = ps[1];";
        StringReader reader = new StringReader(input);
        Lexer lexer = new Lexer(reader);
        Parser parser = new Parser(lexer);
        Program program = parser.getAST();
        SemanticAnalysis sa = new SemanticAnalysis(program);
        String answer = sa.analyzeNode(program);
        assertEquals("Everything is OK!", answer);

    }
    @Test
    public void testfinal() throws IOException, SemanticException {
        String input = "final int i = 3;\n" +
                "final float j = 3.2*5.0;\n" +
                "final int k = i*6;";
        StringReader reader = new StringReader(input);
        Lexer lexer = new Lexer(reader);
        Parser parser = new Parser(lexer);
        Program program = parser.getAST();
        SemanticAnalysis sa = new SemanticAnalysis(program);
        String answer = sa.analyzeNode(program);
        assertEquals("Everything is OK!", answer);

    }

    @Test
    public void teststructure() throws IOException, SemanticException {
        String input = "struct Point {\n" +
                "    int long;\n" +
                "    int larg;\n" +
                "}\n" +
                "def Point Point(int integer1, int integer2){\n" +
                "    Point p;\n" +
                "    p.long=integer1;\n" +
                "    p.larg=integer2;\n" +
                "    return p;\n" +
                "\n" +
                "}";
        StringReader reader = new StringReader(input);
        Lexer lexer = new Lexer(reader);
        Parser parser = new Parser(lexer);
        Program program = parser.getAST();
        SemanticAnalysis sa = new SemanticAnalysis(program);
        PrintAST p= new PrintAST(program);
        p.print();
        String answer = sa.analyzeNode(program);
        assertEquals("Everything is OK!", answer);

    }

    @Test
    public void testBuiltIn() throws IOException, SemanticException {
        String input = "int a;" +
                "writeInt(a);";
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

    @Test
    public void testBuiltIn2() throws IOException, SemanticException {
        String input = "int conditions = 1;" +
                "if(!(conditions)){" +
                "       }";
        StringReader reader = new StringReader(input);
        Lexer lexer = new Lexer(reader);
        Parser parser = new Parser(lexer);
        Program program = parser.getAST();
        PrintAST p= new PrintAST(program);
        p.print();
        SemanticAnalysis sa = new SemanticAnalysis(program);

        exceptionRule.expect(SemanticException.class);
        exceptionRule.expectMessage("ArgumentError");
        sa.analyzeNode(program);
    }

    @Test
    public void testFor() throws IOException, SemanticException {
        String input = "int a;" +
                "int b;" +
                "for(a=0,a<3,a=a+1){" +
                "}";
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

    @Test
    public void testVariableDeclaration2() throws IOException, SemanticException {
        String input = "Point a;";
        StringReader reader = new StringReader(input);
        Lexer lexer = new Lexer(reader);
        Parser parser = new Parser(lexer);
        Program program = parser.getAST();
        PrintAST p= new PrintAST(program);
        p.print();
        SemanticAnalysis sa = new SemanticAnalysis(program);
        exceptionRule.expect(SemanticException.class);
        exceptionRule.expectMessage("No Declaration of the used Structure");
        sa.analyzeNode(program);
    }

    @Test
    public void testDoubleDeclartion() throws IOException, SemanticException {
        String input = "struct Point{" +
                "int long;}" +
                "Point p = Point(3);";
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
