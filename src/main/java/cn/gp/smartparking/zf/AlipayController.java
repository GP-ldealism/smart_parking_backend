package cn.gp.smartparking.zf;

import cn.gp.smartparking.annotation.Log;
import cn.gp.smartparking.common.Result;
import com.alipay.api.AlipayApiException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


@Slf4j
@Controller
@RequestMapping("/api/alipay")
@Tag(name = "支付宝支付管理")
public class AlipayController {

    @Resource
    private PayService payService;

    @Log(module = "支付管理", operation = "支付", description = "Web端支付")
    @Operation(summary = "Web端支付 - 返回HTML表单")
    @GetMapping("/web/pay")
    @ResponseBody
    public String webPay(@RequestParam Long orderId) {
        try {
            String payForm = payService.createWebPayOrder(orderId);
            if (payForm != null) {
                return payForm;
            } else {
                return "订单不存在或支付表单构建失败";
            }
        } catch (Exception e) {
            log.error("Web支付异常", e);
            return "支付请求失败: " + e.getMessage();
        }
    }

    @Log(module = "支付管理", operation = "支付", description = "移动端支付")
    @Operation(summary = "移动端APP支付 - 返回orderString")
    @PostMapping("/mobile/pay")
    @ResponseBody
    public Result<String> mobilePay(@RequestParam Long orderId) {
        try {
            String orderString = payService.createMobilePayOrder(orderId);
            if (orderString != null) {
                return Result.success("获取APP支付订单成功", orderString);
            } else {
                return Result.fail("APP支付订单构建失败");
            }
        } catch (Exception e) {
            log.error("移动端APP支付异常", e);
            return Result.fail("APP支付请求失败: " + e.getMessage());
        }
    }

    @Operation(summary = "查询支付结果")
    @GetMapping("/query")
    @ResponseBody
    public String queryPayResult(@RequestParam String tradeNo) {
        try {
            return payService.queryPayResult(tradeNo);
        } catch (Exception e) {
            log.error("查询支付结果异常", e);
            return "查询失败: " + e.getMessage();
        }
    }

    @Operation(summary = "支付回调通知 - 异步")
    @PostMapping("/notify")
    @ResponseBody
    public String notify(@RequestParam Map<String, String> params) {
        log.info("收到支付宝异步回调 - params: {}", params);
        boolean success = payService.processNotify(params);
        return success ? "success" : "fail";
    }

    @Operation(summary = "移动端支付同步回调 - 返回JSON供移动端处理")
    @GetMapping("/mobile/return")
    @ResponseBody
    public Result<PayService.MobileReturnResponse> mobileReturn(@RequestParam Map<String, String> params) {
        log.info("收到移动端支付同步回调 - params: {}", params);
        String tradeStatus = params.get("trade_status");
        String outTradeNo = params.get("out_trade_no");

        if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {
            return Result.success("支付成功", new PayService.MobileReturnResponse(outTradeNo, "SUCCESS", tradeStatus));
        }
        return Result.fail("支付失败");
    }

    @Operation(summary = "Web端支付同步回调 - 重定向到前端页面")
    @GetMapping("/return")
    public String returnUrl(@RequestParam String out_trade_no) {
        log.info("收到Web端支付同步回调 - out_trade_no: {}", out_trade_no);
        try {
            String queryResult = payService.queryPayResult(out_trade_no);
            log.info("查询支付结果: {}", queryResult);

            com.alibaba.fastjson.JSONObject jsonObject = com.alibaba.fastjson.JSONObject.parseObject(queryResult);
            com.alibaba.fastjson.JSONObject tradeResponse = jsonObject.getJSONObject("alipay_trade_query_response");
            String tradeStatus = tradeResponse.getString("trade_status");

            log.info("交易状态: {}", tradeStatus);

            if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {
                return "redirect:http://localhost:9003/#/paysuccess?tradeNo=" + out_trade_no;
            }
        } catch (Exception e) {
            log.error("处理支付回调异常", e);
        }
        return "redirect:http://localhost:9003/#/payfail";
    }

}