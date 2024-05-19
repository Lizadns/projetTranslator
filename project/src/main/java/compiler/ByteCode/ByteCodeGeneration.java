package compiler.ByteCode;
import compiler.Parser.*;
import compiler.SemanticAnalysis.SemanticException;
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
    private String[] builtInProcedures = {"readInt", "readFloat", "readString", "writeInt", "writeFloat", "write", "writeln","len","floor","chr"};

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
        int surroundingVariableCounter = variableCounter;
        MethodVisitor surroundingMethod = mv;
        boolean surrondingIsTopLevel = topLevel;
        variableCounter=0;
        topLevel= false;

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
        variableCounter= surroundingVariableCounter;
        topLevel= surrondingIsTopLevel;
        return null;
    }

    private String getDescriptor(Method node) {
        StringBuilder descriptor = new StringBuilder();
        descriptor.append("(");

        for (Node child : node.children) {
            if (child instanceof Param) {
                Param p = (Param) child;
                descriptor.append(getTypeDescriptor(p.children.get(0).children.get(0).value));
            }
        }

        descriptor.append(")");

        descriptor.append(getTypeDescriptor(node.children.get(1).children.get(0).value));

        return descriptor.toString();
    }

    private String getTypeDescriptor(String type) {
        switch (type) {
            case "int":
                return "I";
            case "bool":
                return "Z";
            case "float":
                return "F";
            case "void":
                return "V";
            case "string":
                return "Ljava/lang/String;";
            default:
                // if arrays
                if (type.endsWith("[]")) {
                    return "[" + getTypeDescriptor(type.substring(0, type.length() - 2));
                }
                // if other type like a structure that is in another class
                return "L" + type.replace('.', '/') + ";";
        }
    }

    private Object argument(Argument node){
        return null;
    }

    private Object binaryExpression (BinaryExpression node)
    {
        if (isShortCircuit((BinaryOperator) node.children.get(1)))
            return shortCircuit(node);
        expressionStmt((Expression) node.children.get(0));
        // promote int to float for mixed operations
        Type left = null;
        Type right = null; // initialiser


        expressionStmt((Expression) node.children.get(2));
        switch (node.children.get(1).children.get(0).value) {
            case "+":
                if (getType(node.children.get(0)).equal("string")) { //voir si left == string
                    mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "concat", "(Ljava/lang/String;)Ljava/lang/String;", false);
                } else {
                    numOperation(Opcodes.IADD, Opcodes.FADD, left, right);
                } break;

            case "*":  numOperation(Opcodes.IMUL, Opcodes.FMUL, left, right); break;
            case "/":    numOperation(Opcodes.IDIV, Opcodes.FDIV, left, right); break;
            case "%": numOperation(Opcodes.IREM, Opcodes.IREM, left, right); break;
            case "-":  numOperation(Opcodes.ISUB, Opcodes.FSUB, left, right); break;

            case "==":
                comparison((BinaryOperator) node.children.get(1), Opcodes.IFEQ, Opcodes.IF_ICMPEQ, Opcodes.IF_ACMPEQ, left, right); break;
            case "!=":
                comparison((BinaryOperator) node.children.get(1), Opcodes.IFNE, Opcodes.IF_ICMPNE, Opcodes.IF_ACMPNE, left, right); break;
            case ">":
                comparison((BinaryOperator)node.children.get(1), Opcodes.IFGT, -1, -1, left, right); break;
            case "<":
                comparison((BinaryOperator)node.children.get(1), Opcodes.IFLT, -1, -1, left, right); break;
            case ">=":
                comparison((BinaryOperator) node.children.get(1), Opcodes.IFGE, -1, -1, left, right); break;
            case "<=":
                comparison((BinaryOperator) node.children.get(1), Opcodes.IFLE, -1, -1, left, right); break;
        }
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

    private void comparison (BinaryOperator op, int doubleWidthOpcode, int boolOpcode, int objOpcode,  Type left, Type right) {
        int l = left.getSort();
        int r = right.getSort();
        Label trueLabel = new Label();
        Label endLabel = new Label();

        if (l== Type.INT && r == Type.INT) {
            mv.visitInsn(LCMP);
            mv.visitJumpInsn(doubleWidthOpcode, trueLabel);
        } else if ((l == Type.FLOAT || l== Type.INT) && r == Type.FLOAT) {
            // If left is an Int, we've added a L2D instruction before the long operand beforehand
            // Proper NaN handling: if NaN is involved, has to be false for all operations.
            int opcode = op.children.get(0).value.equals("<") || op.children.get(0).value.equals("<=")  ? DCMPG : DCMPL;
            mv.visitInsn(opcode);
            mv.visitJumpInsn(doubleWidthOpcode, trueLabel);
        } else if (l == Type.FLOAT && r == Type.INT) {
            mv.visitInsn(L2D);
            // Proper NaN handling: if NaN is involved, has to be false for all operations.
            int opcode = op.children.get(0).value.equals("<") || op.children.get(0).value.equals("<=")  ? DCMPG : DCMPL;
            mv.visitInsn(opcode);
            mv.visitJumpInsn(doubleWidthOpcode, trueLabel);
        } else if (l == Type.BOOLEAN && r == Type.BOOLEAN) {
            mv.visitJumpInsn(boolOpcode, trueLabel);
        } else {
            mv.visitJumpInsn(objOpcode, trueLabel);
        }

        mv.visitInsn(ICONST_0);
        mv.visitJumpInsn(GOTO, endLabel);
        mv.visitLabel(trueLabel);
        mv.visitInsn(ICONST_1);
        mv.visitLabel(endLabel);
    }

    private void numOperation(int intOpcode, int floatOpcode, Type left, Type right)
    {
        int l = left.getSort();
        int r = right.getSort();
        if (l== Type.INT && r == Type.INT) {
            mv.visitInsn(intOpcode);
        } else if (l == Type.FLOAT && r == Type.FLOAT) {
            mv.visitInsn(floatOpcode);
        } else if (l == Type.FLOAT && r == Type.INT) {
            mv.visitInsn(Opcodes.I2D);
            mv.visitInsn(floatOpcode);
        } else if (l == Type.INT && r == Type.FLOAT) {
            // in this case, we've added a L2D instruction before the long operand beforehand
            mv.visitInsn(floatOpcode);
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
                //pas sur de comment load
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
        // instruction to load constants directly onto the stack
        String literal = node.children.get(0).value;
        if (literal.equals("true")) {
            mv.visitLdcInsn(true);
        } else if (literal.equals("false")) {
            mv.visitLdcInsn(false);
        }else if(literal.toCharArray()[0] == '\"'){
            String a = literal.substring(1,literal.length()-1);
            mv.visitLdcInsn(a);
        }else if(literal.contains(".")){
            Float a =  Float.parseFloat(literal);
            mv.visitLdcInsn(a);
        }else {
            Integer a = Integer.parseInt(literal);
            mv.visitLdcInsn(a);
        }
        return null;
    }
    private Object variable(Variable node) {
        String varName = node.children.get(0).value; // Assuming the variable name is stored in the first child
        Type type = variables.get(varName).getValue(); // You would need a method to determine the variable's type

        // Load the variable based on its type
        switch (type.getSort()) {
            case org.objectweb.asm.Type.INT:
                loadIntVariable(varName);
                break;
            case  org.objectweb.asm.Type.FLOAT:
                loadFloatVariable(varName);
                break;
            case Type.BOOLEAN:
                loadBooleanVariable(varName);
                break;
            case Type.CHAR: //string??
            case Type.OBJECT: // Structures??
                loadReferenceVariable(varName);
                break;
        }
        return null;
    }

    private void loadIntVariable(String varName) {
        int index = getLocalVariableIndex(varName);
        mv.visitVarInsn(Opcodes.ILOAD, index);
    }

    private void loadFloatVariable(String varName) {
        int index = getLocalVariableIndex(varName);
        mv.visitVarInsn(Opcodes.FLOAD, index);
    }

    private void loadBooleanVariable(String varName) {
        int index = getLocalVariableIndex(varName);
        mv.visitVarInsn(Opcodes.ILOAD, index); // Booleans are handled as integers in the JVM
    }

    private void loadReferenceVariable(String varName) {
        int index = getLocalVariableIndex(varName);
        mv.visitVarInsn(Opcodes.ALOAD, index);
    }

    private int getLocalVariableIndex(String varName) {
        return variables.get(varName).getKey();
    }
    private Object arrayAccess(ArrayElementAccess node) {
        String arrayName = ((Leaf)node.children.get(0)).value; // This retrieves the array name
        Expression indexExpression = (Expression)node.children.get(1); // This retrieves the index expression

        // Load the array onto the stack
        // Assuming arrayName corresponds to a local variable index or field
        loadArrayReference(arrayName);

        // Evaluate the index expression and load the index onto the stack
        expressionStmt(indexExpression);

        // Perform the array load operation based on the array type
        loadArrayElement();

        return null;
    }

    private void loadArrayReference(String arrayName) {
        // Assuming local variable; modify if stored differently (e.g., as a field)
        int arrayVarIndex = getLocalVariableIndex(arrayName);
        mv.visitVarInsn(Opcodes.ALOAD, arrayVarIndex); // Load the array reference from a local variable
    }

    private void loadArrayElement() {
        // You need to know the type of the array elements to choose the correct IALOAD, BALOAD, CALOAD, SALOAD, AALOAD, FALOAD, DALOAD, or LALOAD
        // Example using IALOAD for an integer array
        //voir le type de l'array
        mv.visitInsn(Opcodes.IALOAD);
    }
    private Object arrayAndstructAccess (ArrayAndStructAccess node){
        return null;
    }
    private Object funCall (FunctionCall node) {
        String functionName = node.children.get(0).value;
        String descriptor = getFunctionDescriptor(node);
        for(int i = 1; i< node.children.size(); i++){
            //pushes the result of expressions onto the stack
            expressionStmt((Expression) node.children.get(i).children.get(0));
        }
        for(String str : builtInProcedures){
            if(str.equals(functionName)){
                //faire quelque chose pour les functions buildin
                return null;
            }
        }
        //j'avais oublié que ça pouvait aussi être une initialisation d'une structure oupssi
        mv.visitMethodInsn(INVOKESTATIC, className, functionName, descriptor,false);
        return null;
    }
    private String getFunctionDescriptor(FunctionCall node) {
        StringBuilder descriptor = new StringBuilder();
        descriptor.append("(");

        for (Node child : node.children) {
            if (child instanceof Argument) {
                Argument a = (Argument) child;
                descriptor.append(getTypeDescriptor(a.children.get(1).children.get(0).value));
            }
        }

        descriptor.append(")");

        descriptor.append(getTypeDescriptor(node.children.get(node.children.size()-1).children.get(0).value));

        return descriptor.toString();
    }
    private Object expressionStmt (Expression node) {
        Node n = node.children.get(0);
        if(n instanceof Variable){
            variable((Variable) n);
        }
        else if (n instanceof BinaryExpression){
            binaryExpression((BinaryExpression) n);
        }else if(n instanceof UnaryExpression){
            unaryExpression((UnaryExpression) n);
        }else if (n instanceof Expression){
            expressionStmt((Expression) n);
        }else if(n instanceof Literal){
            Literal((Literal) n);
        }else if(n instanceof ArrayAndStructAccess){
            arrayAndstructAccess((ArrayAndStructAccess) n);
        }else if (n instanceof ArrayElementAccess){
            arrayAccess((ArrayElementAccess) n);
        }else if(n instanceof FunctionCall){
            funCall((FunctionCall) n);
        }
        else if(n instanceof StructFieldAccess){
            fieldAccess((StructFieldAccess) n);
        }
        return null;
    }
    private Object returnStmt (ReturnStatement node) {
        if(node.children.get(0)==null){
            mv.visitInsn(Opcodes.RETURN); //return void
        }
        else{
            expressionStmt((Expression)node.children.get(0));
            //mv.visitInsn();

        }
        return null;
    }
    private Object block (BlockInstruction node) {
        ArrayList<Node> BlockIntructions = node.children;
        for (Node nodeChildren : BlockIntructions){
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
            }
            else if( nodeChildren instanceof FunctionCall){
                funCall((FunctionCall) nodeChildren);
            }else if(nodeChildren instanceof ReturnStatement){
                returnStmt((ReturnStatement) nodeChildren);
            }else if(nodeChildren instanceof Assignment){
                assignment((Assignment) nodeChildren);
            }else if (nodeChildren instanceof VariableDeclaration){
                varDecl((VariableDeclaration) nodeChildren);
            }else if (nodeChildren instanceof GlobalDeclaration){
                globalDeclaration((GlobalDeclaration) nodeChildren);
            }
        }
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
            }
        }
        return null;
    }

    private Object ifStmt(IfStatement node) {
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

    private Object whileStmt(WhileStatement node)
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

    private Object forStmt(ForStatement node){
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



    private int registerVariable (String name, org.objectweb.asm.Type type) {
        int index = variableCounter;
        variableCounter += type.getSize();
        variables.put(name,new Pair<>(index, type));
        return index;
    }
    private Object varDecl (VariableDeclaration node)
    {
        String typeVariable = node.children.get(0).value;
        org.objectweb.asm.Type type = org.objectweb.asm.Type.getType(node.children.get(0).value);
        registerVariable(node.children.get(1).value, type);
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
        Node left = node.children.get(0);
        if(left instanceof Variable){
            expressionStmt((Expression) node.children.get(0));
            Pair p = variables.get(left.children.get(0).value);
            org.objectweb.asm.Type type = (org.objectweb.asm.Type) p.getValue();
            int index = (int) p.getKey();
            //convertir les 2 types ???
            mv.visitVarInsn(type.getOpcode(ISTORE), index);

        }else if (left instanceof StructFieldAccess){

        }else if(left instanceof ArrayElementAccess){

        }

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
