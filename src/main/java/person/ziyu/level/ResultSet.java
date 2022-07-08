package person.ziyu.level;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.write.style.column.SimpleColumnWidthStyleStrategy;
import person.ziyu.level.utils.CompareItem;
import person.ziyu.level.utils.CompareItemTwo;
import person.ziyu.level.utils.ResultSetDataListener;
import person.ziyu.level.utils.Utils;

import java.util.*;
public class ResultSet {

    // 分层结果
    private final List<Set<String>> levels;
    // 结果中的环节点
    private final List<Node> circles;
    // 拓扑排序
    private List<String> linearList;
    // 分层结果 - 导出文件用
    private List<List<String>> levelsExport;
    // 环节点 - 导出文件用
    private List<List<String>> circlesExport;
    // 是否执行过导出
    private boolean isExported = false;

    public ResultSet(List<Set<String>> levels, List<Node> circles) {
        this.levels = levels;
        this.circles = circles;
    }

    protected ResultSet(List<Set<String>> levels, List<Node> circles, List<String> linearList) {
        this.levels = levels;
        this.circles = circles;
        this.linearList = linearList;
        this.levelsExport = new ArrayList<>(levels.size());
        this.circlesExport = new ArrayList<>(circles.size());
    }

    public static ResultSet getInstanceFromFile(String filename, String sheetName, int levelNum, int circleNum) {
        Utils.checkFile(filename, ".xlsx");
        ResultSet ans = new ResultSet(new ArrayList<>(), new ArrayList<>());
        EasyExcel.read(filename, new ResultSetDataListener(ans, levelNum, circleNum))
                .sheet(sheetName)
                .doRead();
        return ans;
    }

    public List<Set<String>> getLevels() {
        return levels;
    }

    public List<Node> getCircles() {
        return circles;
    }

    public List<String> getLinearList() {
        return linearList;
    }

    public void toExcel(String filename) {
        if (!isExported) {
            for (Set<String> level : levels) {
                this.levelsExport.add(new ArrayList<>(level));
            }
            for (Node node : circles) {
                this.circlesExport.add(new ArrayList<>(node.getCircle()));
            }
            isExported = true;
        }
        Utils.createFile(filename, ".xlsx");
        EasyExcel.write(filename)
                .head(header())
                .sheet("Sheet1")
                .registerWriteHandler(new SimpleColumnWidthStyleStrategy(20))
                .doWrite(dataList());
    }

    private List<List<String>> header() {
        List<List<String>> list = new ArrayList<>();
        for (int i = 0; i < levelsExport.size(); ++i) {
            List<String> head = new ArrayList<>();
            head.add("layer-" + (i + 1));
            list.add(head);
        }
        for (int i = 0; i < circlesExport.size(); ++i) {
            List<String> head = new ArrayList<>();
            head.add("circle-" + (i + 1));
            list.add(head);
        }
        return list;
    }

    private List<List<Object>> dataList() {
        List<List<Object>> list = new ArrayList<>();
        int maxLevelSize = 0, maxCircleSize = 0;
        for (List<String> level : levelsExport) {
            maxLevelSize = Math.max(maxLevelSize, level.size());
        }
        for (List<String> circle : circlesExport) {
            maxCircleSize = Math.max(maxCircleSize, circle.size());
        }
        int lineNum = Math.max(maxLevelSize, maxCircleSize);
        for (int i = 0; i < lineNum; ++i) {
            List<Object> data = new ArrayList<>();
            for (List<String> strings : levelsExport) {
                if (i < strings.size()) {
                    data.add(strings.get(i));
                } else {
                    data.add(null);
                }
            }
            for (List<String> strings : circlesExport) {
                if (i < strings.size()) {
                    data.add(strings.get(i));
                } else {
                    data.add(null);
                }
            }
            list.add(data);
        }
        return list;
    }

    public Set<String> getNodes() {
        Set<String> ans = new HashSet<>();
        for (Set<String> level : levels) {
            ans.addAll(level);
        }
        for (Node node : circles) {
            ans.addAll(node.getCircle());
        }
        return ans;
    }

    public int getNodeNum() {
        return getNodes().size();
    }

    private Set<String> getCircleByName(String name) {
        for (Node node : circles) {
            if (name.equals(node.getValue())) {
                return new HashSet<>(node.getCircle());
            }
        }
        return null;
    }

    public Map<String, Integer> getLevelMap() {
        Map<String, Integer> map = new HashMap<>();
        for (int i = 0; i < levels.size(); ++i) {
            for (String node : levels.get(i)) {
                map.put(node, i + 1);
                // 如果名称以 cicle- 开头，说明是环节点
                if (!node.startsWith("circle-")) continue;
                // 找到环节点的集合
                Set<String> circle = getCircleByName(node);
                // 找不到就当没有
                if (circle == null) {
                    System.out.println("环节点 '" + node + "' 没有找到具体环中的元素");
                    continue;
                }
                for (String node1 : circle) {
                    map.put(node1, i + 1);
                }
            }
        }
        return map;
    }

