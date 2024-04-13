package compiler.Parser;


import compiler.Lexer.Lexer;
import compiler.Lexer.Symbol;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;


public class Parser {
    static Lexer lexer;
    static Symbol lookahead;

    public Parser(Lexer lexer){
        this.lexer=lexer;
    }

    public Program getAST() throws IOException {
        lookahead = lexer.getNextSymbol(); // Initialise le symbole de lookahead avec le premier symbole du flux d'entrée
        // Commence l'analyse syntaxique du programme
        return parseProgram();
    }

    public static Symbol match(String type) throws IOException {
        if(!lookahead.type.equals(type)) {
            throw new RuntimeException("No match " + lookahead.value);
        } else {
            Symbol matchingSymbol = lookahead;
            lookahead = lexer.getNextSymbol();
            return matchingSymbol;
        }
    }

    public static Symbol matchValue(String value) throws IOException {
        if(!lookahead.value.equals(value)) {
            throw new RuntimeException("No match " + lookahead.value);
        } else {
            Symbol matchingSymbol = lookahead;
            lookahead = lexer.getNextSymbol();
            return matchingSymbol;
        }
    }

    public static Symbol match2(String type1, String type2) throws IOException {
        if(!lookahead.type.equals(type1) && !lookahead.type.equals(type2)) {
            throw new RuntimeException("No match " + lookahead.value + " or " + lookahead.type);
        } else {
            Symbol matchingSymbol = lookahead;
            lookahead = lexer.getNextSymbol();
            return matchingSymbol;
        }
    }

    public static Symbol match4(String type1, String type2,String type3,String type4) throws IOException {
        if(!lookahead.type.equals(type1) && !lookahead.type.equals(type2)&& !lookahead.type.equals(type3)&& !lookahead.type.equals(type4)) {
            throw new RuntimeException("No match " + lookahead.value);
        } else {
            Symbol matchingSymbol = lookahead;
            lookahead = lexer.getNextSymbol();
            return matchingSymbol;
        }
    }

    public static Program parseProgram() throws IOException{
        Node declarations = parseDeclaration();
        Node statements= parseStatement();
        return new Program(declarations, statements);
    }

    public static Declaration parseDeclaration() throws IOException{
        ArrayList<Node> declarations = new ArrayList<>();
        while (lookahead!=null && ( lookahead.value.equals("struct") || lookahead.value.equals("final") || lookahead.type.equals("BaseType") || lookahead.type.equals("Identifier"))){
            if (lookahead.value.equals("final")) {
                declarations.add(parseConstantDeclaration());
            } else if (lookahead.value.equals("struct")) {
                declarations.add(parseStructDeclaration());
            } else {

                Symbol ancien = lookahead;
                Symbol s = match2("Identifier","BaseType");
                if(lookahead.value.equals("[")){
                    matchValue("[");
                    if(!lookahead.value.equals("]")){ //si a[3] par ex.
                        Lexer.addAtBeginning(lookahead.value);
                        Lexer.addAtBeginning("[");
                        lookahead=ancien;
                        break;
                    }
                    else{
                        Lexer.addAtBeginning("]");
                        Lexer.addAtBeginning("[");
                        lookahead=ancien;
                    }
                }
                else {
                    Lexer.addAtBeginning(lookahead.value);
                    lookahead = ancien;
                }
                Object[] type = parseType2();
                if (lookahead.type.equals("AssignmentOperator")||lookahead.value.equals(".")){ //si direct "a =1;"
                    Lexer.addAtBeginning(lookahead.value);
                    lookahead= ancien;
                    break;
                }
                else {
                    Symbol identifier = match("Identifier");
                    if (lookahead.type.equals("AssignmentOperator")) {
                        declarations.add(parseGlobalVariableDeclaration((Type)type[0], identifier));
                    } else { // si c'est "int a;" par ex.
                        Lexer.addAtBeginning(lookahead.value);
                        Lexer.addAtBeginning(identifier.value);
                        if((boolean)type[1]){
                            Lexer.addAtBeginning("]");
                            Lexer.addAtBeginning("[");
                        }
                        lookahead = ancien;

                        break;
                    }
                }
            }
        }
        return new Declaration(declarations);

    }
    public static ConstantDeclaration parseConstantDeclaration() throws IOException {
        matchValue("final");
        Type type = parseBaseType(); //seulement des basesType pour les final declaration
        Symbol identifier = match("Identifier");
        match("AssignmentOperator");
        Expression expression = parseExpression();
        matchValue(";");
        return new ConstantDeclaration(type,identifier.value,expression);
    }
    public static StructDeclaration parseStructDeclaration() throws IOException {
        matchValue("struct");
        Symbol identifier = match("Identifier");
        match("OpeningBrace");
        ArrayList<StructField> structFields = parseStructFields();
        match("ClosingBrace");
        return new StructDeclaration(identifier.value, structFields);
    }

