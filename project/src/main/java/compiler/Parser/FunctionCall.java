package compiler.Parser;

import java.util.ArrayList;

public class FunctionCall extends Node{
    public FunctionCall(String name,ArrayList<Argument> arguments){
        super("FunctionCall", new ArrayList<>());
        this.children.add(new Leaf(name));
        this.children.addAll(arguments);
    }
}
