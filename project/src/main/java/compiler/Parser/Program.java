package compiler.Parser;

import java.util.ArrayList;

public class Program extends Node {

    public Program(Node d, Node s){
        super("Program",new ArrayList<>());
        this.children.add(d);
        this.children.add(s);
    }
}
