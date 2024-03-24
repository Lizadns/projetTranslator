package compiler.Parser;

import java.util.ArrayList;

public class IfStatement {

    String type;
    Expression expression;

    ArrayList<BlockInstruction> body;

    public IfStatement(String type, Expression expression, ArrayList<BlockInstruction> body){
        this.body=body;
        this.type=type;
        this.expression=expression;
    }

}
