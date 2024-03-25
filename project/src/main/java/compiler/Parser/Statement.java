package compiler.Parser;


import java.util.ArrayList;

public class Statement extends Node{

    public Statement(ArrayList<Node> statements){
        super("Statements",new ArrayList<>());
        this.children.addAll(statements);
    }



}

