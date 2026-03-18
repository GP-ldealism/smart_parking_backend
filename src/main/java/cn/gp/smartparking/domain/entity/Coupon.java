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
 * 优惠券表
 * @TableName coupon
 */
@TableName(value ="coupon")
@Data
public class Coupon implements Serializable {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 所属用户（NULL=通用券）
     */
    private Long user_id;

    /**
     * 优惠码
     */
    private String code;

    /**
     * 优惠券名称
     */
    private String name;

    /**
     * 0=满减 1=折扣 2=时长
     */
    private Integer type;

    /**
     * 优惠值
     */
    private BigDecimal value;

    /**
     * 最低消费
     */
    private BigDecimal min_amount;

    /**
     * 有效期开始
     */
    private Date start_time;

    /**
     * 有效期结束
     */
    private Date end_time;

    /**
     * 0=未使用 1=已使用 2=已过期
     */
    private Integer status;

    /**
     * 使用时间
     */
    private Date used_time;

    /**
     * 使用订单
     */
    private Long used_order_id;

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