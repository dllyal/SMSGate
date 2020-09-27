package com.dllyal.cmpp.entity;

import com.dllyal.cmpp.utils.CmppUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * @author
 */
public class CmppSubmit extends CmppMessageHeader {

    Logger logger = LoggerFactory.getLogger(CmppSubmit.class);

    private long msgId = 0; // 信息标示
    private byte pkTotal = 0x01; // 相同的msgId总数，从1开始
    private byte pkNumber = 0x01; // 想用的msgId序号，从1开始
    private byte registeredDelivery = 0x00; // 是否要求返回状态报告，0不需要，1需要
    private byte msgLevel = 0x01; // 信息级别
    private String serviceId = ""; // 业务标示，企业代码
    private byte feeUserType = 0x00;// 用户计费类型， 谁接收，计谁的费
    private String feeTerminalId = ""; // 被计费的号码
    private byte feeTerminalType = 0x00; // 被计费号码的类型，真实号码或者虚拟号码
    private byte tpPId = 0x00;
    private byte tpUdhi = 0x00;
    private byte msgFmt = 0x0f; // 信息格式 15含GB汉字
    private String msgSrc; // 企业代码

    // 01：对“计费用户号码”免费；
    // 02：对“计费用户号码”按条计信息费；
    // 03：对“计费用户号码”按包月收取信息费
    private String feeType = "01";// 资费类型， 默认为按条计费
    private String feeCode = "5";
    private String valIdTime = "";// 暂不支持
    private String atTime = "";// 暂不支持
    // SP的服务代码或前缀为服务代码的长号码,
    // 网关将该号码完整的填到SMPP协议Submit_SM消息相应的source_addr字段，该号码最终在用户手机上显示为短消息的主叫号码。
    private String srcId;
    //private byte destUsrTl = 0x01;// 群发
    private int destUsrTl = 1;
    private String destTerminalId;// 接收手机号码，
    private byte destTerminalType = 0x00;// 真实号码
    private byte msgLength;
    private byte[] msgContent; // 信息内容
    // 点播业务使用的LinkID，非点播类业务的MT流程不使用该字段
    private String linkID = "";

    public CmppSubmit(int commandId, byte version) {
        //super(commandId, version);
    }

    @Override
    public byte[] toByteArray(){

        ByteArrayOutputStream bous = new ByteArrayOutputStream();
        DataOutputStream dous = new DataOutputStream(bous);
        try {
            dous.writeInt(this.getTotal_Length());
            dous.writeInt(this.getCommand_Id());
            dous.writeInt(this.getSequence_Id());
            dous.writeLong(this.msgId);// Msg_Id 信息标识，由SP接入的短信网关本身产生，本处填空
            dous.writeByte(this.pkTotal);// Pk_total 相同Msg_Id的信息总条数
            dous.writeByte(this.pkNumber);// Pk_number 相同Msg_Id的信息序号，从1开始
            dous.writeByte(this.registeredDelivery);// Registered_Delivery
            // 是否要求返回状态确认报告
            dous.writeByte(this.msgLevel);// Msg_level 信息级别
            CmppDefine.writeString(dous, this.serviceId, 10);// Service_Id
            // 业务标识，是数字、字母和符号的组合。
            dous.writeByte(this.feeUserType);// Fee_UserType 计费用户类型字段
            // 0：对目的终端MSISDN计费；1：对源终端MSISDN计费；2：对SP计费;3：表示本字段无效，对谁计费参见Fee_terminal_Id字段。
            CmppDefine.writeString(dous, this.feeTerminalId, 32);// Fee_terminal_Id
            // 被计费用户的号码
            dous.writeByte(this.feeTerminalType);// Fee_terminal_type
            // 被计费用户的号码类型，0：真实号码；1：伪码
            dous.writeByte(this.tpPId);// TP_pId
            dous.writeByte(this.tpUdhi);// TP_udhi
            dous.writeByte(this.msgFmt);// Msg_Fmt
            CmppDefine.writeString(dous, this.msgSrc, 6);// Msg_src
            // 信息内容来源(SP_Id)
            CmppDefine.writeString(dous, this.feeType, 2);// FeeType 资费类别
            CmppDefine.writeString(dous, this.feeCode, 6);// FeeCode
            CmppDefine.writeString(dous, this.valIdTime, 17);// 存活有效期
            CmppDefine.writeString(dous, this.atTime, 17);// 定时发送时间
            CmppDefine.writeString(dous, this.srcId, 21);// Src_Id spCode
            //dous.writeByte(CmppUtils.intToByte(this.destUsrTl));// DestUsr_tl
            dous.writeByte((byte) this.destUsrTl);// DestUsr_tl
            /*List<String> allReceiveNumList = Arrays.asList(StringUtils.split(this.destTerminalId, ","));
            for (String theDestTerminalId : allReceiveNumList){
                CmppDefine.writeString(dous, theDestTerminalId, 32);// Dest_terminal_Id
            }*/
            CmppDefine.writeString(dous, this.destTerminalId, 32 * this.destUsrTl);// Dest_terminal_Id
            dous.writeByte(this.destTerminalType);// Dest_terminal_type
            // 接收短信的用户的号码类型，0：真实号码；1：伪码
            dous.writeByte(this.msgLength);// Msg_Length
            // dous.write(this.msgContent);//信息内容
            CmppDefine.writeBytes(dous, this.msgContent, CmppUtils.byteToInt(this.msgLength));
            CmppDefine.writeString(dous, this.linkID, 20);// 点播业务使用的LinkID
            dous.close();
        } catch (IOException e) {
            logger.error("封装短信发送二进制数组失败。",e);
            //System.out.println("封装短信发送二进制数组失败。");
        }
        return bous.toByteArray();
    }

