package compiler.Parser;

import java.util.ArrayList;

public class Argument extends Node{
    public Argument(Expression expression){
        super("Argument", new ArrayList<>());
        this.children.add(expression);
    }
}
