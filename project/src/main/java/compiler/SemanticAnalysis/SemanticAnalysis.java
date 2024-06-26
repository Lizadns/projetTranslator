package compiler.SemanticAnalysis;
import compiler.Parser.*;

import java.util.ArrayList;

public class SemanticAnalysis {

    private Node root;
    private ArrayList<String> definedStructure = new ArrayList<>();
    private ArrayList<Node> scope= new ArrayList<>();
    private ArrayList<Node> constantDeclaration = new ArrayList<>();
    private ArrayList<String> variablesInthisScope= new ArrayList<>();
    private Boolean a=false;

    public SemanticAnalysis(Node root) {
        this.root = root;
        this.scope.add(root);
    }

    public int analyzeNode(Node node) throws SemanticException {
        ArrayList<Node> listNode = node.children;
        for(Node nodeChildren : listNode){
            if (nodeChildren instanceof Declaration) {
                checkDeclaration(nodeChildren);
            }
            else if (nodeChildren instanceof Statement) {
                checkStatement(nodeChildren);
            }
        }return 0;
    }

    void checkDeclaration(Node declaration) throws SemanticException {
        ArrayList<Node> listNode = declaration.children;
        for(int i =0; i< listNode.size();i++){
            Node node = listNode.get(i);
            //vérifie que le type des constants declaration et globaldeclaration
            if(node instanceof ConstantDeclaration || node instanceof GlobalDeclaration){
                ArrayList<Node> childrenDeclaration = node.children;
                Type leftDeclaration = (Type) childrenDeclaration.get(0);
                if(variablesInthisScope.contains(childrenDeclaration.get(1).value)){
                    throw new SemanticException("Overwrite another variable");
                }
                else{
                    variablesInthisScope.add(childrenDeclaration.get(1).value);
                }
                Expression rightDeclaration = (Expression) childrenDeclaration.get(2);
                String rightDclrt = getType(rightDeclaration);
                if(rightDeclaration.children.get(0) instanceof ArrayElementAccess){
                    isTheSameTypeWithArrayElementAccess(leftDeclaration.children.get(0).value,rightDclrt);
                }
                else{
                    isTheSameType(leftDeclaration.children.get(0).value,rightDclrt);
                }
                if(node instanceof ConstantDeclaration){
                    constantDeclaration.add(node);
                }
            // struct cannot overwrite existing types
            }else if(node instanceof StructDeclaration){
                String identifier = node.children.get(0).value;
                if (identifier.equals("BaseType")){
                    throw new SemanticException("StructError");
                }
                else if (identifier.equals("Keyword")){
                    throw new SemanticException("StructError");
                }else if(identifier.equals("Boolean")){
                    throw new SemanticException("StructError");
                }
                else{ // overwrite a previously defined structure
                    for (int j =0; j<i; j++){
                        Node node2=listNode.get(j);
                        if(node2 instanceof StructDeclaration && identifier.equals(node2.children.get(0).value)){
                            throw new SemanticException("StructError");
                        }
                    }
                }
                if(variablesInthisScope.contains(identifier)){
                    throw new SemanticException("Overwrite another variable");
                }
                else{
                    variablesInthisScope.add(identifier);
                }
                definedStructure.add(identifier);

            }else{
                throw new SemanticException("No Matching Type for the Declaration "+ node);
            }
        }
    }

