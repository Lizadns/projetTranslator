package compiler.ByteCode;
import compiler.Parser.*;
import compiler.SemanticAnalysis.SemanticException;
import jdk.nashorn.internal.runtime.Scope;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.Type;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static org.objectweb.asm.Opcodes.*;


public class ByteCodeGeneration {

    private ClassWriter cw;
    private Label start;
    private Label end;
    private MethodVisitor mv;
    private String className;
    private Program root;
    private int variableCounter = 0;
    private boolean topLevel;
    private boolean isInitialise;
    private HashMap<String,Integer> variables = new HashMap<>();
    private HashMap<String, String> classVariable = new HashMap<>();

    private ClassWriter struct;
    HashMap<String, ClassWriter> structs = new HashMap<>();
    private String[] builtInProcedures = {"readInt", "readFloat", "readString", "writeInt", "writeFloat", "write", "writeln","len","floor","chr"};

    public ByteCodeGeneration(String className,Program root){
        this.className = className;

    }

    public Map<String, byte[]> compile(String binaryName, Node root) {
        this.className = binaryName.replace(".class", "");
        root((Program) root);
        Map<String, byte[]> compiledClasses = new HashMap<>();
        compiledClasses.put(className, cw.toByteArray());

        Map<String, byte[]> structClasses = new HashMap<>();
        for (Map.Entry<String, ClassWriter> entry : structs.entrySet()) {
            String className = entry.getKey();
            ClassWriter cw = entry.getValue();
            byte[] bytecode = cw.toByteArray();  // Compile the ClassWriter to bytecode
            structClasses.put(className, bytecode);  // Store in the new map
        }
        compiledClasses.putAll(structClasses);

        // Write the main class bytecode
        writeFile(binaryName, compiledClasses.get(className));



        // Write each struct class bytecode
        for (Map.Entry<String, byte[]> entry : structClasses.entrySet()) {
            String structClassName = entry.getKey().replace("/", ".") + ".class";
            structClassName = binaryName.substring(0,binaryName.lastIndexOf("/")+1)+structClassName;
            writeFile(structClassName, entry.getValue());
        }

        return compiledClasses;
    }

