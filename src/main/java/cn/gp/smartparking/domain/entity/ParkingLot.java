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
 * 停车场信息表
 * @TableName parking_lot
 */
@TableName(value ="parking_lot")
@Data
public class ParkingLot implements Serializable {
    /**
     * 停车场ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 停车场名称
     */
    private String name;

    /**
     * 地址
     */
    private String address;

    /**
     * 经度
     */
    private BigDecimal longitude;

    /**
     * 纬度
     */
    private BigDecimal latitude;

    /**
     * 总车位
     */
    private Integer total_space;

    /**
     * 空闲车位
     */
    private Integer free_space;

    /**
     * 每小时费率
     */
    private BigDecimal rate;

    /**
     * 开放时间
     */
    private String open_time;

    /**
     * 0=关闭 1=正常
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