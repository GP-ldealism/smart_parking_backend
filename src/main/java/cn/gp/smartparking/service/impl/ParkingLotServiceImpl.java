package cn.gp.smartparking.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.gp.smartparking.model.entity.ParkingLot;
import cn.gp.smartparking.model.entity.ParkingSpace;
import cn.gp.smartparking.service.ParkingLotService;
import cn.gp.smartparking.service.ParkingSpaceService;
import cn.gp.smartparking.mapper.ParkingLotMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

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

    @Override
    public ParkingLot calculateSpaceStats(Long parkingLotId) {
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
        // 统计空闲车位
        long freeSpace = parkingSpaceService.lambdaQuery()
                .eq(ParkingSpace::getParkingLotId, parkingLotId)
                .eq(ParkingSpace::getStatus, 1) // 1=空闲
                .eq(ParkingSpace::getIsDeleted, 0)
                .count();

        parkingLot.setTotalSpace((int) totalSpace);
        parkingLot.setFreeSpace((int) freeSpace);

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
}




