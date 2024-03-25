package compiler.Parser;

import java.util.ArrayList;

public class Expression extends Node{
    public Expression(Node child) {
        super("Expression", new ArrayList<>());
        this.children.add(child);
    }
}
