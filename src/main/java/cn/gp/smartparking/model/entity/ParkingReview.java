package cn.gp.smartparking.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 停车场评价表
 * @TableName parking_review
 */
@TableName(value ="parking_review")
@Data
public class ParkingReview implements Serializable {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 停车场ID
     */
    private Long parkingLotId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 关联订单ID
     */
    private Long orderId;

    /**
     * 评分 1-5
     */
    private Integer score;

    /**
     * 评价内容
     */
    private String content;

    /**
     * 乐观锁
     */
    private Integer version;

    /**
     * 创建时间
     */
    private Date createTime;

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
