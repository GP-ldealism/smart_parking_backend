package cn.gp.smartparking.model.dto;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 推荐结果DTO
 */
@Data
public class RecommendationResultDTO {
    /**
     * 停车场ID
     */
    private Long id;
    
    /**
     * 停车场名称
     */
    private String name;
    
    /**
     * 地址
     */
    private String address;
    
    /**
     * 经度
     */
    private BigDecimal longitude;
    
    /**
     * 纬度
     */
    private BigDecimal latitude;
    
    /**
     * 总车位
     */
    private Integer totalSpace;
    
    /**
     * 空闲车位
     */
    private Integer freeSpace;
    
    /**
     * 每小时费率
     */
    private BigDecimal rate;
    
    /**
     * 开放时间
     */
    private String openTime;
    
    /**
     * 状态
     */
    private Integer status;
    
    /**
     * 推荐分数（0-100）
     */
    private Double score;
    
    /**
     * 距离（米）
     */
    private Double distance;
    
    /**
     * 空闲率（0-1）
     */
    private Double freeSpaceRate;
    
    /**
     * 推荐理由
     */
    private String reason;
}
