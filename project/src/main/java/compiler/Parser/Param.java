package compiler.Parser;
import compiler.Lexer.Symbol;
import jdk.nashorn.internal.runtime.ParserException;
import java.io.IOException;
import java.util.ArrayList;

import static compiler.Parser.Parser.match;
import static compiler.Parser.Type.parseType;

public class Param {
    Type type;
    String name;

    public Param(Type type, String name){
        this.type=type;
        this.name=name;
    }

    static Param parseParam() throws ParserException, IOException {
        Type type = parseType();
        Symbol identifier = match("Identifier");
        return new Param(type,identifier.value);
    }

    ArrayList<Param> parseParams(Symbol lookahead) throws ParserException, IOException {
        ArrayList<Param> parameters = new ArrayList<>();
        if(!lookahead.value.equals(")")) {
            parameters.add(parseParam());
            while(lookahead.type.equals("Comma")) {
                match("Comma");
                parameters.add(parseParam());
            }
        }
        return parameters;
    }


}
