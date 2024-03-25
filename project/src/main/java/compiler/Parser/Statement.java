package compiler.Parser;

import jdk.nashorn.internal.runtime.ParserException;

import java.util.ArrayList;

public class Statement extends Node{

    public Statement(Node statement){
        super("Statement",new ArrayList<>());
        this.children.add(statement);
    }



}

