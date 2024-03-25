package compiler.Parser;

import java.util.ArrayList;

public class UnaryExpression extends Node{

    public UnaryExpression(UnaryOperator u, Expression e){
        super("UnaryExpression", new ArrayList<>());
        this.children.add(u);
        this.children.add(e);
    }
}
