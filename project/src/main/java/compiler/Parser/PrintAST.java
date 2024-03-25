package compiler.Parser;

public class PrintAST {

    Program programNode;

    public PrintAST(Program p ){
        this.programNode=p;
    }

    public void print(){
        printIndent(programNode, 0);
    }

    public void printIndent(Node node,int indent){
        for (int i = 0; i < indent; i++) {
            System.out.print("   ");
        }
        System.out.println(node);
        System.out.println("\n");
        while (node.children!=null && node.children.size()!=0){
            for (Node child:node.children) {
                printIndent(child,indent+1);
            }
        }
    }
}
