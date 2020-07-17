package com.dllyal.cmpp.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CmppConfig {

    // 网关IP
    public static String ServerIp;
    // 网关端口
    public static int ServerPort;
    // 服务代码
    public static String ServiceCode;
    // 企业代码
    public static String CompanyCode;
    // 业务代码
    public static String BusinessCode;
    // 密码
    public static String Pwd;
    //尝试连接次数
    public static int ReCount;

    @Value("${CMPP.ServerIp}")
    public void setServerIp(String serverIp) {
        CmppConfig.ServerIp = serverIp;
    }

    @Value("${CMPP.ServerPort}")
    public void setServerPort(int serverPort) {
        CmppConfig.ServerPort = serverPort;
    }

    @Value("${CMPP.ServiceCode}")
    public void setServiceCode(String serviceCode) {
        CmppConfig.ServiceCode = serviceCode;
    }

    @Value("${CMPP.CompanyCode}")
    public void setCompanyCode(String companyCode) {
        CmppConfig.CompanyCode = companyCode;
    }

    @Value("${CMPP.BusinessCode}")
    public void setBusinessCode(String businessCode) {
        CmppConfig.BusinessCode = businessCode;
    }

    @Value("${CMPP.Pwd}")
    public void setPwd(String pwd) {
        CmppConfig.Pwd = pwd;
    }

    @Value("${CMPP.ReCount}")
    public void setReCount(int reCount) {
        CmppConfig.ReCount = reCount;
    }
}
