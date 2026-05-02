package cn.gp.smartparking.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 车位信息表
 * @TableName parking_space
 */
@TableName(value ="parking_space")
@Data
public class ParkingSpace implements Serializable {
    /**
     * 车位ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 所属停车场ID
     */
    private Long parkingLotId;

    /**
     * 车位编号
     */
    private String spaceNo;

    /**
     * 0=普通 1=新能源 2=VIP 3=其他
     */
    private Integer type;

    /**
     * 0=占用 1=空闲
     */
    private Integer status;

    /**
     * 乐观锁
     */
    @Version
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