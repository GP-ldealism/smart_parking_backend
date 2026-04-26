package cn.gp.smartparking.service;

import cn.gp.smartparking.model.entity.ParkingLot;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author HeGuoping
* @description 针对表【parking_lot(停车场信息表)】的数据库操作Service
* @createDate 2026-03-18 22:44:40
*/
public interface ParkingLotService extends IService<ParkingLot> {

    /**
     * 计算停车场的车位统计信息（用于返回前端时动态计算）
     * @param parkingLotId 停车场ID
     * @return 包含动态计算的 totalSpace 和 freeSpace 的停车场对象
     */
    ParkingLot calculateSpaceStats(Long parkingLotId);

    /**
     * 批量计算停车场的车位统计信息
     * @param parkingLots 停车场列表
     */
    void calculateSpaceStatsForList(java.util.List<ParkingLot> parkingLots);
}
