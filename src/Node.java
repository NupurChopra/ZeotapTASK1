public class Node {
    String type;
    String value;
    Node left;
    Node right;

    public Node(String type, String value, Node left, Node right) {
        this.type = type;
        this.value = value;
        this.left = left;
        this.right = right;
    }
}