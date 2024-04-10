package compiler.Parser;

import java.util.ArrayList;

public class Declaration extends Node {
    public Declaration(ArrayList<Node> declarations) {
        super("Declarations", new ArrayList<>());
        this.children.addAll(declarations);
    }
}
