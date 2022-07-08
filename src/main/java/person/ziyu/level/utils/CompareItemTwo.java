package person.ziyu.level.utils;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentStyle;
import com.alibaba.excel.annotation.write.style.HeadStyle;
import com.alibaba.excel.enums.poi.HorizontalAlignmentEnum;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
@HeadStyle(horizontalAlignment = HorizontalAlignmentEnum.CENTER)
@ContentStyle(horizontalAlignment = HorizontalAlignmentEnum.CENTER)
@ColumnWidth(25)
public class CompareItemTwo {
    // 包名称，此处不包括环名称
    @ExcelProperty("节点名称")
    private String name;
    // 在结果 1 中是否为环
    @ExcelProperty("在结果1中是否为环")
    private String circleInOne;
    // 在结果 2 中是否为环
    @ExcelProperty("在结果2中是否为环")
    private String circleInTwo;
    // 在结果 1 中的层数
    @ExcelProperty("在结果1中层数")
    private int layerInOne;
    // 在结果 2 中的层数
    @ExcelProperty("在结果2中层数")
    private int layerInTwo;
    // 在结果 3 中的层数
    // 是否同层
    @ExcelProperty("是否同层")
    private String same;
    // 是否同层

    public CompareItemTwo() {}

    public CompareItemTwo(String name) {
        this.name = name;
        this.layerInOne = -1;
        this.layerInTwo = -1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CompareItemTwo that = (CompareItemTwo) o;
        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    public void calculateSame() {
        same = (layerInOne == layerInTwo) ? "是" : "不同";
    }
}
