package compiler.Parser;

import compiler.Lexer.Symbol;
import jdk.nashorn.internal.runtime.ParserException;

import java.io.IOException;

import static compiler.Parser.Parser.match2;
import static compiler.Parser.Parser.match;

public class Type {
    String type;
    String value;

    public Type(String type,String value){
        this.type=type;
        this.value=value;
    }

}
