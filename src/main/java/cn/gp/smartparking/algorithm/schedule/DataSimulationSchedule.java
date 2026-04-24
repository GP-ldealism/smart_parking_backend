package cn.gp.smartparking.algorithm.schedule;

import cn.gp.smartparking.algorithm.service.DataSimulationService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 数据模拟定时任务
 * 定期生成停车数据，提高推荐和预测准确性
 */
@Slf4j
@Component
@EnableScheduling
@ConditionalOnProperty(name = "data.simulation.enabled", havingValue = "true", matchIfMissing = true)
public class DataSimulationSchedule {

    @Resource
    private DataSimulationService dataSimulationService;

    /**
     * 每天凌晨2点生成前一天的数据
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void generateDailyData() {
        log.info("开始执行每日数据生成任务");
        try {
            // 生成昨天24小时的数据
            dataSimulationService.generateHistoricalData(1);
            log.info("每日数据生成任务完成");
        } catch (Exception e) {
            log.error("每日数据生成任务失败", e);
        }
    }

    /**
     * 每周日凌晨3点生成历史数据（补全缺失数据）
     */
    @Scheduled(cron = "0 0 3 ? * SUN")
    public void generateWeeklyData() {
        log.info("开始执行每周数据生成任务");
        try {
            // 生成过去7天的数据
            dataSimulationService.generateHistoricalData(7);
            log.info("每周数据生成任务完成");
        } catch (Exception e) {
            log.error("每周数据生成任务失败", e);
        }
    }

    /**
     * 每月1号凌晨4点生成月度数据
     */
    @Scheduled(cron = "0 0 4 1 * ?")
    public void generateMonthlyData() {
        log.info("开始执行每月数据生成任务");
        try {
            // 生成过去30天的数据
            dataSimulationService.generateHistoricalData(30);
            log.info("每月数据生成任务完成");
        } catch (Exception e) {
            log.error("每月数据生成任务失败", e);
        }
    }

    /**
     * 每小时生成当前小时的数据（实时性）
     */
    @Scheduled(cron = "0 5 * * * ?") // 每小时第5分钟执行
    public void generateHourlyData() {
        log.info("开始执行每小时数据生成任务");
        try {
            // 生成当前小时的数据
            dataSimulationService.generateHistoricalData(0); // 生成当前小时
            log.info("每小时数据生成任务完成");
        } catch (Exception e) {
            log.error("每小时数据生成任务失败", e);
        }
    }

    /**
     * 每天凌晨5点清理旧数据
     */
    @Scheduled(cron = "0 0 5 * * ?")
    public void cleanOldData() {
        log.info("开始执行数据清理任务");
        try {
            // 保留最近90天的数据
            dataSimulationService.cleanOldData(90);
            log.info("数据清理任务完成");
        } catch (Exception e) {
            log.error("数据清理任务失败", e);
        }
    }

    /**
     * 手动触发数据生成（用于测试和初始化）
     */
    public void generateInitialData() {
        log.info("开始执行初始数据生成任务");
        try {
            // 生成过去30天的历史数据作为初始化
            dataSimulationService.generateHistoricalData(30);
            log.info("初始数据生成任务完成");
        } catch (Exception e) {
            log.error("初始数据生成任务失败", e);
        }
    }
}
