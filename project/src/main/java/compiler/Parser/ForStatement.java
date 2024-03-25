package compiler.Parser;

import java.util.ArrayList;

public class ForStatement extends Node{

    public ForStatement(Assignment assignment,Expression border,Assignment incrementation,ArrayList<BlockInstruction> body){
        super("ForStatement", new ArrayList<>());
        this.children.add(assignment);
        this.children.add(border);
        this.children.add(incrementation);
        this.children.addAll(body);

    }
}