    public static ArrayList<StructField> parseStructFields() throws IOException{
        ArrayList<StructField> structFields= new ArrayList<>();
        while (!lookahead.value.equals("}")){
            Object[] type = parseType2(); //type baseType, struct ou tableaux
            Symbol identifier = match("Identifier");
            matchValue(";");
            structFields.add(new StructField((Type)type[0], identifier.value));
        }
        return structFields;
    }


    public static GlobalDeclaration parseGlobalVariableDeclaration(Type type, Symbol identifier) throws IOException {
        match("AssignmentOperator");
        Expression expression = parseExpression();
        matchValue(";");
        if((expression.children.get(0) instanceof ArrayElementAccess) && type.children.get(0).value.contains("[]")){
            return new GlobalDeclaration(type, identifier.value, new Expression(new NewArray(new Type(expression.children.get(0).children.get(0).value),(Expression) expression.children.get(0).children.get(1))));
        }
        return new GlobalDeclaration(type, identifier.value, expression);
    }

    public static Expression parseExpression() throws IOException {
        Expression expression = parseTerm();

        while (lookahead.type.equals("ArithmeticOperator") || lookahead.type.equals("ComparisonOperator") ||lookahead.type.equals("AndOperator")||lookahead.type.equals("OrOperator") ) {
            Symbol operator = match4("ArithmeticOperator","ComparisonOperator","AndOperator","OrOperator");
            Expression rightTerm = parseTerm();
            BinaryExpression b = new BinaryExpression(expression, new BinaryOperator(operator.value), rightTerm);
            expression= new Expression(b);
        }

        return expression;
    }

    public static Expression parseTerm() throws IOException {
        if (lookahead.type.equals("Identifier")) {
            Symbol identifier =match("Identifier");
            if(lookahead.type.equals("OpenParenthesis")){ //Expression Function call
                FunctionCall functionCall = parseFunctionCall(identifier.value);
                return new Expression(functionCall);
            }
            else if (lookahead.value.equals("[")){
                Symbol open = matchValue("[");
                Expression e= parseExpression();
                Symbol close= matchValue("]");
                if(lookahead.value.equals(".")){
                    matchValue(".");
                    Symbol identifier2 = match("Identifier");
                    return new Expression(new ArrayAndStructAccess(identifier.value,e, identifier2.value));
                }
                return new Expression(new ArrayElementAccess(identifier.value,e));
            }
            else if(lookahead.value.equals(".")){
                matchValue(".");
                Symbol identifier2 = match("Identifier");
                return new Expression(new StructFieldAccess(identifier.value, identifier2.value));
            }
            Variable v = new Variable(identifier.value);
            return new Expression(v);
        } else if (lookahead.value.equals("(")) {
            // If the expression starts with a parenthesis, parse the nested expression
            matchValue("(");
            Expression nestedExpression = parseExpression();
            matchValue(")");
            return nestedExpression;
        }else if (lookahead.type.equals("Number")){
            Symbol number =match("Number");
            if (lookahead.value.equals(".")) {
                matchValue(".");
                Symbol number2 = match("Number");
                String f = number.value + "." + number2.value;
                return new Expression(new Literal(f));
            }
            return new Expression(new Literal(number.value));
        }else if (lookahead.type.equals("Boolean")){
            Symbol bool =match("Boolean");
            return new Expression(new Literal(bool.value));
        }else if (lookahead.type.equals("String")){
            Symbol string = match("String");
            return new Expression(new Literal(string.value));
        }else if (lookahead.value.equals("!")){ //UnaryExpression !
            UnaryOperator u= new UnaryOperator(lookahead.value);
            match("!");
            Expression e = parseExpression();
            return new Expression(new UnaryExpression(u,e));
        }else if (lookahead.type.equals("BaseType")){
            Symbol type = match("BaseType");
            Symbol open = matchValue("[");
            Expression e= parseExpression();
            Symbol close= matchValue("]");
            return new Expression(new NewArray(new Type(type.value),e));
        }
        else{ //UnaryExpression -
            UnaryOperator u= new UnaryOperator(lookahead.value);
            matchValue("-");
            Expression e = parseExpression();
            return new Expression(new UnaryExpression(u,e));
        }
    }

