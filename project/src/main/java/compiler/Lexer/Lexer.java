package compiler.Lexer;
import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Lexer {

    private Reader reader;
    private static PushbackReader pushbackReader;
    private static final int PUSHBACK_BUFFER_SIZE = 100; // Taille personnalisée du tampon de rembobinage

    public Lexer(Reader input) {
        this.reader = input;
        this.pushbackReader = new PushbackReader(this.reader, PUSHBACK_BUFFER_SIZE);
    }

    public static void addAtBeginning(String textToAdd) throws IOException {
        char[] charsToAdd = textToAdd.toCharArray();
        for (int i = charsToAdd.length - 1; i >= 0; i--) {
            pushbackReader.unread(charsToAdd[i]);
        }
    }

    public static void addSymbolAtBeginning(Symbol symbol) throws IOException {
        // Convertir le symbole en une chaîne de caractères
        String symbolString = symbol.toString(); // Suppose que vous avez une méthode toString() appropriée dans la classe Symbol
        // Insérer la chaîne de caractères au début du flux d'entrée
        addAtBeginning(symbol.type);
    }

    public Symbol getNextSymbol() throws IOException {
        char c = (char) pushbackReader.read();
        if(c=='/' || c == '+' || c == '-' || c == '%' || c=='*'){
            String s ="";
            s= s + c;
            c = (char) pushbackReader.read();
            if(c=='/' && s.equals("/")){ //on a un commentaire
                while (true) {
                    c =(char) pushbackReader.read();
                    if(c=='\n'){ // si c = \n fin du comment
                        return this.getNextSymbol();
                    }
                    else if (c == '\uFFFF'){ //si la dernière ligne de code est un comment sans \n
                        return null;
                    }
                }
            }else {
                pushbackReader.unread(c);
                return new Symbol("ArithmeticOperator",s);
            }

        }
        else if(c>='0' && c<='9') {
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
                if( c == '\uFFFF'){
                    throw new RuntimeException("Error: Invalid character found - " + s);// si le string ne se finit jamais
                }
                if (c == '"'){
                    s= s+c;
                    break;
                }
            }
            return new Symbol("String",s);
        }
        else if( c == ' ' || c == '\n' || c== '\t' || c=='\r' ){ //whitespace: space, new line, tab
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

                    if(s.equals("def")){
                        return new Symbol("KeywordMethod",s);

                    } else if (s.equals("if") || s.equals("else")){
                        return new Symbol("KeywordCondition",s);

                    }else if(s.equals("while")){
                        return new Symbol("KeywordWhile",s);
                    }else if(s.equals("for")){
                        return new Symbol("KeywordFor",s);
                    }
                    else if (s.equals("return")){
                        return new Symbol("KeywordReturn",s);
                    }
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
        else if( c== '[' || c==']' ||c =='.' || c==';'){
            String s="";
            s=s+c;
            return new Symbol("SpecialCharacter", s);
        }
        else if (c=='('){
            String s="";
            s=s+c;
            return new Symbol("OpenParenthesis",s);
        }
        else if (c==')'){
            String s="";
            s=s+c;
            return new Symbol("CloseParenthesis",s);
        }
        else if(c==','){
            String s="";
            s=s+c;
            return new Symbol("Comma",s);
        }
        else if(c==']'){
            String s="";
            s=s+c;
            return new Symbol("ClosingHook",s);
        }
        else if (c=='['){
            String s="";
            s=s+c;
            return new Symbol("OpeningHook",s);
        }
        else if(c=='}'){
            String s="";
            s=s+c;
            return new Symbol("ClosingBrace",s);
        }
        else if (c=='{'){
            String s="";
            s=s+c;
            return new Symbol("OpeningBrace",s);
        }
        // specials characters: () [] {} . ; ,
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
                return new Symbol("AssignmentOperator","=");
            }
        }

        else if ( c !='\uFFFF'){
            String s = String.valueOf(c);
            throw new RuntimeException("Error: Invalid character found - " + s);
        }
        return null;
    }
}

