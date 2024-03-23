package compiler.Parser;

import java.util.ArrayList;

public class Program {
    ArrayList<Declaration> declarations;
    ArrayList<Statement> statements;

    public Program(ArrayList<Declaration> d, ArrayList<Statement> s){
        this.declarations=d;
        this.statements=s;
    }
}
