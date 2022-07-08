package person.ziyu.level;

import person.ziyu.level.utils.Utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class GraphUtils {

    private GraphUtils() {}

    public static Graph readDotfile(String filename) {
        Utils.checkFile(filename, ".dot");
        Graph graph = new Graph();
        try {
            InputStreamReader isr = new InputStreamReader(Files.newInputStream(Paths.get(filename)), StandardCharsets.UTF_8);
            BufferedReader reader = new BufferedReader(isr, 5 * 1024 * 1024);
            String line;
            int lineNum = 1;
            while ((line = reader.readLine()) != null) {
                if (lineNum < 3) {
                    lineNum += 1;
                    continue;
                }
                line = Utils.removeSpecialChar(line);
                if (line.equals("}")) break;
                String[] stringGroups = line.split(" -> ");
                if (stringGroups.length == 1) {
                    graph.addNode(stringGroups[0]);
                }
                if (stringGroups.length == 2) {
                    if (!graph.existNode(stringGroups[0])) {
                        graph.addNode(stringGroups[0]);
                    }
                    if (!graph.existNode(stringGroups[1])) {
                        graph.addNode(stringGroups[1]);
                    }
                    if (!graph.existNode(stringGroups[0]) || !graph.existNode(stringGroups[1])) {
                        System.out.println("尝试添加边 " + stringGroups[0] + "->" + stringGroups[1] + " 时，发现点不存在");
                        reader.close();
                        isr.close();
                        System.exit(0);
                    }
                    graph.addEdge(stringGroups[0], stringGroups[1]);
                }
            }
            reader.close();
            isr.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return graph;
    }

    public static ResultSet level(Graph graph) {
        List<Node> circles = graph.removeCircle();
        List<String> linearList = linearList(graph);
        List<Set<String>> levels = new ArrayList<>();
        Map<String, Integer> inDegreeTable = graph.inDegreeTable();
        Map<String, Integer> outDegreeTable = graph.outDegreeTable();
        // index 记录第一个入度不为 0 的点
        int index = 0;
        Set<String> level = new HashSet<>();
        for (int i = 0; i < linearList.size(); ++i) {
            if (inDegreeTable.get(linearList.get(i)) == 0) {
                level.add(linearList.get(i));
            } else {
                index = i;
                break;
            }
        }
        levels.add(level);
        // 当前层在 levels 中的下标
        int currentLevelIndex = 1;
        level = new HashSet<>();
        for (int i = index; i < linearList.size(); ++i) {
            if (levelTotalDependNum(outDegreeTable, levels, currentLevelIndex - 1) != 0) {
                level.add(linearList.get(i));
                Node end = graph.getReverseLinkedNodeList(linearList.get(i)).getFistEdgeEnd();
                while (end != null) {
                    outDegreeTable.put(end.getValue(), outDegreeTable.get(end.getValue()) - 1);
                    end = end.getNext();
                }
            } else {
                levels.add(level);
                level = new HashSet<>();
                level.add(linearList.get(i));
                currentLevelIndex += 1;
            }
        }
        levels.add(level);
        if (!graph.isReverse()) {
            Collections.reverse(levels);
        }
        return new ResultSet(levels, circles, linearList);
    }

    public static List<String> linearList(Graph graph) {
        Map<String, Integer> weight = new HashMap<>();
        Queue<String> queue = new LinkedList<>();
        Map<String, Integer> inDegreeTable = graph.inDegreeTable();
        List<String> ans = new ArrayList<>();
        for (String node : inDegreeTable.keySet()) {
            if (inDegreeTable.get(node) == 0) {
                queue.add(node);
                weight.put(node, 0);
            }
        }
        while(!queue.isEmpty()) {
            String node = queue.poll();
            ans.add(node);
            Node end = graph.getLinkedNodeList(node).getFistEdgeEnd();
            while (end != null) {
                inDegreeTable.put(end.getValue(), inDegreeTable.get(end.getValue()) - 1);
                if (inDegreeTable.get(end.getValue()) == 0) {
                    queue.add(end.getValue());
                    calculateWeight(graph, weight, end.getValue());
                }
                end = end.getNext();
            }
        }
        return ans;
    }

    private static void calculateWeight(Graph graph, Map<String, Integer> weight, String node) {
        Node end = graph.getReverseLinkedNodeList(node).getFistEdgeEnd();
        int nodeWeight = 1;
        while (end != null) {
            if (!weight.containsKey(end.getValue())) {
                System.out.println("拓扑排序过程中计算点权重时，前驱节点的权重未计算");
                return;
            }
            nodeWeight += weight.get(end.getValue());
            end = end.getNext();
        }
        weight.put(node, nodeWeight);
    }

    private static int levelTotalDependNum(Map<String, Integer> outDegreeTable, List<Set<String>> levels, int levelNum) {
        int num = 0;
        for (String node : levels.get(levelNum)) {
            num += outDegreeTable.get(node);
        }
        return num;
    }
}
