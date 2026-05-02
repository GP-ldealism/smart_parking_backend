package cn.gp.smartparking.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.gp.smartparking.model.entity.ParkingLot;
import cn.gp.smartparking.model.entity.ParkingSpace;
import cn.gp.smartparking.service.ParkingLotService;
import cn.gp.smartparking.service.ParkingSpaceService;
import cn.gp.smartparking.mapper.ParkingLotMapper;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.util.concurrent.TimeUnit;

/**
* @author HeGuoping
* @description 针对表【parking_lot(停车场信息表)】的数据库操作Service实现
* @createDate 2026-03-18 22:44:40
*/
@Service
public class ParkingLotServiceImpl extends ServiceImpl<ParkingLotMapper, ParkingLot>
    implements ParkingLotService{

    @Resource
    private ParkingSpaceService parkingSpaceService;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    private static final String PARKING_LOT_CACHE_PREFIX = "smart_parking:parking_lot:";
    private static final long CACHE_EXPIRE_MINUTES = 30;

    @Override
    public ParkingLot calculateSpaceStats(Long parkingLotId) {
        String cacheKey = PARKING_LOT_CACHE_PREFIX + parkingLotId;
        
        // 先从Redis缓存获取
        ParkingLot cachedLot = (ParkingLot) redisTemplate.opsForValue().get(cacheKey);
        if (cachedLot != null) {
            return cachedLot;
        }
        
        // 缓存未命中，查询数据库
        ParkingLot parkingLot = this.getById(parkingLotId);
        if (parkingLot == null) {
            return null;
        }

        // 动态计算总车位（从车位表统计）
        long totalSpace = parkingSpaceService.lambdaQuery()
                .eq(ParkingSpace::getParkingLotId, parkingLotId)
                .eq(ParkingSpace::getIsDeleted, 0)
                .count();

        // 动态计算空闲车位（从车位表统计）
        long freeSpace = parkingSpaceService.lambdaQuery()
                .eq(ParkingSpace::getParkingLotId, parkingLotId)
                .eq(ParkingSpace::getStatus, 1) // 1=空闲
                .eq(ParkingSpace::getIsDeleted, 0)
                .count();

        parkingLot.setTotalSpace((int) totalSpace);
        parkingLot.setFreeSpace((int) freeSpace);

        // 写入Redis缓存，30分钟过期
        redisTemplate.opsForValue().set(cacheKey, parkingLot, CACHE_EXPIRE_MINUTES, TimeUnit.MINUTES);

        return parkingLot;
    }

    @Override
    public void calculateSpaceStatsForList(java.util.List<ParkingLot> parkingLots) {
        if (parkingLots == null || parkingLots.isEmpty()) {
            return;
        }

        for (ParkingLot parkingLot : parkingLots) {
            if (parkingLot.getId() != null) {
                // 统计总车位
                long totalSpace = parkingSpaceService.lambdaQuery()
                        .eq(ParkingSpace::getParkingLotId, parkingLot.getId())
                        .eq(ParkingSpace::getIsDeleted, 0)
                        .count();

                // 统计空闲车位
                long freeSpace = parkingSpaceService.lambdaQuery()
                        .eq(ParkingSpace::getParkingLotId, parkingLot.getId())
                        .eq(ParkingSpace::getStatus, 1) // 1=空闲
                        .eq(ParkingSpace::getIsDeleted, 0)
                        .count();

                parkingLot.setTotalSpace((int) totalSpace);
                parkingLot.setFreeSpace((int) freeSpace);
            }
        }
    }

    /**
     * 清除停车场缓存
     */
    public void clearParkingLotCache(Long parkingLotId) {
        String cacheKey = PARKING_LOT_CACHE_PREFIX + parkingLotId;
        redisTemplate.delete(cacheKey);
    }

    /**
     * 清除所有停车场缓存
     */
    public void clearAllParkingLotCache() {
        redisTemplate.delete(redisTemplate.keys(PARKING_LOT_CACHE_PREFIX + "*"));
    }
}