    public static Statement parseStatement() throws IOException {
        ArrayList<Node> statements = new ArrayList<>();
        while(lookahead!=null){
            if(lookahead.value.equals("def")){
                Method method = parseMethod();
                statements.add(method);
            }else if(lookahead.value.equals("free")){
                Free freeStatement = parseFreeStatement();
                statements.add(freeStatement);
            }
            else if (lookahead.type.equals("KeywordCondition")){
                IfStatement ifStatement = parseIfStatement();
                statements.add(ifStatement);
            }else if (lookahead.type.equals("KeywordWhile")){
                WhileStatement whileStatement = parseWhileStatement();
                statements.add(whileStatement);
            }
            else if (lookahead.type.equals("KeywordFor")){
                ForStatement forStatement = parseForStatement();
                statements.add(forStatement);
            }
            else if (lookahead.type.equals("Identifier")){
                Symbol name = match("Identifier");
                if(lookahead.type.equals("OpeningParenthesis")){// Identifier(
                    FunctionCall functionCall = parseFunctionCall(name.value);
                    statements.add(functionCall);
                }else if (lookahead.type.equals("AssignmentOperator")||lookahead.value.equals(".")){
                    //si . dire que c'est un tableau dans parseAssignement
                    Assignment assignment;
                    if(lookahead.value.equals(".")){//Identifier.
                        assignment = parseAssignment(name.value, false, true, null);
                    }else{//Identifier =
                        assignment = parseAssignment(name.value, false, false, null);
                    }
                    statements.add(assignment);

                }
                else if(lookahead.type.equals("Identifier")){//Identifier Identifier
                    Symbol nameVariable = match("Identifier");
                    if(lookahead.type.equals("AssignmentOperator")){//Identifier Identifier = -> GD
                        Type type = new Type(name.value);
                        GlobalDeclaration globalDeclaration = parseGlobalVariableDeclaration(type, nameVariable);
                        statements.add(globalDeclaration);
                    }
                    else if (lookahead.value.equals(";")){//Identifier Identifier ; -> VD
                        Type type = new Type(name.value);
                        VariableDeclaration variableDeclaration = parseVariableDeclaration(type,nameVariable.value,false);
                        statements.add(variableDeclaration);
                    }
                }else if(lookahead.value.equals("[")){//Identifier [
                    matchValue("[");
                    if(lookahead.value.equals("]")){
                        matchValue("]");
                        if(lookahead.type.equals("Identifier")){//Identifier [] Identifier -> Declaration
                            Symbol nameVariable = match("Identifier");
                            if (lookahead.type.equals("AssignmentOperator")){// Identifier [] Identifier = -> GV
                                name.value = name.value + "[]";
                                Type type = new Type(name.value);
                                GlobalDeclaration globalDeclaration = parseGlobalVariableDeclaration(type, nameVariable);
                                statements.add(globalDeclaration);
                            }else if(lookahead.value.equals(";")) {
                                Type type = new Type(name.value);
                                VariableDeclaration variableDeclaration = parseVariableDeclaration(type,nameVariable.value,true);
                                statements.add(variableDeclaration);
                            }
                        }
                    }else{//Identifier [3
                        Expression expression = parseExpression();
                        matchValue("]");//Identifier[expression]
                        if(lookahead.value.equals(".")){//Identifier[3].
                            Assignment assignment = parseAssignment(name.value,true,true,expression);
                            statements.add(assignment);
                        }else if(lookahead.type.equals("AssignmentOperator")){//Identifier [expression] = ..;
                            Assignment assignment = parseAssignment(name.value,true,false,expression);
                            statements.add(assignment);
                        }
                    }
                }

            }else if( lookahead.type.equals("BaseType")){

                Type type = parseType();
                if(lookahead.type.equals("Identifier")){//BaseType Identifier

                    Symbol nameVariable = match("Identifier");
                    if(lookahead.type.equals("AssignmentOperator")){//BaseType Identifier = -> GD
                        GlobalDeclaration globalDeclaration = parseGlobalVariableDeclaration(type, nameVariable);
                        statements.add(globalDeclaration);
                    }
                    else if (lookahead.value.equals(";")){//BaseType Identifier ; -> VD

                        VariableDeclaration variableDeclaration = parseVariableDeclaration(type,nameVariable.value,false);
                        statements.add(variableDeclaration);
                    }

                }else if(lookahead.value.equals("[")){
                    matchValue("[");
                    matchValue("]");
                    Symbol nameVariable = match("Identifier");
                    if(lookahead.value.equals(";")){//BaseType[] Identifier ;
                        VariableDeclaration variableDeclaration = parseVariableDeclaration(type,nameVariable.value,true);
                        statements.add(variableDeclaration);
                    }else if(lookahead.type.equals("AssignmentOperator")){//BaseType[] Identifier =
                        type.value = type.value + "[]";
                        GlobalDeclaration globalDeclaration = parseGlobalVariableDeclaration(type, nameVariable);
                        statements.add(globalDeclaration);
                    }

                }
            }
            else{
                throw new RuntimeException("No match "+ lookahead.type + " " + lookahead.value);
            }
        } return new Statement(statements);
    }

