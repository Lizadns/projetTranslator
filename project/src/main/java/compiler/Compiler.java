/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package compiler;

import compiler.Lexer.Lexer;
import compiler.Lexer.Symbol;
import compiler.Parser.Parser;
import compiler.Parser.Program;

import java.io.FileReader;
import java.io.IOException;


public class Compiler {
    public static void main(String[] args) throws IOException {

        Lexer lexer;
        Parser parser;
        Boolean printLexer;
        Boolean printParser;

        if (args.length == 1) {
            try {
                lexer = new Lexer(new FileReader(args[0]));
                parser = new Parser(lexer);
                printLexer=Boolean.FALSE;
                printParser=Boolean.FALSE;
            } catch (IOException e) {
                System.err.println("Erreur lors de la lecture du fichier : " + e.getMessage());
                return;
            }
        } else if (args.length == 2 && args[0].equals("-lexer")) {
            try {
                lexer = new Lexer(new FileReader(args[1]));
                parser = new Parser(lexer);
                printLexer= Boolean.TRUE;
                printParser=Boolean.FALSE;
            } catch (IOException e) {
                System.err.println("Erreur lors de la lecture du fichier : " + e.getMessage());
                return;
            }
        } else if (args.length == 2 && args[0].equals("-parser")) {
            try {
                lexer = new Lexer(new FileReader(args[1]));
                parser = new Parser(lexer);
                printLexer= Boolean.FALSE;
                printParser=Boolean.TRUE;
            } catch (IOException e) {
                System.err.println("Erreur lors de la lecture du fichier : " + e.getMessage());
                return;
            }
        }
        else {
            System.err.println("Erreur : Mauvais Arguments");
            return;
        }

        if(printLexer) {
            Symbol symbol = lexer.getNextSymbol();
            while (symbol != null) {
                System.out.println(symbol);
                symbol = lexer.getNextSymbol();
            }
            System.out.println("Fin du Lexer");
        }

        Program node = parser.parse();
    }
    }