    void checkStatement(Node statement) throws SemanticException {
        ArrayList<Node> StatementNodes = statement.children;
        for (Node nodeChildren : StatementNodes) {
            if (nodeChildren instanceof IfStatement) {
                if (nodeChildren.children.get(0).value.equals("if")) {
                    checkTypesConditionStatement((Expression) nodeChildren.children.get(1));
                }
                this.scope.add(nodeChildren);
                ArrayList<String > avant = (ArrayList<String>) this.variablesInthisScope.clone();
                this.variablesInthisScope.clear();
                checkStatement(nodeChildren.children.get(2));
                this.scope.remove(nodeChildren);
                this.variablesInthisScope.addAll(avant);
            } else if (nodeChildren instanceof WhileStatement) {
                checkTypesConditionStatement((Expression) nodeChildren.children.get(0));
                ArrayList<String > avant = (ArrayList<String>) this.variablesInthisScope.clone();
                this.variablesInthisScope.clear();
                this.scope.add(nodeChildren);
                checkStatement(nodeChildren.children.get(1));
                this.scope.remove(nodeChildren);
                this.variablesInthisScope.addAll(avant);
            } else if (nodeChildren instanceof ForStatement) {
                Assignment assignmentFor = (Assignment) nodeChildren.children.get(0);
                Assignment incrementationFor = (Assignment) nodeChildren.children.get(2);
                checkAssignmentFor(assignmentFor, incrementationFor);
                checkTypesConditionStatement((Expression) nodeChildren.children.get(1));
                ArrayList<String > avant = (ArrayList<String>) this.variablesInthisScope.clone();
                this.variablesInthisScope.clear();
                this.scope.add(nodeChildren);
                checkStatement(nodeChildren.children.get(3));
                this.scope.remove(nodeChildren);
                this.variablesInthisScope.addAll(avant);
            } else if (nodeChildren instanceof Free) {
                Node variableDeclaration = getParent(root, nodeChildren.children.get(0).children.get(0).value);
                if (variableDeclaration == null) {
                    throw new SemanticException("No declaration of the variable ");
                }
                String typeVariable = variableDeclaration.children.get(0).children.get(0).value;
                Node v = checkScope(null,this.root, nodeChildren,nodeChildren.children.get(0).children.get(0).value);
                this.a=false;
                if(v== null){
                    throw new SemanticException("ScopeError");
                }else{
                    typeVariable=v.children.get(0).children.get(0).value;
                }
                if (!typeVariable.contains("[]") && (typeVariable.equals("int") || typeVariable.equals("bool") || typeVariable.equals("string") || typeVariable.equals("float"))) {
                    throw new SemanticException("free array or struct");
                }
            } else if (nodeChildren instanceof GlobalDeclaration) {
                ArrayList<Node> globalNode = nodeChildren.children;
                Type leftDeclaration = (Type) globalNode.get(0);
                if(variablesInthisScope.contains(nodeChildren.children.get(1).value)){
                    throw new SemanticException("Overwrite another variable");
                }
                else{
                    variablesInthisScope.add(nodeChildren.children.get(1).value);
                }
                Expression rightDeclaration = (Expression) globalNode.get(2);
                String rightDclrt = getType(rightDeclaration);

                isTheSameTypeWithArrayElementAccess(leftDeclaration.children.get(0).value, rightDclrt);
            } else if (nodeChildren instanceof Assignment) {
                ArrayList<Node> assignmentChildren = nodeChildren.children;
                if (assignmentChildren.get(0) instanceof Variable) { // a = expression
                    Node variableDeclaration = getParent(root, assignmentChildren.get(0).children.get(0).value);
                    if (variableDeclaration == null) {
                        throw new SemanticException("No declaration of the variable ");
                    }
                    String typeVariable = variableDeclaration.children.get(0).children.get(0).value;
                    Node v = checkScope(null,this.root, nodeChildren,assignmentChildren.get(0).children.get(0).value);
                    this.a=false;
                    if(v== null){
                        throw new SemanticException("ScopeError");
                    }
                    else{
                        typeVariable=v.children.get(0).children.get(0).value;
                        if (constantDeclaration.contains(v)){
                            throw new SemanticException("Modification of a constant value");
                        }
                    }
                    String typeExpression = getType((Expression) nodeChildren.children.get(1));
                    isTheSameType(typeVariable, typeExpression);
                } else if (assignmentChildren.get(0) instanceof StructFieldAccess) { // p.x = e
                    String nameVariableStruct = assignmentChildren.get(0).children.get(0).value; // p
                    String nameStructField = assignmentChildren.get(0).children.get(1).value; // x
                    Node variableDeclaration = getParent(root, nameVariableStruct); // on verifie que p est bien déclaré
                    if (variableDeclaration == null) {
                        throw new SemanticException("StructError");
                    }
                    String structName = variableDeclaration.children.get(0).children.get(0).value; //Point
                    Node v = checkScope(null,this.root,  nodeChildren ,assignmentChildren.get(0).children.get(0).value);
                    this.a=false;
                    if(v== null){
                        throw new SemanticException("ScopeError");
                    }else{
                        structName=v.children.get(0).children.get(0).value;
                    }
                    //est ce que la struct Point a bien un attribut x, si oui on retourne son type
                    //1.trouver la structure Point
                    String typeAttribute = isTheStrucDefined(root, structName, nameStructField);
                    String rightType = getType((Expression) assignmentChildren.get(1));
                    isTheSameType(typeAttribute, rightType);
                } else if (assignmentChildren.get(0) instanceof ArrayElementAccess) { //array[2] = ....
                    //1. on cherche le type du tableau
                    String arrayName = assignmentChildren.get(0).children.get(0).value;
                    String typeArrayDeclaration = isTheArrayDefined(root, arrayName);
                    //2. que l'expression est bien un int
                    String elementAccess = getType((Expression) assignmentChildren.get(0).children.get(1));
                    if (!elementAccess.equals("int")) {
                        throw new SemanticException("TypeError");
                    }
                    String typeExpression = getType((Expression) assignmentChildren.get(1));
                    isTheSameType(typeArrayDeclaration, typeExpression);

                } else if (assignmentChildren.get(0) instanceof ArrayAndStructAccess) { // arrayStruct[2].x = ...
                    String arrayName = assignmentChildren.get(0).children.get(0).value; //arrayStruct
                    Expression e = (Expression) assignmentChildren.get(0).children.get(1);  //2
                    String elementAccess = getType((Expression) e);
                    if (!elementAccess.equals("int")) {
                        throw new SemanticException("TypeError");
                    }
                    String attributName = assignmentChildren.get(0).children.get(2).value;  //x

                    Node variableDeclaration = getParent(root, arrayName); // on verifie que arraystruct est bien déclaré
                    if (variableDeclaration == null) {
                        throw new SemanticException("No declaration of the arraystructure");
                    }
                    String structName = variableDeclaration.children.get(0).children.get(0).value; //Point[]
                    Node v = checkScope(null,this.root,  nodeChildren ,assignmentChildren.get(0).children.get(0).value);
                    this.a=false;
                    if(v== null){
                        throw new SemanticException("ScopeError");
                    }
                    else{
                        structName=v.children.get(0).children.get(0).value; //Point[]
                    }
                    //est ce que la struct Point a bien un attribut x, c'est le leftType
                    //1.trouver la structure Point
                    String typeAttribute = isTheStrucDefined(root, structName, attributName);
                    String rightType = getType((Expression) assignmentChildren.get(1));

                    isTheSameType(typeAttribute, rightType);

                } else {
                    throw new SemanticException("Not this type of assignment :" + nodeChildren.children.get(0));
                }
            } else if (nodeChildren instanceof VariableDeclaration) {//check if this is a struc declaration that it exist
                String[] str = {"int", "int[]", "float", "float[]", "bool", "bool[]", "string", "string[]"};
                Boolean isABaseType = false;
                for (String string : str) {
                    if (string.equals(nodeChildren.children.get(0).children.get(0).value)) {
                        isABaseType = true;
                        break;
                    }
                }
                if(variablesInthisScope.contains(nodeChildren.children.get(1).children.get(0).value)){
                    throw new SemanticException("Overwrite another variable");
                }
                else{
                    variablesInthisScope.add(nodeChildren.children.get(1).children.get(0).value);
                }
                if (!isABaseType) {
                    String strucName = nodeChildren.children.get(0).children.get(0).value;
                    String typeStructDeclaration = isTheStrucDefined(root, strucName, null);
                    if (typeStructDeclaration == null) {
                        throw new SemanticException("No Declaration of the used Structure");
                    }

                }

            } else if (nodeChildren instanceof Method) {
                ArrayList<String > avant = (ArrayList<String>) this.variablesInthisScope.clone();
                this.variablesInthisScope.clear();
                this.scope.add(nodeChildren);
                String nameMethod = nodeChildren.children.get(0).children.get(0).value;
                String returnType = nodeChildren.children.get(1).children.get(0).value;
                int i = 2;
                while (i < nodeChildren.children.size() && nodeChildren.children.get(i) instanceof Param) {
                    i++;
                }
                if (i < nodeChildren.children.size() && nodeChildren.children.get(i) instanceof BlockInstruction) {
                    checkStatement(nodeChildren.children.get(i));
                    int j = 0;
                    while (j < nodeChildren.children.get(i).children.size()) {
                        checkReturnStatement(returnType, nodeChildren.children.get(i).children.get(j));
                        j++;
                    }
                }
                this.scope.remove(nodeChildren);
                this.variablesInthisScope.addAll(avant);
            } else if (nodeChildren instanceof FunctionCall) {
                String nameFunctionCall = nodeChildren.children.get(0).value;
                String[] builtInProcedures = {"readInt", "readFloat", "readString", "writeInt", "writeFloat", "write", "writeln", "len", "chr", "len", "floor"};
                for (String str : builtInProcedures) {
                    if (str.equals(nameFunctionCall)) {
                        parseBuiltInProcedures(str, (FunctionCall) nodeChildren);
                    }
                }
                if (definedStructure.contains(nameFunctionCall)) {
                    checkStructureInitialisation(nodeChildren);
                } else {
                    checkFunctionCall((FunctionCall) nodeChildren);
                }
            }
        }
    }
    Node checkScope(Node result, Node begin, Node end, String variableName) throws SemanticException{
        if (begin.equals(end) && result==null) {
            throw new SemanticException("ScopeError");
        }
        if (begin.equals(end) && result!=null) {
            this.a=true;
        }
        if (begin instanceof VariableDeclaration) {
            VariableDeclaration varDecl = (VariableDeclaration) begin;
            if (varDecl.children.get(1).children.get(0).value.equals(variableName)) {
                result = begin; // Si le nom de la VariableDeclaration correspond, nous avons trouvé le nœud parent recherché
            }
        }else if(begin instanceof GlobalDeclaration){
            if(begin.children.get(1).value.equals(variableName)){
                result= begin;
            }
        }else if(begin instanceof ConstantDeclaration){//pas besoin de verifier la scope
            if(begin.children.get(1).value.equals(variableName)){
                result= begin;
            }
        }
        else if(begin instanceof Param){
            if(begin.children.get(1).value.equals(variableName)){
                result= begin;
            }
        }
        if(begin.children!=null) {
            for (Node child : begin.children) {
                if ((!(child instanceof ForStatement || child instanceof WhileStatement || child instanceof IfStatement || child instanceof Method) || this.scope.contains(child)) && this.a==false) {
                    result = checkScope(result, child, end, variableName);
                }
            }
        }
        return result;
    }
    private void checkStructureInitialisation(Node functionCall) throws SemanticException {
        //verifier que les arguments de la fonction d'initialisation de la structure match avec les fields de la structure
        for(Node nodeChildren : root.children){
            if (nodeChildren instanceof Declaration) {
                for (Node node : nodeChildren.children){
                    if (node  instanceof StructDeclaration && node.children.get(0).value.equals(functionCall.children.get(0).value)){
                        int j =1;
                        while (j<node.children.size() && node.children.get(j) instanceof StructField){
                            StructField p = (StructField) node.children.get(j);
                            if (j>= functionCall.children.size()){ //mauvais nombre d'argument
                                throw new SemanticException("ArgumentError");
                            }
                            Argument a = (Argument) functionCall.children.get(j);
                            String typeparam = p.children.get(0).children.get(0).value;
                            String typearg = getType((Expression)a.children.get(0));
                            if(!typearg.equals(typeparam)){ //mauvais type
                                throw  new SemanticException("ArgumentError");
                            }
                            j++;
                        }
                    }
                }
            }
        }

    }


