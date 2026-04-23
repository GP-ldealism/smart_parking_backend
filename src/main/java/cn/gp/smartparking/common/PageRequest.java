package cn.gp.smartparking.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @ClassName PageRequest
 * @Description TODO
 * @Author Guoping He
 * @Date 2026/3/22 11:30
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageRequest {
    /**
     * 当前页号
     */
    private int current = 1;
    /**
     * 页面大小
     */
    private int pageSize = 10;
    /**
     * 排序字段
     */
    private List<String> sortFieldList;
    /**
     * 排序顺序
     */
    private List<String> sortOrderList;
}
