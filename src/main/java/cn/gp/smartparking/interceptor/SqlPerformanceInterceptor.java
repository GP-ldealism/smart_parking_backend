package cn.gp.smartparking.interceptor;

import com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.util.Properties;

/**
 * SQL性能监控拦截器
 * 拦截所有SQL执行，记录执行时间，用于分析慢SQL
 */
@Slf4j
@Component
public class SqlPerformanceInterceptor implements InnerInterceptor {

    /**
     * 慢SQL阈值（毫秒），超过此值记录为慢SQL
     */
    private static final long SLOW_SQL_THRESHOLD = 200L;


    public void beforeQuery(Executor executor, MappedStatement ms, Object parameter, 
                          RowBounds rowBounds, ResultHandler resultHandler) {
        long startTime = System.currentTimeMillis();
        MDC.put("sqlStartTime", String.valueOf(startTime));
    }


    public void afterQuery(Executor executor, MappedStatement ms, Object parameter, 
                         RowBounds rowBounds, ResultHandler resultHandler) {
        long endTime = System.currentTimeMillis();
        String startTimeStr = MDC.get("sqlStartTime");
        if (startTimeStr != null) {
            long startTime = Long.parseLong(startTimeStr);
            long executeTime = endTime - startTime;
            
            // 获取SQL信息
            String sql = getSql(ms, parameter);
            String sqlId = ms.getId();
            
            // 记录SQL执行日志
            logSqlExecution(sqlId, sql, executeTime, null);
            
            MDC.remove("sqlStartTime");
        }
    }

    @Override
    public void beforeUpdate(Executor executor, MappedStatement ms, Object parameter) {
        long startTime = System.currentTimeMillis();
        MDC.put("sqlStartTime", String.valueOf(startTime));
    }


    public void afterUpdate(Executor executor, MappedStatement ms, Object parameter) {
        long endTime = System.currentTimeMillis();
        String startTimeStr = MDC.get("sqlStartTime");
        if (startTimeStr != null) {
            long startTime = Long.parseLong(startTimeStr);
            long executeTime = endTime - startTime;
            
            // 获取SQL信息
            String sql = getSql(ms, parameter);
            String sqlId = ms.getId();
            
            // 记录SQL执行日志
            logSqlExecution(sqlId, sql, executeTime, null);
            
            MDC.remove("sqlStartTime");
        }
    }

    /**
     * 记录SQL执行日志
     */
    private void logSqlExecution(String sqlId, String sql, long executeTime, Exception exception) {
        // 格式化SQL，去除多余空格和换行
        String formattedSql = formatSql(sql);
        
        // 从MDC获取接口路径信息
        String requestUri = MDC.get("requestUri");
        String requestMethod = MDC.get("requestMethod");
        
        // 构建接口信息前缀
        String apiInfo = "";
        if (requestUri != null && requestMethod != null) {
            apiInfo = String.format("接口: %s %s | ", requestMethod, requestUri);
        }
        
        if (exception != null) {
            // SQL执行失败
            log.error("SQL执行失败 - {}ID: {}, 耗时: {}ms, SQL: {}, 错误: {}", 
                apiInfo, sqlId, executeTime, formattedSql, exception.getMessage());
        } else if (executeTime > SLOW_SQL_THRESHOLD) {
            // 慢SQL警告
            log.warn("慢SQL警告 - {}ID: {}, 耗时: {}ms, SQL: {}", 
                apiInfo, sqlId, executeTime, formattedSql);
        } else {
            // 正常SQL记录（可选，可根据需要开启）
            log.info("SQL执行 - {}ID: {}, 耗时: {}ms, SQL: {}", 
                apiInfo, sqlId, executeTime, formattedSql);
        }
    }

    /**
     * 获取SQL语句
     */
    private String getSql(MappedStatement mappedStatement, Object parameter) {
        try {
            BoundSql boundSql = mappedStatement.getBoundSql(parameter);
            return boundSql.getSql();
        } catch (Exception e) {
            return "获取SQL失败: " + e.getMessage();
        }
    }

    /**
     * 格式化SQL，去除多余空格和换行
     */
    private String formatSql(String sql) {
        if (sql == null) {
            return "";
        }
        return sql.replaceAll("\\s+", " ").trim();
    }

    public Object plugin(Object target) {
        return Plugin.wrap(target, (Interceptor) this);
    }

    @Override
    public void setProperties(Properties properties) {
        // 可以从配置文件读取慢SQL阈值等配置
        String threshold = properties.getProperty("slowSqlThreshold");
        if (threshold != null) {
            try {
                // 这里可以动态设置阈值，目前使用静态常量
                log.info("SQL性能监控拦截器已配置，慢SQL阈值: {}ms", threshold);
            } catch (NumberFormatException e) {
                log.warn("慢SQL阈值配置格式错误: {}", threshold);
            }
        }
    }
}
