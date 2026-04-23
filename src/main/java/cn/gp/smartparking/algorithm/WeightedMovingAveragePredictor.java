package cn.gp.smartparking.algorithm;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class WeightedMovingAveragePredictor implements PredictionModel {

    private static final int DEFAULT_WINDOW_SIZE = 7;
    private static final double[] WEIGHTS = {0.05, 0.08, 0.12, 0.15, 0.20, 0.18, 0.22};

    @Override
    public String getModelName() {
        return "加权移动平均预测模型";
    }

    @Override
    public String getModelDescription() {
        return "基于历史7天数据的加权移动平均算法，近期数据权重更高，适用于短期预测";
    }

    @Override
    public List<PredictionResult> predict(Long parkingLotId, LocalDateTime startTime, int hours) {
        List<PredictionResult> results = new ArrayList<>();

        for (int i = 0; i < hours; i++) {
            LocalDateTime targetTime = startTime.plusHours(i);
            BigDecimal predictedRate = predictSinglePoint(parkingLotId, targetTime);

            double confidence = calculateConfidence(i, hours);
            results.add(new PredictionResult(targetTime, predictedRate, confidence, getModelName()));
        }

        log.info("加权移动平均预测完成 - 停车场ID: {}, 预测时长: {}小时", parkingLotId, hours);
        return results;
    }

    @Override
    public BigDecimal predictSinglePoint(Long parkingLotId, LocalDateTime targetTime) {
        List<HistoricalDataPoint> historicalData = generateSimulatedHistoricalData(parkingLotId, targetTime);

        if (historicalData.isEmpty()) {
            return getDefaultPrediction(targetTime.getHour());
        }

        double weightedSum = 0.0;
        double weightTotal = 0.0;

        int dataPoints = Math.min(historicalData.size(), WEIGHTS.length);
        for (int i = 0; i < dataPoints; i++) {
            HistoricalDataPoint point = historicalData.get(i);
            double weight = WEIGHTS[WEIGHTS.length - dataPoints + i];
            weightedSum += point.getOccupancyRate().doubleValue() * weight;
            weightTotal += weight;
        }

        if (weightTotal > 0) {
            double result = weightedSum / weightTotal;
            result = applyTimePattern(result, targetTime.getHour());
            return BigDecimal.valueOf(result).setScale(2, RoundingMode.HALF_UP);
        }

        return getDefaultPrediction(targetTime.getHour());
    }

    private double calculateConfidence(int hourOffset, int totalHours) {
        if (hourOffset <= 6) {
            return 0.85 - (hourOffset * 0.03);
        } else if (hourOffset <= 12) {
            return 0.70 - ((hourOffset - 6) * 0.02);
        } else {
            return 0.58 - ((hourOffset - 12) * 0.01);
        }
    }

    private double applyTimePattern(double baseValue, int hourOfDay) {
        double[] timeFactors = {
            0.3, 0.2, 0.15, 0.15, 0.2, 0.35,
            0.55, 0.75, 0.85, 0.90, 0.88, 0.82,
            0.80, 0.78, 0.80, 0.82, 0.85, 0.90,
            0.95, 0.92, 0.80, 0.65, 0.50, 0.40
        };

        if (hourOfDay >= 0 && hourOfDay < 24) {
            baseValue = baseValue * 0.6 + timeFactors[hourOfDay] * 100 * 0.4;
        }

        return Math.max(5.0, Math.min(98.0, baseValue));
    }

    private BigDecimal getDefaultPrediction(int hourOfDay) {
        double[] defaults = {
            25, 18, 12, 10, 15, 28,
            45, 62, 75, 82, 85, 78,
            72, 68, 70, 73, 78, 85,
            92, 88, 72, 55, 38, 30
        };

        double value = (hourOfDay >= 0 && hourOfDay < 24) ? defaults[hourOfDay] : 50.0;
        value += (Math.random() - 0.5) * 10;
        return BigDecimal.valueOf(Math.max(5.0, Math.min(98.0, value))).setScale(2, RoundingMode.HALF_UP);
    }

    private List<HistoricalDataPoint> generateSimulatedHistoricalData(Long parkingLotId, LocalDateTime targetTime) {
        List<HistoricalDataPoint> data = new ArrayList<>();
        for (int dayOffset = 6; dayOffset >= 0; dayOffset--) {
            LocalDateTime pastTime = targetTime.minusDays(dayOffset);
            double baseOccupancy = getBaseOccupancyForHour(pastTime.getHour());
            double noise = (Math.random() - 0.5) * 15;
            double occupancy = Math.max(5, Math.min(95, baseOccupancy + noise));
            data.add(new HistoricalDataPoint(pastTime, BigDecimal.valueOf(occupancy).setScale(2, RoundingMode.HALF_UP)));
        }
        return data;
    }

    private double getBaseOccupancyForHour(int hour) {
        double[] pattern = {
            22, 16, 11, 9, 14, 26,
            42, 58, 70, 78, 82, 76,
            68, 64, 66, 69, 74, 81,
            89, 84, 68, 52, 36, 28
        };
        return (hour >= 0 && hour < 24) ? pattern[hour] : 50.0;
    }
}
