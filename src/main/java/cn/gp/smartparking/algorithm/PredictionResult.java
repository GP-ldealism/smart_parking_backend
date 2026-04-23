package cn.gp.smartparking.algorithm;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PredictionResult {

    private LocalDateTime predictTime;

    private BigDecimal occupancyRate;

    private Double confidence;

    private String algorithmName;
}
