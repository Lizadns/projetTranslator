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
        for(int i =0; i< listNode.size();i++){
            Node node = listNode.get(i);
            if(node instanceof ConstantDeclaration || node instanceof GlobalDeclaration){
                ArrayList<Node> childrenDeclaration = node.children;
                Type leftDeclaration = (Type) childrenDeclaration.get(0);
                Expression rightDeclaration = (Expression) childrenDeclaration.get(2);
                String rightDclrt = getType(rightDeclaration);
                isTheSameType(leftDeclaration.children.get(0).value,rightDclrt);
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
                }
            }
            else if(nodeChildren instanceof Method){
                String nameMethod= nodeChildren.children.get(0).children.get(0).value;
                String returnType = nodeChildren.children.get(1).children.get(0).value;
                int i =2;
                while(nodeChildren.children.get(i)!=null && nodeChildren.children.get(i) instanceof Param){
                    i++;
                }
                while(nodeChildren.children.get(i)!=null && nodeChildren.children.get(i) instanceof BlockInstruction){
                    checkReturnStatement(returnType, (BlockInstruction) nodeChildren.children.get(i));
                    i++;
                }
            }
        }
    }
    void checkReturnStatement(String type_expected, BlockInstruction node) throws SemanticException{
        if (node.children.get(0) instanceof ReturnStatement){
            Expression e = (Expression) node.children.get(0).children.get(0);
            String type = getType(e);
            if(!type.equals(type_expected)){
                throw new SemanticException("ReturnError");
            }
        }
        else if(node.children.get(0) instanceof WhileStatement){
            Node n = node.children.get(0);
            int i =1;
            while (n.children.get(i)!=null && n.children.get(i) instanceof BlockInstruction){
                checkReturnStatement(type_expected,(BlockInstruction) n.children.get(i));
            }
        }
        else if(node.children.get(0) instanceof ForStatement){
            Node n = node.children.get(0);
            int i =3;
            while (n.children.get(i)!=null && n.children.get(i) instanceof BlockInstruction){
                checkReturnStatement(type_expected,(BlockInstruction) n.children.get(i));
            }
        }
        else if(node.children.get(0) instanceof IfStatement){
            Node n = node.children.get(0);
            int i =2;
            while (n.children.get(i)!=null && n.children.get(i) instanceof BlockInstruction){
                checkReturnStatement(type_expected,(BlockInstruction) n.children.get(i));
            }
        }
    }

    String getType(Expression expression) throws SemanticException {
        ArrayList<Node> listExpression = expression.children;
        for (Node node : listExpression){
            if(node instanceof FunctionCall){
                return getReturnType((FunctionCall) node,root);
            }
            else if(node instanceof ArrayAndStructAccess){

            }else if(node instanceof ArrayElementAccess){

            }else if(node instanceof StructFieldAccess){

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
                throw new SemanticException("No Matching Type");
            }
        }
        return " blabl ";
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
        System.out.println(leftOperator);
        System.out.println(rightOperator);
        if(!leftOperator.equals(rightOperator)){
            throw new SemanticException("OperatorError");
        }
    }

    void isTheSameType(String left, String right) throws SemanticException {

        if(!left.equals(right)){
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
        else{
            throw new SemanticException("No declaration of the variable 2");
        }
        return null;
    }



    void checkTypesConditionStatement(Expression conditionStatement) throws SemanticException {
        Boolean condition = isItACondition((Expression) conditionStatement);
        if (!condition && !getType((Expression)conditionStatement).equals("bool")) {
            throw new SemanticException("MissingConditionError");
        }


    }

    void checkBooleanCondition(Node booleanExpression){
        ArrayList<Node> expressionChildren = booleanExpression.children;
        //regarder si il y a == ,<,>,!=, true, false, =<,=>
        //function call qui retourne un boolean
        for(Node nodeChildren : expressionChildren){
            if(nodeChildren instanceof FunctionCall){
                //comment retrouver le returnType en fonction de la fonctionCall et pas la method
            }else if (nodeChildren instanceof BinaryExpression){
                ArrayList<Node> childrenBinaryExpression = nodeChildren.children;
                if(childrenBinaryExpression.get(1) instanceof BinaryOperator){
                    ArrayList<String> comparisonOperator= new ArrayList<>(Arrays.asList("==", "<", ">", "<=", ">="));
                }
            }
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


}


