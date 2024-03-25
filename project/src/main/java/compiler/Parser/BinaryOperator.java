package compiler.Parser;

import java.util.ArrayList;

public class BinaryOperator extends Node{

    public BinaryOperator(String s){
        super("BinaryOperator", new ArrayList<>());
        this.children.add(new Leaf(s));
    }
}
