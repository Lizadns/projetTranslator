package compiler.ByteCode;
import com.sun.org.apache.xalan.internal.xsltc.compiler.util.IntType;
import compiler.Parser.*;
import compiler.SemanticAnalysis.SemanticAnalysis;
import compiler.SemanticAnalysis.SemanticException;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.Type;

import java.util.ArrayList;

import static jdk.internal.org.objectweb.asm.Opcodes.IFNE;


public class ByteCodeGeneration {

    private ClassWriter cw;
    private MethodVisitor mv;
    private String className;

    private void compile(){


    }

    private byte[] root (Program node){
        cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        cw.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC, className, null, "java/lang/Object", null);
        mv = cw.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null);
        mv.visitCode();
        for (Node n :node.children) {
            if(n instanceof Declaration){
                declaration((Declaration) n);
            }
            else{
                Stmt((Statement) n);
            }
        }
        mv.visitInsn(Opcodes.RETURN);
        mv.visitEnd();
        mv.visitMaxs(-1, -1);

        cw.visitEnd();
        return cw.toByteArray();
    }

    private Object funcDecl (Method node){
        MethodVisitor surroundingMethod = mv;
        for(Node n : node.children){
            if(n instanceof Param){
                parameter((Param)n);
            }
        }
        String descriptor= getDescriptor(node) ;
        mv = cw.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, node.children.get(0).children.get(0).value, descriptor, null, null);
        mv.visitCode();
        for(Node n : node.children){
            if(n instanceof BlockInstruction){
                block((BlockInstruction) n);
            }
        }
        mv.visitEnd();
        mv.visitMaxs(-1, -1);
        mv= surroundingMethod;
        return null;
    }

    private Object argument(Argument node){
        return null;
    }

    private Object binaryExpression (BinaryExpression node)
    {
        if (isShortCircuit((BinaryOperator) node.children.get(1)))
            return shortCircuit(node);
        expressionStmt((Expression) node.children.get(0));
        // promote int to double for mixed operations
        if (enablesPromotion(node.operator) && left instanceof IntType && right instanceof FloatType)
            mv.visitInsn(Opcodes.I2D);

        expressionStmt((Expression) node.children.get(2));
        switch (node.children.get(1).children.get(0).value) {
            case "+":
                if (left instanceof StringType) {
                    convertToString(right);
                    invokeStatic(method, SighRuntime.class, "concat", String.class, String.class);
                } else if (right instanceof StringType) {
                    // left already converted to string in this case
                    invokeStatic(method, SighRuntime.class, "concat", String.class, String.class);
                } else {
                    numOperation(Opcodes.IADD, Opcodes.DADD, left, right);
                } break;

            case "*":  numOperation(Opcodes.IMUL, Opcodes.DMUL, left, right); break;
            case "/":    numOperation(Opcodes.IDIV, Opcodes.DDIV, left, right); break;
            case "%": numOperation(Opcodes.IREM, Opcodes.DREM, left, right); break;
            case "-":  numOperation(Opcodes.ISUB, Opcodes.DSUB, left, right); break;

            case "==":
                comparison(node.operator, Opcodes.IFEQ, Opcodes.IF_ICMPEQ, Opcodes.IF_ACMPEQ, left, right); break;
            case "!=":
                comparison(node.operator, Opcodes.IFNE, Opcodes.IF_ICMPNE, Opcodes.IF_ACMPNE, left, right); break;
            case ">":
                comparison(node.operator, Opcodes.IFGT, -1, -1, left, right); break;
            case "<":
                comparison(node.operator, Opcodes.IFLT, -1, -1, left, right); break;
            case ">=":
                comparison(node.operator, Opcodes.IFGE, -1, -1, left, right); break;
            case "<=":
                comparison(node.operator, Opcodes.IFLE, -1, -1, left, right); break;
        }
        return null;
    }
    private Object binaryOperator (BinaryOperator node)
    {

        return null;
    }

    private Object shortCircuit (BinaryExpression node)
    {
        int opcode = node.children.get(1).children.get(0).value.equals("&&")? Opcodes.IFEQ /* if 0 */ : /* OR */ IFNE /* if 1 */;
        Label endLabel = new Label();
        expressionStmt((Expression) node.children.get(0));
        mv.visitInsn(Opcodes.DUP);
        mv.visitJumpInsn(opcode, endLabel);
        mv.visitInsn(Opcodes.POP);
        expressionStmt((Expression) node.children.get(2));
        mv.visitLabel(endLabel);
        return null;
    }

    private void numOperation(int longOpcode, int doubleOpcode, Type left, Type right)
    {
        if (left instanceof IntType && right instanceof IntType) {
            mv.visitInsn(longOpcode);
        } else if (left instanceof FloatType && right instanceof FloatType) {
            mv.visitInsn(doubleOpcode);
        } else if (left instanceof FloatType && right instanceof IntType) {
            mv.visitInsn(Opcodes.I2D);
            mv.visitInsn(doubleOpcode);
        } else if (left instanceof IntType && right instanceof FloatType) {
            // in this case, we've added a L2D instruction before the long operand beforehand
            mv.visitInsn(doubleOpcode);
        } else {
            throw new Error("unexpected numeric operation type combination: " + left + ", " + right);
        }
    }

    private boolean isShortCircuit (BinaryOperator op) {
        return op.children.get(0).value.equals("&&") || op.children.get(0).value.equals("||");
    }

    private boolean isArithmetic (BinaryOperator op) {
        return op.children.get(0).value.equals("+") || op.children.get(0).value.equals("*") || op.children.get(0).value.equals("-") || op.children.get(0).value.equals("/") || op.children.get(0).value.equals("%");
    }

    private boolean isComparison (BinaryOperator op) {
        return op.children.get(0).value.equals(">") || op.children.get(0).value.equals(">=") || op.children.get(0).value.equals("<") || op.children.get(0).value.equals("<=");
    }

    private boolean isEquality (BinaryOperator op) {
        return op.children.get(0).value.equals("==") || op.children.get(0).value.equals("!=");
    }


    private Object unaryExpression (UnaryExpression node)
    {
        UnaryOperator operator = (UnaryOperator) node.children.get(0);
        Expression e = (Expression) node.children.get(1);
        Object operandValue =expressionStmt(e);
        String operation= operator.children.get(0).value;
        switch (operation) {
            case "-":
                if (operandValue instanceof Integer) {
                    mv.visitInsn(Opcodes.ILOAD);  // Load an integer
                    mv.visitInsn(Opcodes.ICONST_0);
                    mv.visitInsn(Opcodes.ISUB);
                } else if (operandValue instanceof Double) {
                    mv.visitInsn(Opcodes.DLOAD);
                    mv.visitInsn(Opcodes.DCONST_0);
                    mv.visitInsn(Opcodes.DSUB);
                }
                break;
            case "!":
                if (operandValue instanceof Boolean) {
                    Label falseLabel = new Label();
                    Label endLabel = new Label();
                    //If the value on top of the stack is false (0), the IFEQ, it will jump to the falseLabel where it pushes 1 (true) onto the stack
                    mv.visitJumpInsn(Opcodes.IFEQ, falseLabel);
                    mv.visitInsn(Opcodes.ICONST_0);
                    mv.visitJumpInsn(Opcodes.GOTO, endLabel);
                    mv.visitLabel(falseLabel);
                    mv.visitInsn(Opcodes.ICONST_1);
                    mv.visitLabel(endLabel);
                }
                break;
        }
        return null;
    }

    private Object Literal (Literal node) {
        return null;
    }
    private Object Variable(Variable node){
        return null;
    }
    private Object arrayAccess (ArrayElementAccess node)
    {
        return null;
    }
    private Object arrayAndstructAccess (ArrayAndStructAccess node){
        return null;
    }
    private Object funCall (FunctionCall node) {
        return null;
    }
    private Object expressionStmt (Expression node) {
        return null;
    }
    private Object returnStmt (ReturnStatement node) {
        if(node.children.get(0)==null){
            mv.visitInsn(Opcodes.RETURN); //return void
        }
        else{
            expressionStmt((Expression)node.children.get(0));

        }
        return null;
    }
    private Object block (BlockInstruction node) {
        return null;
    }

