package compiler.Parser;

import java.util.ArrayList;

class Declaration extends Node {
    public Declaration(Node child) {
        super("Declaration", new ArrayList<>());
        this.children.add(child);
    }
}