    static Assignment parseAssignment(String variableName, Boolean isAnArray, Boolean isAnFildAcces, Expression expressionArray) throws IOException {
        if(isAnFildAcces && !isAnArray){//a.x=...;
            matchValue(".");
            Symbol identifierName = match("Identifier");
            match("AssignmentOperator");
            Expression expression = parseExpression();
            matchValue(";");
            return new Assignment(new StructFieldAccess(variableName,identifierName.value), expression);
        }
        else if(isAnArray && ! isAnFildAcces){//a[expressionArray]=.....;
            match("AssignmentOperator");
            Expression expression = parseExpression();
            matchValue(";");
            return new Assignment(new ArrayElementAccess(variableName,expressionArray),expression);

        }
        else if(isAnArray && isAnFildAcces){//a[expression].x
            matchValue(".");
            Symbol identifier = match("Identifier");
            match("AssignmentOperator");
            Expression expression = parseExpression();
            matchValue(";");
            return new Assignment(new ArrayAndStructAccess(variableName,expressionArray,identifier.value),expression);
        }
        else {
            match("AssignmentOperator");
            Expression expression = parseExpression();
            matchValue(";");
            return new Assignment(new Variable(variableName),expression);
        }
    }
    static Type parseType() throws IOException {
        Symbol type = match2("Identifier","BaseType");    //mais aussi basetype, est ce que faire 1 autref fonction match avec 2 arguments?
        return new Type(type.value);
    }

    static Type parseBaseType() throws IOException {
        Symbol type = match("BaseType");
        return new Type(type.value);
    }

    static Object[] parseType2() throws IOException {
        Symbol type = match2("Identifier", "BaseType");
        boolean isArray = lookahead.value.equals("[");
        if (isArray) {
            Symbol open = matchValue("[");
            Symbol close = matchValue("]");
            String s = type.value + open.value + close.value;
            return new Object[]{new Type(s), true};
        }
        return new Object[]{new Type(type.value), false};
    }

    static Param parseParam() throws IOException {
        Object[] type = parseType2();
        Symbol identifier = match("Identifier");
        return new Param((Type)type[0],identifier.value);
    }

    static ArrayList<Param> parseParams() throws IOException {
        ArrayList<Param> parameters = new ArrayList<>();
        if(!lookahead.value.equals(")")) {
            parameters.add(parseParam());
            while(lookahead.value.equals(",")) {
                matchValue(",");
                parameters.add(parseParam());
            }
        }
        return parameters;
    }
    public static Method parseMethod() throws IOException {
        matchValue("def");                 // method commence par def
        Type returnType = parseType();              // ensuite le returnType
        String name = (String)match("Identifier").value; //ensuite le nom de la methode
        matchValue("(");               //debut des parametres
        ArrayList<Param> params = parseParams();
        matchValue(")");              //fin des paramètres
        ArrayList<BlockInstruction> body = parseBlock();
        return new Method(name, returnType, params, body);
    }

