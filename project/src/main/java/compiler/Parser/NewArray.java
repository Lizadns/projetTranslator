package compiler.Parser;

import java.util.ArrayList;

public class NewArray extends Node{
    public NewArray(Type type, Expression e){
        super("NewArray", new ArrayList<>());
        this.children.add(type);
        this.children.add(e);
    }

}
