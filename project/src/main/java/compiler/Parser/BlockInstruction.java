package compiler.Parser;

import compiler.Lexer.Symbol;

import java.io.IOException;
import java.util.ArrayList;

import static compiler.Parser.Parser.match;

public class BlockInstruction extends Node {

    public BlockInstruction(Node instruction){
        super("BlockInstruction",new ArrayList<>());
        this.children.add(instruction);
    }
}
