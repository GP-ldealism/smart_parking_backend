package cn.gp.smartparking.algorithm;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface PredictionModel {

    String getModelName();

    String getModelDescription();

    List<PredictionResult> predict(Long parkingLotId, LocalDateTime startTime, int hours);

    BigDecimal predictSinglePoint(Long parkingLotId, LocalDateTime targetTime);
}
