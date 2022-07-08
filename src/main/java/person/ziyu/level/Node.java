package person.ziyu.level;

import person.ziyu.level.utils.NodeType;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class Node {

    // 包名
    private String value;
    // 包类型 普通节点 = 1, 环节点 = 2
    private NodeType type;
    // 环中的包名集合
    private Set<String> circle;
    // 邻接表中的下一个节点
    private Node next;

    public Node() {}

    public Node(String value) {
        this.value = value;
        this.type = NodeType.NODE;
        this.circle = null;
        this.next = null;
    }

    public Node(String value, Node next) {
        this.value = value;
        this.type = NodeType.NODE;
        this.circle = null;
        this.next = next;
    }

    public Node(String value, NodeType type) {
        this.value = value;
        this.type = type;
        if (type == NodeType.CIRCLE) {
            this.circle = new HashSet<>();
        }
    }

    public Node(String value, Set<String> circle) {
        this.value = value;
        this.type = NodeType.CIRCLE;
        this.circle = circle;
        this.next = null;
    }

    public Node(String value, Set<String> circle, Node next) {
        this.value = value;
        this.type = NodeType.CIRCLE;
        this.circle = circle;
        this.next = next;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public NodeType getType() {
        return type;
    }

    public void setType(NodeType type) {
        this.type = type;
    }

    public Set<String> getCircle() {
        return circle;
    }

    public void setCircle(Set<String> circle) {
        this.circle = circle;
    }

    public Node getNext() {
        return next;
    }

    public void setNext(Node next) {
        this.next = next;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Node node = (Node) o;
        if (type == NodeType.NODE) {
            return value.equals(node.value) && type == node.type;
        } else {
            return value.equals(node.value) && type == node.type && circle.equals(node.getCircle());
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, type, circle, next);
    }

    @Override
    public String toString() {
        if (type == NodeType.NODE) return value;
        else return value + circle;
    }
}
