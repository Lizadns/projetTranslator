package compiler.Parser;

import java.util.ArrayList;

public class ReturnStatement extends Node {

    public ReturnStatement(Expression expression){
        super("return",new ArrayList<>());
        this.children.add(expression);
    }
}
