package compiler.Parser;

import java.util.ArrayList;

public class Literal extends Node{

    public Literal(String value){
        super("Literal",new ArrayList<>());
        this.children.add(new Leaf(value));
    }

}
