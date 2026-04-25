package cn.gp.smartparking.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.gp.smartparking.model.entity.BaseSysLog;
import cn.gp.smartparking.service.BaseSysLogService;
import cn.gp.smartparking.mapper.BaseSysLogMapper;
import org.springframework.stereotype.Service;

/**
* @author HeGuoping
* @description 针对表【base_sys_log(系统操作日志表)】的数据库操作Service实现
* @createDate 2026-03-18 22:44:40
*/
@Service
public class BaseSysLogServiceImpl extends ServiceImpl<BaseSysLogMapper, BaseSysLog>
    implements BaseSysLogService{

}




