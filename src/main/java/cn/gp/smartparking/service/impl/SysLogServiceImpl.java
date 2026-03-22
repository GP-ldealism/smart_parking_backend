package cn.gp.smartparking.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.gp.smartparking.model.entity.SysLog;
import cn.gp.smartparking.service.SysLogService;
import cn.gp.smartparking.mapper.SysLogMapper;
import org.springframework.stereotype.Service;

/**
* @author HeGuoping
* @description 针对表【sys_log(系统操作日志表)】的数据库操作Service实现
* @createDate 2026-03-18 22:44:40
*/
@Service
public class SysLogServiceImpl extends ServiceImpl<SysLogMapper, SysLog>
    implements SysLogService{

}




