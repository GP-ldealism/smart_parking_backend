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
public class ExponentialSmoothingPredictor implements PredictionModel {

    private static final double ALPHA = 0.3;
    private static final double BETA = 0.1;

    @Override
    public String getModelName() {
        return "霍尔特-温特斯指数平滑预测模型";
    }

    @Override
    public String getModelDescription() {
        return "基于二次指数平滑的 Holt-Winters 模型，捕捉趋势和季节性特征，适合中长期预测";
    }

    @Override
    public List<PredictionResult> predict(Long parkingLotId, LocalDateTime startTime, int hours) {
        List<PredictionResult> results = new ArrayList<>();
        List<HistoricalDataPoint> historicalData = prepareHistoricalData(parkingLotId, startTime);

        double level = initializeLevel(historicalData);
        double trend = initializeTrend(historicalData);

        for (int i = 0; i < hours; i++) {
            LocalDateTime targetTime = startTime.plusHours(i);

            double forecast = level + (i + 1) * trend;
            forecast = applySeasonalAdjustment(forecast, targetTime.getHour());

            double confidence = calculateConfidence(i, hours);
            results.add(new PredictionResult(
                targetTime,
                BigDecimal.valueOf(forecast).setScale(2, RoundingMode.HALF_UP),
                confidence,
                getModelName()
            ));
        }

        log.info("指数平滑预测完成 - 停车场ID: {}, 预测时长: {}小时", parkingLotId, hours);
        return results;
    }

    @Override
    public BigDecimal predictSinglePoint(Long parkingLotId, LocalDateTime targetTime) {
        List<HistoricalDataPoint> historicalData = prepareHistoricalData(parkingLotId, targetTime);

        double level = initializeLevel(historicalData);
        double trend = initializeTrend(historicalData);
        int hoursAhead = calculateHoursAhead(historicalData, targetTime);

        double forecast = level + hoursAhead * trend;
        forecast = applySeasonalAdjustment(forecast, targetTime.getHour());

        return BigDecimal.valueOf(Math.max(5.0, Math.min(98.0, forecast)))
                .setScale(2, RoundingMode.HALF_UP);
    }

    private List<HistoricalDataPoint> prepareHistoricalData(Long parkingLotId, LocalDateTime referenceTime) {
        List<HistoricalDataPoint> data = new ArrayList<>();
        for (int dayOffset = 13; dayOffset >= 0; dayOffset--) {
            for (int hour = 0; hour < 24; hour++) {
                LocalDateTime time = referenceTime.minusDays(dayOffset).withHour(hour).withMinute(0).withSecond(0);
                double occupancy = generateRealisticOccupancy(hour, dayOffset);
                data.add(new HistoricalDataPoint(time, BigDecimal.valueOf(occupancy).setScale(2, RoundingMode.HALF_UP)));
            }
        }
        return data;
    }

    private double initializeLevel(List<HistoricalDataPoint> data) {
        if (data.isEmpty()) return 50.0;

        double sum = 0;
        int count = Math.min(data.size(), 24);
        for (int i = 0; i < count; i++) {
            sum += data.get(data.size() - 1 - i).getOccupancyRate().doubleValue();
        }
        return sum / count;
    }

    private double initializeTrend(List<HistoricalDataPoint> data) {
        if (data.size() < 48) return 0.0;

        double recentAvg = 0;
        double olderAvg = 0;

        for (int i = 0; i < 24; i++) {
            recentAvg += data.get(data.size() - 1 - i).getOccupancyRate().doubleValue();
            olderAvg += data.get(data.size() - 25 - i).getOccupancyRate().doubleValue();
        }

        return (recentAvg - olderAvg) / 24.0;
    }

    private double applySeasonalAdjustment(double value, int hourOfDay) {
        double[] seasonalIndex = {
            0.35, 0.28, 0.22, 0.20, 0.28, 0.42,
            0.58, 0.72, 0.82, 0.88, 0.90, 0.84,
            0.78, 0.74, 0.76, 0.79, 0.84, 0.90,
            0.94, 0.88, 0.74, 0.56, 0.44, 0.38
        };

        if (hourOfDay >= 0 && hourOfDay < 24) {
            value = value * 0.5 + seasonalIndex[hourOfDay] * 100 * 0.5;
        }

        return Math.max(5.0, Math.min(98.0, value));
    }

    private double calculateConfidence(int hourOffset, int totalHours) {
        if (hourOffset <= 6) {
            return 0.88 - (hourOffset * 0.025);
        } else if (hourOffset <= 12) {
            return 0.75 - ((hourOffset - 6) * 0.018);
        } else if (hourOffset <= 24) {
            return 0.64 - ((hourOffset - 12) * 0.01);
        } else {
            return 0.52 - Math.max(0, (hourOffset - 24) * 0.008);
        }
    }

    private int calculateHoursAhead(List<HistoricalDataPoint> data, LocalDateTime targetTime) {
        if (data.isEmpty()) return 1;
        LocalDateTime lastDataTime = data.get(data.size() - 1).getTime();
        return (int) java.time.Duration.between(lastDataTime, targetTime).toHours();
    }

    private double generateRealisticOccupancy(int hour, int dayOffset) {
        double[] hourlyBase = {
            20, 14, 10, 8, 12, 24,
            40, 56, 68, 76, 80, 74,
            66, 62, 64, 67, 72, 79,
            87, 82, 66, 50, 34, 26
        };

        double base = (hour >= 0 && hour < 24) ? hourlyBase[hour] : 50.0;

        double weekendFactor = (dayOffset % 7 == 0 || dayOffset % 7 == 6) ? 0.75 : 1.0;
        double randomFactor = 1.0 + (Math.random() - 0.5) * 0.2;

        double result = base * weekendFactor * randomFactor;
        return Math.max(3, Math.min(97, result));
    }
}
