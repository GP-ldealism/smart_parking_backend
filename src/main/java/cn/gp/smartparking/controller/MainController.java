package cn.gp.smartparking.controller;

import cn.gp.smartparking.common.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/main")
public class MainController {

    @GetMapping("/health")
    public Result<String> health() {
        return Result.success("OK");
    }

    @GetMapping("/exception")
    public Result<String> exceptionHandler() {
        int a = 0;
        int b = 1;
        int c = b / a;
        return Result.success("exception");
    }
}
