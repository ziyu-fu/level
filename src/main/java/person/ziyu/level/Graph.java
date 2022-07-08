package person.ziyu.level;

import javafx.util.Pair;
import lombok.extern.slf4j.Slf4j;
import person.ziyu.level.utils.ColorType;
import person.ziyu.level.utils.UnionFind;

import java.util.*;

@Slf4j
public class Graph {

    // 图的邻接表 - 出边表
    private Map<String, LinkedNodeList> table;
    // 图的邻接表 - 入边表
    private Map<String, LinkedNodeList> reverseTable;
    // 是否是反转图，创建图时必须是 依赖包 -> 被依赖包
    private boolean isReverse = false;

    public Graph() {
        table = new HashMap<>();
        reverseTable = new HashMap<>();
    }

    public void addNode(String value) {
        if (table.containsKey(value)) {
            log.trace("添加节点失败! 节点" + value + "已经存在!");
            return;
        }
        table.put(value, new LinkedNodeList(value));
        reverseTable.put(value, new LinkedNodeList(value));
    }

    public void addNode(String value, Set<String> circle) {
        if (table.containsKey(value)) {
            log.trace("添加节点失败! 节点" + value + "已经存在!");
            return;
        }
        table.put(value, new LinkedNodeList(value, new HashSet<>(circle)));
        reverseTable.put(value, new LinkedNodeList(value, new HashSet<>(circle)));
    }

    public void addEdge(String start, String end) {
        if (!checkEdgeNode(start, end)) return;
        table.get(start).addEdge(end);
        reverseTable.get(end).addEdge(start);
    }

    public void addEdge(String start, String end, String direct, Set<String> circle) {
        if (!checkEdgeNode(start, end)) return;
        if ("start".equals(direct)) {
            table.get(start).addEdge(end);
            reverseTable.get(end).addEdge(start, new HashSet<>(circle));
        } else if ("end".equals(direct)){
            table.get(start).addEdge(end, new HashSet<>(circle));
            reverseTable.get(end).addEdge(start);
        } else {
            log.warn("添加边 (addEdge) 时，direct 参数必须为 'start' 或 'end' 中的一个");
            System.exit(0);
        }
    }

    private boolean checkEdgeNode(String start, String end) {
        if (!table.containsKey(start) || !table.containsKey(end)) {
            log.trace("添加边失败! 节点 " + start + " 或 " + end + " 不存在存在!");
            return false;
        }
        if (table.get(start).existEdge(end)) {
            log.trace("添加边失败! 边 " + start + "->" + end + " 已经存在!");
            return false;
        }
        return true;
    }

    public void removeNode(String value) {
        table.remove(value);
        reverseTable.remove(value);
        for (String start : table.keySet()) {
            table.get(start).removeEdge(value);
        }
        for (String end : reverseTable.keySet()) {
            reverseTable.get(end).removeEdge(value);
        }
    }

    public void removeEdge(String start, String end) {
        table.get(start).removeEdge(end);
        reverseTable.get(end).removeEdge(start);
    }

    public boolean existNode(String value) {
        return table.containsKey(value);
    }

    public boolean existEdge(String start, String end) {
        if (!table.containsKey(start) || !table.containsKey(end)) {
            return false;
        }
        return table.get(start).existEdge(end);
    }

    private List<Set<String>> findAllCircle() {
        // color 为记录所有节点颜色的字典, white = 0, gray = 1, black = 2
        Map<String, ColorType> color = new HashMap<>();
        // pre_node 为记录遍历过程中所有节点前驱节点的字典
        Map<String, String> preNode = new HashMap<>();
        // 颜色初始化为全白色，前驱节点全部是空串
        for (String value : table.keySet()) {
            color.put(value, ColorType.WHITE);
            preNode.put(value, "");
        }
        List<Set<String>> ans = new ArrayList<>();
        for (String node : table.keySet()) {
            if (color.get(node) == ColorType.WHITE) {
                this.findAllCircleDfs(new Node(node), color, preNode, ans);
            }
        }
        return ans;
    }