    private void writeFile(String filename, byte[] data) {
        try (FileOutputStream fos = new FileOutputStream(filename)) {
            fos.write(data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private byte[] root (Program node){
        cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        cw.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC, className, null, "java/lang/Object", null);
        mv = cw.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null);
        mv.visitCode();
        topLevel = true;
        start = new Label();
        end = new Label();
        mv.visitLabel(start);
        for (Node n :node.children) {
            if(n instanceof Declaration){
                declaration((Declaration) n);
            }
            else{
                Stmt((Statement) n);
            }
        }
        mv.visitInsn(Opcodes.RETURN);
        mv.visitLabel(end);
        mv.visitEnd();
        mv.visitMaxs(-1, -1);

        cw.visitEnd();
        return cw.toByteArray();
    }

    private Object funcDecl (Method node){
        Label ancienstart =start;
        Label ancienend = end;
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
        start = new Label();
        end = new Label();
        mv.visitLabel(start);
        for(Node n : node.children){
            if(n instanceof BlockInstruction){
                block((BlockInstruction) n);
            }
        }
        mv.visitLabel(end);
        mv.visitEnd();
        mv.visitMaxs(-1, -1);
        mv= surroundingMethod;
        variableCounter= surroundingVariableCounter;
        topLevel= surrondingIsTopLevel;
        start = ancienstart;
        end = ancienend;
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
        String left = getType((Expression)node.children.get(0));
        String right = getType((Expression)node.children.get(2));


        expressionStmt((Expression) node.children.get(2));
        switch (node.children.get(1).children.get(0).value) {
            case "+":
                if (left.equals("string")) {
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

    private void comparison (BinaryOperator op, int doubleWidthOpcode, int boolOpcode, int objOpcode,  String left, String right) {
        Label trueLabel = new Label();
        Label endLabel = new Label();

        if (left.equals("int") && right.equals("int")) {
            mv.visitInsn(LCMP);
            mv.visitJumpInsn(doubleWidthOpcode, trueLabel);
        } else if ((left.equals("float")|| left.equals("int")) && right.equals("float")) {
            // If left is an Int, we've added a L2D instruction before the long operand beforehand
            // Proper NaN handling: if NaN is involved, has to be false for all operations.
            int opcode = op.children.get(0).value.equals("<") || op.children.get(0).value.equals("<=")  ? FCMPG : FCMPL;
            mv.visitInsn(opcode);
            mv.visitJumpInsn(doubleWidthOpcode, trueLabel);
        } else if (left.equals("float") && right.equals("int")) {
            mv.visitInsn(L2F);
            // Proper NaN handling: if NaN is involved, has to be false for all operations.
            int opcode = op.children.get(0).value.equals("<") || op.children.get(0).value.equals("<=")  ? FCMPG : FCMPL;
            mv.visitInsn(opcode);
            mv.visitJumpInsn(doubleWidthOpcode, trueLabel);
        } else if (left.equals("bool") && right.equals("bool")) {
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

    private void numOperation(int intOpcode, int floatOpcode, String left, String right)
    {
        if (left.equals("int")  &&  right.equals("int")) {
            mv.visitInsn(intOpcode);
        } else if (left.equals("float") &&right.equals("float")) {
            mv.visitInsn(floatOpcode);
        } else if (left.equals("float") && right.equals("int")) {
            mv.visitInsn(Opcodes.I2D);
            mv.visitInsn(floatOpcode);
        } else if (left.equals("int")  && right.equals("float")) {
            // in this case, we've added a L2D instruction before the long operand beforehand
            mv.visitInsn(floatOpcode);
        } else {
            throw new Error("unexpected numeric operation type combination: " + left + ", " + right);
        }
    }

    private boolean isShortCircuit (BinaryOperator op) {
        return op.children.get(0).value.equals("&&") || op.children.get(0).value.equals("||");
    }

    private boolean enablesPromotion (BinaryOperator op) {
        return isArithmetic(op) || isComparison(op) || isEquality(op);
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
        expressionStmt(e);
        String type =getType(e);
        String operation= operator.children.get(0).value;
        switch (operation) {
            case "-":
                //pas sur de comment load
                if (type.equals("int")) {
                    mv.visitInsn(Opcodes.ILOAD);  // Load an integer
                    mv.visitInsn(Opcodes.ICONST_0);
                    mv.visitInsn(Opcodes.ISUB);
                } else if (type.equals("float")) {
                    mv.visitInsn(Opcodes.FLOAD);
                    mv.visitInsn(Opcodes.FCONST_0);
                    mv.visitInsn(Opcodes.FSUB);
                }
                break;
            case "!":
                if (type.equals("bool")) {
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
            if(isInitialise){
                return true;
            }
            mv.visitLdcInsn(true);
        } else if (literal.equals("false")) {
            if(isInitialise){
                return false;
            }
            mv.visitLdcInsn(false);
        }else if(literal.toCharArray()[0] == '\"'){
            String a = literal.substring(1,literal.length()-1);
            if(isInitialise){
                return a;
            }
            mv.visitLdcInsn(a);
        }else if(literal.contains(".")){
            Float a =  Float.parseFloat(literal);
            if(isInitialise){
                return a;
            }
            mv.visitLdcInsn(a);
        }else {
            Integer a = Integer.parseInt(literal);
            if(isInitialise){
                return a;
            }
            mv.visitLdcInsn(a);
        }
        return null;
    }
    private Object variable(Variable node) {
        String varName = node.children.get(0).value; // Assuming the variable name is stored in the first child
        // You would need a method to determine the variable's type
        String type = "int";
        if(node.children.size()==1){

        }else{
            compiler.Parser.Type t = (compiler.Parser.Type) node.children.get(1);
            type = t.children.get(0).value;
        }
        // Load the variable based on its type
        switch (type) {
            case "int":
                loadIntVariable(varName);
                break;
            case  "float":
                loadFloatVariable(varName);
                break;
            case "bool":
                loadBooleanVariable(varName);
                break;
            case "string":
                loadStringVariable(varName);
                break;
            default: // Structures??
                loadReferenceVariable(varName);
                break;
        }
        return null;
    }

    private void loadIntVariable(String varName) {
        if(topLevel){
            //cw.visitField(GETSTATIC, varName, "I",null,null);
            mv.visitFieldInsn(GETSTATIC, className, varName, "I");
        }
        else {
            if(variables.containsKey(varName)) {
                int index = getLocalVariableIndex(varName);
                mv.visitVarInsn(Opcodes.ILOAD, index);
            }else{
                mv.visitFieldInsn(GETSTATIC, className, varName, "I");
            }
        }
    }

    private void loadFloatVariable(String varName) {
        if(topLevel){
            //cw.visitField(GETSTATIC, varName, "F",null,null);
            mv.visitFieldInsn(GETSTATIC, className, varName, "F");
        }
        else {
            if(variables.containsKey(varName)) {
                int index = getLocalVariableIndex(varName);
                mv.visitVarInsn(Opcodes.FLOAD, index);
            }else{
                mv.visitFieldInsn(GETSTATIC, className, varName, "F");
            }
        }
    }

    private void loadBooleanVariable(String varName) {
        if(topLevel){
            //cw.visitField(GETSTATIC, varName, "I",null,null);
            mv.visitFieldInsn(GETSTATIC, className, varName, "I");
        }
        else {
            if(variables.containsKey(varName)) {
                int index = getLocalVariableIndex(varName);
                mv.visitVarInsn(Opcodes.ILOAD, index);
            }else{
                mv.visitFieldInsn(GETSTATIC, className, varName, "I");
            }
        }// Booleans are handled as integers in the JVM
    }

    private void loadReferenceVariable(String varName) {
        if(topLevel){
            //cw.visitField(GETSTATIC, varName, "A",null,null);
            mv.visitFieldInsn(GETSTATIC, className, varName, "A");
        }
        else {
            if(variables.containsKey(varName)) {
                int index = getLocalVariableIndex(varName);
                mv.visitVarInsn(Opcodes.ALOAD, index);
            }else{
                mv.visitFieldInsn(GETSTATIC, className, varName, "A");
            }
        }
    }
    private void loadStringVariable(String varName) {
        if(topLevel){
            //cw.visitField(GETSTATIC, varName, "Ljava/lang/String;",null,null);
            mv.visitFieldInsn(GETSTATIC, className, varName, "Ljava/lang/String;");
        }
        else {
            if(variables.containsKey(varName)) {
                int index = getLocalVariableIndex(varName);
                mv.visitVarInsn(Opcodes.ALOAD, index);
            }else{
                mv.visitFieldInsn(GETSTATIC, className, varName, "Ljava/lang/String;");
            }
        }
    }

    private int getLocalVariableIndex(String varName) {
        return variables.get(varName);
    }
    private Object arrayAccess(ArrayElementAccess node) {
        String arrayName = ((Leaf)node.children.get(0)).value; // This retrieves the array name
        Expression indexExpression = (Expression)node.children.get(1); // This retrieves the index expression

        // Load the array onto the stack
        // Assuming arrayName corresponds to a local variable index or field
        loadReference(arrayName);

        // Evaluate the index expression and load the index onto the stack
        expressionStmt(indexExpression);
        String type = node.children.get(2).children.get(0).value;
        // Perform the array load operation based on the array type
        loadArrayElement(type);

        return null;
    }

    private void loadReference(String arrayName) {
        // Assuming local variable; modify if stored differently (e.g., as a field)
        int arrayVarIndex = getLocalVariableIndex(arrayName);
        mv.visitVarInsn(Opcodes.ALOAD, arrayVarIndex); // Load the array reference from a local variable
    }

    private void loadArrayElement(String type) {
        // You need to know the type of the array elements to choose the correct IALOAD, BALOAD, CALOAD, SALOAD, AALOAD, FALOAD, DALOAD, or LALOAD
        // Example using IALOAD for an integer array
        //voir le type de l'array
        if(type.equals("int")){
            mv.visitInsn(Opcodes.IALOAD);
        }else if(type.equals("bool")){
            mv.visitInsn(Opcodes.BALOAD);
        }else if(type.equals("float")){
            mv.visitInsn(Opcodes.FALOAD);
        }else { //string or struct
            mv.visitInsn(Opcodes.AALOAD);
        }
    }
    private Object arrayAndstructAccess (ArrayAndStructAccess node){
        String arrayName = ((Leaf)node.children.get(0)).value; // This could be an array or object name
        Expression indexExpression = (Expression)node.children.get(1); // This is used as an index or could be an expression for further field access
        String fieldName = ((Leaf)node.children.get(2)).value; // This could be an array index as a string or a field name

        // Load the array or object reference onto the stack
        // Assuming we know how to get the reference (local var, field, etc.)
        loadReference(arrayName);

        expressionStmt(indexExpression);
        // Determine if the operation is on an array or a structure
        mv.visitInsn(Opcodes.AALOAD);

        // Now the structure is on top of the stack; access its field
        String fieldDescriptor = getFieldDescriptor(arrayName, fieldName); // You need to define how to get this
        mv.visitFieldInsn(Opcodes.GETFIELD, getTypeInternalName(arrayName), fieldName, fieldDescriptor);

        return null;
    }
    private String getTypeInternalName(String arrayName) {
        // You need to determine the internal JVM name for the structure's class
        // Example: if your structures are instances of a class 'MyStructure', this name would be something like 'com/myapp/MyStructure'
        return "com/myapp/MyStructure";  // Adjust this method to fetch actual type name
    }

    private String getFieldDescriptor(String arrayName, String fieldName) {
        // Return the field descriptor based on the actual type of the field
        // Example: for integer fields, it's "I"; for object references, it's "Lcom/myapp/MyObject;"
        // This could be more dynamic depending on how you manage type information
        return "I";  // Example for an integer field; adjust as needed
    }

    private Object funCall (FunctionCall node) {

        String functionName = node.children.get(0).value;
        for(int i = 1; i< node.children.size(); i++){
            //pushes the result of expressions onto the stack
            if(node.children.get(i) instanceof Argument) {
                expressionStmt((Expression) node.children.get(i).children.get(0));
            }
        }
        for(String str : builtInProcedures){
            if(str.equals(functionName)){
                //faire quelque chose pour les functions buildin
                handleBuiltInFunction(functionName, node);
                return null;
            }
        }
        if(structs.containsKey(functionName)){
            //initialisation structure
            return null;
        }
        String descriptor = getFunctionDescriptor(node);
        //j'avais oublié que ça pouvait aussi être une initialisation d'une structure oupssi
        mv.visitMethodInsn(INVOKESTATIC, className, functionName, descriptor,false);
        return null;
    }

    private void handleBuiltInFunction(String functionName, FunctionCall node) {
        switch (functionName) {
            case "readInt":
                readInt();
                break;
            case "readFloat":
                readFloat();
                break;
            case "readString":
                readString();
                break;
            case "writeInt":
            case "writeFloat":
            case "write":
            case "writeln":
                write(node, functionName);
                break;
            case "len":
                len(node);
                break;
            case "floor":
                floor(node);
                break;
            case "chr":
                chr();
                break;
            default:
                throw new IllegalArgumentException("Unsupported function: " + functionName);
        }
    }

    private void readInt() {
        mv.visitTypeInsn(NEW, "java/util/Scanner");
        mv.visitInsn(DUP);
        mv.visitFieldInsn(GETSTATIC, "java/lang/System", "in", "Ljava/io/InputStream;");
        mv.visitMethodInsn(INVOKESPECIAL, "java/util/Scanner", "<init>", "(Ljava/io/InputStream;)V", false);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/util/Scanner", "nextInt", "()I", false);
    }
    private void readFloat() {
        mv.visitTypeInsn(NEW, "java/util/Scanner");
        mv.visitInsn(DUP);
        mv.visitFieldInsn(GETSTATIC, "java/lang/System", "in", "Ljava/io/InputStream;");
        mv.visitMethodInsn(INVOKESPECIAL, "java/util/Scanner", "<init>", "(Ljava/io/InputStream;)V", false);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/util/Scanner", "nextFloat", "()F", false);
    }
    private void readString() {
        mv.visitTypeInsn(NEW, "java/util/Scanner");
        mv.visitInsn(DUP);
        mv.visitFieldInsn(GETSTATIC, "java/lang/System", "in", "Ljava/io/InputStream;");
        mv.visitMethodInsn(INVOKESPECIAL, "java/util/Scanner", "<init>", "(Ljava/io/InputStream;)V", false);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/util/Scanner", "nextLine", "()Ljava/lang/String;", false);
    }

    private void write(FunctionCall node, String functionName) {
        // Assume node.children.get(1) holds the value to write
        mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
        mv.visitInsn(SWAP);  // Swap to match the expected order for print methods
        String descriptor = functionName.equals("writeln") ? "(Ljava/lang/Object;)V" : "(I)V"; // Simplified
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", functionName.equals("writeln") ? "println" : "print", descriptor, false);
    }
    private void floor(FunctionCall node) {
        mv.visitMethodInsn(INVOKESTATIC, "java/lang/Math", "floor", "(F)I", false);
    }
    private void len(FunctionCall node){
        String type = getType((Expression) node.children.get(1).children.get(0)); //argument
        if (type.equals("string")){
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "length", "()I", false);
        }else {
            mv.visitInsn(Opcodes.ARRAYLENGTH);
        }
    }
    private void chr(){
        mv.visitTypeInsn(Opcodes.NEW, "java/lang/String");
        mv.visitInsn(Opcodes.DUP_X1);
        mv.visitInsn(Opcodes.SWAP);
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/String", "<init>", "(I)V", false);
    }
    private String getFunctionDescriptor(FunctionCall node) {
        StringBuilder descriptor = new StringBuilder();
        descriptor.append("(");

        for (Node child : node.children) {
            if (child instanceof Argument) {
                Argument a = (Argument) child;
                descriptor.append(getTypeDescriptor(getType((Expression) a.children.get(0))));
            }
        }

        descriptor.append(")");
        descriptor.append(getTypeDescriptor(node.children.get(node.children.size()-1).children.get(0).value));

        return descriptor.toString();
    }
    private Object expressionStmt (Expression node) {
        Node n = node.children.get(0);
        if(n instanceof Variable){
            return variable((Variable) n);
        }
        else if (n instanceof BinaryExpression){
            return binaryExpression((BinaryExpression) n);
        }else if(n instanceof UnaryExpression){
            return unaryExpression((UnaryExpression) n);
        }else if (n instanceof Expression){
            return expressionStmt((Expression) n);
        }else if(n instanceof Literal){
            return Literal((Literal) n);
        }else if(n instanceof ArrayAndStructAccess){
            return arrayAndstructAccess((ArrayAndStructAccess) n);
        }else if (n instanceof ArrayElementAccess){
            return arrayAccess((ArrayElementAccess) n);
        }else if(n instanceof FunctionCall){
            return funCall((FunctionCall) n);
        }
        else if(n instanceof StructFieldAccess){
            return fieldAccess((StructFieldAccess) n);
        }else if(n instanceof NewArray){
            return newArray((NewArray) n);
        }
        return null;
    }
    private Object returnStmt (ReturnStatement node) {
        if(node.children.get(0)==null){
            mv.visitInsn(Opcodes.RETURN); //return void
        }
        else{
            expressionStmt((Expression)node.children.get(0));
            String type = getType((Expression)node.children.get(0));
            switch (type) {
                case "int":
                    mv.visitInsn(Opcodes.IRETURN); // return an integer
                    break;
                case "float":
                    mv.visitInsn(Opcodes.FRETURN); // return a float
                    break;
                case "bool":
                    mv.visitInsn(Opcodes.IRETURN); // return a boolean
                    break;
                case "string":
                    mv.visitInsn(Opcodes.ARETURN); // return a string
                    break;
                default: //pour les structures
                    mv.visitInsn(Opcodes.ARETURN);
                    break;
            }
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

    private String getType(Expression expression){
        Node n = expression.children.get(0);
        if(n instanceof FunctionCall){
            String functionName = n.children.get(0).value;
            if(structs.containsKey(functionName)){
                return functionName;
            }
            if(n.children.get(n.children.size()-1) instanceof Argument){
                return "int";
            }
            compiler.Parser.Type t = (compiler.Parser.Type) n.children.get(n.children.size()-1);
            return t.children.get(0).value;
        }
        else if( n instanceof Literal){
            String literal = n.children.get(0).value;
            if(literal.equals("true") || literal.equals("false")){
                return "bool";
            }else if(literal.toCharArray()[0] == '\"'){
                return "string";
            }else if(literal.contains(".")){
                return "float";
            }else {
                return "int";
            }
        }
        else if( n instanceof Expression){
            return getType((Expression)n);
        }
        else if( n instanceof UnaryExpression){
            return getType((Expression)n.children.get(1));
        }
        else if( n instanceof BinaryExpression){
            if(isComparison((BinaryOperator) n.children.get(1))||isEquality((BinaryOperator) n.children.get(1))){
                return "bool";
            }
            return getType((Expression)n.children.get(0));
        }
        else if (n instanceof Variable){
            if(n.children.size()==1){
                return "int";
            }
            compiler.Parser.Type t = (compiler.Parser.Type) n.children.get(1);
            return t.children.get(0).value;
        }
        else if (n instanceof ArrayElementAccess){
            compiler.Parser.Type t = (compiler.Parser.Type) n.children.get(2);
            return t.children.get(0).value;
        }
        else if(n instanceof ArrayAndStructAccess){
            compiler.Parser.Type t = (compiler.Parser.Type) n.children.get(3);
            return t.children.get(0).value;
        }
        else if (n instanceof StructFieldAccess){
            compiler.Parser.Type t = (compiler.Parser.Type) n.children.get(2);
            return t.children.get(0).value;
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

        if(node.children.get(0).value.equals("if")){//c'est le if
            expressionStmt((Expression) node.children.get(1));
            mv.visitJumpInsn(IFEQ, endIfLabel);//si condition false, va à la fin du if
            block((BlockInstruction) node.children.get(2));
            mv.visitLabel(endIfLabel);

        }else{//c'est le else
            expressionStmt((Expression) node.children.get(1));
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

        assignment((Assignment) node.children.get(0));
        mv.visitLabel(startFor);
        expressionStmt((Expression) node.children.get(1));
        mv.visitJumpInsn(Opcodes.IFEQ,endFor);
        block((BlockInstruction) node.children.get(3));
        assignment((Assignment) node.children.get(2));
        mv.visitJumpInsn(Opcodes.GOTO,startFor);  //fin du block donc retourne au début de la boucle
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
        variables.put(name,index);
        return index;
    }

    private Object varDecl (VariableDeclaration node)
    {
        String typeVariable = node.children.get(0).value;
        org.objectweb.asm.Type type = org.objectweb.asm.Type.getType(getTypeDescriptor(node.children.get(0).value));
        int index =registerVariable(node.children.get(1).children.get(0).value, type);
        mv.visitLocalVariable(node.children.get(1).children.get(0).value, getTypeDescriptor(typeVariable), null, start, end, index);
        return null;
    }
    private Object constantDeclaration (ConstantDeclaration node){
        String name = node.children.get(1).value;
        String type = node.children.get(0).children.get(0).value;
        isInitialise = true;
        Object value = expressionStmt((Expression) node.children.get(2));
        cw.visitField(Opcodes.ACC_FINAL + Opcodes.ACC_STATIC , name, getTypeDescriptor(type),null,value);//sais pas si la class est ok
        isInitialise = false;
        classVariable.put(name,getTypeDescriptor(type));//pour savoir que cette variable est une constante
        return null;
    }
    private Object globalDeclaration(GlobalDeclaration node){
        String name = node.children.get(1).value;
        String type = node.children.get(0).children.get(0).value;
        if (topLevel){//si variable de classe
            //expressionStmt((Expression) node.children.get(2));
            cw.visitField(Opcodes.ACC_STATIC , name, getTypeDescriptor(type),null,null);//sais pas si la class est ok
            classVariable.put(name,getTypeDescriptor(type));//pour savoir que cette variable est une instance de classe
            assignment(new Assignment(new Variable(name), (Expression)node.children.get(2)));
        }
        else{//si variable locale
            expressionStmt((Expression) node.children.get(2));
            org.objectweb.asm.Type typeASM = org.objectweb.asm.Type.getType(getTypeDescriptor(type));
            int index = registerVariable(node.children.get(1).value, typeASM);
            mv.visitVarInsn(typeASM.getOpcode(ISTORE), index);
            //mv.visitFieldInsn(Opcodes.PUTSTATIC, className, "a", "I");
            mv.visitLocalVariable(name, getTypeDescriptor(type), null, start, end, index);
            //mv.visitFieldInsn(PUTFIELD, className, name,getTypeDescriptor(type));
            variables.put(name, index);
        }

        return null;
    }

    private Object newArray(NewArray node){

        String type = node.children.get(0).children.get(0).value;
        expressionStmt((Expression) node.children.get(1));
        if (type.equals("int")){
            mv.visitIntInsn(Opcodes.NEWARRAY, Opcodes.T_INT);
        }else if(type.equals("bool")){
            mv.visitIntInsn(Opcodes.NEWARRAY, T_BOOLEAN);
        }else if(type.equals("float")){
            mv.visitIntInsn(Opcodes.NEWARRAY, T_FLOAT);
        }else if(type.equals("string")){
            mv.visitTypeInsn(Opcodes.ANEWARRAY,"java/lang/String");
        }
        return null;
    }

    private Object parameter (Param node) {
        int index = registerVariable(node.children.get(1).value,org.objectweb.asm.Type.getType(getTypeDescriptor(node.children.get(0).children.get(0).value)));
        //mv.visitVarInsn(ISTORE, index);
        //mv.visitLocalVariable(node.children.get(1).value,getTypeDescriptor(node.children.get(0).children.get(0).value),null,start,end,index);
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
                int index= variables.get(left.children.get(0).value);
                org.objectweb.asm.Type type = org.objectweb.asm.Type.getType(getTypeDescriptor(getType((Expression) node.children.get(1))));
                //convertir les 2 types ???
                mv.visitVarInsn(type.getOpcode(ISTORE), index);
            }else {//variable globale
                // Accéder à la variable globale et la charger sur la pile
                String type = classVariable.get(left.children.get(0).value);
                //mv.visitFieldInsn(Opcodes.GETSTATIC, className,left.children.get(0).value,type);
                expressionStmt((Expression) node.children.get(1));
                //mv.visitInsn(Opcodes.IADD); //ajoute la valeur à la variable
                mv.visitFieldInsn(Opcodes.PUTSTATIC, className,left.children.get(0).value , type);//remet à jour la variablede classe sur la heap
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
                .map(field -> org.objectweb.asm.Type.getType(getTypeDescriptor(field.children.get(0).children.get(0).value)))
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
            org.objectweb.asm.Type type = Type.getType(getTypeDescriptor(field.children.get(0).children.get(0).value));
            init.visitVarInsn(type.getOpcode(Opcodes.ILOAD), index);
            index += type.getSize();
            init.visitFieldInsn(Opcodes.PUTFIELD, binaryName, field.children.get(1).value, type.getDescriptor());
        }

        init.visitInsn(Opcodes.RETURN);
        init.visitMaxs(-1, -1);
        init.visitEnd();

        struct.visitEnd();
        structs.put(binaryName, struct);
        struct = null;
        return null;
    }

    private Object fieldDecl (StructField node)
    {
        struct.visitField(ACC_PUBLIC, node.children.get(1).value, getTypeDescriptor(node.children.get(0).children.get(0).value), null, null);
        return null;
    }

    private Object fieldAccess (StructFieldAccess node) {
        //run(node.stem); c'est une expression mais je comprends pas
        // je ne sais pas comment retrouver le nom de la classe de la structure (binaryName) et le type du field (nodeFieldDescriptor)

        //mv.visitFieldInsn(GETFIELD, binaryName, node.children.get(1).value, nodeFieldDescriptor(node));
        return null;
    }

    private Object charAccessInStringArray(CharAccessInStringArray node){
        return null;
    }



}
