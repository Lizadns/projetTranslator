package compiler.Parser;

import java.util.ArrayList;

public class WhileStatement extends Node{

    public WhileStatement(Expression expression, BlockInstruction body){
        super("WhileStatement", new ArrayList<>());
        this.children.add(expression);
        this.children.add(body);
    }
}
