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
 * 停车订单表
 * @TableName parking_order
 */
@TableName(value ="parking_order")
@Data
public class ParkingOrder implements Serializable {
    /**
     * 订单ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 订单号
     */
    private String order_no;

    /**
     * 用户ID
     */
    private Long user_id;

    /**
     * 停车场ID
     */
    private Long parking_lot_id;

    /**
     * 车位号
     */
    private String space_no;

    /**
     * 车牌号
     */
    private String plate_number;

    /**
     * 0=预约 1=停车
     */
    private Integer type;

    /**
     * 开始时间
     */
    private Date start_time;

    /**
     * 结束时间
     */
    private Date end_time;

    /**
     * 停车时长（分钟）
     */
    private Integer duration_minutes;

    /**
     * 金额
     */
    private BigDecimal amount;

    /**
     * 0=待进场 1=已完成 2=已取消
     */
    private Integer status;

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