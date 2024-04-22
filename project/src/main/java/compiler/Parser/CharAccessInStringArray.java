package compiler.Parser;

import java.util.ArrayList;

public class CharAccessInStringArray extends Node{
    public CharAccessInStringArray(String s,Expression expression1, Expression expression2) {
        super("CharAccessInStringArray", new ArrayList<>());
        this.children.add(new Leaf(s));
        this.children.add(expression1);
        this.children.add(expression2);
    }
}
