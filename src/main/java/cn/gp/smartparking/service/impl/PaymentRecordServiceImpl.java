package cn.gp.smartparking.service.impl;

import cn.gp.smartparking.mapper.PaymentRecordMapper;
import cn.gp.smartparking.model.entity.PaymentRecord;
import cn.gp.smartparking.service.PaymentRecordService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class PaymentRecordServiceImpl extends ServiceImpl<PaymentRecordMapper, PaymentRecord> implements PaymentRecordService {
}
