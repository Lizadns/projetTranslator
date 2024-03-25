package compiler.Parser;


import java.util.ArrayList;

public class BinaryExpression extends Node{

    public BinaryExpression(Expression e1, BinaryOperator b, Expression e2){
        super("BinaryExpression", new ArrayList<>());
        this.children.add(e1);
        this.children.add(b);
        this.children.add(e2);

    }
}
