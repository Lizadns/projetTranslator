package compiler.Lexer;
import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;

public class Lexer {

    private Reader reader;
    private PushbackReader pushbackReader;

    public Lexer(Reader input) {
        this.reader=input;
        this.pushbackReader=new PushbackReader(this.reader);
    }

    public Symbol getNextSymbol() throws IOException {
        char c = (char) pushbackReader.read();
        if(c=='/' || c == '+' || c == '-' || c == '%'){
            String s ="";
            s= s + c;
            c = (char) pushbackReader.read();
            if(c=='/' && s.equals("/")){ //on a un commentaire
                while (true) {
                    c =(char) pushbackReader.read();
                    if(c=='\n'){ // si c = \tab fin du comment
                        return this.getNextSymbol();
                    }
                }
            }else {
                pushbackReader.unread(c);
                return new Symbol("ArithmeticOperator",s);
            }

        }
        else if(c>='1' && c<='9') {
            String s = "";
            while (true) {
                s = s + c;
                c = (char) pushbackReader.read();
                if (c < '0' || c > '9') {
                    pushbackReader.unread(c);
                    break;
                }
            }
            return new Symbol("Number", s);
        }

        else if (c=='!' || c=='<' || c=='>'){
            String s = "";
            s=s+c;
            c= (char) pushbackReader.read();
            if((s.equals("!") && c=='=') || (s.equals("<") && c=='=') || (s.equals(">") && c=='=')){
                s=s+c;
                return new Symbol("ComparisonOperator",s);
            }
            else if (s.equals("!")){
                pushbackReader.unread(c);
                return new Symbol("NegativeOperator",s);
            }
            pushbackReader.unread(c);
            return new Symbol("ComparisonOperator",s);
        }
        else if (c=='&'){
            String s = "";
            s=s+c;
            c = (char) pushbackReader.read();
            if(c=='&'){
                return new Symbol("AndOperator");
            }
            else{
                return null;
            }
        }
        else if (c=='|'){
            String s = "";
            s=s+c;
            c = (char) pushbackReader.read();
            if(c=='|'){
                return new Symbol("OrOperator");
            }
            else{
                return null;
            }

        }
        else if( c == '"'){ //string
            String s = "";
            while(true){
                s=s + c;
                c = (char) pushbackReader.read();
                if(c == -1){
                    return null; // si le string ne se finit jamais
                }
                if (c == '"'){
                    s= s+c;
                    break;
                }
            }
            return new Symbol("String",s);
        }
        else if( c == ' ' || c == '\n' || c== '\t'){ //whitespace: space, new line, tab
            return this.getNextSymbol();
        }
        else if ((c>='A'&& c<='Z')|| (c>='a' && c<='z')|| c=='_'){ //si ça commence par une lettre ou un underscore
            String s = "";
            while(true){
                s=s+ c;
                c=(char) pushbackReader.read();
                if (!((c>='A'&& c<='Z')|| (c>='a' && c<='z')|| c=='_' || (c>='0' && c<='9'))){ //si ce n'est pas un underscore, une lettre ou un chiffre
                    pushbackReader.unread(c);
                    break;
                }
            }
            String[]  keywords = {"free", "struct", "for","def", "final", "if", "else", "while","return"}; //pour les Keywords
            for (int i=0; i< keywords.length; i++ ){
                if(s.compareTo(keywords[i])==0){
                    return new Symbol("Keyword", s);
                }
            }
            String[] basetypes= {"string","int","float","bool"}; //pour les BaseTypes
            for (int i=0; i< basetypes.length; i++ ){
                if(s.compareTo(basetypes[i])==0){
                    return new Symbol("BaseType", s);
                }
            }
            String[] booleans ={"true", "false"}; // pour les boolean
            for (int i=0; i< booleans.length; i++ ){
                if(s.compareTo(booleans[i])==0){
                    return new Symbol("Boolean", s);
                }
            }
            return new Symbol("Identifier",s);
        }
        else if( c== '(' || c==')' || c=='{' || c=='}' || c== '[' || c==']' ||c =='.' || c==';' || c==','){
            String s="";
            s=s+c;
            return new Symbol("SpecialCharacter", s);
        } // specials characters: () [] {} . ; ,
        else if( c== '='){ // si =
            String s="";
            s = s +c;
            c = (char) pushbackReader.read();
            if(c== '='){
                s = s + c;
                return new Symbol("ComparisonOperator", s);
            }
            else{
                pushbackReader.unread(c);
                return new Symbol("AssignmentOperator");
            }
        }

        else if ((int) c != -1){
            String s = String.valueOf(c);
            throw new RuntimeException("Error: Invalid character found - " + s);
        }
        c= (char)pushbackReader.read();


        return null;
    }
}