    public void compare(ResultSet resultSet, String filename) {
        Map<String, Integer> map1 = getLevelMap();
        Map<String, Integer> map2 = resultSet.getLevelMap();
        Map<String, CompareItemTwo> table = new HashMap<>();
        for (String node : map1.keySet()) {
            if (node.startsWith("circle")) continue;
            if (!table.containsKey(node)) {
                table.put(node, new CompareItemTwo(node));
            }
            table.get(node).setCircleInOne("否");
            table.get(node).setLayerInOne(map1.get(node));
        }
        for (String node : map2.keySet()) {
            if (node.startsWith("circle")) continue;
            if (!table.containsKey(node)) {
                table.put(node, new CompareItemTwo(node));
            }
            table.get(node).setCircleInTwo("否");
            table.get(node).setLayerInTwo(map2.get(node));
        }
        for (Node node : circles) {
            Set<String> circle = node.getCircle();
            for (String value : circle) {
                if (!table.containsKey(value)) {
                    table.put(value, new CompareItemTwo(value));
                }
                table.get(value).setCircleInOne("是");
                table.get(value).setLayerInOne(map1.get(value));
            }
        }
        for (Node node : resultSet.getCircles()) {
            Set<String> circle = node.getCircle();
            for (String value : circle) {
                if (!table.containsKey(value)) {
                    table.put(value, new CompareItemTwo(value));
                }
                table.get(value).setCircleInTwo("是");
                table.get(value).setLayerInTwo(map2.get(value));
            }
        }
        List<CompareItemTwo> dataList = new ArrayList<>();
        for (String node : table.keySet()) {
            CompareItemTwo item = table.get(node);
            item.calculateSame();
            dataList.add(item);
        }
        Utils.createFile(filename, ".xlsx");
        EasyExcel.write(filename, CompareItemTwo.class)
                .sheet("Sheet1")
                .doWrite(dataList);
    }

    public void compare(ResultSet resultSet, ResultSet resultSet3, String filename) {
        Map<String, Integer> map1 = getLevelMap();
        Map<String, Integer> map2 = resultSet.getLevelMap();
        Map<String, Integer> map3 = resultSet3.getLevelMap();
        Map<String, CompareItem> table = new HashMap<>();
        for (String node : map1.keySet()) {
            if (node.startsWith("circle")) continue;
            if (!table.containsKey(node)) {
                table.put(node, new CompareItem(node));
            }
            table.get(node).setCircleInOne("否");
            table.get(node).setLayerInOne(map1.get(node));
        }
        for (String node : map2.keySet()) {
            if (node.startsWith("circle")) continue;
            if (!table.containsKey(node)) {
                table.put(node, new CompareItem(node));
            }
            table.get(node).setCircleInTwo("否");
            table.get(node).setLayerInTwo(map2.get(node));
        }
        for (String node : map3.keySet()) {
            if (node.startsWith("circle")) continue;
            if (!table.containsKey(node)) {
                table.put(node, new CompareItem(node));
            }
            table.get(node).setCircleInThree("否");
            table.get(node).setLayerInThree(map3.get(node));
        }
        for (Node node : circles) {
            Set<String> circle = node.getCircle();
            for (String value : circle) {
                if (!table.containsKey(value)) {
                    table.put(value, new CompareItem(value));
                }
                table.get(value).setCircleInOne("是");
                table.get(value).setLayerInOne(map1.get(value));
            }
        }
        for (Node node : resultSet.getCircles()) {
            Set<String> circle = node.getCircle();
            for (String value : circle) {
                if (!table.containsKey(value)) {
                    table.put(value, new CompareItem(value));
                }
                table.get(value).setCircleInTwo("是");
                table.get(value).setLayerInTwo(map2.get(value));
            }
        }
        for (Node node : resultSet3.getCircles()) {
            Set<String> circle = node.getCircle();
            for (String value : circle) {
                if (!table.containsKey(value)) {
                    table.put(value, new CompareItem(value));
                }
                table.get(value).setCircleInThree("是");
                table.get(value).setLayerInThree(map3.get(value));
            }
        }
        List<CompareItem> dataList = new ArrayList<>();
        for (String node : table.keySet()) {
            CompareItem item = table.get(node);
            item.calculateSame();
            dataList.add(item);
        }
        Utils.createFile(filename, ".xlsx");
        EasyExcel.write(filename, CompareItem.class)
                .sheet("Sheet1")
                .doWrite(dataList);
    }

    @Override
    public String toString() {
        return "ResultSet{" +
                "levels=" + levels +
                ", circles=" + circles +
                ", linearList=" + linearList +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ResultSet resultSet = (ResultSet) o;
        if (levels.size() != resultSet.getLevels().size() || circles.size() != resultSet.getCircles().size()) {
            return false;
        }
        for (int i = 0; i < levels.size(); ++i) {
            if (!levels.get(i).equals(resultSet.getLevels().get(i))) {
                return false;
            }
        }
        for (int i = 0; i < circles.size(); ++i) {
            if (!circles.get(i).equals(resultSet.getCircles().get(i))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(levels, circles, linearList, levelsExport, circlesExport);
    }
}
