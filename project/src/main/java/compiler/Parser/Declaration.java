package compiler.Parser;

public class Declaration {
    ConstantDeclaration c;
    StructDeclaration s;
    GlobalDeclaration g;

    public Declaration(ConstantDeclaration c){
        this.c = c;
    }
    public Declaration(StructDeclaration s){
        this.s = s;
    }
    public Declaration(GlobalDeclaration g){
        this.g = g;
    }
}
