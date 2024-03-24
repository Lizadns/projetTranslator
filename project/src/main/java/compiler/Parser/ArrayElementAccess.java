package compiler.Parser;

public class ArrayElementAccess {
    String arrayName;
    Expression expression;

    public ArrayElementAccess(String arrayName, Expression expression) {
        this.arrayName= arrayName;
        this.expression=expression;
    }
}
