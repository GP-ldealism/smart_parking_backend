package cn.gp.smartparking.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
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
    private Long parking_lot_id;

    /**
     * 车位编号
     */
    private String space_no;

    /**
     * 0=普通 1=新能源 2=VIP
     */
    private Integer type;

    /**
     * 0=占用 1=空闲
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