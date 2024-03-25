package compiler.Parser;

public class PrintAST {

    Program programNode;

    public PrintAST(Program p ){
        this.programNode=p;
    }

    public void print(){
        String s = printIndent(programNode, 0);
        System.out.println(s);
    }

    public String printIndent(Node node,int indent){
        if (node == null) {
            return ""; // Return an empty string if the node is null
        }
        String s = "";
        for (int i = 0; i < indent; i++) {
            s = s+"   ";
        }
        if (node.children!=null){
            if (node.children.size()==1){
                s+=node+","+node.children.get(0).toString()+"\n";
                if(node.children.get(0).children!=null) {
                    for (Node n : node.children.get(0).children) {
                        s += printIndent(n, indent + 1);
                    }
                }
            }
            else if(node.children.size()!=0){
                s+=node+"\n";
                for(Node n: node.children){
                    s+=printIndent(n,indent+1);
                }
            }
            else{
                s+=node+"\n";;
            }
        }
        else{
            s+=node+"\n";;
        }
        return s;
    }
}
