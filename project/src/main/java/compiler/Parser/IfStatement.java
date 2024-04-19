package compiler.Parser;

import java.util.ArrayList;

public class IfStatement extends Node{

    public IfStatement(String type, Expression expression, BlockInstruction body){
        super("IfStatement",new ArrayList<>());
        this.children.add(new Leaf(type));
        this.children.add(expression);
        this.children.add(body);
    }

}
