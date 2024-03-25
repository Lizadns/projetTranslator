package compiler.Parser;

import org.checkerframework.checker.signature.qual.Identifier;

import java.util.ArrayList;

public class Free extends Node{

    public Free(Variable variableName){
        super("free",new ArrayList<>());
        this.children.add(variableName);

    }
}
