package cn.gp.smartparking.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.gp.smartparking.domain.entity.ParkingPrediction;
import cn.gp.smartparking.service.ParkingPredictionService;
import cn.gp.smartparking.mapper.ParkingPredictionMapper;
import org.springframework.stereotype.Service;

/**
* @author HeGuoping
* @description 针对表【parking_prediction(车位占用率预测结果表)】的数据库操作Service实现
* @createDate 2026-03-18 22:44:40
*/
@Service
public class ParkingPredictionServiceImpl extends ServiceImpl<ParkingPredictionMapper, ParkingPrediction>
    implements ParkingPredictionService{

}




