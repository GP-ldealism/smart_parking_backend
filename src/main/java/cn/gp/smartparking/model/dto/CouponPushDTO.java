package cn.gp.smartparking.model.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 优惠券推送DTO
 * 用于接收管理员推送优惠券的请求
 */
@Data
public class CouponPushDTO {
    
    /**
     * 优惠券名称
     */
    private String name;
    
    /**
     * 优惠券类型 0=满减 1=折扣 2=时长
     */
    private Integer type;
    
    /**
     * 优惠值
     */
    private BigDecimal value;
    
    /**
     * 最低消费
     */
    private BigDecimal minAmount;
    
    /**
     * 有效期开始
     */
    private Date startTime;
    
    /**
     * 有效期结束
     */
    private Date endTime;
    
    /**
     * 优惠码（可选，不传则自动生成）
     */
    private String code;
    
    /**
     * 推送范围 0=所有用户 1=指定用户
     */
    private Integer pushScope;
    
    /**
     * 指定用户ID列表（当pushScope=1时使用）
     */
    private Long[] userIds;
}
