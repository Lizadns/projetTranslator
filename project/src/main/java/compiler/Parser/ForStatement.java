package compiler.Parser;

import java.util.ArrayList;

public class ForStatement {
    Assignment assignment;
    Expression border;
    Assignment incrementation;
    ArrayList<BlockInstruction> body;

    public ForStatement(Assignment assignment,Expression border,Assignment incrementation,ArrayList<BlockInstruction> body){
        this.assignment=assignment;
        this.border=border;
        this.incrementation=incrementation;
        this.body=body;

    }
}
