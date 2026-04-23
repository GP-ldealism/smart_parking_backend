package cn.gp.smartparking.zftest;

import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Controller
@RequestMapping("/api/alipaytest")
public class AliPayController {
    @Autowired
    private PayUtil payUtil;

    @Autowired
    private OrderService orderService;

    private OrderModel orderModel = null;
    private String tokens = "";


    @ResponseBody
    @GetMapping("/pay")
    public String alipay(@RequestParam Integer id) throws AlipayApiException {
//        tokens = token;
        //生成订单号（支付宝的要求？）
        String time = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String user = UUID.randomUUID().toString().replace("-", "").toUpperCase();
        log.debug("id:  " + id);
        OrderModel order = orderService.getOrder(id);
        String OrderNum = time + user;
//        float oderValue = order.getOrderPrice().floatValue();
//
//        // ✅ 关键修改：将金额格式化为两位小数字符串
//        BigDecimal price = order.getOrderPrice();
//        String totalAmount = String.format("%.2f", price);  // 确保是两位小数

        //调用封装好的方法（给支付宝接口发送请求）tId
        return payUtil.sendRequestToAlipay(OrderNum, Float.valueOf("100.28"), "test");
    }

    //    当我们支付完成之后跳转这个请求并携带参数，我们将里面的订单id接收到，通过订单id查询订单信息，信息包括支付是否成功等
    @GetMapping("/toSuccess")
    public String returns(String out_trade_no) throws ParseException {
        String query = payUtil.query(out_trade_no);
        System.out.println("==>" + query);
        JSONObject jsonObject = JSONObject.parseObject(query);
        Object o = jsonObject.get("alipay_trade_query_response");
        Map map = (Map) o;
        System.out.println(map);
        Object s = map.get("trade_status");
        if ("TRADE_SUCCESS".equals(s)) {
            //当支付成功之后要执行的操作
            System.out.println("订单支付成功");
            log.debug("zfb 订单支付成功");
            System.out.println(s);
            return "redirect:http://localhost:9003/paysuccess";
        } else {
//            支付失败要执行的操作
            System.out.println("订单支付失败");
            log.debug("zfb 订单支付失败");
            return "index";
        }
    }

    /*
参数1：订单号
参数2：订单金额
参数3：订单名称
 */

}