    public static ArrayList<BlockInstruction>  parseBlock() throws IOException {
        ArrayList<BlockInstruction> blockInstructions = new ArrayList<>();
        matchValue("{");
        while(!lookahead.value.equals("}") && !lookahead.equals(null)) {
            if (lookahead.value.equals("if")||lookahead.value.equals("else")){
                IfStatement ifStatement = parseIfStatement();
                blockInstructions.add(new BlockInstruction(ifStatement));
            }else if (lookahead.value.equals("while")){
                WhileStatement whileStatement = parseWhileStatement();
                blockInstructions.add(new BlockInstruction(whileStatement));
            }
            else if (lookahead.value.equals("for")){
                ForStatement forStatement = parseForStatement();
                blockInstructions.add(new BlockInstruction(forStatement));
            }else if(lookahead.value.equals("return")){
                ReturnStatement returnStatement = parseReturnStatement();
                blockInstructions.add(new BlockInstruction(returnStatement));
            }
            else if (lookahead.type.equals("Identifier")){
                Type identifierName = parseType();
                if(lookahead.value.equals("(")){// Identifier(
                    FunctionCall functionCall = parseFunctionCall(identifierName.value);
                    matchValue(";");
                    blockInstructions.add(new BlockInstruction(functionCall));
                }else if (lookahead.type.equals("AssignmentOperator")||lookahead.value.equals(".")){
                    //si . dire que c'est un tableau dans parseAssignement
                    Assignment assignment;
                    if(lookahead.value.equals(".")){//Identifier.
                        assignment = parseAssignment(identifierName.value, false, true, null);
                    }else{//Identifier =
                        assignment = parseAssignment(identifierName.value, false, false, null);
                    }
                    blockInstructions.add(new BlockInstruction(assignment));

                }
                else if(lookahead.type.equals("Identifier")){//Identifier Identifier
                    Symbol nameVariable = match("Identifier");
                    if(lookahead.type.equals("AssignmentOperator")){//Identifier Identifier = -> GD
                        GlobalDeclaration globalDeclaration = parseGlobalVariableDeclaration(identifierName, nameVariable);
                        blockInstructions.add(new BlockInstruction(globalDeclaration));
                    }
                    else if (lookahead.value.equals(";")){//Identifier Identifier ; -> VD
                        VariableDeclaration variableDeclaration = parseVariableDeclaration(identifierName,nameVariable.value,false);
                        blockInstructions.add(new BlockInstruction(variableDeclaration));
                    }
                }else if(lookahead.value.equals("[")){//Identifier [
                    matchValue("[");
                    if(lookahead.value.equals("]")){
                        matchValue("]");
                        if(lookahead.type.equals("Identifier")){//Identifier [] Identifier -> Declaration
                            Symbol nameVariable = match("Identifier");
                            if (lookahead.type.equals("AssignmentOperator")){// Identifier [] Identifier = -> GV
                                identifierName.value = identifierName.value + "[]";
                                GlobalDeclaration globalDeclaration = parseGlobalVariableDeclaration(identifierName, nameVariable);
                                blockInstructions.add(new BlockInstruction(globalDeclaration));
                            }else if(lookahead.value.equals(";")) {
                                VariableDeclaration variableDeclaration = parseVariableDeclaration(identifierName,nameVariable.value,true);
                                blockInstructions.add(new BlockInstruction(variableDeclaration));
                            }
                        }
                    }else if(!lookahead.value.equals("]")){//Identifier [3
                        Expression expression = parseExpression();
                        matchValue("]");//Identifier[expression]
                        if(lookahead.value.equals(".")){//Identifier[3].
                            Assignment assignment = parseAssignment(identifierName.value,true,true,expression);
                            blockInstructions.add(new BlockInstruction(assignment));
                        }else if(lookahead.type.equals("AssignmentOperator")){//Identifier [expression] = ..;
                            Assignment assignment = parseAssignment(identifierName.value,true,false,expression);
                            blockInstructions.add(new BlockInstruction(assignment));
                        }
                    }
                }

            }else if( lookahead.type.equals("BaseType")){
                Type type = parseType();
                if(lookahead.type.equals("Identifier")){//BaseType Identifier
                    Symbol nameVariable = match("Identifier");
                    if(lookahead.type.equals("AssignmentOperator")){//BaseType Identifier = -> GD
                        GlobalDeclaration globalDeclaration = parseGlobalVariableDeclaration(type, nameVariable);
                        blockInstructions.add(new BlockInstruction(globalDeclaration));
                    }
                    else if (lookahead.value.equals(";")){//BaseType Identifier ; -> VD

                        VariableDeclaration variableDeclaration = parseVariableDeclaration(type,nameVariable.value,false);
                        blockInstructions.add(new BlockInstruction(variableDeclaration));
                    }

                }else if(lookahead.value.equals("[")){
                    match("[");
                    match("]");
                    Symbol nameVariable = match("Identifier");
                    if(lookahead.value.equals(";")){//BaseType[] Identifier ;
                        VariableDeclaration variableDeclaration = parseVariableDeclaration(type,nameVariable.value,true);
                        blockInstructions.add(new BlockInstruction(variableDeclaration));
                    }else if(lookahead.type.equals("AssignmentOperator")){//BaseType[] Identifier =
                        type.value = type.value + "[]";
                        GlobalDeclaration globalDeclaration = parseGlobalVariableDeclaration(type, nameVariable);
                        blockInstructions.add(new BlockInstruction(globalDeclaration));
                    }

                }
            }
            else{
                throw new RuntimeException("No match " + lookahead.value);
            }
        }
        matchValue("}");
        return blockInstructions;
    }

