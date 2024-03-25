package compiler.Parser;

import compiler.Lexer.Symbol;
import jdk.nashorn.internal.runtime.ParserException;

import java.io.IOException;
import java.util.ArrayList;

import static compiler.Parser.Parser.match;

public class BlockInstruction {
    Assignment assignment;
    FunctionCall functionCall;
    IfStatement ifstatement;
    WhileStatement whileStatement;
    ForStatement forStatement;
    VariableDeclaration variableDeclaration;
    ReturnStatement returnStatement;
    GlobalDeclaration globaldeclaration;

    Declaration declaration;

    public BlockInstruction(Assignment assignment){

        this.assignment=assignment;
    }
    public BlockInstruction(FunctionCall functionCall){
        this.functionCall=functionCall;
    }
    public BlockInstruction(IfStatement ifstatement){
        this.ifstatement=ifstatement;
    }
    public BlockInstruction(WhileStatement whileStatement){
        this.whileStatement=whileStatement;
    }
    public BlockInstruction(ForStatement forStatement){
        this.forStatement=forStatement;
    }
    public BlockInstruction(VariableDeclaration variableDeclaration){
        this.variableDeclaration=variableDeclaration;
    }
    public BlockInstruction(ReturnStatement returnStatement){
        this.returnStatement=returnStatement;
    }
    public BlockInstruction(GlobalDeclaration globaldeclaration){
        this.globaldeclaration=globaldeclaration;
    }
    public BlockInstruction(Declaration declaration){
        this.declaration=declaration;
    }
}
