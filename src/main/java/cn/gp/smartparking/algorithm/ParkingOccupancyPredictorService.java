package cn.gp.smartparking.algorithm;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ParkingOccupancyPredictorService {

    private final WeightedMovingAveragePredictor wmaPredictor;
    private final ExponentialSmoothingPredictor esPredictor;

    public ParkingOccupancyPredictorService(
            WeightedMovingAveragePredictor wmaPredictor,
            ExponentialSmoothingPredictor esPredictor) {
        this.wmaPredictor = wmaPredictor;
        this.esPredictor = esPredictor;
    }

    public List<PredictionResult> predictWithModel(Long parkingLotId, LocalDateTime startTime, int hours, String modelType) {
        PredictionModel model = getPredictionModel(modelType);
        return model.predict(parkingLotId, startTime, hours);
    }

    public PredictionResult predictSinglePoint(Long parkingLotId, LocalDateTime targetTime, String modelType) {
        PredictionModel model = getPredictionModel(modelType);
        BigDecimal predictedRate = model.predictSinglePoint(parkingLotId, targetTime);
        double confidence = calculateConfidenceForPoint(targetTime);

        return new PredictionResult(targetTime, predictedRate, confidence, model.getModelName());
    }

    public EnsemblePredictionResult predictEnsemble(Long parkingLotId, LocalDateTime startTime, int hours) {
        List<PredictionResult> wmaResults = wmaPredictor.predict(parkingLotId, startTime, hours);
        List<PredictionResult> esResults = esPredictor.predict(parkingLotId, startTime, hours);

        List<PredictionResult> ensembleResults = new ArrayList<>();
        for (int i = 0; i < hours; i++) {
            PredictionResult wma = wmaResults.get(i);
            PredictionResult es = esResults.get(i);

            double wmaWeight = 0.5 * wma.getConfidence();
            double esWeight = 0.5 * es.getConfidence();
            double totalWeight = wmaWeight + esWeight;

            double ensembleRate;
            if (totalWeight > 0) {
                ensembleRate = (wma.getOccupancyRate().doubleValue() * wmaWeight +
                               es.getOccupancyRate().doubleValue() * esWeight) / totalWeight;
            } else {
                ensembleRate = (wma.getOccupancyRate().doubleValue() + es.getOccupancyRate().doubleValue()) / 2.0;
            }

            double ensembleConfidence = Math.min(wma.getConfidence(), es.getConfidence()) * 1.1;
            ensembleConfidence = Math.min(ensembleConfidence, 0.95);

            ensembleResults.add(new PredictionResult(
                wma.getPredictTime(),
                BigDecimal.valueOf(ensembleRate).setScale(2, java.math.RoundingMode.HALF_UP),
                ensembleConfidence,
                "集成预测模型(WMA+Holt-Winters)"
            ));
        }

        return new EnsemblePredictionResult(ensembleResults, wmaResults, esResults);
    }

    public List<BestParkingTimeRecommendation> recommendBestParkingTimes(Long parkingLotId, LocalDateTime startTime, int hours) {
        List<PredictionResult> predictions = predictEnsemble(parkingLotId, startTime, hours).getEnsembleResults();

        List<BestParkingTimeRecommendation> recommendations = predictions.stream()
                .sorted(Comparator.comparingDouble(p -> p.getOccupancyRate().doubleValue()))
                .limit(5)
                .map(pred -> new BestParkingTimeRecommendation(
                        pred.getPredictTime(),
                        pred.getOccupancyRate(),
                        pred.getConfidence(),
                        generateParkingTip(pred.getOccupancyRate().doubleValue())
                ))
                .collect(Collectors.toList());

        return recommendations;
    }

    public ModelComparisonInfo compareModels(Long parkingLotId, LocalDateTime startTime, int hours) {
        List<PredictionResult> wmaResults = wmaPredictor.predict(parkingLotId, startTime, hours);
        List<PredictionResult> esResults = esPredictor.predict(parkingLotId, startTime, hours);

        double wmaAvgConfidence = wmaResults.stream()
                .mapToDouble(PredictionResult::getConfidence)
                .average()
                .orElse(0.0);

        double esAvgConfidence = esResults.stream()
                .mapToDouble(PredictionResult::getConfidence)
                .average()
                .orElse(0.0);

        return new ModelComparisonInfo(
                wmaPredictor.getModelName(), wmaPredictor.getModelDescription(), wmaAvgConfidence,
                esPredictor.getModelName(), esPredictor.getModelDescription(), esAvgConfidence
        );
    }

    public List<Map<String, Object>> getPredictionSummary(Long parkingLotId) {
        LocalDateTime now = LocalDateTime.now();
        List<Map<String, Object>> summary = new ArrayList<>();

        for (int i = 0; i < 24; i += 6) {
            LocalDateTime time = now.plusHours(i);
            PredictionResult wma = predictSinglePoint(parkingLotId, time, "wma");
            PredictionResult es = predictSinglePoint(parkingLotId, time, "es");

            summary.add(Map.of(
                    "time", time.toString(),
                    "wma_prediction", wma.getOccupancyRate(),
                    "es_prediction", es.getOccupancyRate(),
                    "recommended", wma.getOccupancyRate().compareTo(es.getOccupancyRate()) <= 0 ? "WMA" : "ES"
            ));
        }

        return summary;
    }

    private PredictionModel getPredictionModel(String modelType) {
        if (modelType == null || "wma".equalsIgnoreCase(modelType)) {
            return wmaPredictor;
        } else if ("es".equalsIgnoreCase(modelType)) {
            return esPredictor;
        } else {
            throw new IllegalArgumentException("不支持的预测模型类型: " + modelType + ", 可选值: wma, es");
        }
    }

    private double calculateConfidenceForPoint(LocalDateTime targetTime) {
        int hoursAhead = (int) java.time.Duration.between(LocalDateTime.now(), targetTime).toHours();
        if (hoursAhead <= 0) hoursAhead = 1;

        if (hoursAhead <= 6) return 0.85 - (hoursAhead * 0.03);
        else if (hoursAhead <= 12) return 0.70 - ((hoursAhead - 6) * 0.02);
        else if (hoursAhead <= 24) return 0.58 - ((hoursAhead - 12) * 0.01);
        else return 0.46 - Math.max(0, (hoursAhead - 24) * 0.008);
    }

    private String generateParkingTip(double occupancyRate) {
        if (occupancyRate < 30) return "车位充足，随时可停";
        else if (occupancyRate < 50) return "车位较多，建议前往";
        else if (occupancyRate < 70) return "车位适中，建议提前预约";
        else if (occupancyRate < 85) return "车位紧张，请尽早到达";
        else return "车位已满或接近满员，建议选择其他停车场";
    }

    @Data
    @ConfigurationProperties(prefix = "prediction")
    public static class PredictorConfig {
        private String defaultModel = "wma";
        private int defaultForecastHours = 24;
        private double ensembleWmaWeight = 0.5;
        private double ensembleEsWeight = 0.5;
    }

    @Data
    @AllArgsConstructor
    public static class EnsemblePredictionResult {
        private List<PredictionResult> ensembleResults;
        private List<PredictionResult> wmaResults;
        private List<PredictionResult> esResults;
    }

    @Data
    @AllArgsConstructor
    public static class BestParkingTimeRecommendation {
        private LocalDateTime recommendedTime;
        private BigDecimal expectedOccupancyRate;
        private Double confidence;
        private String tip;
    }

    @Data
    @AllArgsConstructor
    public static class ModelComparisonInfo {
        private String wmaModelName;
        private String wmaDescription;
        private double wmaAvgConfidence;
        private String esModelName;
        private String esDescription;
        private double esAvgConfidence;
    }
}