    public static ReturnStatement parseReturnStatement() throws IOException {
        matchValue("return");
        if(lookahead.value.equals(";")){
            matchValue(";");
            return new ReturnStatement(null);
        }
        Expression expression = parseExpression();
        matchValue(";");
        return new ReturnStatement(expression);
    }

    public static Free parseFreeStatement() throws IOException{
        matchValue("free");
        Symbol name = match("Identifier");
        if(lookahead.value.equals("[")){
            matchValue("[");
            matchValue("]");
            name.value = name.value + "[]";
        }
        matchValue(";");
        return new Free(new Variable(name.value));
    }


    public static FunctionCall parseFunctionCall(String nameCall) throws IOException {
        matchValue("(");
        ArrayList<Argument> arguments = parseArguments();
        matchValue(")");
        return new FunctionCall(nameCall,arguments);

    }
    public static ArrayList<Argument> parseArguments() throws IOException {
        ArrayList<Argument> arguments = new ArrayList<>();
        while(!lookahead.value.equals(")") && lookahead!=null){
            Expression expression = parseExpression();
            if(lookahead.value.equals(",")){
                matchValue(",");
            }
            arguments.add(new Argument(expression));
        }
        return arguments;
    }

    public static VariableDeclaration parseVariableDeclaration(Type type, String nameVariable,Boolean isAnArray) throws IOException{
        matchValue(";");
        if(isAnArray){
            type.children.get(0).value = type.children.get(0).value + "[]";
        }
        return new VariableDeclaration(type, new Variable(nameVariable));

    }

    public static IfStatement parseIfStatement() throws IOException{
        if(lookahead.value.equals("if")){
            matchValue("if");
            matchValue("(");
            Expression expression = parseExpression();
            matchValue(")");
            ArrayList<BlockInstruction> body = parseBlock();
            return new IfStatement("if",expression,body);
        } else {
            matchValue("else");
            ArrayList<BlockInstruction> body = parseBlock();
            return new IfStatement("else",null,body);
        }
    }

    public static ForStatement parseForStatement() throws IOException{
        matchValue("for");
        matchValue("(");
        Assignment assignment1 = parseAssignmentRoot();
        matchValue(",");
        Expression border = parseExpression();
        matchValue(",");
        Assignment assignment2 = parseAssignmentRoot();
        matchValue(")");
        ArrayList<BlockInstruction> body = parseBlock();
        return new ForStatement(assignment1,border,assignment2,body);


    }
    public static WhileStatement parseWhileStatement() throws IOException{
        matchValue("while");
        matchValue("(");
        Expression expression = parseExpression();
        matchValue(")");
        ArrayList<BlockInstruction> body = parseBlock();
        return new WhileStatement(expression,body);
    }

    public static Assignment parseAssignmentRoot() throws IOException {
        Symbol identifier = match("Identifier");
        if(lookahead.type.equals("AssignmentOperator")){ // a = 3;
            match("AssignmentOperator");
            Expression expression = parseExpression();
            return new Assignment(new Variable(identifier.value),expression);
        } else if(lookahead.value.equals(".")){
            matchValue(".");
            Symbol identifierStruct = match("Identifier");
            match("AssignmentOperator");
            Expression expression = parseExpression();
            return new Assignment(new StructFieldAccess(identifier.value,identifierStruct.value),expression);
        }
        else if(lookahead.value.equals("[")){
            Expression expressionArray = parseExpression();
            matchValue("]");
            if(lookahead.type.equals("AssignmentOperator")){
                match("AssignmentOperator");
                Expression expression = parseExpression();
                return new Assignment(new ArrayElementAccess(identifier.value,expressionArray),expression);
            }else if(lookahead.value.equals(".")){
                matchValue(".");
                Symbol attribute = match("Identifier");
                match("AssignmentOperator");
                Expression expression = parseExpression();
                return new Assignment(new ArrayElementAccess(identifier.value, expressionArray),new StructFieldAccess(identifier.value, attribute.value),expression);
            }
        }
        throw new RuntimeException("No match " + lookahead.value);

    }


}
