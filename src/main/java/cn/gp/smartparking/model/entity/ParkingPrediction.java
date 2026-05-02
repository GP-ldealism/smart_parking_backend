package cn.gp.smartparking.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

/**
 * 车位占用率预测结果表
 * @TableName parking_prediction
 */
@TableName(value ="parking_prediction")
@Data
public class ParkingPrediction implements Serializable {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 
     */
    private Long parkingLotId;

    /**
     * 预测时间点
     */
    private Date predictTime;

    /**
     * 预测占用率
     */
    private BigDecimal occupancyRate;

    /**
     * 预测置信度 0-1
     */
    private BigDecimal confidence;

    /**
     * 模型版本
     */
    private String modelVersion;

    /**
     * 实际占用率（回填用）
     */
    private BigDecimal actualRate;

    /**
     * 预测误差
     */
    private BigDecimal errorRate;

    /**
     * 乐观锁
     */
    private Integer version;

    /**
     * 创建人ID
     */
    private Long createBy;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新人ID
     */
    private Long updateBy;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 0=未删除 1=已删除
     */
    private Integer isDeleted;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}