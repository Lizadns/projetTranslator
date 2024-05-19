package compiler.ByteCode;
import compiler.Parser.*;
import javafx.util.Pair;
import jdk.nashorn.internal.runtime.Scope;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;

import java.util.ArrayList;
import java.util.HashMap;

import static jdk.internal.org.objectweb.asm.Opcodes.*;


public class ByteCodeGeneration {

    private ClassWriter cw;
    private MethodVisitor mv;
    private String className;
    private int variableCounter = 0;
    private boolean topLevel;

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
            if(nodeChildren instanceof IfStatement){
                ifStmt((IfStatement) nodeChildren);
            }
            else if (nodeChildren instanceof WhileStatement){
                whileStmt((WhileStatement) nodeChildren);
            }
            else if (nodeChildren instanceof ForStatement){
                forStmt((ForStatement) nodeChildren);

            }else if (nodeChildren instanceof Free){
                free((Free) nodeChildren);

            }else if(nodeChildren instanceof GlobalDeclaration){
                globalDeclaration((GlobalDeclaration) nodeChildren);

            }else if (nodeChildren instanceof Assignment){
                assignment((Assignment) nodeChildren);

            }else if (nodeChildren instanceof VariableDeclaration){
                varDecl((VariableDeclaration) nodeChildren);

            }else if (nodeChildren instanceof Method){
                funcDecl((Method) nodeChildren);

            }else { //function call
                funCall((FunctionCall) nodeChildren);
            }
        }
        return null;
    }
    private Object ifStmt (IfStatement node)
    {
        MethodVisitor mvs = mv;
        Label endIfLabel = new Label();
        Label endElseLabel = new Label();

        if(node.children.get(0).equals("if")){//c'est le if
            expressionStmt((Expression) node.children.get(1));
            mvs.visitJumpInsn(IFEQ, endIfLabel);//si condition false, va à la fin du if
            block((BlockInstruction) node.children.get(2));
            mvs.visitLabel(endIfLabel);

        }else{//c'est le else
            mvs.visitJumpInsn(IFNE , endElseLabel); //si la condtion est vrai et qu'il y a un else, va à la fin du else
            block((BlockInstruction) node.children.get(2));
            mvs.visitLabel(endElseLabel);

        }
        mv = mvs;
        return null;
    }

    private Object whileStmt (WhileStatement node)
    {
        MethodVisitor mvs = mv;
        Label startLabel = new Label();
        Label endLabel = new Label();

        mvs.visitLabel(startLabel); //ici c le start
        expressionStmt((Expression) node.children.get(0));
        mvs.visitJumpInsn(IFEQ,endLabel); //si condition fausse, saute à la fin du while
        block((BlockInstruction) node.children.get(1));
        mvs.visitJumpInsn(GOTO,startLabel);  //fin du block donc retourne au début de la boucle
        mvs.visitLabel(endLabel);    //marque la fin de la boucle

        mv =mvs;
        return null;
    }

    private Object forStmt (ForStatement node){
        MethodVisitor mvs = mv;
        Label startFor = new Label();
        Label endFor = new Label();

        mvs.visitLabel(startFor);
        expressionStmt((Expression) node.children.get(1));
        mvs.visitJumpInsn(IFEQ,endFor);
        block((BlockInstruction) node.children.get(3));
        mvs.visitJumpInsn(GOTO,startFor);  //fin du block donc retourne au début de la boucle
        mvs.visitLabel(endFor);

        mv=mvs;
        return null;
    }
    private Object declaration(Declaration node){
        for(Node declaration : node.children){
            if(declaration instanceof ConstantDeclaration){
                constantDeclaration((ConstantDeclaration) declaration);

            }else if(declaration instanceof GlobalDeclaration){
                globalDeclaration((GlobalDeclaration) declaration);

            }else if (declaration instanceof StructDeclaration){
                structDecl((StructDeclaration) declaration);

            }
        }
        return null;
    }



    private int registerVariable (Node node, org.objectweb.asm.Type type) {
        int index = variableCounter;
        variableCounter += type.getSize();
        return index;
    }
    private Object varDecl (VariableDeclaration node)
    {
        String typeVariable = node.children.get(0).value;
        org.objectweb.asm.Type type = org.objectweb.asm.Type.getType(node.children.get(0).value);
        int index = registerVariable(node, type);
        //PAS FINI
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
