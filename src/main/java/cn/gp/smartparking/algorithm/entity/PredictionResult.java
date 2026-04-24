package cn.gp.smartparking.algorithm.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PredictionResult implements Serializable {

    private LocalDateTime predictTime;

    private BigDecimal predictedOccupancyRate;

    private Double confidence;

    private String modelName;

    private String tip;
}