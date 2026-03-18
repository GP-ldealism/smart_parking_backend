package cn.gp.smartparking.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.gp.smartparking.domain.entity.Blacklist;
import cn.gp.smartparking.service.BlacklistService;
import cn.gp.smartparking.mapper.BlacklistMapper;
import org.springframework.stereotype.Service;

/**
* @author HeGuoping
* @description 针对表【blacklist(黑名单表)】的数据库操作Service实现
* @createDate 2026-03-18 22:44:40
*/
@Service
public class BlacklistServiceImpl extends ServiceImpl<BlacklistMapper, Blacklist>
    implements BlacklistService{

}




