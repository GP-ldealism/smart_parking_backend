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
 * 支付记录表
 * @TableName payment_record
 */
@TableName(value ="payment_record")
@Data
public class PaymentRecord implements Serializable {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 订单ID
     */
    private Long order_id;

    /**
     * 支付流水号
     */
    private String payment_no;

    /**
     * 支付金额
     */
    private BigDecimal amount;

    /**
     * 0=微信 1=支付宝 2=余额
     */
    private Integer payment_method;

    /**
     * 0=待支付 1=成功 2=失败
     */
    private Integer payment_status;

    /**
     * 第三方交易号
     */
    private String transaction_id;

    /**
     * 支付完成时间
     */
    private Date payment_time;

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