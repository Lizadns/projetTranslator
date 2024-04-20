package compiler.SemanticAnalysis;
import compiler.Parser.*;

import java.util.ArrayList;
import java.util.Arrays;

public class SemanticAnalysis {

    private Node root;



    public SemanticAnalysis(Node root) {
        this.root = root;
    }

    public String analyzeNode(Node node) throws SemanticException {
        ArrayList<Node> listNode = node.children;
        for(Node nodeChildren : listNode){
            if (nodeChildren instanceof Declaration) {
                checkDeclaration(nodeChildren);
            }
            else if (nodeChildren instanceof Statement) {
                checkStatement(nodeChildren);
            }
        }return "Everything is OK!";
    }

    void checkDeclaration(Node declaration) throws SemanticException {
        ArrayList<Node> listNode = declaration.children;
        for(int i =0; i< listNode.size();i++){
            Node node = listNode.get(i);
            //vérifie que le type des constants declaration et globaldeclaration
            if(node instanceof ConstantDeclaration || node instanceof GlobalDeclaration){
                ArrayList<Node> childrenDeclaration = node.children;
                Type leftDeclaration = (Type) childrenDeclaration.get(0);
                Expression rightDeclaration = (Expression) childrenDeclaration.get(2);
                String rightDclrt = getType(rightDeclaration);
                if(rightDeclaration.children.get(0) instanceof ArrayElementAccess){
                    isTheSameTypeWithArrayElementAccess(leftDeclaration.children.get(0).value,rightDclrt);
                }
                else{

                    isTheSameType(leftDeclaration.children.get(0).value,rightDclrt);
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

            }else{
                throw new SemanticException("No Matching Type for the Declaration "+ node);
            }
        }
    }

    void checkStatement(Node statement) throws SemanticException {
        ArrayList<Node> StatementNodes = statement.children;
        for(Node nodeChildren : StatementNodes){
            if(nodeChildren instanceof IfStatement){
                checkTypesConditionStatement((Expression) nodeChildren.children.get(1));
                checkStatement(nodeChildren.children.get(2));
            }else if(nodeChildren instanceof WhileStatement){
                checkTypesConditionStatement((Expression) nodeChildren.children.get(0));
                checkStatement(nodeChildren.children.get(1));
            }else if(nodeChildren instanceof ForStatement){
                checkTypesConditionStatement((Expression) nodeChildren.children.get(1));
                checkStatement(nodeChildren.children.get(3));
            }
            else if(nodeChildren instanceof GlobalDeclaration){
                ArrayList<Node> globalNode = nodeChildren.children;
                Type leftDeclaration = (Type) globalNode.get(0);
                Expression rightDeclaration = (Expression) globalNode.get(2);
                String rightDclrt = getType(rightDeclaration);
                isTheSameTypeWithArrayElementAccess(leftDeclaration.children.get(0).value,rightDclrt);
            }else if(nodeChildren instanceof Assignment){
                ArrayList<Node> assignmentChildren = nodeChildren.children;
                if(assignmentChildren.get(0) instanceof Variable){ // a = expression
                    Node variableDeclaration = getParent(root,assignmentChildren.get(0).children.get(0).value);
                    String typeVariable = variableDeclaration.children.get(0).children.get(0).value;
                    String typeExpression = getType((Expression) nodeChildren.children.get(1));
                    isTheSameType(typeVariable,typeExpression);
                }else if (assignmentChildren.get(0) instanceof StructFieldAccess){ // p.x = e
                    String nameVariableStruct = assignmentChildren.get(0).children.get(0).value; // p
                    String nameStructField = assignmentChildren.get(0).children.get(1).value; // x
                    Node variableDeclaration = getParent(root, nameVariableStruct); // on verifie que p est bien déclaré
                    String structName = variableDeclaration.children.get(0).children.get(0).value; //Point
                    //est ce que la struct Point a bien un attribut x, si oui on retourne son type
                    //1.trouver la structure Point
                    String typeAttribute = isTheStrucDefined(root, structName,nameStructField);
                    String rightType = getType((Expression) assignmentChildren.get(1));
                    isTheSameType(typeAttribute,rightType);
                }else if(assignmentChildren.get(0) instanceof ArrayElementAccess){ //array[2] = ....
                    //1. on cherche le type du tableau
                    String arrayName = assignmentChildren.get(0).children.get(0).value;
                    String typeArrayDeclaration = isTheArrayDefined(root,arrayName);
                    //2. que l'expression est bien un int
                    String elementAccess = getType((Expression) assignmentChildren.get(0).children.get(1));
                    if(!elementAccess.equals("int")){
                        throw new SemanticException("TypeError");
                    }
                    String typeExpression = getType((Expression) assignmentChildren.get(1));
                    isTheSameType(typeArrayDeclaration,typeExpression);

                }else if (assignmentChildren.get(0) instanceof ArrayAndStructAccess){ // arrayStruct[2].x = ...
                    String arrayName = assignmentChildren.get(0).children.get(0).value; //arrayStruct
                    Expression e = (Expression) assignmentChildren.get(0).children.get(1);  //2
                    String elementAccess = getType((Expression) e);
                    if(!elementAccess.equals("int")){
                        throw new SemanticException("TypeError");
                    }
                    String attributName = assignmentChildren.get(0).children.get(2).value;  //x

                    Node variableDeclaration = getParent(root, arrayName); // on verifie que arraystruct est bien déclaré
                    String structName = variableDeclaration.children.get(0).children.get(0).value; //Point[]
                    //est ce que la struct Point a bien un attribut x, c'est le leftType
                    //1.trouver la structure Point
                    String typeAttribute = isTheStrucDefined(root, structName,attributName);
                    String rightType = getType((Expression) assignmentChildren.get(1));

                    isTheSameType(typeAttribute,rightType);

                }
                else{
                    throw new SemanticException("Not this type of assignment :" + nodeChildren.children.get(0));
                }
            }
            else if(nodeChildren instanceof Method){
                String nameMethod= nodeChildren.children.get(0).children.get(0).value;
                String returnType = nodeChildren.children.get(1).children.get(0).value;
                int i =2;
                while(i<nodeChildren.children.size() && nodeChildren.children.get(i) instanceof Param){
                    i++;
                }
                if (i<nodeChildren.children.size() && nodeChildren.children.get(i) instanceof BlockInstruction){
                    checkStatement(nodeChildren.children.get(i));
                    int j=0;
                    while (j< nodeChildren.children.get(i).children.size()){
                        checkReturnStatement(returnType, nodeChildren.children.get(i).children.get(j));
                        j++;
                    }
                }

            }
            else if (nodeChildren instanceof FunctionCall){
                String nameFunctionCall = nodeChildren.children.get(0).value;
                String[] builtInProcedures = {"readInt", "readFloat", "readString", "writeInt", "writeFloat", "write", "writeln"};
                for(String str : builtInProcedures){
                    if(str.equals(nameFunctionCall)){
                        parseBuiltInProcedures(str,(FunctionCall) nodeChildren);
                    }
                }
                checkFunctionCall((FunctionCall) nodeChildren);
            }
        }
    }
    void checkReturnStatement(String type_expected, Node node) throws SemanticException{
        if (node instanceof ReturnStatement){
            Expression e = (Expression) node.children.get(0);
            String type = getType(e);
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
                            j++;
                        }
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
            String[] builtInProcedures = {"readInt", "readFloat", "readString", "writeInt", "writeFloat", "write", "writeln"};
            for(String str : builtInProcedures){
                if(str.equals(nameFunctionCall)){
                    return parseBuiltInProcedures(str,(FunctionCall) node);
                }
            }
            checkFunctionCall((FunctionCall) node);
            return getReturnType((FunctionCall) node,root);
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
            String structName = variableDeclaration.children.get(0).children.get(0).value; //Point[]
                //est ce que la struct Point a bien un attribut x, c'est le leftType
                //1.trouver la structure Point
            String typeAttribute = isTheStrucDefined(root, structName,attributName);
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
            String structName = variableDeclaration.children.get(0).children.get(0).value; //Point
            String nameStructField = node.children.get(1).value; //x
            String type = isTheStrucDefined(root, structName, nameStructField);
            return type;

        }else if(node instanceof Variable){
            Node parent = getParent(root, node.children.get(0).value);
            if(parent==null){
                throw new SemanticException("No Declaration Variable");
            }
            String typeDeclaration = parent.children.get(0).children.get(0).value;
            return typeDeclaration;
        }else if (node instanceof Expression){
                //nestedExpression
            return getType((Expression) node);
        }else if (node instanceof Literal){
            ArrayList<Node> literalNodes = node.children;
            String literal = literalNodes.get(0).value;
            if(literal.equals("true") || literal.equals("false")){
                return "bool";
            }else if(literal.contains(".")){
                return "float";
            }else if(literal.contains("0")||literal.contains("1")||literal.contains("2")||literal.contains("3")||literal.contains("4")||literal.contains("5")||literal.contains("6")||literal.contains("7")||literal.contains("8")||literal.contains("9")){
                return "int";
            }else {
                return "string";
            }

        }else if (node instanceof UnaryExpression){
                // ne rien faire, ça ne change rien
            return "UnaryExpression";

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
                }return rightOperator;
            }
            else if (binaryOperator.equals("%")){
                if (!rightOperator.equals("int")){
                    throw new SemanticException("TypeError in ArithmeticOperation");
                }return "int";
            }
            else if(binaryOperator.equals("-") || binaryOperator.equals("/") ||binaryOperator.equals("*")){
                if(rightOperator.equals("float")){
                    return "float";
                }else if(rightOperator.equals("int")){
                    return "int";
                }
                else{
                    throw new SemanticException("TypeError in ArithmeticOperation");
                }
            }else if(binaryOperator.equals("<")||binaryOperator.equals("<=")||binaryOperator.equals("==")||binaryOperator.equals("!=")||binaryOperator.equals(">")||binaryOperator.equals(">=")){
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
        if(builtInProcedure.contains("read")){
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
                if(type.contains("[")){//être sur que c un tableau et pas juste une variable
                    return type;
                }//retourner une erreur si variable ?
            }
        }else if(node instanceof VariableDeclaration){
            if (node.children.get(1).children.get(0).value.equals(arrayName)) {
                String type = node.children.get(0).children.get(0).value;
                if(type.contains("[")){//être sur que c un tableau et pas juste une variable
                    return type;
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
        if(!leftOperator.equals(rightOperator)){
            throw new SemanticException("OperatorError");
        }
    }

    void isTheSameType(String left, String right) throws SemanticException {
        if(!left.equals(right)){
            throw new SemanticException("TypeError");
        }
    }

    void isTheSameTypeWithArrayElementAccess(String left, String right) throws SemanticException {

        if(!right.contains(left)){
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
        }else if(node instanceof ConstantDeclaration){
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
            throw new SemanticException("No declaration of the structure");
        }
        if (node instanceof StructDeclaration) {

            StructDeclaration structDeclarationDecl = (StructDeclaration) node;
            if (structDeclarationDecl.children.get(0).value.equals(nameStruct)) {
                ArrayList<Node> children = structDeclarationDecl.children;
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


