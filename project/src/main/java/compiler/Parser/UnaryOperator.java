package compiler.Parser;

import java.util.ArrayList;

public class UnaryOperator extends Node{

    public UnaryOperator(String o){
        super("UnaryOperation",new ArrayList<>());
        this.children.add(new Leaf(o));
    }
}
