package compiler.Lexer;

public class Symbol {

    String type="";
    String value="";

    public Symbol(String t){
        type =t;
    }

    public Symbol(String t, String v){
        type = t;
        value = v;
    }

    @Override
    public String toString(){
        return "<"+type+","+value+">";
    }

}