    private void checkAssignmentFor(Assignment assignment,Assignment incrementationFor) throws SemanticException {
        //1.verification que c la meme variable
        String nameAssignment = assignment.children.get(0).children.get(0).value;
        String nameIncrementation = incrementationFor.children.get(0).children.get(0).value;
        if(!nameIncrementation.equals(nameAssignment)){
            throw new SemanticException("Not the same variable in the For assignment and the For incrementation");
        }
        //2.verification de l'assignment
        if(assignment.children.get(0) instanceof Variable){

            Node parentVariable = getParent(root,nameAssignment);
            if(parentVariable==null){
                throw new SemanticException("No declaration of the variable assignment for the For statement");
            }
            String structName = parentVariable.children.get(0).children.get(0).value;
            Node v = checkScope(null,this.root, assignment ,assignment.children.get(0).children.get(0).value);
            this.a=false;
            if(v==null){
                throw new SemanticException("ScopeError");
            }else{
                structName=v.children.get(0).children.get(0).value;
            }
            if(!structName.equals("int")){
                throw new SemanticException("TypeError");
            }String expression = getType((Expression) assignment.children.get(1));
            isTheSameType(structName,expression);
        }
        //3.verification de l'incrementation(pas sure que ce soit nécessaire)
        if(incrementationFor.children.get(0) instanceof Variable){
            Node parentVariable = getParent(root,nameIncrementation);
            if(parentVariable==null){
                throw new SemanticException("No declaration of the variable incrementation for the For statement");
            }
            String structName = parentVariable.children.get(0).children.get(0).value;
            Node v = checkScope(null,this.root, incrementationFor ,incrementationFor.children.get(0).children.get(0).value);
            this.a=false;
            if(v==null){
                throw new SemanticException("ScopeError");
            }
            else{
                structName=v.children.get(0).children.get(0).value;
            }
            if(!structName.equals("int")){
                throw new SemanticException("TypeError");
            }String expression = getType((Expression) incrementationFor.children.get(1));
            isTheSameType(structName,expression);
            if(!(incrementationFor.children.get(1).children.get(0) instanceof BinaryExpression)){
                throw new SemanticException("TypeError");
            }
        }

    }

