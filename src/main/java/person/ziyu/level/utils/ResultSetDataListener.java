package person.ziyu.level.utils;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.util.ListUtils;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import person.ziyu.level.Node;
import person.ziyu.level.ResultSet;

import java.util.HashSet;
import java.util.List;
import java.util.Map;

@Slf4j
public class ResultSetDataListener extends AnalysisEventListener<Map<Integer, String>> {

    private static final int BATCH_COUNT = 100;
    private List<Map<Integer, String>> cachedDataList = ListUtils.newArrayListWithExpectedSize(BATCH_COUNT);
    private final int levelNum;
    private final int circleNum;
    private final ResultSet resultSet;

    public ResultSetDataListener(ResultSet resultSet, int levelNum, int circleNum) {
        this.resultSet = resultSet;
        this.levelNum = levelNum;
        this.circleNum = circleNum;
        for (int i = 0; i < levelNum; ++i) {
            this.resultSet.getLevels().add(new HashSet<>());
        }
        for (int i = 0; i < circleNum; ++i) {
            this.resultSet.getCircles().add(new Node("", NodeType.CIRCLE));
        }
    }

    @Override
    public void invoke(Map<Integer, String> data, AnalysisContext context) {
        log.info("解析到一条数据:{}", JSON.toJSONString(data));
        cachedDataList.add(data);
        if (cachedDataList.size() >= BATCH_COUNT) {
            saveData();
            cachedDataList = ListUtils.newArrayListWithExpectedSize(BATCH_COUNT);
        }
    }

    @Override
    public void invokeHeadMap(Map<Integer, String> headMap, AnalysisContext context) {
        if (headMap.size() != (levelNum + circleNum)) {
            log.warn("表的列数和给定的层数和列数不符合!");
            System.exit(0);
        }
        log.info("解析到一条头数据:{}", JSON.toJSONString(headMap));
        for (int i = levelNum; i < headMap.size(); ++i) {
            resultSet.getCircles().get(i - levelNum).setValue(headMap.get(i));
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        saveData();
        log.info("所有数据解析完成！");
    }

    private void saveData() {
        for (Map<Integer, String> data : cachedDataList) {
            for (int i = 0; i < levelNum; ++i) {
                if (data.get(i) != null) {
                    resultSet.getLevels().get(i).add(data.get(i));
                }
            }
            for (int i = levelNum; i < data.size(); ++i) {
                if (data.get(i) != null) {
                    resultSet.getCircles().get(i - levelNum).getCircle().add(data.get(i));
                }
            }
        }
    }
}
