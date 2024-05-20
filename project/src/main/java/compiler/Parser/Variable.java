package compiler.Parser;

import java.util.ArrayList;

public class Variable extends Node{

    public Variable(String name){
        super("Variable",new ArrayList<>());
        this.children.add(new Leaf(name));

    }
    public void addType(String type){
        this.children.add(new Type(type));
    }
}