    void checkReturnStatement(String type_expected, Node node) throws SemanticException{
        if (node instanceof ReturnStatement){
            Expression e = (Expression) node.children.get(0);
            String type=null;
            if (e == null){
                type= "void";
            }
            else{
                type= getType(e);
            }
            if(!type.equals(type_expected)){
                throw new SemanticException("ReturnError");
            }
        }
        else if(node instanceof WhileStatement){
            Node n = node.children.get(0);
            int i =1;
            if (i<node.children.size() && node.children.get(i) instanceof BlockInstruction){
                int j=0;
                while (j< node.children.get(i).children.size()){
                    checkReturnStatement(type_expected, node.children.get(i).children.get(j));
                    j++;
                }
            }
        }
        else if(node instanceof ForStatement){
            Node n = node.children.get(0);
            int i =3;
            if (i<node.children.size() && node.children.get(i) instanceof BlockInstruction){
                int j=0;
                while (j< node.children.get(i).children.size()){
                    checkReturnStatement(type_expected, node.children.get(i).children.get(j));
                    j++;
                }
            }
        }
        else if(node instanceof IfStatement){
            Node n = node.children.get(0);
            int i =2;
            if (i<node.children.size() && node.children.get(i) instanceof BlockInstruction){
                int j=0;
                while (j< node.children.get(i).children.size()){
                    checkReturnStatement(type_expected, node.children.get(i).children.get(j));
                    j++;
                }
            }
        }
    }

