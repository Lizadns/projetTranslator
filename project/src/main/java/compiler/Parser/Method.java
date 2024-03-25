package compiler.Parser;

import compiler.Lexer.Symbol;

import java.io.IOException;
import java.util.ArrayList;
public class Method extends Node{

    public Method( String nameMethod,Type returnType, ArrayList<Param> params, ArrayList<BlockInstruction> body){
        super("Method",new ArrayList<>());
        Node n = new Node("NameMethod",new ArrayList<>());
        n.children.add(new Leaf(nameMethod));
        this.children.add(n);
        this.children.add(returnType);
        this.children.addAll(params);
        this.children.addAll(body);
    }



}
