package cn.gp.smartparking;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan({"cn.gp.smartparking.mapper", "cn.gp.smartparking.algorithm.mapper"})
public class SmartParkingCloudPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartParkingCloudPlatformApplication.class, args);
    }

}
