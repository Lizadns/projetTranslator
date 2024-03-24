package compiler.Parser;

public class StructField {
    Type type;
    String identifier;

    public StructField(Type t, String i){
        this.type = t;
        this.identifier=i;
    }
}
