package cn.gp.smartparking.zf;

import com.alipay.api.AlipayApiException;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeAppPayRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Slf4j
@Component
public class AlipayWrapper {

    private static final String APP_ID = "9021000162693334";
    private static final String APP_PRIVATE_KEY = "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQCTznc+0XfS3z3ehesOyC+c25q4BOhyaHG7lxf0bY1R16lHCY68EIUPZWQ5+UPnGVSMwFN5okqcQ7g84eNTP72nArHBbiXICuIK5i0aG3+VnBRM2s+eICH80B1s/nEwmrM6LgfnYRboT+78BFPG619jYIM+Bk5M02xo8LjMXWg1PYDtqSri8rqUbUIV2PZO4e2RLASYzymXUWHI+gK3QBOg90Mm9K3l55hjJgaMam/LHvJ3H85f9JN5+43IaMex4CAPQ6n+adZ9ZyBGXQCrpr55u4ZH7VzgOspBFB7ggAZHxdX2BMr3OdzZq9S7usEGWIMv+zvntpIxZRCk9R8KY/y3AgMBAAECggEASsEvukvC6JoT9omQuucwfYrsE4oX7lU4wIHIkc0enngOm2kQEbnhMko7iL6zQxyikJVtIqWcSrBNbC1rBd3FE8TfFk+ZODWY4Z5MzVl/C7Hm6n/4ZacmyXCQkWGW6dg0MYa6ZKHMLi7PVhLyNDczBVX4ytPxohWETQcC0Aml/NaYN2JMosYR6bus0gjD8Md/9PfP/If8HeVMNUZJONFUaA/lpipQi0kiaLDlqRq8fIz46rWeJ/+nQsitvR+TdEpQKz+GLJq6LDNIXnMibT/fzDMoSfKhiM/aRE48P5/itpOqBcjWR1r27GTg6XXGCAw4BfFGAFN1qbIeFKz8Y0lDcQKBgQDqkkC+Xz0YPCMDICRElhiEoMbtSpnwWnuw0pyMnx8v2XxirvNhRk6m0oiwhzqV/0PF1WOpF34UPTtk+oygwB5C5xHnv5TX6FvjTFexZ6O4wE/QSg1QYs7qcKf45RPoHyyUo9j/ShbmINqNUzHoLfoT5QwML1ZooMQF9F6U4s9/swKBgQChTxu3AZSCyd+kqfFC6PBUh7qkv9HFona9KTJcYdoSdtRwjl9RjdYJwYZJYn9/aDaD9H6tFDUxkT7B5iXMTbLQret2/NIhfB3wnBO67U540WMGqW+RsSxFe9zcBhxJPHXYsEQzejbbarxC3Ua5Il9kUwDD64YxR4lVJxpqlEgs7QKBgCgVa+jcH3WHuTfsgyYRPAlV7AhaiimvXJmfs8631j13/bBjUwWZXRFWwrLfuNwu7abdyxT3Bb1xpTehNOy3R7SJBd9DNq0acLFLt2SCYFDO1BS0wsesysz3CU0X4Cn3Qcf4o7kkMPuMO6yQveN+D9DcgA2I/UwNxNF7q7nndCvPAoGBAJATHzbyk1nJwxXIr/NKKLXnUN4rccpq1FEZxaBbybHqLs6lax9lZsHp9u3+qs/6HY74RliCjwxJpVr8G9CBqW/Xl1L8dxZvTDKGxNyVbhxdMGxX0422SEt7WQWv8326Iau7q/6qburFR4cVKkDbIYZy2GElMQM82L2UonSFIn31AoGBAI7uPidJZAXLjjlw041II5ptJymOJ6cbS8j4HolnY9h3DAbjbLHPox9tU0D+9dRb7V2Dk4ePS5bqMS45Q04gSbVkL23ZvuY02F1fJeC/k5FklflciTde4V+R9cWmeySyMiQ2VR8n9qrZ405qh2lZLS+FViFnmZFlRhWcEGnJnsrv";
    private static final String CHARSET = "UTF-8";
    private static final String ALIPAY_PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAo80p9QwZcaSLNY6LirnGJYFywPVQUSA1qmLZBY8tdbRIOW/K0DtCbMt27PNKVla+J32PwaZq7Ab4+S2lM0ZZiMimAez/JnoXBzdSxdrne9cItJ/Nm6y7R/rmOplUkIHQ/5BA+JDSDrp6PAGKC99fsYjtPQtHV2SLOu/j0MY0rIw/BU7wLbqGlRlcmPUNF7FgvJECps1iViuTPhvCo7Tk6W2UjXNrqzKumuXqQbsA3egp8XApWoRQnxbHQvHr+2LRFkXC+c88qwM4IiNLXafCnswkW6kh8kSzB4ydt7tjorMerCSdd+mj3WnQ7SFGoWX93HYoook1RhObOJHpS+AQdQIDAQAB";
    private static final String GATEWAY_URL = "https://openapi-sandbox.dl.alipaydev.com/gateway.do";
    private static final String FORMAT = "JSON";
    private static final String SIGN_TYPE = "RSA2";
    private static final String NOTIFY_URL = "http://vcb86389.natappfree.cc/smart-parking/api/alipay/notify";
    private static final String RETURN_URL_WEB = "http://localhost:9003/smart-parking/api/alipay/return";
    private static final String RETURN_URL_MOBILE = "http://localhost:9003/smart-parking/api/alipay/mobile/return";

