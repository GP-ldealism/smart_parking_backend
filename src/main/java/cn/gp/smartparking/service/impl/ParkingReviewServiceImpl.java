package cn.gp.smartparking.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.gp.smartparking.model.entity.ParkingReview;
import cn.gp.smartparking.service.ParkingReviewService;
import cn.gp.smartparking.mapper.ParkingReviewMapper;
import org.springframework.stereotype.Service;

/**
* @author HeGuoping
* @description 针对表【parking_review(停车场评价表)】的数据库操作Service实现
* @createDate 2026-04-25
*/
@Service
public class ParkingReviewServiceImpl extends ServiceImpl<ParkingReviewMapper, ParkingReview>
    implements ParkingReviewService{

}
