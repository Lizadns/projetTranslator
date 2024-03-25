package compiler.Parser;

import java.util.ArrayList;

public class ArrayAndStructAccess extends Node{
    public ArrayAndStructAccess(String arrayname, Expression e, String identifier ){
        super("ArrayAndStructAccess",new ArrayList<>());
        this.children.add(new Leaf(arrayname));
        this.children.add(e);
        this.children.add(new Leaf(identifier));
    }
}
