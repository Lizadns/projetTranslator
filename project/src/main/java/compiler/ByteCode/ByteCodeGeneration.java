package compiler.ByteCode;
import compiler.Parser.*;
import javafx.util.Pair;
import jdk.nashorn.internal.runtime.Scope;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.Type;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static jdk.internal.org.objectweb.asm.Opcodes.*;


public class ByteCodeGeneration {

    private ClassWriter cw;
    private MethodVisitor mv;
    private String className;
    private int variableCounter = 0;
    private boolean topLevel;
    private HashMap<String, Pair<Integer,org.objectweb.asm.Type>> variables = new HashMap<>();
    private HashMap<String, String> classVariable = new HashMap<>();

    private ClassWriter struct;
    ArrayList<Pair<String, ClassWriter>> structs = new ArrayList<>();

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
            if(nodeChildren instanceof IfStatement){
                ifStmt((IfStatement) nodeChildren);
            }
            else if (nodeChildren instanceof WhileStatement){
                whileStmt((WhileStatement) nodeChildren);
            }
            else if (nodeChildren instanceof ForStatement){
                forStmt((ForStatement) nodeChildren);

            }else if (nodeChildren instanceof Free) {
                free((Free) nodeChildren);

            }else if(nodeChildren instanceof Method){
                funcDecl((Method) nodeChildren);

            }else if (nodeChildren instanceof FunctionCall){
                funCall((FunctionCall) nodeChildren);

            }else if(nodeChildren instanceof Assignment){
                assignment((Assignment) nodeChildren);

            }else if(nodeChildren instanceof GlobalDeclaration){
                globalDeclaration((GlobalDeclaration) nodeChildren);

            }else if (nodeChildren instanceof VariableDeclaration){
                varDecl((VariableDeclaration) nodeChildren);
            }
        }
        return null;
    }

    private Object ifStmt(IfStatement node) {

        Label endIfLabel = new Label();
        Label endElseLabel = new Label();

        if(node.children.get(0).equals("if")){//c'est le if
            expressionStmt((Expression) node.children.get(1));
            mv.visitJumpInsn(IFEQ, endIfLabel);//si condition false, va à la fin du if
            block((BlockInstruction) node.children.get(2));
            mv.visitLabel(endIfLabel);

        }else{//c'est le else
            mv.visitJumpInsn(IFNE , endElseLabel); //si la condtion est vrai et qu'il y a un else, va à la fin du else
            block((BlockInstruction) node.children.get(2));
            mv.visitLabel(endElseLabel);

        }

        return null;
    }

    private Object whileStmt(WhileStatement node)
    {

        Label startLabel = new Label();
        Label endLabel = new Label();

        mv.visitLabel(startLabel); //ici c le start
        expressionStmt((Expression) node.children.get(0));
        mv.visitJumpInsn(IFEQ,endLabel); //si condition fausse, saute à la fin du while
        block((BlockInstruction) node.children.get(1));
        mv.visitJumpInsn(GOTO,startLabel);  //fin du block donc retourne au début de la boucle
        mv.visitLabel(endLabel);    //marque la fin de la boucle

        return null;
    }

    private Object forStmt(ForStatement node){

        Label startFor = new Label();
        Label endFor = new Label();

        mv.visitLabel(startFor);
        expressionStmt((Expression) node.children.get(1));
        mv.visitJumpInsn(IFEQ,endFor);
        block((BlockInstruction) node.children.get(3));
        mv.visitJumpInsn(GOTO,startFor);  //fin du block donc retourne au début de la boucle
        mv.visitLabel(endFor);

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



    private int registerVariable (String name, org.objectweb.asm.Type type) {
        int index = variableCounter;
        variableCounter += type.getSize();
        variables.put(name,new Pair<>(index, type));
        return index;
    }

    private Object varDecl (VariableDeclaration node)
    {
        String typeVariable = node.children.get(0).value;
        org.objectweb.asm.Type type = org.objectweb.asm.Type.getType(getASMType(node.children.get(0).value));
        registerVariable(node.children.get(1).value, type);
        return null;
    }
    private Object constantDeclaration (ConstantDeclaration node){
        String name = node.children.get(1).value;
        String type = node.children.get(0).children.get(0).value;
        expressionStmt((Expression) node.children.get(2));
        mv.visitFieldInsn(Opcodes.PUTSTATIC , "main", name, getASMType(type));//sais pas si la class est ok
        classVariable.put(name,getASMType(type));//pour savoir que cette variable est une constante

        return null;
    }
    private Object globalDeclaration(GlobalDeclaration node){
        String name = node.children.get(1).value;
        String type = node.children.get(0).children.get(0).value;
        expressionStmt((Expression) node.children.get(2));
        if (topLevel){//si variable de classe
            mv.visitFieldInsn(Opcodes.PUTSTATIC , "main", name, getASMType(type));//sais pas si la class est ok
            classVariable.put(name,getASMType(type));//pour savoir que cette variable est une instance de classe
        }
        else{//si variable locale
            org.objectweb.asm.Type typeASM = org.objectweb.asm.Type.getType(getASMType(type));
            int index = registerVariable(node.children.get(1).value, typeASM);
            mv.visitVarInsn(typeASM.getOpcode(ISTORE), index);
            variables.put(name, new Pair<>(index,typeASM));
        }

        return null;
    }

    private Object newArray(NewArray node){
        return null;
    }

    private Object parameter (Param node) {
        registerVariable(node.children.get(1).value,org.objectweb.asm.Type.getType(getASMType(node.children.get(0).children.get(0).value)));
        return null;
    }

    private Object free(Free node){
        return null;
    }

    public Object assignment (Assignment node)
    {
        Node left = node.children.get(0);

        if(left instanceof Variable){
            if(variables.containsKey(left.children.get(0).value)){//si variable locale
                expressionStmt((Expression) node.children.get(1));
                Pair p = variables.get(left.children.get(0).value);
                org.objectweb.asm.Type type = (org.objectweb.asm.Type) p.getValue();
                int index = (int) p.getKey();
                //convertir les 2 types ???
                mv.visitVarInsn(type.getOpcode(ISTORE), index);
            }else {//variable globale
                // Accéder à la variable globale et la charger sur la pile
                String type = classVariable.get(left.children.get(0).value);
                mv.visitFieldInsn(Opcodes.GETSTATIC, "main",left.children.get(0).value,type);
                expressionStmt((Expression) node.children.get(1));
                mv.visitInsn(Opcodes.IADD); //ajoute la valeur à la variable
                mv.visitFieldInsn(Opcodes.PUTSTATIC, "main",left.children.get(0).value , type);//remet à jour la variablede classe sur la heap
            }


        }else if (left instanceof StructFieldAccess){
            //fieldAccess((StructFieldAccess) left);

        }else if(left instanceof ArrayElementAccess){
            /*
            if(variables.containsKey(left.children.get(0).value)){
                arrayAccess((ArrayElementAccess) left);
                expressionStmt((Expression) left.children.get(1));
                mv.visitInsn(L2I);
                expressionStmt((Expression) node.children.get(1));
                Pair type = variables.get(left.children.get(0).value);
                dup_x2((String) type.getValue());
                mv.visitInsn(nodeAsmType(node).getOpcode(IASTORE); //nodeAsmType(node) ?
            }
             */

        }

        return null;
    }

    private void dup_x1 (String type) {
        if (type.equals("float") || type.equals("int"))
            mv.visitInsn(DUP2_X1);
        else
            mv.visitInsn(DUP_X1);
    }

    private void dup_x2 (String type) {
        if (type.equals("float") || type.equals("int"))
            mv.visitInsn(DUP2_X2);
        else
            mv.visitInsn(DUP_X2);
    }

    private String implicitConversion (String left, String right) {//convertir le droit int en float si gauche est un float
        if (left.equals("float") && right.equals("int")) {
            mv.visitInsn(L2D);
            return left;
        }
        return right;
    }

    private Object structDecl (StructDeclaration node)
    {
        String binaryName = node.children.get(0).value;
        struct = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        struct.visit(V1_8, ACC_PUBLIC, binaryName, null, "java/lang/Object", null);

        for (int i = 1; i < node.children.size(); i++) {
            StructField field = (StructField) node.children.get(i);
            fieldDecl(field);
        }
        // Générer le constructeur
        org.objectweb.asm.Type[] paramTypes = node.children.stream()
                .skip(1)  // Skip the first element
                .map(child -> (StructField) child)
                .map(field -> org.objectweb.asm.Type.getType(getASMType(field.children.get(0).children.get(0).value)))
                .toArray(org.objectweb.asm.Type[]::new);
        String descriptor = Type.getMethodDescriptor(org.objectweb.asm.Type.VOID_TYPE, paramTypes);

        MethodVisitor init = struct.visitMethod(Opcodes.ACC_PUBLIC, "<init>", descriptor, null, null);
        init.visitCode();
        init.visitVarInsn(Opcodes.ALOAD, 0); // this
        init.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);

        int index = 1;
        for (int i = 1; i < node.children.size(); i++) {
            StructField field = (StructField) node.children.get(i);
            init.visitVarInsn(Opcodes.ALOAD, 0);
            org.objectweb.asm.Type type = Type.getType(getASMType(field.children.get(0).children.get(0).value));
            init.visitVarInsn(type.getOpcode(Opcodes.ILOAD), index);
            index += type.getSize();
            init.visitFieldInsn(Opcodes.PUTFIELD, binaryName, field.children.get(1).value, type.getDescriptor());
        }

        init.visitInsn(Opcodes.RETURN);
        init.visitMaxs(-1, -1);
        init.visitEnd();

        struct.visitEnd();
        structs.add(new Pair<>(binaryName, struct));
        struct = null;
        return null;
    }

    private Object fieldDecl (StructField node)
    {
        struct.visitField(ACC_PUBLIC, node.children.get(1).value, getASMType(node.children.get(0).children.get(0).value), null, null);
        return null;
    }

    private Object fieldAccess (StructFieldAccess node) {
        //run(node.stem); c'est une expression mais je comprends pas
        // je ne sais pas comment retrouver le nom de la structure (binaryName) et le type du field (nodeFieldDescriptor)

        //mv.visitFieldInsn(GETFIELD, binaryName, node.children.get(1).value, nodeFieldDescriptor(node));
        return null;
    }

    private Object charAccessInStringArray(CharAccessInStringArray node){
        return null;
    }

    private String getASMType(String type){//jsp comment faire si le type c'est une structure
        switch (type){
            case "int":
                return "I";
            case "bool":
                return "Z";
            case "string":
                return "Ljava/lang/String;";
            case "float":
                return "F";
            case "int[]":
                return "[I";
            case "bool[]":
                return "[Z";
            case "string[]":
                return "[Ljava/lang/String;";
            case "float[]":
                return "[F";
            default:
                throw new IllegalArgumentException("Type non supporté: " + type);
        }
    }







}
