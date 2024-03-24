package compiler.Parser;

public class ReturnStatement {
    Expression returnExpression;

    public ReturnStatement(Expression expression){
        this.returnExpression=expression;
    }
}