    public long getMsgId() {
        return msgId;
    }

    public void setMsgId(long msgId) {
        this.msgId = msgId;
    }

    public byte getPkTotal() {
        return pkTotal;
    }

    public void setPkTotal(byte pkTotal) {
        this.pkTotal = pkTotal;
    }

    public byte getPkNumber() {
        return pkNumber;
    }

    public void setPkNumber(byte pkNumber) {
        this.pkNumber = pkNumber;
    }

    public byte getRegisteredDelivery() {
        return registeredDelivery;
    }

    public void setRegisteredDelivery(byte registeredDelivery) {
        this.registeredDelivery = registeredDelivery;
    }

    public byte getMsgLevel() {
        return msgLevel;
    }

    public void setMsgLevel(byte msgLevel) {
        this.msgLevel = msgLevel;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public byte getFeeUserType() {
        return feeUserType;
    }

    public void setFeeUserType(byte feeUserType) {
        this.feeUserType = feeUserType;
    }

    public String getFeeTerminalId() {
        return feeTerminalId;
    }

    public void setFeeTerminalId(String feeTerminalId) {
        this.feeTerminalId = feeTerminalId;
    }

    public byte getFeeTerminalType() {
        return feeTerminalType;
    }

    public void setFeeTerminalType(byte feeTerminalType) {
        this.feeTerminalType = feeTerminalType;
    }

    public byte getTpPId() {
        return tpPId;
    }

    public void setTpPId(byte tpPId) {
        this.tpPId = tpPId;
    }

    public byte getTpUdhi() {
        return tpUdhi;
    }

    public void setTpUdhi(byte tpUdhi) {
        this.tpUdhi = tpUdhi;
    }

    public byte getMsgFmt() {
        return msgFmt;
    }

    public void setMsgFmt(byte msgFmt) {
        this.msgFmt = msgFmt;
    }

    public String getMsgSrc() {
        return msgSrc;
    }

    public void setMsgSrc(String msgSrc) {
        this.msgSrc = msgSrc;
    }

    public String getFeeType() {
        return feeType;
    }

    public void setFeeType(String feeType) {
        this.feeType = feeType;
    }

    public String getFeeCode() {
        return feeCode;
    }

    public void setFeeCode(String feeCode) {
        this.feeCode = feeCode;
    }

    public String getValIdTime() {
        return valIdTime;
    }

    public void setValIdTime(String valIdTime) {
        this.valIdTime = valIdTime;
    }

    public String getAtTime() {
        return atTime;
    }

    public void setAtTime(String atTime) {
        this.atTime = atTime;
    }

    public String getSrcId() {
        return srcId;
    }

    public void setSrcId(String srcId) {
        this.srcId = srcId;
    }

    public int getDestUsrTl() {
        return destUsrTl;
    }

    public void setDestUsrTl(int destUsrTl) {
        this.destUsrTl = destUsrTl;
    }

    public String getDestTerminalId() {
        return destTerminalId;
    }

    /*public void setDestTerminalId(String destTerminalId) {
        this.destTerminalId = destTerminalId;
    }*/

    public void setDestTerminalId(String destterminalId) {
        this.destTerminalId = destterminalId;
        //this.destUsrTl = destterminalId.size();
    }

    /*public void setDestTerminalId(String destterminalId) {
        this.destTerminalId = new ArrayList<>();
        destTerminalId.add(destterminalId);
        this.destUsrTl = 1;
    }*/

    public byte getDestTerminalType() {
        return destTerminalType;
    }

    public void setDestTerminalType(byte destTerminalType) {
        this.destTerminalType = destTerminalType;
    }

    public byte getMsgLength() {
        return msgLength;
    }

    public void setMsgLength(byte msgLength) {
        this.msgLength = msgLength;
    }

    public byte[] getMsgContent() {
        return msgContent;
    }

    public void setMsgContent(byte[] msgContent) {
        this.msgContent = msgContent;
    }

    public String getLinkID() {
        return linkID;
    }

    public void setLinkID(String linkID) {
        this.linkID = linkID;
    }

}
