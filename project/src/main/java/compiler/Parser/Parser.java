package compiler.Parser;

import compiler.Lexer.Lexer;
import compiler.Lexer.Symbol;
import jdk.nashorn.internal.runtime.ParserException;

import java.io.IOException;
import java.util.ArrayList;

import compiler.Parser.Program;

import static compiler.Parser.BlockInstruction.parseBlock;

public class Parser {
    static Lexer lexer;
    static Symbol lookahead;

    public Parser(Lexer lexer){
        this.lexer=lexer;
    }
    void main() throws ParserException, IOException {
        lookahead=lexer.getNextSymbol();
    }

    public static Symbol match(String type) throws ParserException, IOException {
        if(!lookahead.type.equals(type)) {
            throw new ParserException("No match");
        } else {
            Symbol matchingSymbol = lookahead;
            lookahead = lexer.getNextSymbol();
            return matchingSymbol;
        }
    }

    public static Symbol match2(String type1, String type2) throws ParserException, IOException {
        if(!lookahead.type.equals(type1) && !lookahead.type.equals(type2)) {
            throw new ParserException("No match");
        } else {
            Symbol matchingSymbol = lookahead;
            lookahead = lexer.getNextSymbol();
            return matchingSymbol;
        }
    }

    public static Program parseProgram() throws ParserException{
        ArrayList<Declaration> declarations = parseDeclaration();
        ArrayList<Statement> statements= parseStatement();
        return new Program(declarations, statements);
    }

    public static ArrayList<Declaration> parseDeclaration() throws ParserException{
        ArrayList<Declaration> declarations = new ArrayList<>();

    }
    public static ArrayList<Statement> parseStatement() throws ParserException{
        ArrayList<Statement> statements = new ArrayList<>();
    }

    static Type parseType() throws ParserException, IOException {
        Symbol type = match2("Identifier","BaseType");    //mais aussi basetype, est ce que faire 1 autref fonction match avec 2 arguments?
        return new Type(type.type,type.value);
    }

    static Param parseParam() throws ParserException, IOException {
        Type type = parseType();
        Symbol identifier = match("Identifier");
        return new Param(type,identifier.value);
    }

    static ArrayList<Param> parseParams() throws ParserException, IOException {
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
    public static Method parseMethod() throws ParserException, IOException {
        match("KeywordMethod");                 // method commence par def
        Type returnType = parseType();              // ensuite le returnType
        String name = (String)match("Identifier").value; //ensuite le nom de la methode
        match("OpenParenthesis");               //debut des parametres
        ArrayList<Param> params = parseParams();
        match("CloseParenthesis");              //fin des paramètres
        ArrayList<BlockInstruction> body = parseBlock();
        return new Method(name, returnType, params, body);
    }

    public static ArrayList<BlockInstruction>  parseBlock() throws IOException {
        ArrayList<BlockInstruction> blockInstructions = new ArrayList<>();
        match("OpeningHook");
        while(!lookahead.type.equals("ClosingHook") && !lookahead.equals(null)) {
            BlockInstruction instruction = parseSingleInstruction();
            blockInstructions.add(instruction);
        }
        match("ClosingHook");
        return blockInstructions;
    }

    public static BlockInstruction parseSingleInstruction(){
        if(lookahead.type.equals("KeywordCondition")){
            IfStatement statement = parseIfStatement();
            return new BlockInstruction(statement);
        }else if (lookahead.type.equals("KeywordWhile")){
            WhileStatement statement = parseWhileStatement();
            return new BlockInstruction(statement);
        }
        else if (lookahead.type.equals("KeywordFor")){
            ForStatement statement = parseForStatement();
            return new BlockInstruction(statement);
        }
        else if (lookahead.type.equals("KeywordReturn")){
            ReturnStatement statement = parseReturnStatement();
            return new BlockInstruction(statement);
        }
        else {
            try{
                VariableDeclaration statement = parseVariableDeclaration();
                return new BlockInstruction(statement);
            }catch (ParserException e0){
                try {
                    GlobalDeclaration statement = parseGlobalDeclaration();
                    return new BlockInstruction(statement);
                }catch (ParserException e1){
                    try {
                        FunctionCall statement = parseFunctionCall();
                        return new BlockInstruction(statement);
                    }catch (ParserException e2){
                        try{
                            Assignment statement = parseAssignement();
                            return new BlockInstruction(statement);
                        }catch (ParserException e4){
                            return null;
                        }

                    }
                }
            }

        }

    }
    public static IfStatement parseIfStatement(){

    }

}
