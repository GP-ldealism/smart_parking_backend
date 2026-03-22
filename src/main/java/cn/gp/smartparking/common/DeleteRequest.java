package cn.gp.smartparking.common;

import lombok.Data;

import java.io.Serializable;

/**
 * @ClassName DeleteRequest
 * @Description TODO
 * @Author Guoping He
 * @Date 2026/3/22 11:27
 */
@Data
public class DeleteRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
}
