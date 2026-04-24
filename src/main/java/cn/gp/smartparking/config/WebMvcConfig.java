package cn.gp.smartparking.config;

import cn.gp.smartparking.interceptor.JwtAuthInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private JwtAuthInterceptor jwtAuthInterceptor;



    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtAuthInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/doc.html",
                        "/doc.html/**",
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/v3/api-docs/**",
                        "/webjars/**",
                        "/user/login",
                        "/user/register",
                        "/parkingLot/list",
                        "/parking-lot/list",
                        "/prediction/**",
                        "/algorithm/prediction/**",
                        "/alipay/**",
                        "/main/**",
                        "/error"
                );
    }
}