package compiler.Parser;

import java.util.ArrayList;

public class Node {

    String value;
    ArrayList<Node> children;
    public Node(String v, ArrayList<Node> c){
        this.value = v;
        this.children= c;
    }

    @Override
    public String toString() {
        return value;
    }
}