    private com.alipay.api.AlipayClient alipayClient = null;

    public AlipayWrapper() {
        initAlipayClient();
    }

    private void initAlipayClient() {
        alipayClient = new DefaultAlipayClient(GATEWAY_URL, APP_ID, APP_PRIVATE_KEY, FORMAT, CHARSET, ALIPAY_PUBLIC_KEY, SIGN_TYPE);
    }

    public String buildWebPayRequest(String outTradeNo, BigDecimal totalAmount, String subject, String body) throws AlipayApiException {
        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        request.setReturnUrl(RETURN_URL_WEB);
        request.setNotifyUrl(NOTIFY_URL);

        String bizContent = String.format(
                "{\"out_trade_no\":\"%s\",\"total_amount\":\"%.2f\",\"subject\":\"%s\",\"body\":\"%s\",\"product_code\":\"FAST_INSTANT_TRADE_PAY\"}",
                outTradeNo, totalAmount, subject, body != null ? body : ""
        );
        request.setBizContent(bizContent);

        String result = alipayClient.pageExecute(request).getBody();
        System.out.println("Web支付请求构建成功 - outTradeNo: " + outTradeNo + ", amount: " + totalAmount);
        return result;
    }

    public String buildAppPayRequest(String outTradeNo, BigDecimal totalAmount, String subject, String body) throws AlipayApiException {
        AlipayTradeAppPayRequest request = new AlipayTradeAppPayRequest();
        request.setNotifyUrl(NOTIFY_URL);

        // 关键修复：添加 product_code 参数
        String bizContent = String.format(
                "{\"out_trade_no\":\"%s\",\"total_amount\":\"%.2f\",\"subject\":\"%s\",\"body\":\"%s\",\"product_code\":\"QUICK_MSECURITY_PAY\"}",
                outTradeNo, totalAmount, subject, body != null ? body : ""
        );
        request.setBizContent(bizContent);

        com.alipay.api.response.AlipayTradeAppPayResponse response = alipayClient.sdkExecute(request);
        if (response.isSuccess()) {
            log.debug("APP支付请求构建成功 - outTradeNo: " + outTradeNo + ", orderString: " + response.getBody());
            System.out.println("APP支付请求构建成功 - outTradeNo: " + outTradeNo + ", orderString: " + response.getBody());
            return response.getBody();
        } else {
            log.debug("APP支付请求构建失败 - outTradeNo: " + outTradeNo + ", error: " + response.getMsg() + ", subError: " + response.getSubMsg());
            System.err.println("APP支付请求构建失败 - outTradeNo: " + outTradeNo + ", error: " + response.getMsg() + ", subError: " + response.getSubMsg());
            return null;
        }
    }

    public String queryTradeStatus(String outTradeNo) throws AlipayApiException {
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        request.setBizContent(String.format("{\"out_trade_no\":\"%s\"}", outTradeNo));

        AlipayTradeQueryResponse response = alipayClient.execute(request);
        if (response.isSuccess()) {
            System.out.println("查询交易状态成功 - outTradeNo: " + outTradeNo + ", tradeStatus: " + response.getTradeStatus());
            return response.getBody();
        } else {
            System.err.println("查询交易状态失败 - outTradeNo: " + outTradeNo + ", error: " + response.getMsg());
            return null;
        }
    }
}
