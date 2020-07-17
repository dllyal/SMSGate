package com.dllyal.cmpp.entity;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.dllyal.cmpp.utils.CmppUtils;
import org.apache.commons.lang3.ArrayUtils;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;


/**
 * @author
 * @date
 */
public class CmppSubmitResp extends CmppMessageHeader{

    Logger logger = LoggerFactory.getLogger(CmppSubmitResp.class);

    private int seqId;
    private long msgId;
    private int result;// 结果 0：正确 1：消息结构错 2：命令字错 3：消息序号重复 4：消息长度错 5：资费代码错
    // 6：超过最大信息长 7：业务代码错 8：流量控制错 9：本网关不负责服务此计费号码 10：Src_Id错误
    // 11：Msg_src错误 12：Fee_terminal_Id错误
    // 13：Dest_terminal_Id错误  145：超速（东软网关自定义）
    private String resultstr;// 结果 0：正确 1：消息结构错 2：命令字错 3：消息序号重复 4：消息长度错 5：资费代码错
    // 6：超过最大信息长 7：业务代码错 8：流量控制错 9：本网关不负责服务此计费号码
    // 10：Src_Id错误
    // 11：Msg_src错误 12：Fee_terminal_Id错误
    // 13：Dest_terminal_Id错误 145：超速（东软网关自定义）

    public CmppSubmitResp(byte[] data) {
        if (data.length == 8 + 8 + 4) {
            ByteArrayInputStream bins = new ByteArrayInputStream(data);
            DataInputStream dins = new DataInputStream(bins);
            try {
                this.setTotal_Length(data.length + 4);
                this.setCommand_Id(dins.readInt());
                this.setSequence_Id(dins.readInt());
                this.msgId = dins.readLong();
                this.result = dins.readInt();
                this.seqId= CmppUtils.bytesToInt(ArrayUtils.subarray(data, 4, 8));
                this.msgId = CmppUtils.bytesToLong(ArrayUtils.subarray(data, 8, 16));
                this.msgId = Math.abs(this.msgId);
                dins.close();
                bins.close();
                this.setResult(this.result);
                /*logger.info("收到CMPPSubmitResp消息");
                logger.info("MSGID：" + this.getMsgIdStr());
                logger.info("序号：" + this.getSequence_Id());
                logger.info("响应状态：" + this.getResult());*/
                //System.out.println("收到CMPPSubmitResp消息 MSGID：" + this.getMsgIdStr() + " 序号：" + this.getSequence_Id() + "响应状态：" + resultstr);
            } catch (IOException e) {
                logger.error("发送短信IMSP回复解析异常",e);
            }
        } else {
            logger.info("发送短信IMSP回复,解析数据包出错，包长度不一致。长度为:" + data.length);
            //System.out.println("发送短信IMSP回复,解析数据包出错，包长度不一致。长度为:" + data.length);
        }
    }

    public long getMsgId() {
        return this.msgId;
    }

    public String getMsgIdStr() {
        return String.valueOf(this.msgId);
    }

    public void setMsgId(long msgId) {
        this.msgId = msgId;
    }

    public int getResult() {
        return this.result;
    }

    public void setResult(int result) {
        this.result = result;
        switch (result) {
            case 0:
                resultstr = "正确";
                break;
            case 1:
                resultstr = "消息结构错";
                break;
            case 2:
                resultstr = "命令字错";
                break;
            case 3:
                resultstr = "消息序号重复";
                break;
            case 4:
                resultstr = "消息长度错";
                break;
            case 5:
                resultstr = "资费代码错";
                break;
            case 6:
                resultstr = "超过最大信息长";
                break;
            case 7:
                resultstr = "业务代码错";
                break;
            case 8:
                resultstr = "流量控制错";
                break;
            case 9:
                resultstr = "本网关不负责服务此计费号码";
                break;
            case 10:
                resultstr = "Src_Id错误";
                break;
            case 11:
                resultstr = "Msg_src错误";
                break;
            case 12:
                resultstr = "Fee_terminal_Id错误";
                break;
            case 13:
                resultstr = "Dest_terminal_Id错误";
                break;
            default:
                resultstr = result + "请咨询运营商";
                break;
        }
    }

    //返回字符串类型的MSGID

    public int getSeqId() {
        return seqId;
    }

    public String getResultstr() {
        return resultstr;
    }

    public void setResultstr(String resultstr) {
        this.resultstr = resultstr;
    }

    public void setSeqId(int seqId) {
        this.seqId = seqId;
    }

    /*public static void main(String[] args) {
        String str = "80000004192657f558010480b81cc84100000000";
        byte[] b = CmppUtils.hexToBytes(str);
        CmppSubmitResp cmppSubmitResp = new CmppSubmitResp(b);
        System.out.println(cmppSubmitResp.getMsgIdStr());
        System.out.println(cmppSubmitResp.getSequence_Id());
        System.out.println(cmppSubmitResp.getResult());
    }*/
}