    void checkFunctionCall(FunctionCall call)throws SemanticException{
        String name = call.children.get(0).value;
        if(root.children.get(1)!=null){
            int i =0;
            while(i<root.children.get(1).children.size()){
                if (root.children.get(1).children.get(i) instanceof Method){
                    Method m = (Method) root.children.get(1).children.get(i);
                    String n =m.children.get(0).children.get(0).value;
                    if (n.equals(name)){
                        int j =2;
                        while (j<m.children.size() && m.children.get(j) instanceof Param){
                            Param p = (Param) m.children.get(j);
                            if (j-1>= call.children.size()){ //mauvais nombre d'argument
                                throw new SemanticException("ArgumentError");
                            }
                            Argument a = (Argument) call.children.get(j-1);
                            String typeparam = p.children.get(0).children.get(0).value;
                            String typearg = getType((Expression)a.children.get(0));
                            if(!typearg.equals(typeparam)){ //mauvais type
                                throw  new SemanticException("ArgumentError");
                            }
                            a.addType(typearg);
                            j++;
                        }
                        call.addType(m.children.get(1).children.get(0).value);
                    }
                }
                i++;
            }
        }
    }

    String getType(Expression expression) throws SemanticException {
        Node node = expression.children.get(0);
        if(node instanceof FunctionCall){
            String nameFunctionCall = node.children.get(0).value;
            String[] builtInProcedures = {"readInt", "readFloat", "readString", "writeInt", "writeFloat", "write", "writeln","len","floor","chr"};
            for(String str : builtInProcedures){
                if(str.equals(nameFunctionCall)){
                    return parseBuiltInProcedures(str,(FunctionCall) node);
                }
            }
            //si initialisation d'une structure;
            if (definedStructure.contains(nameFunctionCall)){
                //1.Check les arguments
                checkStructureInitialisation(node);
                return nameFunctionCall;
            }
            else{
                checkFunctionCall((FunctionCall) node);
                String type = getReturnType((FunctionCall) node, root);
                ((FunctionCall) node).addType(type);
                return type;
            }
        }
        else if(node instanceof ArrayAndStructAccess){ //... = array[e].attribute
            String arrayName = node.children.get(0).value; //array
            Expression e = (Expression) node.children.get(1);  //e
            String elementAccess = getType((Expression) e);
            if(!elementAccess.equals("int")){
                throw new SemanticException("TypeError");
            }
            String attributName = node.children.get(2).value;  //attribute
            Node variableDeclaration = getParent(root, arrayName); // on verifie que array est bien déclaré
            Node v = checkScope(null,this.root,node, node.children.get(0).value);
            this.a=false;
            String structName = v.children.get(0).children.get(0).value; //Point[]
                //est ce que la struct Point a bien un attribut x, c'est le leftType
                //1.trouver la structure Point
            String typeAttribute = isTheStrucDefined(root, structName,attributName);
            ((ArrayAndStructAccess) node).addType(typeAttribute);
            return typeAttribute;
        }else if(node instanceof ArrayElementAccess){//...=array[6]
                //1. on cherche le type du tableau
            String arrayName = node.children.get(0).value;
            String typeArrayDeclaration = isTheArrayDefined(root,arrayName);
                //2. que l'expression est bien un int
            String elementAccess = getType((Expression) node.children.get(1));
            if(!elementAccess.equals("int")){
                throw new SemanticException("TypeError");
            }
            ((ArrayElementAccess) node).addType(typeArrayDeclaration);
            return typeArrayDeclaration;

        }else if(node instanceof NewArray){//..=int[5]
            String typeArray = node.children.get(0).children.get(0).value;
            String lengthArray = getType((Expression) node.children.get(1));
            if(!lengthArray.equals("int")){
                throw new SemanticException("TypeError");
            }
            return typeArray+"["+"]";
        }
        else if(node instanceof StructFieldAccess){ // .... = p.x
            String nameStructVariable = node.children.get(0).value; //p
            Node variableDeclaration = getParent(root, nameStructVariable); // on verifie que p est bien déclaré
            Node v = checkScope(null,this.root,node, node.children.get(0).value);
            this.a=false;
            String structName = v.children.get(0).children.get(0).value; //Point
            String nameStructField = node.children.get(1).value; //x
            String type = isTheStrucDefined(root, structName, nameStructField);
            ((StructFieldAccess) node).addType(type);
            return type;

        }else if(node instanceof Variable){
            Node parent = getParent(root, node.children.get(0).value);
            if(parent==null){
                throw new SemanticException("No Declaration Variable");
            }
            String typeDeclaration = parent.children.get(0).children.get(0).value;
            Node v = checkScope(null,this.root,node, node.children.get(0).value);
            this.a=false;
            if(v==null){
                throw new SemanticException("ScopeError");
            }
            else{
                typeDeclaration=v.children.get(0).children.get(0).value;
            }
            if(parent.children.get(0).value.contains("[]")){
                typeDeclaration+="[]";
            }
            ((Variable) node).addType(typeDeclaration);
            return typeDeclaration;
        }else if (node instanceof Expression){
                //nestedExpression
            return getType((Expression) node);
        }else if (node instanceof Literal){
            ArrayList<Node> literalNodes = node.children;
            String literal = literalNodes.get(0).value;
            if(literal.equals("true") || literal.equals("false")){
                return "bool";
            }else if(literal.toCharArray()[0] == '\"'){
                return "string";
            }else if(literal.contains(".")){
                return "float";
            }else {
                return "int";
            }

        }else if(node instanceof CharAccessInStringArray){//arrayString[3][2]
            String arrayStringName = node.children.get(0).value;
            Node arrayStringDeclaration = getParent(root,arrayStringName);
            String type = arrayStringDeclaration.children.get(0).children.get(0).value;
            if(!type.equals("string[]")){
                throw new SemanticException("Use of a array of 2 dimension for a non-string basetype");
            }
            String typeExpression1 = getType((Expression) node.children.get(1));
            String typeExpression2 = getType((Expression) node.children.get(2));
            if(!typeExpression2.equals("int")||!typeExpression1.equals("int")){
                throw new SemanticException("TypeError");
            }
            return "int";
        }
        else if (node instanceof UnaryExpression){
            String typeUnary = getType((Expression) node.children.get(1));
            if(node.children.get(0).children.get(0).value.equals("!")){
                if(typeUnary.equals("bool")){
                    return typeUnary;
                }throw new SemanticException("ArgumentError");
            }else{
                if(typeUnary.equals("int")|| typeUnary.equals("float")){
                    return typeUnary;
                }else{
                    throw new SemanticException("TypeError");
                }

            }

        }else if (node instanceof BinaryExpression){
                //return, si les opérant sont du meme type,
                // le type de binary expression "ArithmeticOperator","ComparisonOperator","AndOperator","OrOperator"
            String binaryOperator = node.children.get(1).children.get(0).value;
            String leftOperator = getType((Expression) node.children.get(0));
            String rightOperator = getType((Expression) node.children.get(2));
            isTheSameTypeOperator(leftOperator,rightOperator);
            if(binaryOperator.equals("+")){
                if (rightOperator.equals("bool")){
                    throw new SemanticException("TypeError in ArithmeticOperation");
                }
                if(rightOperator.equals("float")||leftOperator.equals("float")){
                    return "float";
                }
                return rightOperator;
            }
            else if (binaryOperator.equals("%")){
                if (!rightOperator.equals("int") || !leftOperator.equals("int")){
                    throw new SemanticException("TypeError in ArithmeticOperation");
                }
                return "int";
            }
            else if(binaryOperator.equals("-") || binaryOperator.equals("/") ||binaryOperator.equals("*")){
                if(rightOperator.equals("float")||leftOperator.equals("float")){
                    return "float";
                }else if(rightOperator.equals("int")){
                    return "int";
                }
                else{
                    throw new SemanticException("TypeError in ArithmeticOperation");
                }
            }else if(binaryOperator.equals("<")||binaryOperator.equals("<=")||binaryOperator.equals(">")||binaryOperator.equals(">=")){
                if (!(leftOperator.equals("int"))&& !(leftOperator.equals("float"))){
                    throw new SemanticException("TypeError in ComparisonOperation");
                }
                return "bool";
            }
            else if (binaryOperator.equals("==")||binaryOperator.equals("!=")){
                return "bool";
            }
            else{
                    //verifier que de chaque coté est le meme type et que l'operateur soit un comparaison operator
                Boolean leftCondition = isItACondition((Expression) node.children.get(0));
                Boolean rightCondition = isItACondition((Expression) node.children.get(2));
                if(leftCondition && rightCondition){
                    return "bool";
                }
            }
        }
        else{
            throw new SemanticException("No Matching Type for the Expression"+ node);
        }
        return "Empty Expression";
    }
    private String parseBuiltInProcedures(String builtInProcedure,FunctionCall node) throws SemanticException {
        String returnType;
        ArrayList<Node> children = node.children;
        if(builtInProcedure.equals("len")||builtInProcedure.equals("floor")||builtInProcedure.equals("chr")){
            if(children.size()!=2){
                throw new SemanticException("not the right amomunt of argument");
            }
            String argType = getType((Expression) children.get(1).children.get(0));
            if(builtInProcedure.equals("len")){
                if(argType.equals("string")||argType.contains("[]")){
                    return "int";
                }
            }else if(builtInProcedure.equals("floor")){
                if(argType.equals("float")){
                    return "int";
                }
            }else {
                if(argType.equals("int")){
                    return "string";
                }
            }
            throw new SemanticException("ArgumentError");

        }
        else if(builtInProcedure.contains("read")){
            //verifier que rien en argument
            if(children.size()!=1){
                throw new SemanticException("Arguments found in the built in procedures read");
            }
            //trouver le returntype
            returnType = builtInProcedure.replace("read","");
            return returnType.toLowerCase();
        }
        else if(builtInProcedure.equals("write")){
            return null;
        }
        else if (builtInProcedure.equals("writeln")){
            String argType = getType((Expression) children.get(1).children.get(0));
            return null;
        }

        else{
            if(children.size()!=2){
                throw new SemanticException("Wrong number of argument in the built in procedure writeInt");
            }
            String typeArgument = getType((Expression) children.get(1).children.get(0));
            if(builtInProcedure.equals("writeInt")){
                if(!typeArgument.equals("int")){
                    throw new SemanticException("ArgumentError");
                }
            }else{//equal writeFloat
                if(!typeArgument.equals("float")){
                    throw new SemanticException("ArgumentError");
                }
            }
            return null;
        }
    }

