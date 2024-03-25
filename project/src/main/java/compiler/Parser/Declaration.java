package compiler.Parser;

public class Declaration {
    ConstantDeclaration c;
    StructDeclaration s;
    GlobalDeclaration g;

    VariableDeclaration v;

    public Declaration(ConstantDeclaration c){
        this.c = c;
    }
    public Declaration(StructDeclaration s){
        this.s = s;
    }
    public Declaration(GlobalDeclaration g){
        this.g = g;
    }
    public Declaration(VariableDeclaration v){
        this.v = v;
    }
}
