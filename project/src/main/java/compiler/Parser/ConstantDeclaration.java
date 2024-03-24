package compiler.Parser;

public class ConstantDeclaration {
    Type type;
    String identifier;
    Expression expression;

    public ConstantDeclaration (Type t, String i, Expression e){
        this.type = t;
        this.identifier = i;
        this.expression = e;
    }
}
