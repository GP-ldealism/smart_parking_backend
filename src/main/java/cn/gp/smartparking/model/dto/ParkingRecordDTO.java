package cn.gp.smartparking.model.dto;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 停车记录DTO
 */
@Data
public class ParkingRecordDTO implements Serializable {
    
    /**
     * 订单ID
     */
    private Long id;
    
    /**
     * 停车场名称
     */
    private String parkingLotName;
    
    /**
     * 车牌号
     */
    private String plateNumber;
    
    /**
     * 金额
     */
    private BigDecimal amount;
    
    /**
     * 入场时间
     */
    private Date startTime;
    
    /**
     * 离场时间
     */
    private Date endTime;
    
    /**
     * 停车时长（分钟）
     */
    private Integer durationMinutes;
    
    private static final long serialVersionUID = 1L;
}
