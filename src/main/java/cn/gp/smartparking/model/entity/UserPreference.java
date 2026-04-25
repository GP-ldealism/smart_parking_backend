package cn.gp.smartparking.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 用户停车偏好表
 * @TableName user_preference
 */
@TableName(value ="user_preference")
@Data
public class UserPreference implements Serializable {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 偏好最大距离（米）
     */
    private Integer preferDistance;

    /**
     * 0=便宜优先 1=距离优先
     */
    private Integer preferPrice;

    /**
     * 偏好车位类型
     */
    private Integer preferType;

    /**
     * 价格偏好区间（如：0-5,5-10）
     */
    private String preferPriceRange;

    /**
     * 偏好时段（如：早高峰、晚高峰）
     */
    private String preferTimeSlot;

    /**
     * 偏好设施（充电桩、监控等）
     */
    private String preferFacilities;

    /**
     * 最后更新时间
     */
    private Date lastUpdateTime;

    /**
     * 偏好更新次数
     */
    private Integer updateCount;

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