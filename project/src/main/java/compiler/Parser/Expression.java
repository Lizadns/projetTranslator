package compiler.Parser;

public class Expression {
    Literal literal;
    Variable variable;
    UnaryExpression unaryExpression;
    BinaryExpression binaryExpression;
    FunctionCall functionCall;

    public Expression(Literal literal){
        this.literal= literal;
    }
    public Expression(Variable v){
        this.variable= v;
    }
    public Expression(UnaryExpression u){
        this.unaryExpression= u;
    }
    public Expression(BinaryExpression b){
        this.binaryExpression= b;
    }
    public Expression(FunctionCall f){
        this.functionCall=f;
    }
}
