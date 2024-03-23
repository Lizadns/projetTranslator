package compiler.Parser;

import jdk.nashorn.internal.runtime.ParserException;

import java.util.ArrayList;

public class Statement {
    Assignment assignment;
    Method method;
    FunctionCall functionCall;
    IfStatement ifstatement;
    WhileStatement whileStatement;
    ForStatement forStatement;
    VariableDeclaration variableDeclaration;
    GlobalDeclaration globaldeclaration;

    public Statement(Assignment assignment){

        this.assignment=assignment;
    }
    public Statement(Method method){
        this.method=method;
    }
    public Statement(FunctionCall functionCall){
        this.functionCall=functionCall;
    }
    public Statement(IfStatement ifstatement){
        this.ifstatement=ifstatement;
    }
    public Statement(WhileStatement whileStatement){
        this.whileStatement=whileStatement;
    }
    public Statement(ForStatement forStatement){
        this.forStatement=forStatement;
    }
    public Statement(VariableDeclaration variableDeclaration){
        this.variableDeclaration=variableDeclaration;
    }
    public Statement(GlobalDeclaration globaldeclaration){
        this.globaldeclaration=globaldeclaration;
    }


}

