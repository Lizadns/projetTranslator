package compiler.SemanticAnalysis;
import compiler.Parser.*;

import java.util.ArrayList;

public class SemanticAnalysis {

    private Node root;
    public void SemanticAnalyzer(Node root) {
        this.root = root;
    }

    private void analyzeNode(Node node) throws SemanticException {
        ArrayList<Node> listNode = node.children;
        for(Node nodeChildren : listNode){
            if (nodeChildren instanceof Declaration) {
                checkTypesDeclaration( nodeChildren);
            }
            else if (nodeChildren instanceof Statement) {
                checkTypesStatement(nodeChildren);
            }
            else{
                throw new SemanticException("No Declaration or Statement");
            }
        }
    }

    void checkTypesDeclaration(Node declaration){

    }

    void checkTypesStatement(Node statement){
        ArrayList<Node> StatementNodes = statement.children;
        for(Node nodeChildren : StatementNodes){
            if(nodeChildren instanceof IfStatement){
                checkTypesIfStatement(nodeChildren);
            }else if(nodeChildren instanceof WhileStatement){

            }
        }
    }

    void checkTypesIfStatement(Node ifStatement){
        ArrayList<Node> ifNodes = ifStatement.children;
        for(Node nodeChildren : ifNodes){
            if(nodeChildren instanceof Expression){
                //verifier que c un truc boolean
                checkBooleanCondition(nodeChildren);
            }else if(nodeChildren instanceof BlockInstruction){

            }
        }
    }
    void checkBooleanCondition(Node booleanExpression){
        ArrayList<Node> expressionChildren = booleanExpression.children;
        //regarder si il y a == ,<,>,!=, true, false, =<,=>
        //function call qui retourne un boolean
    }


}

class SemanticException extends Exception {
    public SemanticException(String message) {
        super(message);
    }
}
