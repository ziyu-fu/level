package person.ziyu.level.utils;

import java.util.*;

/**
 * 并查集
 */
public class UnionFind {

    private final Map<String, String> parent;

    public UnionFind(Set<String> data) {
        parent = new HashMap<>();
        for (String d : data) {
            parent.put(d, d);
        }
    }

    public String find(String node) {
        if (!parent.containsKey(node)) {
            return "";
        }
        return parent.get(node).equals(node) ? node : find(parent.get(node));
    }

    public void union(String i, String j) {
        if (!parent.containsKey(i) || !parent.containsKey(j))
            return;
        parent.put(find(j), find(i));
    }

    public void compress() {
        parent.replaceAll((i, v) -> find(i));
    }

    public Map<String, Set<String>> group() {
        Map<String, Set<String>> ans = new HashMap<>();
        compress();
        for (String node : parent.keySet()) {
            String nodeParent = parent.get(node);
            if (!ans.containsKey(nodeParent)) {
                ans.put(nodeParent, new HashSet<>());
            }
            ans.get(nodeParent).add(node);
        }
        return ans;
    }
}
