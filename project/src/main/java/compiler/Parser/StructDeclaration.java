package compiler.Parser;

import java.util.ArrayList;

public class StructDeclaration extends Node {

    public StructDeclaration(String i, ArrayList<StructField> s){
        super("StructDeclaration", new ArrayList<>());
        this.children.add(new Leaf(i));
        this.children.addAll(s);
    }
}
