package compiler.Parser;

import java.util.ArrayList;

public class StructFieldAccess extends Node{

    public StructFieldAccess(String structname, String identifier){
        super("StructFieldAccess",new ArrayList<>());
        this.children.add(new Leaf(structname));
        this.children.add(new Leaf(identifier));
    }
}
