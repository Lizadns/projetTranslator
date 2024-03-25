package compiler.Parser;

import java.util.ArrayList;

public class Program extends Node {

    public Program(ArrayList<Declaration> d, ArrayList<Statement> s){
        super("Program",new ArrayList<>());
        this.children.addAll(d);
        this.children.addAll(s);
    }
}
