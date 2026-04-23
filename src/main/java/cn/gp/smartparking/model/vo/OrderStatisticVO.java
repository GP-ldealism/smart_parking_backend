package cn.gp.smartparking.model.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @ClassName OrderStatistic
 * @Description TODO
 * @Author Guoping He
 * @Date 2026/4/19 14:44
 */
@Data
public class OrderStatisticVO {
    private Long totalCount;
    private Long totalDurationMinutes;
    private BigDecimal totalAmount;
}
