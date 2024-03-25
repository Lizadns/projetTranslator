package compiler.Parser;

import java.util.ArrayList;

public class ConstantDeclaration extends Node{

    public ConstantDeclaration (Type t, String i, Expression e){
        super("ConstantDeclaration", new ArrayList<>());
        this.children.add(t);
        this.children.add(new Leaf(i));
        this.children.add(e);
    }
}
