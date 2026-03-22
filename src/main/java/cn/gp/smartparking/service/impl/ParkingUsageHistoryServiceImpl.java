package cn.gp.smartparking.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.gp.smartparking.model.entity.ParkingUsageHistory;
import cn.gp.smartparking.service.ParkingUsageHistoryService;
import cn.gp.smartparking.mapper.ParkingUsageHistoryMapper;
import org.springframework.stereotype.Service;

/**
* @author HeGuoping
* @description 针对表【parking_usage_history(车位历史占用表)】的数据库操作Service实现
* @createDate 2026-03-18 22:44:40
*/
@Service
public class ParkingUsageHistoryServiceImpl extends ServiceImpl<ParkingUsageHistoryMapper, ParkingUsageHistory>
    implements ParkingUsageHistoryService{

}