    private String isTheArrayDefined(Node node, String arrayName) throws SemanticException {
        if (node == null) {
            throw new SemanticException("No declaration of the array");
        }
        if (node instanceof GlobalDeclaration) {
            if (node.children.get(1).value.equals(arrayName)) {
                String type = node.children.get(0).children.get(0).value;
                if(type.equals("string")){
                    return "int";
                }
                if(type.contains("[")){//être sur que c un tableau et pas juste une variable
                    return type.substring(0,type.length()-2);
                }//retourner une erreur si variable ?
            }
        }else if(node instanceof VariableDeclaration){
            if (node.children.get(1).children.get(0).value.equals(arrayName)) {
                String type = node.children.get(0).children.get(0).value;
                if(type.contains("[")){//être sur que c un tableau et pas juste une variable
                    return type.substring(0,type.length()-2);
                }
                else{
                    throw new SemanticException("The declaration of the array is not an array but a variable");
                }//retourner une erreur si variable ?
            }
        }

        if (node.children != null) {
            for (Node child : node.children) {
                // Appel récursif avec child seulement si child n'est pas null
                String parentType = isTheArrayDefined(child, arrayName);
                if (parentType != null) {
                    return parentType;
                }
            }
        }
        return null;
    }

    private Boolean isItACondition(Expression expression) throws SemanticException {
        Node childrenNode = expression.children.get(0);
        if(childrenNode instanceof BinaryExpression){
            String operator = childrenNode.children.get(1).children.get(0).value;
            String leftOperator = getType((Expression) childrenNode.children.get(0));
            String rightOperator = getType((Expression) childrenNode.children.get(2));
            isTheSameTypeOperator(leftOperator,rightOperator);
            if(operator.equals("<")||operator.equals("<=")||operator.equals("==")||operator.equals("!=")||operator.equals(">")||operator.equals(">=")){
                return true;
            }
        }
        return false;
    }

