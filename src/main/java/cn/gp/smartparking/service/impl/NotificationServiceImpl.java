package cn.gp.smartparking.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.gp.smartparking.domain.entity.Notification;
import cn.gp.smartparking.service.NotificationService;
import cn.gp.smartparking.mapper.NotificationMapper;
import org.springframework.stereotype.Service;

/**
* @author HeGuoping
* @description 针对表【notification(消息通知表)】的数据库操作Service实现
* @createDate 2026-03-18 22:44:40
*/
@Service
public class NotificationServiceImpl extends ServiceImpl<NotificationMapper, Notification>
    implements NotificationService{

}




