package cn.gp.smartparking.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @ClassName PageResponse
 * @Description 分页数据
 * @Author Guoping He
 * @Date 2026/4/14 21:31
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageResponse<T> {
    // 总记录数
    private long total;
    // 当前页
    private int page;
    // 页大小
    private int pageSize;
    // 分页数据
    private T records;
}
