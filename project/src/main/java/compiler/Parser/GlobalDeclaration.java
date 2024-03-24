package compiler.Parser;

public class GlobalDeclaration {
    Type type;
    String identifier;
    Expression expression;

    public GlobalDeclaration(Type t , String i, Expression e){
        this.type=t;
        this.identifier=i;
        this.expression=e;
    }
}
