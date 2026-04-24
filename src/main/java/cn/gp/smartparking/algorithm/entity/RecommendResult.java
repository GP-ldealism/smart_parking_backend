package cn.gp.smartparking.algorithm.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RecommendResult implements Serializable {

    private Long parkingLotId;

    private String parkingLotName;

    private String address;

    private BigDecimal distance;

    private BigDecimal rate;

    private Integer freeSpace;

    private Integer totalSpace;

    private Double score;

    private String tip;
}