    private void isTheSameTypeOperator(String leftOperator, String rightOperator) throws SemanticException {
        if(leftOperator.equals("float") && rightOperator.equals("int")){
            rightOperator="float";
        }
        if(rightOperator.equals("float") && leftOperator.equals("int")){
            leftOperator="float";
        }
        if(!leftOperator.equals(rightOperator) ){
            System.out.println(leftOperator);
            System.out.println(rightOperator);
            throw new SemanticException("OperatorError");
        }
    }

    void isTheSameType(String left, String right) throws SemanticException {
        if(!left.equals(right)){
            throw new SemanticException("TypeError");
        }
    }

    void isTheSameTypeWithArrayElementAccess(String left, String right) throws SemanticException {

        if(!right.toLowerCase().contains(left.toLowerCase())){
            System.out.println(left);
            System.out.println(right);
            throw new SemanticException("TypeError");
        }
    }

    Node getParent(Node node, String variableName) throws SemanticException {
        if (node == null) {
            throw new SemanticException("No declaration of the variable");
        }
        if (node instanceof VariableDeclaration) {
            VariableDeclaration varDecl = (VariableDeclaration) node;
            if (varDecl.children.get(1).children.get(0).value.equals(variableName)) {
                return node; // Si le nom de la VariableDeclaration correspond, nous avons trouvé le nœud parent recherché
            }
        }else if(node instanceof GlobalDeclaration){
            if(node.children.get(1).value.equals(variableName)){
                return node;
            }
        }else if(node instanceof ConstantDeclaration){//pas besoin de verifier la scope
            if(node.children.get(1).value.equals(variableName)){
                return node;
            }
        }
        else if(node instanceof Param){
            if(node.children.get(1).value.equals(variableName)){
                return node;
            }
        }
        if (node.children != null) {
            for (Node child : node.children) {
                // Appel récursif avec child seulement si child n'est pas null
                Node parent = getParent(child, variableName);
                if (parent != null) {
                    return parent; // Si le nœud parent est trouvé dans les enfants, retournez-le
                }
            }
        }
        return null;
    }



