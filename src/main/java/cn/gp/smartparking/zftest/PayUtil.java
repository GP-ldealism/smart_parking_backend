package cn.gp.smartparking.zftest;

import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import org.springframework.stereotype.Component;

/**
 * @ClassName PayUtil
 * @Description TODO
 * @Author Guoping He
 * @Date 2026/4/21 19:02
 */
@Component
public class PayUtil {
//    @Autowired
//    private OrderService orderService;
    //appid
    private final String APP_ID = "9021000162693334";
    //应用私钥
    private final String APP_PRIVATE_KEY = "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQCAOVSeDnZ5nLe2gzvKo65384jPL10D0qiCdltXjVvc+9r+76expoaCw8K9OWSh+jF2IrKx4uxK8BCAhgSLnZHSd3wz7ZpaJjOYjbX3hCdfGepLu8cFa8hwNoGWDtU7Hn6JdQ16W0jrnVycmZxAqNYNcteQbhFiNO1HPoIEr+uZtyaFFh1eaN+MoVLBm8yReEB113so/Knc9uizwk/oY7tJ3YYCUnWCosE1a5XNqGVtaiJGaDLBrTkb484eHzg9ZLZFfX6bv8nowe6kRz+mqxWv8HZBkMyVgytDUz77GeiUI8kQ2mB9JP5BIPLZIRpvNG0+AU7hX9J5fHugPomlMB3fAgMBAAECggEAHnwC1xT/K8iLkNPAelvR1ejLZN6Q0aDauTGkY7tS1MX3+dZGWyEJJ7uc59nmR/AwpDJ5o3sczHAorDWwjPToP05y8/GQWdBtQd6nHXKI3b5OirTZCzvlym/a/S8HmOeWE94FV0qumn2uD/khQeoiAc6E22yfq2aD5wVAahbc/41Jmy8oRdLD9zw8S9BIpk6OMKEfvmqwdXJeccJti6sTc3vGBL1fR7Q/pd9ax6PRrlK8JLK/lZkYka08+8i1366b36ilb/HHGq28+lRoDS4ockAoixcWHK4Gwkv1CqoSftQ/HJySXM9Zn9I0DgBGvr7BZ8Z1miMp6C5AFedL5REPkQKBgQDQKHzzrJyhIlQWFLiP0/hP1/4R/+mNnpuwGZn26lcT7kbQ/iQJFpYVEKzEerN/J9d9zN8hXAdROKwkBjtb/XqedKD8IFOJ29NJx+KV6y6GYHDLXBhqMpzW4MGPWD7VH604iJ2oYPVkLjTmxTsV+BUWoJFJVDYTvk+Wfdin/jvP1wKBgQCdsbYBxPA++U60/k54UrdRdBx5FsOjoHKnByRElZvCaMMX6wlfeUiasU5oUXwIfmHBOHks3N8QYB+duJES6B6ciQDV31kkGFqUfsieVihcXrg5U4a5gOH36QGWSlxH71EFu0qNy7+B5vUeZOTv7VitcLM/hia0aEQCEMdIjIkBOQKBgA6q67MX9Kl+C8LKjv7DhvnJVIPcLo7mID5Wb3zwolYrMh5D6jIhv02YsTPfGYraOdsJdqeZHoLgpXCcZ85hnCRSP43EkV5jeQ334cWCnvKlFQ2fXbEOA/M4PytkonjOFJypM8LLoj7+6odgWnt/0ayYf+RU5MKbZGxMTdn4lipdAoGBAIiMcqsTimktXj8RmodMMNsCbQlZce0FKWKCp6AP4IE2UAEtD7+xePIiqF9id8GvFhzHNv5m/2zhn17mjBDQGHm5lJSdw0WjJQRCyJt6xZY+ms1hC18kQAd4p1wpZcEQdJs+867XCCyG/ifke+1aG5iL51sRYgCD7KfROY8TyirZAoGBAKTsP+pCsv8vg5yTgPKqWaD63XXTutkfcUJ6nV2t+uxdPxVWBN9CCg9hLY4Pwlm1Kn8nyD0bALY3Y1PfjUJXKaJ5lwn8UdQjKxHaSSJtDPaHdA3qLi6R5Kr5EtrrbPvkfMm5BcNsTs4FoQu1eBi87jfpNc0EEbRBOROM+q4FFCwL";
    private final String CHARSET = "UTF-8";
    // 支付宝公钥
    private final String ALIPAY_PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAo80p9QwZcaSLNY6LirnGJYFywPVQUSA1qmLZBY8tdbRIOW/K0DtCbMt27PNKVla+J32PwaZq7Ab4+S2lM0ZZiMimAez/JnoXBzdSxdrne9cItJ/Nm6y7R/rmOplUkIHQ/5BA+JDSDrp6PAGKC99fsYjtPQtHV2SLOu/j0MY0rIw/BU7wLbqGlRlcmPUNF7FgvJECps1iViuTPhvCo7Tk6W2UjXNrqzKumuXqQbsA3egp8XApWoRQnxbHQvHr+2LRFkXC+c88qwM4IiNLXafCnswkW6kh8kSzB4ydt7tjorMerCSdd+mj3WnQ7SFGoWX93HYoook1RhObOJHpS+AQdQIDAQAB";
    //这是沙箱接口路径,正式路径为https://openapi.alipay.com/gateway.do
    private final String GATEWAY_URL = "https://openapi-sandbox.dl.alipaydev.com/gateway.do";
    private final String FORMAT = "JSON";
    //签名方式
    private final String SIGN_TYPE = "RSA2";
    //支付宝异步通知路径,付款完毕后会异步调用本项目的方法,必须为公网地址 todo
    private final String NOTIFY_URL = "http://t247faad.natappfree.cc/smart-parking/api/alipay/toSuccess";
    //支付宝同步通知路径,也就是当付款完毕后跳转本项目的页面,可以不是公网地址
    private final String RETURN_URL = "http://localhost:9003/smart-parking/api/alipay/toSuccess";
    private AlipayClient alipayClient = null;
    //支付宝官方提供的接口
    public String sendRequestToAlipay(String outTradeNo, Float totalAmount, String subject) throws AlipayApiException {
        //获得初始化的AlipayClient
        alipayClient = new DefaultAlipayClient(GATEWAY_URL, APP_ID, APP_PRIVATE_KEY, FORMAT, CHARSET, ALIPAY_PUBLIC_KEY, SIGN_TYPE);

        //设置请求参数
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();
        alipayRequest.setReturnUrl(RETURN_URL);
        alipayRequest.setNotifyUrl(NOTIFY_URL);

        //商品描述（可空）
        String body = "";
        alipayRequest.setBizContent("{\"out_trade_no\":\"" + outTradeNo + "\","
                + "\"total_amount\":\"" + totalAmount + "\","
                + "\"subject\":\"" + subject + "\","
                + "\"body\":\"" + body + "\","
                + "\"product_code\":\"FAST_INSTANT_TRADE_PAY\"}");

        //请求
        String result = alipayClient.pageExecute(alipayRequest).getBody();
        System.out.println("返回的结果是："+result );
        return result;
    }

    //    通过订单编号查询
    public String query(String id){
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        JSONObject bizContent = new JSONObject();
        bizContent.put("out_trade_no", id);
        request.setBizContent(bizContent.toString());
        AlipayTradeQueryResponse response = null;
        String body=null;
        try {
            response = alipayClient.execute(request);
            body = response.getBody();
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        if(response.isSuccess()){
            System.out.println("调用成功");
        } else {
            System.out.println("调用失败");
        }
        return body;
    }
}
