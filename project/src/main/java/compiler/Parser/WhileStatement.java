package compiler.Parser;

import java.util.ArrayList;

public class WhileStatement extends Node{

    public WhileStatement(Expression expression, ArrayList<BlockInstruction> body){
        super("WhileStatement", new ArrayList<>());
        this.children.add(expression);
        this.children.addAll(body);
    }
}
