package compiler.Parser;
import compiler.Lexer.Symbol;
import jdk.nashorn.internal.runtime.ParserException;
import java.io.IOException;
import java.util.ArrayList;

import static compiler.Parser.Parser.match;

public class Param {
    Type type;
    String name;

    public Param(Type type, String name){
        this.type=type;
        this.name=name;
    }



}
