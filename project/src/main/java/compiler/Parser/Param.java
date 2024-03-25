package compiler.Parser;
import compiler.Lexer.Symbol;
import java.io.IOException;
import java.util.ArrayList;

import static compiler.Parser.Parser.match;

public class Param extends Node{
    Type type;
    String name;

    public Param(Type type, String name){
        super("Param",new ArrayList<>());
        this.children.add(type);
        this.children.add(new Leaf(name));
    }



}
