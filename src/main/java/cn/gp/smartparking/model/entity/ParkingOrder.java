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
    private String orderNo;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 停车场ID
     */
    private Long parkingLotId;

    /**
     * 车位号
     */
    private String spaceNo;

    /**
     * 车牌号
     */
    private String plateNumber;

    /**
     * 0=预约 1=停车
     */
    private Integer type;

    /**
     * 开始时间
     */
    private Date startTime;

    /**
     * 结束时间
     */
    private Date endTime;

    /**
     * 停车时长（分钟）
     */
    private Integer durationMinutes;

    /**
     * 金额
     */
    private BigDecimal amount;

    /**
     * 实付金额
     */
    private BigDecimal actualAmount;

    /**
     * 0=待进场 1=已完成 2=已取消
     */
    private Integer status;

    /**
     * 实际进场时间
     */
    private Date actualStartTime;

    /**
     * 实际出场时间
     */
    private Date actualEndTime;

    /**
     * 实际停车时长
     */
    private Integer actualDurationMinutes;

    /**
     * 取消原因
     */
    private String cancelReason;

    /**
     * 取消时间
     */
    private Date cancelTime;

    /**
     * 关联支付记录ID
     */
    private Long paymentId;

    /**
     * 关联优惠券ID
     */
    private Long couponId;

    /**
     * 评价分数 1-5
     */
    private Integer reviewScore;

    /**
     * 评价内容
     */
    private String reviewContent;

    /**
     * 评价时间
     */
    private Date reviewTime;

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