//_____________________________________________________________________________________________

    private Object Stmt(Statement node){
        ArrayList<Node> StatementNodes = node.children;
        for (Node nodeChildren : StatementNodes){

        }
        return null;
    }
    private Object ifStmt (IfStatement node)
    {
        //checker la condition
        expressionStmt((Expression) node.children.get(1));
        MethodVisitor mv = method;

        Label trueLabel = new Label();
        mv.visitJumpInsn(IFNE, trueLabel);

        Label falseLabel = new Label();
        return null;
    }

    private Object whileStmt (WhileStatement node)
    {
        return null;
    }

    private Object forStmt (ForStatement node){
        return null;
    }
    private Object declaration(Declaration node){
        return null;
    }
    private Object varDecl (VariableDeclaration node)
    {
        return null;
    }
    private Object constantDeclaration (ConstantDeclaration node){
        return null;
    }
    private Object globalDeclaration(GlobalDeclaration node){
        return null;
    }

    private Object newArray(NewArray node){
        return null;
    }
    private Object parameter (Param node) {
        return null;
    }

    private Object free(Free node){
        return null;
    }

    public Object assignment (Assignment node)
    {
        return null;
    }

    private Object structDecl (StructDeclaration node)
    {
        return null;
    }

    private Object fieldDecl (StructField node)
    {
        return null;
    }

    private Object fieldAccess (StructFieldAccess node) {
        return null;
    }

    private Object charAccessInStringArray(CharAccessInStringArray node){
        return null;
    }







}
