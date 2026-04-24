package cn.gp.smartparking.algorithm.service;

import cn.gp.smartparking.algorithm.mapper.AlgorithmDataMapper;
import cn.gp.smartparking.model.entity.ParkingUsageHistory;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 数据质量监控服务
 * 监控历史数据的质量，确保推荐和预测的准确性
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DataQualityMonitorService {

    @Resource
    private AlgorithmDataMapper algorithmDataMapper;

    /**
     * 数据质量报告
     */
    public DataQualityReport generateQualityReport() {
        log.info("开始生成数据质量报告");
        
        DataQualityReport report = new DataQualityReport();
        report.setReportTime(LocalDateTime.now());
        
        // 检查数据完整性
        checkDataCompleteness(report);
        
        // 检查数据一致性
        checkDataConsistency(report);
        
        // 检查数据异常值
        checkDataAnomalies(report);
        
        // 检查数据时效性
        checkDataTimeliness(report);
        
        log.info("数据质量报告生成完成: {}", report);
        return report;
    }

    /**
     * 检查数据完整性
     */
    private void checkDataCompleteness(DataQualityReport report) {
        try {
            // 检查过去7天的数据完整性
            LocalDateTime endTime = LocalDateTime.now();
            LocalDateTime startTime = endTime.minusDays(7);
            
            List<ParkingUsageHistory> histories = algorithmDataMapper.selectHistoryByTimeRange(
                null, startTime, endTime);
            
            Map<Integer, AtomicInteger> hourlyDataCount = new HashMap<>();
            
            for (ParkingUsageHistory history : histories) {
                int hour = history.getHour() != null ? history.getHour() : 0;
                hourlyDataCount.computeIfAbsent(hour, k -> new AtomicInteger(0)).incrementAndGet();
            }
            
            // 计算完整性评分
            int expectedHours = 7 * 24; // 7天 * 24小时
            int actualHours = hourlyDataCount.size();
            double completenessScore = (double) actualHours / expectedHours;
            
            report.setCompletenessScore(Math.max(0, Math.min(1, completenessScore)));
            report.setMissingDataPoints(expectedHours - actualHours);
            
            log.info("数据完整性检查完成: 完整性评分={}, 缺失数据点={}", 
                completenessScore, expectedHours - actualHours);
            
        } catch (Exception e) {
            log.error("数据完整性检查失败", e);
            report.setCompletenessScore(0.0);
        }
    }

    /**
     * 检查数据一致性
     */
    private void checkDataConsistency(DataQualityReport report) {
        try {
            List<ParkingUsageHistory> histories = algorithmDataMapper.selectHistoryByTimeRange(
                null, LocalDateTime.now().minusDays(1), LocalDateTime.now());
            
            if (histories.isEmpty()) {
                report.setConsistencyScore(0.0);
                return;
            }
            
            // 检查占用率与可用车位的一致性
            int inconsistentCount = 0;
            int totalCount = histories.size();
            
            for (ParkingUsageHistory history : histories) {
                if (isInconsistent(history)) {
                    inconsistentCount++;
                }
            }
            
            double consistencyScore = 1.0 - (double) inconsistentCount / totalCount;
            report.setConsistencyScore(Math.max(0, Math.min(1, consistencyScore)));
            report.setInconsistentDataPoints(inconsistentCount);
            
            log.info("数据一致性检查完成: 一致性评分={}, 不一致数据点={}", 
                consistencyScore, inconsistentCount);
            
        } catch (Exception e) {
            log.error("数据一致性检查失败", e);
            report.setConsistencyScore(0.0);
        }
    }

    /**
     * 检查数据异常值
     */
    private void checkDataAnomalies(DataQualityReport report) {
        try {
            List<ParkingUsageHistory> histories = algorithmDataMapper.selectHistoryByTimeRange(
                null, LocalDateTime.now().minusDays(7), LocalDateTime.now());
            
            if (histories.isEmpty()) {
                report.setAnomalyScore(1.0); // 没有数据，没有异常
                return;
            }
            
            // 计算统计指标
            double mean = histories.stream()
                .mapToDouble(h -> h.getOccupancyRate() != null ? h.getOccupancyRate().doubleValue() : 50.0)
                .average()
                .orElse(50.0);
            
            double stdDev = Math.sqrt(histories.stream()
                .mapToDouble(h -> {
                    double value = h.getOccupancyRate() != null ? h.getOccupancyRate().doubleValue() : 50.0;
                    return Math.pow(value - mean, 2);
                })
                .average()
                .orElse(0.0));
            
            // 检测异常值（超出3个标准差）
            int anomalyCount = 0;
            for (ParkingUsageHistory history : histories) {
                double value = history.getOccupancyRate() != null ? history.getOccupancyRate().doubleValue() : 50.0;
                if (Math.abs(value - mean) > 3 * stdDev) {
                    anomalyCount++;
                }
            }
            
            double anomalyScore = 1.0 - (double) anomalyCount / histories.size();
            report.setAnomalyScore(Math.max(0, Math.min(1, anomalyScore)));
            report.setAnomalyDataPoints(anomalyCount);
            
            log.info("数据异常值检查完成: 异常评分={}, 异常数据点={}", 
                anomalyScore, anomalyCount);
            
        } catch (Exception e) {
            log.error("数据异常值检查失败", e);
            report.setAnomalyScore(0.0);
        }
    }

    /**
     * 检查数据时效性
     */
    private void checkDataTimeliness(DataQualityReport report) {
        try {
            // 检查最近一小时的数据
            LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
            List<ParkingUsageHistory> recentHistories = algorithmDataMapper.selectHistoryByTimeRange(
                null, oneHourAgo, LocalDateTime.now());
            
            // 检查最近24小时的数据
            LocalDateTime oneDayAgo = LocalDateTime.now().minusDays(1);
            List<ParkingUsageHistory> dailyHistories = algorithmDataMapper.selectHistoryByTimeRange(
                null, oneDayAgo, LocalDateTime.now());
            
            double hourlyTimeliness = recentHistories.isEmpty() ? 0.0 : 1.0;
            double dailyTimeliness = dailyHistories.isEmpty() ? 0.0 : 1.0;
            
            // 综合时效性评分
            double timelinessScore = (hourlyTimeliness * 0.7 + dailyTimeliness * 0.3);
            report.setTimelinessScore(timelinessScore);
            
            log.info("数据时效性检查完成: 时效性评分={}", timelinessScore);
            
        } catch (Exception e) {
            log.error("数据时效性检查失败", e);
            report.setTimelinessScore(0.0);
        }
    }

    /**
     * 检查数据一致性
     */
    private boolean isInconsistent(ParkingUsageHistory history) {
        if (history.getOccupancyRate() == null) {
            return true;
        }
        
        // 检查占用率是否在合理范围内
        double occupancyRate = history.getOccupancyRate().doubleValue();
        return occupancyRate < 0 || occupancyRate > 100;
    }

    /**
     * 数据质量报告
     */
    public static class DataQualityReport {
        private LocalDateTime reportTime;
        private double completenessScore;    // 完整性评分
        private double consistencyScore;     // 一致性评分
        private double anomalyScore;         // 异常评分
        private double timelinessScore;       // 时效性评分
        private int missingDataPoints;        // 缺失数据点
        private int inconsistentDataPoints;  // 不一致数据点
        private int anomalyDataPoints;        // 异常数据点
        
        // 计算总体质量评分
        public double getOverallScore() {
            return (completenessScore * 0.3 + consistencyScore * 0.3 + 
                    anomalyScore * 0.2 + timelinessScore * 0.2);
        }
        
        public String getQualityLevel() {
            double score = getOverallScore();
            if (score >= 0.9) return "优秀";
            if (score >= 0.8) return "良好";
            if (score >= 0.7) return "一般";
            if (score >= 0.6) return "较差";
            return "很差";
        }

        @Override
        public String toString() {
            return String.format("DataQualityReport{时间=%s, 总体评分=%.2f(%s), " +
                "完整性=%.2f, 一致性=%.2f, 异常=%.2f, 时效性=%.2f}",
                reportTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                getOverallScore(), getQualityLevel(),
                completenessScore, consistencyScore, anomalyScore, timelinessScore);
        }

        // Getters and Setters
        public LocalDateTime getReportTime() { return reportTime; }
        public void setReportTime(LocalDateTime reportTime) { this.reportTime = reportTime; }
        
        public double getCompletenessScore() { return completenessScore; }
        public void setCompletenessScore(double completenessScore) { this.completenessScore = completenessScore; }
        
        public double getConsistencyScore() { return consistencyScore; }
        public void setConsistencyScore(double consistencyScore) { this.consistencyScore = consistencyScore; }
        
        public double getAnomalyScore() { return anomalyScore; }
        public void setAnomalyScore(double anomalyScore) { this.anomalyScore = anomalyScore; }
        
        public double getTimelinessScore() { return timelinessScore; }
        public void setTimelinessScore(double timelinessScore) { this.timelinessScore = timelinessScore; }
        
        public int getMissingDataPoints() { return missingDataPoints; }
        public void setMissingDataPoints(int missingDataPoints) { this.missingDataPoints = missingDataPoints; }
        
        public int getInconsistentDataPoints() { return inconsistentDataPoints; }
        public void setInconsistentDataPoints(int inconsistentDataPoints) { this.inconsistentDataPoints = inconsistentDataPoints; }
        
        public int getAnomalyDataPoints() { return anomalyDataPoints; }
        public void setAnomalyDataPoints(int anomalyDataPoints) { this.anomalyDataPoints = anomalyDataPoints; }
    }
}
