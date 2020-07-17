package com.dllyal.cmpp.entity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

/**
 * @author
 */
public class CmppConnect extends CmppMessageHeader {

    Logger logger = LoggerFactory.getLogger(CmppConnect.class);

    // 请求认证消息体

    private String sourceAddr;// 源地址，此处为SP_Id，即SP的企业代码。
    private byte[] authenticatorSource;// 用于鉴别源地址。其值通过单向MD5
    // hash计算得出，表示如下：AuthenticatorSource =
    // MD5（Source_Addr+9 字节的0 +shared
    // secret+timestamp） Shared secret
    // 由中国移动与源地址实体事先商定，timestamp格式为：MMDDHHMMSS，即月日时分秒，10位。
    private byte version;// 双方协商的版本号(高位4bit表示主版本号,低位4bit表示次版本号)，对于3.0的版本，高4bit为3，低4位为0
    public CmppConnect() {

    }
    public CmppConnect(byte[] data) {
        if (data.length == 8 + 4 + 16 + 1) {
            ByteArrayInputStream bins = new ByteArrayInputStream(data);
            DataInputStream dins = new DataInputStream(bins);
            try {
                this.setTotal_Length(data.length + 4);
                this.setCommand_Id(dins.readInt());
                this.setSequence_Id(dins.readInt());
                byte[] aiByte = new byte[16];
                dins.read(aiByte);
                this.version = dins.readByte();
                dins.close();
                bins.close();

            } catch (IOException e) {
            }
        } else {
            logger.info("链接至IMSP,解析数据包出错，包长度不一致。长度为:" + data.length);
            //System.out.println("链接至IMSP,解析数据包出错，包长度不一致。长度为:" + data.length);
        }
    }

    public String getSourceAddr() {
        return sourceAddr;
    }

    public void setSourceAddr(String sourceAddr) {
        this.sourceAddr = sourceAddr;
    }

    public byte[] getAuthenticatorSource() {
        return authenticatorSource;
    }

    public void setAuthenticatorSource(byte[] authenticatorSource) {
        this.authenticatorSource = authenticatorSource;
    }

    public byte getVersion() {
        return version;
    }

    public void setVersion(byte version) {
        this.version = version;
    }

    public int getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(int timestamp) {
        this.timestamp = timestamp;
    }

    private int timestamp;// 时间戳的明文,由客户端产生,格式为MMDDHHMMSS，即月日时分秒，10位数字的整型，右对齐 。

    // end

    @Override
    public byte[] toByteArray() {
        ByteArrayOutputStream bous = new ByteArrayOutputStream();
        DataOutputStream dous = new DataOutputStream(bous);
        try {
            dous.writeInt(this.getTotal_Length());
            dous.writeInt(this.getCommand_Id());
            dous.writeInt(this.getSequence_Id());
            CmppDefine.writeString(dous, this.sourceAddr, 6);
            dous.write(authenticatorSource);
            dous.writeByte(0x30);
            dous.writeInt(Integer.parseInt(CmppDefine.getTimestamp()));
            dous.close();
        } catch (IOException e) {
            logger.error("封装链接二进制数组失败。",e);
            //System.out.println("封装链接二进制数组失败。");
        }
        return bous.toByteArray();
    }

}
