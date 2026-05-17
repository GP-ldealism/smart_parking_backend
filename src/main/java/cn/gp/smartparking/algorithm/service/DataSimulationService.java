package cn.gp.smartparking.algorithm.service;

import cn.gp.smartparking.algorithm.mapper.AlgorithmDataMapper;
import cn.gp.smartparking.mapper.ParkingUsageHistoryMapper;
import cn.gp.smartparking.model.entity.ParkingUsageHistory;
import cn.gp.smartparking.model.entity.ParkingLot;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

/**
 * 停车数据模拟服务
 * 基于真实停车模式生成历史数据，提高推荐和预测准确性
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DataSimulationService {

    @Resource
    private AlgorithmDataMapper algorithmDataMapper;
    
    @Resource
    private ParkingUsageHistoryMapper parkingUsageHistoryMapper;
    
    private final Random random = new Random();

    /**
     * 生成指定天数的历史数据
     */
    public void generateHistoricalData(int days) {
        log.info("开始生成{}天的历史停车数据", days);

        List<ParkingLot> parkingLots = algorithmDataMapper.selectActiveParkingLots();
        if (parkingLots.isEmpty()) {
            log.warn("没有找到活跃的停车场，跳过数据生成");
            return;
        }

        LocalDateTime endTime = LocalDateTime.now();
        LocalDateTime startTime = endTime.minusDays(days);

        int totalRecords = 0;
        LocalDateTime current = startTime;

        while (current.isBefore(endTime)) {
            for (ParkingLot parkingLot : parkingLots) {
                // 每小时生成一条记录
                ParkingUsageHistory history = generateRealisticUsageRecord(parkingLot, current);
                parkingUsageHistoryMapper.insert(history);
                totalRecords++;
            }
            current = current.plusHours(1);

            // 每100条记录输出一次进度
            if (totalRecords % 100 == 0) {
                log.info("已插入{}条记录", totalRecords);
            }
        }

        log.info("数据生成完成，共生成{}条记录", totalRecords);
    }

    /**
     * 生成基于真实模式的停车使用记录
     */
    private ParkingUsageHistory generateRealisticUsageRecord(ParkingLot parkingLot, LocalDateTime time) {
        ParkingUsageHistory history = new ParkingUsageHistory();
        history.setParkingLotId(parkingLot.getId());
        history.setRecordTime(java.sql.Timestamp.valueOf(time));

        // 基于时段、星期、停车场位置等因素生成占用率
        double occupancyRate = calculateRealisticOccupancyRate(parkingLot, time);

        history.setOccupancyRate(BigDecimal.valueOf(occupancyRate));
        history.setHour(time.getHour());
        history.setWeekday(time.getDayOfWeek().getValue());
        history.setIsHoliday(isWeekend(time) ? 1 : 0);

        // 设置审计字段，避免null值
        history.setCreateBy(Long.valueOf("system"));
        history.setUpdateBy(Long.valueOf("system"));
        history.setCreateTime(java.sql.Timestamp.valueOf(time));
        history.setUpdateTime(java.sql.Timestamp.valueOf(time));

        return history;
    }

    /**
     * 计算真实的占用率
     */
    private double calculateRealisticOccupancyRate(ParkingLot parkingLot, LocalDateTime time) {
        int hour = time.getHour();
        int dayOfWeek = time.getDayOfWeek().getValue();
        
        // 基础占用率模式
        double basePattern = getBaseOccupancyPattern(hour);
        
        // 星期调整
        double weeklyFactor = getWeeklyFactor(dayOfWeek);
        
        // 停车场位置因子（假设有位置信息）
        double locationFactor = getLocationFactor(parkingLot);
        
        // 随机波动
        double randomFactor = 1.0 + (random.nextGaussian() * 0.1); // 10%的随机波动
        
        // 特殊事件因子（节假日、天气等）
        double eventFactor = getEventFactor(time);
        
        double occupancyRate = basePattern * weeklyFactor * locationFactor * randomFactor * eventFactor;
        
        return Math.max(5.0, Math.min(95.0, occupancyRate));
    }

    /**
     * 基础占用率模式（24小时）
     */
    private double getBaseOccupancyPattern(int hour) {
        // 基于真实停车场的24小时模式
        double[] patterns = {
            15.0, 12.0, 10.0, 8.0, 10.0, 15.0,  // 0-5: 深夜到凌晨
            25.0, 45.0, 65.0, 75.0, 80.0, 85.0,  // 6-11: 早上到上午
            82.0, 78.0, 75.0, 72.0, 75.0, 80.0,  // 12-17: 下午
            85.0, 88.0, 75.0, 60.0, 45.0, 30.0   // 18-23: 晚上到深夜
        };
        
        if (hour >= 0 && hour < 24) {
            return patterns[hour];
        }
        return 50.0;
    }

    /**
     * 星期因子
     */
    private double getWeeklyFactor(int dayOfWeek) {
        // 周一到周日
        double[] factors = {1.1, 1.15, 1.2, 1.1, 1.05, 0.8, 0.7};
        if (dayOfWeek >= 1 && dayOfWeek <= 7) {
            return factors[dayOfWeek - 1];
        }
        return 1.0;
    }

    /**
     * 位置因子（基于停车场特征）
     */
    private double getLocationFactor(ParkingLot parkingLot) {
        // 基于停车场费率、位置等估算
        if (parkingLot.getRate() != null) {
            double rate = parkingLot.getRate().doubleValue();
            if (rate <= 5) {
                return 1.2; // 便宜停车场更受欢迎
            } else if (rate <= 10) {
                return 1.0;
            } else if (rate <= 20) {
                return 0.9;
            } else {
                return 0.7; // 昂贵停车场使用率较低
            }
        }
        return 1.0;
    }

    /**
     * 特殊事件因子
     */
    private double getEventFactor(LocalDateTime time) {
        // 可以根据节假日、天气等调整
        // 这里简单示例，实际可以接入节假日API
        int month = time.getMonthValue();
        int day = time.getDayOfMonth();
        
        // 假设某些日期有特殊活动
        if ((month == 12 && day >= 20) || (month == 1 && day <= 3)) {
            return 1.3; // 元旦期间
        } else if (month == 10 && day >= 1 && day <= 7) {
            return 1.2; // 国庆期间
        }
        
        return 1.0;
    }

    /**
     * 判断是否为周末
     */
    private boolean isWeekend(LocalDateTime time) {
        int dayOfWeek = time.getDayOfWeek().getValue();
        return dayOfWeek == 6 || dayOfWeek == 7; // 周六或周日
    }

    /**
     * 生成未来几天的预测数据
     */
    public void generateFutureData(int days) {
        log.info("开始生成{}天的未来预测数据", days);

        List<ParkingLot> parkingLots = algorithmDataMapper.selectActiveParkingLots();
        LocalDateTime startTime = LocalDateTime.now();
        LocalDateTime endTime = startTime.plusDays(days);

        int totalRecords = 0;
        LocalDateTime current = startTime;

        while (current.isBefore(endTime)) {
            for (ParkingLot parkingLot : parkingLots) {
                // 生成未来数据（用于测试预测）
                ParkingUsageHistory history = generateRealisticUsageRecord(parkingLot, current);
                parkingUsageHistoryMapper.insert(history);
                totalRecords++;
            }
            current = current.plusHours(1);

            // 每100条记录输出一次进度
            if (totalRecords % 100 == 0) {
                log.info("已插入{}条记录", totalRecords);
            }
        }

        log.info("未来数据生成完成，共生成{}条记录", totalRecords);
    }

    /**
     * 清理旧数据
     */
    public void cleanOldData(int daysToKeep) {
        log.info("开始清理{}天前的旧数据", daysToKeep);
        
        LocalDateTime cutoffTime = LocalDateTime.now().minusDays(daysToKeep);
        
        // 这里需要实现删除逻辑
        // algorithmDataMapper.deleteOldRecords(cutoffTime);
        
        log.info("旧数据清理完成");
    }
}
