package cn.gp.smartparking.algorithm;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class HistoricalDataPoint {

    private LocalDateTime time;

    private BigDecimal occupancyRate;

    public HistoricalDataPoint(LocalDateTime time, BigDecimal occupancyRate) {
        this.time = time;
        this.occupancyRate = occupancyRate;
    }
}
