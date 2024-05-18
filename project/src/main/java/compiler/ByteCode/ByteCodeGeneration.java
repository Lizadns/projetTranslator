package compiler.ByteCode;
import compiler.Parser.*;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;

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

        for (Node n :node.children) {
            if(n instanceof Declaration){
                declaration((Declaration) n);
            }
            else{
                Stmt((Statement) n);
            }
        }

        cw.visitEnd();
        return cw.toByteArray();
    }

    private Object funcDecl (Method node){
        return null;
    }

    private Object argument(Argument node){
        return null;
    }

    private Object binaryExpression (BinaryExpression node)
    {
        return null;
    }
    private Object binaryOperator (BinaryOperator node)
    {
        return null;
    }

    private Object unaryExpression (UnaryExpression node)
    {
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
