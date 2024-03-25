package compiler.Parser;

import java.util.ArrayList;

public class Assignment extends Node{

    public Assignment(Node node,Expression expression){
        super("Assignement",new ArrayList<>());
        this.children.add(node);
        this.children.add(expression);
    }

    public Assignment(Node node,Node node2, Expression expression){
        super("Assignement",new ArrayList<>());
        this.children.add(node);
        this.children.add(node2);
        this.children.add(expression);
    }

}
