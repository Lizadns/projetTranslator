package compiler.Parser;

import java.util.ArrayList;

public class ArrayElementAccess extends Node{

    public ArrayElementAccess(String arrayName, Expression expression) {
        super("ArrayElementAccess",new ArrayList<>());
        this.children.add(new Leaf(arrayName));
        this.children.add(expression);
    }
}
