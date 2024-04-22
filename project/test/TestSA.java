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
    public void testTypeError1() throws IOException, SemanticException {
        String input = "int leftSide;" +
                "float rightSide = 3.5;" +
                "leftSide = rightSide;";
        StringReader reader = new StringReader(input);
        Lexer lexer = new Lexer(reader);
        Parser parser = new Parser(lexer);
        Program program = parser.getAST();
        SemanticAnalysis sa = new SemanticAnalysis(program);
        exceptionRule.expect(SemanticException.class);
        exceptionRule.expectMessage("TypeError");
        sa.analyzeNode(program);
    }

    @Test
    public void testStructError1() throws IOException, SemanticException {
        String input = "struct int{" +
                "int large;}";
        StringReader reader = new StringReader(input);
        Lexer lexer = new Lexer(reader);
        Parser parser = new Parser(lexer);
        Program program = parser.getAST();
        SemanticAnalysis sa = new SemanticAnalysis(program);
        exceptionRule.expect(SemanticException.class);
        exceptionRule.expectMessage("StructError");
        sa.analyzeNode(program);
    }

    @Test
    public void testOperatorError1() throws IOException, SemanticException {
        String input = "int a = 6;" +
                "bool boolean = true;" +
                "int b = a + boolean;";
        StringReader reader = new StringReader(input);
        Lexer lexer = new Lexer(reader);
        Parser parser = new Parser(lexer);
        Program program = parser.getAST();
        SemanticAnalysis sa = new SemanticAnalysis(program);
        exceptionRule.expect(SemanticException.class);
        exceptionRule.expectMessage("OperatorError");
        sa.analyzeNode(program);
    }

    @Test
    public void testArgumentError1() throws IOException, SemanticException {
        String input = "def int argumentError1(int argument){" +
                "return argument + 1;" +
                "}" +
                "float b = 3.5;" +
                "argumentError1(b);";
        StringReader reader = new StringReader(input);
        Lexer lexer = new Lexer(reader);
        Parser parser = new Parser(lexer);
        Program program = parser.getAST();
        SemanticAnalysis sa = new SemanticAnalysis(program);
        exceptionRule.expect(SemanticException.class);
        exceptionRule.expectMessage("ArgumentError");
        sa.analyzeNode(program);
    }

    @Test
    public void testMissingConditionError1() throws IOException, SemanticException {
        String input = "int a = 2;" +
                "if(a){" +
                "a=a+1;}";
        StringReader reader = new StringReader(input);
        Lexer lexer = new Lexer(reader);
        Parser parser = new Parser(lexer);
        Program program = parser.getAST();
        SemanticAnalysis sa = new SemanticAnalysis(program);
        exceptionRule.expect(SemanticException.class);
        exceptionRule.expectMessage("MissingConditionError");
        sa.analyzeNode(program);
    }

    @Test
    public void testReturnError1() throws IOException, SemanticException {
        String input = "def int argumentError1(int argument){" +
                "bool boolean = true;" +
                "return boolean;" +
                "}";
        StringReader reader = new StringReader(input);
        Lexer lexer = new Lexer(reader);
        Parser parser = new Parser(lexer);
        Program program = parser.getAST();
        SemanticAnalysis sa = new SemanticAnalysis(program);
        exceptionRule.expect(SemanticException.class);
        exceptionRule.expectMessage("ReturnError");
        sa.analyzeNode(program);
    }

    @Test
    public void testScopeError1() throws IOException, SemanticException {
        String input = "def int argumentError1(int argument){" +
                "bool boolean = true;" +
                "int a = 6;" +
                "return a + 1;" +
                "}" +
                "boolean = false;";
        StringReader reader = new StringReader(input);
        Lexer lexer = new Lexer(reader);
        Parser parser = new Parser(lexer);
        Program program = parser.getAST();
        SemanticAnalysis sa = new SemanticAnalysis(program);
        exceptionRule.expect(SemanticException.class);
        exceptionRule.expectMessage("ScopeError");
        sa.analyzeNode(program);
    }
    @Test
    public void testConstantDeclarationLiteral() throws IOException, SemanticException {
        String input = "final int a = 3;";
        StringReader reader = new StringReader(input);
        Lexer lexer = new Lexer(reader);
        Parser parser = new Parser(lexer);
        Program program = parser.getAST();
        SemanticAnalysis sa = new SemanticAnalysis(program);
        int answer = sa.analyzeNode(program);
        assertEquals(0, answer);
    }
    @Test
    public void testVariableDeclaration() throws IOException, SemanticException {
        String input = "int a = 2;";
        StringReader reader = new StringReader(input);
        Lexer lexer = new Lexer(reader);
        Parser parser = new Parser(lexer);
        Program program = parser.getAST();
        SemanticAnalysis sa = new SemanticAnalysis(program);
        int answer = sa.analyzeNode(program);
        assertEquals(0, answer);
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
        int answer = sa.analyzeNode(program);
        assertEquals(0, answer);
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
        exceptionRule.expect(SemanticException.class);
        exceptionRule.expectMessage("OperatorError");
        sa.analyzeNode(program);
    }

    @Test
    public void testFindReturnTypeFunctionCallFalse2() throws IOException, SemanticException {
        String input = "def float add(float a, float b) { if(b>2.0){return a + b;} }\n" +
                "int b = add(2.3,3.5);";
        StringReader reader = new StringReader(input);
        Lexer lexer = new Lexer(reader);
        Parser parser = new Parser(lexer);
        Program program = parser.getAST();
        SemanticAnalysis sa = new SemanticAnalysis(program);
        exceptionRule.expect(SemanticException.class);
        exceptionRule.expectMessage("TypeError");
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
        int answer = sa.analyzeNode(program);
        assertEquals(0, answer);
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
        int answer = sa.analyzeNode(program);
        assertEquals(0, answer);
    }
    @Test
    public void testConditionStatement1() throws IOException, SemanticException {
        String input = "int a = 5;\n" +
                "int b = 6;" +
                "if(a >= 5){" +
                "if(b<2){" +
                "int c;"+
                "}}" ;
        StringReader reader = new StringReader(input);
        Lexer lexer = new Lexer(reader);
        Parser parser = new Parser(lexer);
        Program program = parser.getAST();
        SemanticAnalysis sa = new SemanticAnalysis(program);
        int answer = sa.analyzeNode(program);
        assertEquals(0, answer);
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
        int answer = sa.analyzeNode(program);
        assertEquals(0, answer);
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
        int answer = sa.analyzeNode(program);
        assertEquals(0, answer);

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
        int answer = sa.analyzeNode(program);
        assertEquals(0, answer);

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
        int answer = sa.analyzeNode(program);
        assertEquals(0, answer);

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
        int answer = sa.analyzeNode(program);
        assertEquals(0, answer);
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
        int answer = sa.analyzeNode(program);
        assertEquals(0, answer);
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
        String input = "//Good luck\n" +
                "\n" +
                "final string message = \"Hello\";\n" +
                "final bool run = true;\n" +
                "\n" +
                "struct Point {\n" +
                "    int x;\n" +
                "    int y;\n" +
                "}\n" +
                "\n" +
                "int a = 3;\n" +
                "\n" +
                "def int square(int v) {\n" +
                "    return v*v;\n" +
                "}\n" +
                "\n" +
                "def void main() {\n" +
                "    int value = readInt();\n" +
                "    Point p = Point(a, a+value);\n" +
                "    writeInt(square(value));\n" +
                "    writeln();\n" +
                "    int i;\n" +
                "    for (i=1, i<a, i = i+1) {\n" +
                "        while (value!=0) {\n" +
                "            if (run){\n" +
                "                value = value - 1;\n" +
                "            } else {\n" +
                "                write(message);\n" +
                "            }\n" +
                "        }\n" +
                "    }\n" +
                "    i = (i+2)*2;\n" +
                "}";
        StringReader reader = new StringReader(input);
        Lexer lexer = new Lexer(reader);
        Parser parser = new Parser(lexer);
        Program program = parser.getAST();
        PrintAST p= new PrintAST(program);
        p.print();
        SemanticAnalysis sa = new SemanticAnalysis(program);
        int answer = sa.analyzeNode(program);
        assertEquals(0, answer);
    }
    @Test
    public void testfree() throws IOException, SemanticException {
        String input = "struct Point{" +
                "int long;}" +
                "Point p = Point(3,true);"+
                "free p;";
        StringReader reader = new StringReader(input);
        Lexer lexer = new Lexer(reader);
        Parser parser = new Parser(lexer);
        Program program = parser.getAST();
        PrintAST p= new PrintAST(program);
        p.print();
        SemanticAnalysis sa = new SemanticAnalysis(program);
        int answer = sa.analyzeNode(program);
        assertEquals(0, answer);
    }

    @Test
    public void testconstant() throws IOException, SemanticException {
        String input = "final int a = 2;" +
                "if(true){" +
                "a= 3;"+
                "}";
        StringReader reader = new StringReader(input);
        Lexer lexer = new Lexer(reader);
        Parser parser = new Parser(lexer);
        Program program = parser.getAST();
        PrintAST p= new PrintAST(program);
        p.print();
        SemanticAnalysis sa = new SemanticAnalysis(program);
        exceptionRule.expect(SemanticException.class);
        exceptionRule.expectMessage("Modification of a constant value");
        int answer = sa.analyzeNode(program);
    }



}
