package cn.gp.smartparking.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 黑名单表
 * @TableName blacklist
 */
@TableName(value ="blacklist")
@Data
public class Blacklist implements Serializable {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 车牌号
     */
    private String plate_number;

    /**
     * 加入原因
     */
    private String reason;

    /**
     * 过期时间
     */
    private Date expire_time;

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