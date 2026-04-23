package cn.gp.smartparking.zftest;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @ClassName OrderModel
 * @Description TODO
 * @Author Guoping He
 * @Date 2026/4/21 21:09
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderModel implements Serializable {
    private static final long serialVersionUID = 1L;
    private Integer id;
    private String orderNo;
    private Integer orderStatus;
    private Integer userId;
    private BigDecimal orderPrice;
    private Integer payType;
    private Date payTime;
    private Date createTime;
    private Date updateTime;
}
