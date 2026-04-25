package cn.gp.smartparking.algorithm.mapper;

import cn.gp.smartparking.model.entity.ParkingLot;
import cn.gp.smartparking.model.entity.ParkingUsageHistory;
import cn.gp.smartparking.model.entity.UserPreference;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AlgorithmDataMapper extends BaseMapper<ParkingLot> {

    @Select("SELECT * FROM parking_lot WHERE status = 1 AND is_deleted = 0")
    List<ParkingLot> selectActiveParkingLots();

    @Select("SELECT * FROM parking_usage_history WHERE parking_lot_id = #{parkingLotId} AND record_time >= #{startTime} ORDER BY record_time DESC")
    List<ParkingUsageHistory> selectHistoryByParkingLotAndTimeRange(
            @Param("parkingLotId") Long parkingLotId,
            @Param("startTime") java.time.LocalDateTime startTime);

    @Select("SELECT * FROM parking_usage_history WHERE parking_lot_id = #{parkingLotId} AND record_time >= #{startTime} AND record_time <= #{endTime} ORDER BY record_time DESC")
    List<ParkingUsageHistory> selectHistoryByTimeRange(
            @Param("parkingLotId") Long parkingLotId,
            @Param("startTime") java.time.LocalDateTime startTime,
            @Param("endTime") java.time.LocalDateTime endTime);

    @Select("SELECT * FROM user_preference WHERE user_id = #{userId}")
    UserPreference selectUserPreferenceByUserId(@Param("userId") Long userId);

    @Select("SELECT AVG(occupancy_rate) FROM parking_usage_history WHERE parking_lot_id = #{parkingLotId} AND `hour` = #{hour}")
    Double selectAverageOccupancyRateByHour(@Param("parkingLotId") Long parkingLotId, @Param("hour") Integer hour);

    @Select("SELECT * FROM parking_usage_history WHERE parking_lot_id = #{parkingLotId} ORDER BY record_time DESC LIMIT #{limitNum}")
    List<ParkingUsageHistory> selectRecentHistory(@Param("parkingLotId") Long parkingLotId, @Param("limitNum") Integer limitNum);
}