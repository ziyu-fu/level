package person.ziyu.level;

import java.util.*;

public class LinkedNodeList {

    // head 为头节点，表示邻接表一个边的 start
    private final Node head;
    // tail 为尾节点，表示一个 start 最后一个边，方便插入新边
    private Node tail;
    // num 为邻接表中一个 start 对应边的数量
    private int num;

    // head 节点不为 null
    public LinkedNodeList(String value) {
        this.head = new Node(value);
        this.tail = this.head;
        this.num = 0;
    }

    public LinkedNodeList(String value, Set<String> circle) {
        this.head = new Node(value, circle);
        this.tail = this.head;
        this.num = 0;
    }

    public void addEdge(String value) {
        this.tail.setNext(new Node(value));
        this.tail = this.tail.getNext();
        ++this.num;
    }

    public void addEdge(String value, Set<String> circle) {
        this.tail.setNext(new Node(value, circle));
        this.tail = this.tail.getNext();
        ++this.num;
    }

    public void addEdge(Node node) {
        this.tail.setNext(node);
        this.tail = this.tail.getNext();
        ++this.num;
    }

    public void removeEdge(String value) {
        Node end = this.head.getNext();
        Node pre = this.head;
        while (end != null) {
            if (end.getValue().equals(value)) {
                pre.setNext(end.getNext());
                if (this.tail == end) {
                    this.tail = pre;
                }
                --this.num;
                break;
            }
            pre = end;
            end = end.getNext();
        }
    }

    public void removeEdge(Node node) {
        Node end = this.head.getNext();
        Node pre = this.head;
        while (end != null) {
            if (end.equals(node)) {
                pre.setNext(end.getNext());
                if (this.tail == end) {
                    this.tail = pre;
                }
                --this.num;
                break;
            }
            pre = end;
            end = end.getNext();
        }
    }

    public boolean existEdge(String value) {
        Node end = this.head.getNext();
        while (end != null) {
            if (end.getValue().equals(value)) {
                return true;
            }
            end = end.getNext();
        }
        return false;
    }

    public boolean existEdge(Node node) {
        Node end = this.head.getNext();
        while (end != null) {
            if (end.equals(node)) {
                return true;
            }
            end = end.getNext();
        }
        return false;
    }

    public Node getHead() {
        return head;
    }

    public Node getFistEdgeEnd() {
        return head.getNext();
    }

    public Set<String> getAdjacentNode() {
        Set<String> ans = new HashSet<>();
        Node end = this.head.getNext();
        while (end != null) {
            ans.add(end.getValue());
            end = end.getNext();
        }
        return ans;
    }

    public int getNum() {
        return num;
    }
}
