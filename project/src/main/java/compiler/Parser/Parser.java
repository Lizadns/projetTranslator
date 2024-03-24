package compiler.Parser;

import compiler.Lexer.Lexer;
import compiler.Lexer.Symbol;
import jdk.nashorn.internal.runtime.ParserException;

import java.io.IOException;
import java.util.ArrayList;

import compiler.Parser.Program;


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

    public static Symbol matchValue(String value) throws ParserException, IOException {
        if(!lookahead.value.equals(value)) {
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

    public static Program parseProgram() throws ParserException, IOException{
        ArrayList<Declaration> declarations = parseDeclaration();
        ArrayList<Statement> statements= parseStatement();
        return new Program(declarations, statements);
    }

    public static ArrayList<Declaration> parseDeclaration() throws ParserException, IOException{
        ArrayList<Declaration> declarations = new ArrayList<>();
        while (lookahead.value.equals("struct") || lookahead.value.equals("final") || lookahead.type.equals("BaseType") || lookahead.type.equals("Identifier")){
            if (lookahead.type.equals("final")) {
                declarations.add(parseConstantDeclaration());
            } else if (lookahead.type.equals("struct")) {
                declarations.add(parseStructDeclaration());
            } else {
                declarations.add(parseGlobalVariableDeclaration());
            }
        }
        return declarations;

    }
    public static Declaration parseConstantDeclaration() throws ParserException, IOException {
        matchValue("final");
        Type type = parseType();
        Symbol identifier = match("Identifier");
        match("AssignmentOperator");
        Expression expression = parseExpression();
        return new Declaration(new ConstantDeclaration(type,identifier.value,expression));
    }
    public static Declaration parseStructDeclaration() throws ParserException, IOException {
        matchValue("struct");
        Symbol identifier = match("Identifier");


    }

    public static Declaration parseGlobalVariableDeclaration() throws ParserException, IOException {

    }

    public static Expression parseExpression() throws ParserException, IOException{

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
            if(lookahead.type.equals("KeywordCondition")){
                IfStatement statement = parseIfStatement();
                blockInstructions.add(new BlockInstruction(statement));
            }else if (lookahead.type.equals("KeywordWhile")){
                WhileStatement statement = parseWhileStatement();
                blockInstructions.add(new BlockInstruction(statement));
            }
            else if (lookahead.type.equals("KeywordFor")){
                ForStatement statement = parseForStatement();
                blockInstructions.add(new BlockInstruction(statement));
            }
            else if (lookahead.type.equals("KeywordReturn")){
                ReturnStatement statement = parseReturnStatement();
                blockInstructions.add(new BlockInstruction(statement));
            }else if (lookahead.type.equals("Identifier") || lookahead.type.equals("BaseType")){
                if(lookahead.type.equals("Identifier")){
                    String IdentifierName = lookahead.value;
                    match("Identifier");
                    if(lookahead.type.equals("OpenParenthesis")){
                        FunctionCall functionCall = parseFunctionCall(IdentifierName);
                        blockInstructions.add(new BlockInstruction(functionCall));
                    }else if (lookahead.type.equals("[")){
                        match("SpecialCharacter");//CHANGER DANS LE LEXER en OPENCROCHET
                        match("SpecialCharacter");//CLOSECROCHET
                        if(lookahead.type.equals("Indentifier")){
                            //A FINIR
                        }
                    }
                }
            }
        }
        match("ClosingHook");
        return blockInstructions;
    }
    public static ArrayList<Statement> parseStatement() throws ParserException, IOException {
        ArrayList<Statement> statements = new ArrayList<>();
        while(lookahead!=null){
            if(lookahead.type.equals("KeywordMethod")){
                Method method = parseMethod();
                statements.add(new Statement(method));
            }
            else if (lookahead.type.equals("KeywordCondition")){
                IfStatement ifStatement = parseIfStatement();
                statements.add(new Statement(ifStatement));
            }else if (lookahead.type.equals("KeywordWhile")){
                WhileStatement whileStatement = parseWhileStatement();
                statements.add(new Statement(whileStatement));
            }
            else if (lookahead.type.equals("KeywordFor")){
                ForStatement forStatement = parseForStatement();
                statements.add(new Statement(forStatement));
            }
            else if (lookahead.type.equals("Identifier") || lookahead.type.equals("BaseType")){
                if(lookahead.type.equals("Identifier")){
                    String IdentifierName = lookahead.value;
                    match("Identifier");
                    if(lookahead.type.equals("OpenParenthesis")){
                        FunctionCall functionCall = parseFunctionCall(IdentifierName);
                        statements.add(new Statement(functionCall));
                    }else if (lookahead.type.equals("[")){
                        match("SpecialCharacter");//CHANGER DANS LE LEXER en OPENCROCHET
                        match("SpecialCharacter");//CLOSECROCHET
                        if(lookahead.type.equals("Indentifier")){
                    //A FINIR
                        }
                    }
                }
            }
        }
    }

    public static FunctionCall parseFunctionCall(String nameCall) throws ParserException,IOException {
        match("OpenParenthesis");
        ArrayList<Argument> arguments = parseArguments();
        match("ClosingParenthesis");
        return new FunctionCall(nameCall,arguments);

    }
    public static ArrayList<Argument> parseArguments() throws ParserException,IOException {
        ArrayList<Argument> arguments = new ArrayList<>();
        while(!lookahead.type.equals("ClosingParenthesis") && lookahead!=null){
            Expression expression = parseExpression();
            arguments.add(new Argument(expression));
        }
        match("ClosingParenthesis");
    }

    public static IfStatement parseIfStatement() throws ParserException,IOException{

    }
    public static WhileStatement parseWhileStatement() throws ParserException,IOException{
        match("KeywordWhile");
        match("OpenParenthesis");
        Expression expression = parseExpression();
        match("ClosingParenthesis");
        ArrayList<BlockInstruction> body = parseBlock();
        return new WhileStatement(expression,body);
    }


}
