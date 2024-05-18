package compiler.ByteCode;
import compiler.Parser.*;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class ByteCodeGeneration {



    private Object root (Program node){
        return null;
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

    private Object Stmt(Statement node){
        return null;
    }
    private Object ifStmt (IfStatement node)
    {
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
