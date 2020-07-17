package com.dllyal.cmpp.entity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 移动短信网关CMPP的相关常量定义
 */
public class CmppDefine {

    static Logger logger = LoggerFactory.getLogger(CmppDefine.class);

    public final static int CMPP_VERSION = 0x30; // CMPP = 3.0

    // Command ID 定义说明
    public final static int CMPP_CONNECT = 0x00000001; // 请求连接
    public final static int CMPP_CONNECT_RESP = 0x80000001; // 请求连接应答
    public final static int CMPP_TERMINATE = 0x00000002; // 终止连接
    public final static int CMPP_TERMINATE_RESP = 0x80000002; // 终止连接应答
    public final static int CMPP_SUBMIT = 0x00000004; // 提交短信
    public final static int CMPP_SUBMIT_RESP = 0x80000004; // 提交短信应答
    public final static int CMPP_DELIVER = 0x00000005; // 短信下发
    public final static int CMPP_DELIVER_RESP = 0x80000005; // 下发短信应答
    public final static int CMPP_QUERY = 0x00000006; // 发送短信状态查询
    public final static int CMPP_QUERY_RESP = 0x80000006; // 发送短信状态查询应答
    public final static int CMPP_CANCEL = 0x00000007; // 删除短信
    public final static int CMPP_CANCEL_RESP = 0x80000007; // 删除短信应答
    public final static int CMPP_ACTIVE_TEST = 0x00000008; // 激活测试
    public final static int CMPP_ACTIVE_TEST_RESP = 0x80000008; // 激活测试应答
    public final static int CMPP_FWD = 0x00000009; // 消息前转
    public final static int CMPP_FWD_RESP = 0x80000009; // 消息前转应答
    public final static int CMPP_MT_ROUTE = 0x00000010; // MT路由请求
    public final static int CMPP_MT_ROUTE_RESP = 0x80000010; // MT路由请求应答
    public final static int CMPP_MO_ROUTE = 0x00000011; // MO路由请求
    public final static int CMPP_MO_ROUTE_RESP = 0x80000011; // MO路由请求应答
    public final static int CMPP_GET_MT_ROUTE = 0x00000012; // 获取MT路由请求
    public final static int CMPP_GET_MT_ROUTE_RESP = 0x80000012; // 获取MT路由请求应答
    public final static int CMPP_MT_ROUTE_UPDATE = 0x00000013; // MT路由更新
    public final static int CMPP_MT_ROUTE_UPDATE_RESP = 0x80000013; // MT路由更新应答
    public final static int CMPP_MO_ROUTE_UPDATE = 0x00000014; // MO路由更新
    public final static int CMPP_MO_ROUTE_UPDATE_RESP = 0x80000014; // MO路由更新应答
    public final static int CMPP_PUSH_MT_ROUTE_UPDATE = 0x00000015; // MT路由更新
    public final static int CMPP_PUSH_MT_ROUTE_UPDATE_RESP = 0x80000015; // MT路由更新应答
    public final static int CMPP_PUSH_MO_ROUTE_UPDATE = 0x00000016; // MO路由更新
    public final static int CMPP_PUSH_MO_ROUTE_UPDATE_RESP = 0x80000016; // MO路由更新应答
    public final static int CMPP_GET_MO_ROUTE = 0x00000017; // 获取MO路由请求
    public final static int CMPP_GET_MO_ROUTE_RESP = 0x80000017; // 获取MO路由请求应答

    private static int sequenceId = 0;// 序列编号

    /**
     * 序列 自增
     */
    public static int getSequence() {
        ++sequenceId;
        if (sequenceId > 255) {
            sequenceId = 0;
        }
        return sequenceId;
    }

    /**
     * 时间戳的明文,由客户端产生,格式为MMDDHHMMSS，即月日时分秒，10位数字的整型，右对齐 。
     */
    public static String getTimestamp() {
        DateFormat format = new SimpleDateFormat("MMddhhmmss");
        return format.format(new Date());
    }

    /**
     * 用于鉴别源地址。其值通过单向MD5 hash计算得出，表示如下： AuthenticatorSource = MD5（Source_Addr+9
     * 字节的0 +shared secret+timestamp） Shared secret
     * 由中国移动与源地址实体事先商定，timestamp格式为：MMDDHHMMSS，即月日时分秒，10位。
     * 
     * @return
     */
    public static byte[] getAuthenticatorSource(String spId, String secret) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            byte[] data = (spId + "\0\0\0\0\0\0\0\0\0" + secret + CmppDefine
                    .getTimestamp()).getBytes();
            return md5.digest(data);
        } catch (NoSuchAlgorithmException e) {
            logger.error("SP链接到ISMG拼接AuthenticatorSource失败：" + e.getMessage());
            //System.out.println("SP链接到ISMG拼接AuthenticatorSource失败：" + e.getMessage());
            return null;
        }
    }

    /**
     * 向流中写入指定字节长度的字符串，不足时补0
     * 
     * @param dous
     *            :要写入的流对象
     * @param s
     *            :要写入的字符串
     * @param len
     *            :写入长度,不足补0
     */
    public static void writeString(DataOutputStream dous, String s, int len) {

        try {
            byte[] data = s.getBytes("gb2312");
            if (data.length > len) {
                logger.info("向流中写入的字符串超长！要写" + len + " 字符串是:" + s);
                //System.out.println("向流中写入的字符串超长！要写" + len + " 字符串是:" + s);
            }
            int srcLen = data.length;
            dous.write(data);
            while (srcLen < len) {
                dous.write('\0');
                srcLen++;
            }
        } catch (IOException e) {
            logger.info("向流中写入指定字节长度的字符串失败：" + e.getMessage());
            //System.out.println("向流中写入指定字节长度的字符串失败：" + e.getMessage());
        }
    }

    public static void writeBytes(DataOutputStream dous, byte[] data, int len) {

        try {
            if (data.length > len) {
                logger.info("向流中写入的字符串超长！要写" + len + " 字符串是:" + data.length);
                //System.out.println("向流中写入的字符串超长！要写" + len + " 字符串是:" + data);
            }
            int srcLen = data.length;
            dous.write(data);
            while (srcLen < len) {
                dous.write('\0');
                srcLen++;
            }
        } catch (IOException e) {
            logger.info("向流中写入指定字节长度的字符串失败：" + e.getMessage());
            //System.out.println("向流中写入指定字节长度的字符串失败：" + e.getMessage());
        }
    }

    /**
     * 截取字节
     * 
     * @param msg
     * @param start
     * @param end
     * @return
     */
    public static byte[] getMsgBytes(byte[] msg, int start, int end) {
        byte[] msgByte = new byte[end - start];
        int j = 0;
        for (int i = start; i < end; i++) {
            msgByte[j] = msg[i];
            j++;
        }
        return msgByte;
    }
}
