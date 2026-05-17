package cn.gp.smartparking.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "payment.alipay")
public class AlipayProperties {

    private String notifyUrl;
    private String returnUrlWeb;
    private String returnUrlMobile;
    private String webPayUrl;
    private String frontendSuccessUrl;
    private String frontendFailUrl;

    public String getNotifyUrl() {
        return notifyUrl;
    }

    public void setNotifyUrl(String notifyUrl) {
        this.notifyUrl = notifyUrl;
    }

    public String getReturnUrlWeb() {
        return returnUrlWeb;
    }

    public void setReturnUrlWeb(String returnUrlWeb) {
        this.returnUrlWeb = returnUrlWeb;
    }

    public String getReturnUrlMobile() {
        return returnUrlMobile;
    }

    public void setReturnUrlMobile(String returnUrlMobile) {
        this.returnUrlMobile = returnUrlMobile;
    }

    public String getWebPayUrl() {
        return webPayUrl;
    }

    public void setWebPayUrl(String webPayUrl) {
        this.webPayUrl = webPayUrl;
    }

    public String getFrontendSuccessUrl() {
        return frontendSuccessUrl;
    }

    public void setFrontendSuccessUrl(String frontendSuccessUrl) {
        this.frontendSuccessUrl = frontendSuccessUrl;
    }

    public String getFrontendFailUrl() {
        return frontendFailUrl;
    }

    public void setFrontendFailUrl(String frontendFailUrl) {
        this.frontendFailUrl = frontendFailUrl;
    }
}
