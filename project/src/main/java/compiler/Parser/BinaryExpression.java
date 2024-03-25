package compiler.Parser;


public class BinaryExpression {
    Expression expression1;
    BinaryOperator binaryOperator;
    Expression expression2;
    public BinaryExpression(Expression e1, BinaryOperator b, Expression e2){
        this.expression1= e1;
        this.binaryOperator=b;
        this.expression2=e2;
    }
}
