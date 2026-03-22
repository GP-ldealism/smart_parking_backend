package cn.gp.smartparking.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.gp.smartparking.model.entity.PaymentRecord;
import cn.gp.smartparking.service.PaymentRecordService;
import cn.gp.smartparking.mapper.PaymentRecordMapper;
import org.springframework.stereotype.Service;

/**
* @author HeGuoping
* @description 针对表【payment_record(支付记录表)】的数据库操作Service实现
* @createDate 2026-03-18 22:44:40
*/
@Service
public class PaymentRecordServiceImpl extends ServiceImpl<PaymentRecordMapper, PaymentRecord>
    implements PaymentRecordService{

}




