package compiler.Parser;

public class StructFieldAccess {
    String structname;
    String identifier;

    public StructFieldAccess(String structname, String identifier){
        this.structname=structname;
        this.identifier=identifier;
    }
}