    private void findAllCircleDfs(Node u, Map<String, ColorType> color, Map<String, String> preNode, List<Set<String>> ans) {
        color.put(u.getValue(), ColorType.GRAY);
        Node v = table.get(u.getValue()).getFistEdgeEnd();
        while (v != null) {
            if (color.get(v.getValue()) == ColorType.WHITE) {
                preNode.put(v.getValue(), u.getValue());
                findAllCircleDfs(v, color, preNode, ans);
            } else if (color.get(v.getValue()) == ColorType.GRAY) {
                if (preNode.get(u.getValue()).equals(v.getValue()) && existEdge(v.getValue(), u.getValue())) {
                    Set<String> circle = new HashSet<>();
                    circle.add(u.getValue());
                    circle.add(v.getValue());
                    ans.add(circle);
                }
                Set<String> circle = new HashSet<>();
                circle.add(v.getValue());
                String ptr = u.getValue();
                while (!ptr.equals(v.getValue())) {
                    circle.add(ptr);
                    ptr = preNode.get(ptr);
                }
                if (circle.size() != 1) {
                    ans.add(circle);
                }
            }
            v = v.getNext();
        }
        color.put(u.getValue(), ColorType.BLACK);
    }

    public List<Set<String>> findCircle() {
        List<Set<String>> circles = findAllCircle();
        Set<String> total = new HashSet<>();
        for (Set<String> circle : circles) {
            total.addAll(circle);
        }
        UnionFind unionFind = new UnionFind(total);
        for (Set<String> circle : circles) {
            List<String> dots = new ArrayList<>(circle);
            for (int i = 1; i < dots.size(); ++i) {
                unionFind.union(dots.get(0), dots.get(i));
            }
        }
        Map<String, Set<String>> groups = unionFind.group();
        return new ArrayList<>(groups.values());
    }

    public List<Node> removeCircle() {
        List<Set<String>> circles = findCircle();
        List<Node> newNodes = new ArrayList<>();
        int circleNum = 1;
        for (Set<String> circle : circles) {
            addNode("circle-" + circleNum, circle);
            newNodes.add(new Node("circle-" + circleNum, circle));
            // 添加 circle 的出边
            for (String start : circle) {
                Node end = table.get(start).getFistEdgeEnd();
                while (end != null) {
                    if (!circle.contains(end.getValue())) {
                        addEdge("circle-" + circleNum, end.getValue(), "start", circle);
                    }
                    end = end.getNext();
                }
            }
            // 添加 circle 的入边
            for (String start : table.keySet()) {
                if (circle.contains(start)) continue;
                Node end = table.get(start).getFistEdgeEnd();
                while (end != null) {
                    if (circle.contains(end.getValue())) {
                        removeEdge(start, end.getValue());
                        addEdge(start, "circle-" + circleNum, "end", circle);
                    }
                    end = end.getNext();
                }
            }
            // 删除节点
            for (String node : circle) {
                removeNode(node);
            }
            ++circleNum;
        }
        return newNodes;
    }

    public Map<String, Integer> inDegreeTable() {
        Map<String, Integer> ans = new HashMap<>();
        for (String node : reverseTable.keySet()) {
            ans.put(node, reverseTable.get(node).getNum());
        }
        return ans;
    }

    public Map<String, Integer> outDegreeTable() {
        Map<String, Integer> ans = new HashMap<>();
        for (String node : table.keySet()) {
            ans.put(node, table.get(node).getNum());
        }
        return ans;
    }
    
    public LinkedNodeList getLinkedNodeList(String node) {
        if (!table.containsKey(node)) {
            return null;
        }
        return table.get(node);
    }

    public LinkedNodeList getReverseLinkedNodeList(String node) {
        if (!reverseTable.containsKey(node)) {
            return null;
        }
        return reverseTable.get(node);
    }

    public void view() {
        viewTable(this.table);
    }

    public void view(boolean reverse) {
        if (reverse) viewTable(this.reverseTable);
        else viewTable(this.table);
    }

