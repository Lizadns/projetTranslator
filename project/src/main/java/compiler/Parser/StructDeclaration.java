package compiler.Parser;

import java.util.ArrayList;

public class StructDeclaration {
    String identifier;
    ArrayList<StructField> structfields;

    public StructDeclaration(String i, ArrayList<StructField> s){
        this.identifier=i;
        this.structfields=s;
    }
}
