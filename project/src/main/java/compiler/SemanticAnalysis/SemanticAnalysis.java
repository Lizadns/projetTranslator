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
                checkTypesDeclaration(nodeChildren);
            }
            else if (nodeChildren instanceof Statement) {
                checkTypesStatement(nodeChildren);
            }
            else{
                throw new SemanticException("No Declaration or Statement");
            }
        }return "Everything is OK!";
    }

    void checkTypesDeclaration(Node declaration) throws SemanticException {
        ArrayList<Node> listNode = declaration.children;
        for(Node node : listNode){
            if(node instanceof ConstantDeclaration || node instanceof GlobalDeclaration){
                ArrayList<Node> childrenDeclaration = node.children;
                Type leftDeclaration = (Type) childrenDeclaration.get(0);
                Expression rightDeclaration = (Expression) childrenDeclaration.get(2);
                String rightDclrt = getType(rightDeclaration);
                isTheSameType(leftDeclaration.children.get(0).value,rightDclrt);
            }else if(node instanceof StructDeclaration){

            }
        }
    }

    void checkTypesStatement(Node statement) throws SemanticException {
        ArrayList<Node> StatementNodes = statement.children;
        for(Node nodeChildren : StatementNodes){
            if(nodeChildren instanceof IfStatement){
                checkTypesConditionStatement((Expression) nodeChildren.children.get(1));
            }else if(nodeChildren instanceof WhileStatement){
                checkTypesConditionStatement((Expression) nodeChildren.children.get(0));
            }else if(nodeChildren instanceof ForStatement){
                checkTypesConditionStatement((Expression) nodeChildren.children.get(1));
            }
            else if(nodeChildren instanceof GlobalDeclaration){
                ArrayList<Node> globalNode = nodeChildren.children;
                Type leftDeclaration = (Type) globalNode.get(0);
                Expression rightDeclaration = (Expression) globalNode.get(2);
                String rightDclrt = getType(rightDeclaration);
                isTheSameType(leftDeclaration.children.get(0).value,rightDclrt);
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
                }else if(assignmentChildren.get(0) instanceof ArrayElementAccess){
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
                }
                else{
                    throw new SemanticException("No this type of assignment :" + nodeChildren.children.get(0));
                }

            }
        }
    }

    String getType(Expression expression) throws SemanticException {
        ArrayList<Node> listExpression = expression.children;
        for (Node node : listExpression){
            if(node instanceof FunctionCall){
                return getReturnType((FunctionCall) node,root);
            }
            else if(node instanceof ArrayAndStructAccess){ //... = array[e].attribute



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
                return typeArray;
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
                if(binaryOperator.equals("+") || binaryOperator.equals("-") || binaryOperator.equals("/") ||binaryOperator.equals("*")){
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
        }
        return "Empty Expression";
    }

    private String isTheArrayDefined(Node node, String arrayName) throws SemanticException {
        if (node == null) {
            throw new SemanticException("No declaration of the array");
        }
        if (node instanceof GlobalDeclaration) {
            GlobalDeclaration varDecl = (GlobalDeclaration) node;
            if (varDecl.children.get(1).value.equals(arrayName)) {
                String type = varDecl.children.get(0).children.get(0).value;
                if(type.contains("[")){//être sur que c un tableau et pas juste une variable
                    return type;
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
        else{
            throw new SemanticException("No declaration of the array");
        }
        return null;
    }


    private Boolean isItACondition(Expression expression) throws SemanticException {
        Node childrenNode = expression.children.get(0);
        if(childrenNode instanceof BinaryExpression){
            String operator = childrenNode.children.get(1).children.get(0).value;
            String leftOperator = getType((Expression) childrenNode.children.get(0));
            String rightOperator = getType((Expression) childrenNode.children.get(0));
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

        if(!left.equals(right) && !left.contains(right)){
            throw new SemanticException("TypeError");
        }
    }

    Node getParent(Node node, String variableName) throws SemanticException {
        if (node == null) {
            throw new SemanticException("No declaration of the variable 1");
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
                String returnType = methodInstance.children.get(2).children.get(0).children.get(0).value;
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
                } throw new SemanticException("No declaration of the structField");
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
        return "blabla";
    }


}


