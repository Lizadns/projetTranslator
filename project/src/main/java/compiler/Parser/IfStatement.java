package compiler.Parser;

import java.util.ArrayList;

public class IfStatement {
    Expression expression;

    ArrayList<BlockInstruction> body_if;
    ArrayList<BlockInstruction> body_else;

}
