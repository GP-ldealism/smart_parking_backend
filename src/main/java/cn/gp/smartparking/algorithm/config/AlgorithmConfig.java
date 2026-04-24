package cn.gp.smartparking.algorithm.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "algorithm")
public class AlgorithmConfig {

    private Double distanceWeight = 0.3;

    private Double ratingWeight = 0.25;

    private Double freeSpaceWeight = 0.25;

    private Double preferenceWeight = 0.2;

    private Integer predictionHistoryHours = 168;

    private Integer recommendRadius = 5000;

    private Double defaultRating = 4.0;

    private String modelType = "arima";
}