    private void viewTable(Map<String, LinkedNodeList> table) {
        for (String start : table.keySet()) {
            Node end = table.get(start).getFistEdgeEnd();
            System.out.print(start);
            while (end != null) {
                System.out.print(" -> " + end);
                end = end.getNext();
            }
            System.out.println();
        }
    }

    public int getNodeNum() {
        return table.size();
    }

    public int getEdgeNum() {
        int num = 0;
        for (String start : table.keySet()) {
            Node end = table.get(start).getFistEdgeEnd();
            while (end != null) {
                ++num;
                end = end.getNext();
            }
        }
        return num;
    }

    public Set<String> getNodeSet() {
        return table.keySet();
    }

    public Set<Pair<String, String>> getEdgeSet() {
        Set<Pair<String, String>> ans = new HashSet<>();
        for (String start : table.keySet()) {
            Node end = table.get(start).getFistEdgeEnd();
            while (end != null) {
                ans.add(new Pair<>(start, end.getValue()));
                end = end.getNext();
            }
        }
        for (String start : reverseTable.keySet()) {
            Node end = reverseTable.get(start).getFistEdgeEnd();
            while (end != null) {
                ans.add(new Pair<>(end.getValue(), start));
                end = end.getNext();
            }
        }
        return ans;
    }

    public Set<Pair<String, String>> getEdgeSet(String node) {
        Set<Pair<String, String>> ans = new HashSet<>();
        Node end = table.get(node).getFistEdgeEnd();
        while (end != null) {
            ans.add(new Pair<>(node, end.getValue()));
            end = end.getNext();
        }
        Node end2 = reverseTable.get(node).getFistEdgeEnd();
        while (end2 != null) {
            ans.add(new Pair<>(end2.getValue(), node));
            end2 = end2.getNext();
        }
        return ans;
    }

    public Set<String> getNodeByInDegree(int inDegree) {
        Map<String, Integer> inDegreeTable = inDegreeTable();
        Set<String> ans = new HashSet<>();
        for (String node : table.keySet()) {
            if (inDegreeTable.get(node) == inDegree) {
                ans.add(node);
            }
        }
        return ans;
    }

    public Set<String> getNodeByOutDegree(int outDegree) {
        Map<String, Integer> outDegreeTable = outDegreeTable();
        Set<String> ans = new HashSet<>();
        for (String node : table.keySet()) {
            if (outDegreeTable.get(node) == outDegree) {
                ans.add(node);
            }
        }
        return ans;
    }

    public void reverse() {
        Map<String, LinkedNodeList> tmp = table;
        table = reverseTable;
        reverseTable = tmp;
        this.isReverse = !this.isReverse;
    }

    public boolean isReverse() {
        return this.isReverse;
    }

    public boolean check() {
        // 遍历table的边，查看reverseTable中是否存在
        for (String start : table.keySet()) {
            Node end = table.get(start).getFistEdgeEnd();
            while (end != null) {
                if (!reverseTable.get(end.getValue()).existEdge(start))
                    return false;
                end = end.getNext();
            }
        }
        // 遍历reverseTable的边，查看table中是否存在
        for (String start : reverseTable.keySet()) {
            Node end = reverseTable.get(start).getFistEdgeEnd();
            while (end != null) {
                if (!table.get(end.getValue()).existEdge(start))
                    return false;
                end = end.getNext();
            }
        }
        // 查看table中是否有重边
        if (haveSameEdge(this.table)) return false;
        // 查看reverseTable中是否有重边
        if (haveSameEdge(this.reverseTable)) return false;
        return true;
    }

    private boolean haveSameEdge(Map<String, LinkedNodeList> table) {
        for (String start : table.keySet()) {
            Node end = table.get(start).getFistEdgeEnd();
            Set<String> ends = new HashSet<>();
            int num = 0;
            while (end != null) {
                if (ends.contains(end.getValue())) return true;
                ends.add(end.getValue());
                end = end.getNext();
                ++num;
            }
            if (num != table.get(start).getNum()) return true;
        }
        return false;
    }

}
