package com.dllyal.cmpp.entity;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * 
 * 
 * 
 */
public class CmppDeliverResp extends CmppMessageHeader {

    Logger logger = LoggerFactory.getLogger(CmppDeliverResp.class);

    private byte[] msg_Id;//信息标识（CMPP_DELIVER中的Msg_Id字段）
    private int result;//结果 0：正确 1：消息结构错 2：命令字错 3：消息序号重复 4：消息长度错 5：资费代码错 6：超过最大信息长 7：业务代码错8: 流量控制错9~ ：其他错误
    
    private byte[] seq_Id;
    
    public byte[] getSeq_Id() {
        return seq_Id;
    }
    public void setSeq_Id(byte[] seq_Id) {
        this.seq_Id = seq_Id;
    }
    @Override
    public byte[] toByteArray(){
        ByteArrayOutputStream bous=new ByteArrayOutputStream();
        DataOutputStream dous=new DataOutputStream(bous);
        try {
            dous.writeInt(this.getTotal_Length());
            dous.writeInt(this.getCommand_Id());
            dous.write(this.getSeq_Id());
            dous.write(this.msg_Id);
            dous.writeInt(this.result);
            dous.close();
        } catch (IOException e) {
            logger.error("封装链接二进制数组失败。",e);
            //System.out.println("封装链接二进制数组失败。");
        }
        return bous.toByteArray();
    }
    public byte[] getMsg_Id() {
        return msg_Id;
    }

    public void setMsg_Id(byte[] msg_Id) {
        this.msg_Id = msg_Id;
    }

    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }
}

