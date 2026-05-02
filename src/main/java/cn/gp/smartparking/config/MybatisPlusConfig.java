package cn.gp.smartparking.config;

import cn.gp.smartparking.interceptor.SqlPerformanceInterceptor;
import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus配置类
 * 配置分页插件和SQL性能监控拦截器
 */
@Configuration
public class MybatisPlusConfig {

    @Resource
    private SqlPerformanceInterceptor sqlPerformanceInterceptor;

    /**
     * 配置MyBatis-Plus拦截器
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        
        // 添加乐观锁插件
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
        
        // 添加分页插件
        PaginationInnerInterceptor paginationInnerInterceptor = new PaginationInnerInterceptor(DbType.MYSQL);
        paginationInnerInterceptor.setMaxLimit(500L); // 设置最大单页限制数量
        interceptor.addInnerInterceptor(paginationInnerInterceptor);
        
        // 添加SQL性能监控拦截器
        interceptor.addInnerInterceptor(sqlPerformanceInterceptor);
        
        return interceptor;
    }
}
