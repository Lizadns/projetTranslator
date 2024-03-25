package compiler.Parser;

import java.util.ArrayList;

public class GlobalDeclaration extends Node{


    public GlobalDeclaration(Type t , String i, Expression e){
        super("GlobalDeclaration", new ArrayList<>());
        this.children.add(t);
        this.children.add(new Leaf(i));
        this.children.add(e);
    }
}
