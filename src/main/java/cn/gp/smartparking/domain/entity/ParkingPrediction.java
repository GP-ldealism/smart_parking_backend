package cn.gp.smartparking.domain.entity;

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
    private Long parking_lot_id;

    /**
     * 预测时间点
     */
    private Date predict_time;

    /**
     * 预测占用率
     */
    private BigDecimal occupancy_rate;

    /**
     * 乐观锁
     */
    private Integer version;

    /**
     * 创建人ID
     */
    private Long create_by;

    /**
     * 创建时间
     */
    private Date create_time;

    /**
     * 更新人ID
     */
    private Long update_by;

    /**
     * 更新时间
     */
    private Date update_time;

    /**
     * 0=未删除 1=已删除
     */
    private Integer is_deleted;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}