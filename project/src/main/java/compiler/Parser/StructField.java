package compiler.Parser;

import java.util.ArrayList;

public class StructField extends Node{

    public StructField(Type t, String i){
        super("StructField", new ArrayList<>());
        this.children.add(t);
        this.children.add(new Leaf(i));
    }
}
