package compiler.Parser;

public class UnaryExpression {
    UnaryOperator unaryOperator;
    Expression expression;

    public UnaryExpression(UnaryOperator u, Expression e){
        this.unaryOperator= u;
        this.expression=e;
    }
}
