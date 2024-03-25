package compiler.Parser;

public class VariableDeclaration {
    Type type;
    Variable nameVariable;

    public VariableDeclaration(Type type, Variable nameVariable){
        this.type=type;
        this.nameVariable=nameVariable;
    }

}
