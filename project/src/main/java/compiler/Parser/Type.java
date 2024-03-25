package compiler.Parser;

import compiler.Lexer.Symbol;

import java.io.IOException;
import java.util.ArrayList;

import static compiler.Parser.Parser.match2;
import static compiler.Parser.Parser.match;

public class Type extends Node{

    public Type(String value){
        super("Type",new ArrayList<>());
        this.children.add(new Leaf(value));
    }

}