    void checkTypesConditionStatement(Expression conditionStatement) throws SemanticException {
        Boolean condition = isItACondition((Expression) conditionStatement);
        if (!condition && !getType((Expression)conditionStatement).equals("bool")) {
            throw new SemanticException("MissingConditionError");
        }
    }

    void checkAssignment(){

    }

    String getReturnType(FunctionCall functionCall,Node node){
        if (node == null) {
            return null; // Si le nœud est nul, nous avons atteint la fin de l'arbre
        }
        if (node instanceof Method) {
            Method methodInstance = (Method) node;
            String nameMethod = methodInstance.children.get(0).children.get(0).value;
            if (nameMethod.equals(functionCall.children.get(0).value)) {
                String returnType = methodInstance.children.get(1).children.get(0).value;
                return returnType;
            }
        }
        // Parcourir récursivement les enfants du nœud actuel
        if (node.children != null) {
            for (Node child : node.children) {
                // Appel récursif avec child seulement si child n'est pas null
                String returnType = getReturnType(functionCall, child);
                if (returnType != null) {
                    return returnType; // Retourne le type de retour si trouvé dans les enfants
                }
            }
        }
        return null;
    }


    private String isTheStrucDefined(Node node, String nameStruct, String nameStructField) throws SemanticException {
        if (node == null) {
            throw new SemanticException("StructError");
        }
        if (node instanceof StructDeclaration) {

            StructDeclaration structDeclarationDecl = (StructDeclaration) node;
            String nameStrucDeclaration = structDeclarationDecl.children.get(0).value;

            if(nameStruct.contains("[]")){
                nameStrucDeclaration = nameStrucDeclaration + "[]";
            }
            if (nameStrucDeclaration.equals(nameStruct)) {
                ArrayList<Node> children = structDeclarationDecl.children;
                if(nameStructField==null){
                    return structDeclarationDecl.children.get(0).value;
                }
                for(Node structField : children){
                    if(structField instanceof StructField){
                        if(structField.children.get(1).value.equals(nameStructField)){
                            return structField.children.get(0).children.get(0).value;
                        }
                    }
                } throw new SemanticException("StructError");
            }
        }
        if (node.children != null) {
            for (Node child : node.children) {
                // Appel récursif avec child seulement si child n'est pas null
                String typeStructAccess = isTheStrucDefined(child, nameStruct,nameStructField);
                if (typeStructAccess != null) {
                    return typeStructAccess; // Retourne le type de retour si trouvé dans les enfants
                }
            }
        }
        return null;
    }


}


