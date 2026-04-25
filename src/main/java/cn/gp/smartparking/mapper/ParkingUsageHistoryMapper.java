package cn.gp.smartparking.mapper;

import cn.gp.smartparking.model.entity.ParkingUsageHistory;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
* @author HeGuoping
* @description 针对表【parking_usage_history(车位历史占用表)】的数据库操作Mapper
* @createDate 2026-03-18 22:44:40
* @Entity cn.gp.smartparking.domain.entity.ParkingUsageHistory
*/
@Mapper
public interface ParkingUsageHistoryMapper extends BaseMapper<ParkingUsageHistory> {

    /**
     * 批量插入
     */
    void insertBatchSomeColumn(List<ParkingUsageHistory> entityList);
}




