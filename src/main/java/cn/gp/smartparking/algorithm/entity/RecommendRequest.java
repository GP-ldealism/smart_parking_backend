package cn.gp.smartparking.algorithm.entity;

import lombok.Data;
import java.io.Serializable;

@Data
public class RecommendRequest implements Serializable {

    private Double longitude;

    private Double latitude;

    private Long userId;

    private Integer recommendCount;

    public Double getLongitude() {
        return longitude != null ? longitude : 116.4;
    }

    public Double getLatitude() {
        return latitude != null ? latitude : 39.9;
    }

    public Integer getRecommendCount() {
        return recommendCount != null ? recommendCount : 5;
    }
}