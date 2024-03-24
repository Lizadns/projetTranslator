package compiler.Parser;

public class Assignment {
    StructFieldAccess structFieldAccess;
    ArrayElementAccess arrayElementAccess;
    Variable variable;
    Expression expression;

    public Assignment(StructFieldAccess structFieldAccess,Expression expression){
        this.structFieldAccess=structFieldAccess;
        this.expression=expression;
    }
    public Assignment(ArrayElementAccess arrayElementAccess,Expression expression){
        this.arrayElementAccess=arrayElementAccess;
        this.expression=expression;
    }

    public Assignment(ArrayElementAccess arrayElementAccess,StructFieldAccess structFieldAccess,Expression expression){
        this.arrayElementAccess=arrayElementAccess;
        this.structFieldAccess=structFieldAccess;
        this.expression=expression;
    }
    public Assignment(Variable variable,Expression expression){
        this.variable=variable;
        this.expression=expression;
    }
}
