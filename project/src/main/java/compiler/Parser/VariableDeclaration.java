package compiler.Parser;

import java.util.ArrayList;

public class VariableDeclaration extends Node{

    public VariableDeclaration(Type type, Variable nameVariable){
        super("VariableDeclaration",new ArrayList<>());
        this.children.add(type);
        this.children.add(nameVariable);
    }

}
