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
 * 车位历史占用表
 * @TableName parking_usage_history
 */
@TableName(value ="parking_usage_history")
@Data
public class ParkingUsageHistory implements Serializable {
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
     * 占用率 0-100
     */
    private BigDecimal occupancyRate;

    /**
     * 记录时间
     */
    private Date recordTime;

    /**
     * 小时 0-23
     */
    private Integer hour;

    /**
     * 星期 1-7
     */
    private Integer weekday;

    /**
     * 是否节假日
     */
    private Integer isHoliday;

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