package compiler.Parser;

import compiler.Lexer.Symbol;
import jdk.nashorn.internal.runtime.ParserException;

import java.io.IOException;
import java.util.ArrayList;
public class Method {
    Type returnType;
    String nameMethod;
    ArrayList<Param> params;
    ArrayList<BlockInstruction> body;

    public Method( String nameMethod,Type returnType, ArrayList<Param> params, ArrayList<BlockInstruction> body){
        this.returnType=returnType;
        this.nameMethod=nameMethod;
        this.params=params;
        this.body=body;
    }



}
