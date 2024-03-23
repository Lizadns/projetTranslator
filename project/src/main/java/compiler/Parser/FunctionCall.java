package compiler.Parser;

import java.util.ArrayList;

public class FunctionCall {
    String name;
    ArrayList<Argument> arguments;

    public FunctionCall(String name,ArrayList<Argument> arguments){
        this.name=name;
        this.arguments=arguments;
    }
}
