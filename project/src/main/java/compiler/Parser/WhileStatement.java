package compiler.Parser;

import java.util.ArrayList;

public class WhileStatement {
    Expression expression;

    ArrayList<BlockInstruction> body;

    public WhileStatement(Expression expression, ArrayList<BlockInstruction> body){
        this.expression=expression;
        this.body=body;
    }
}
