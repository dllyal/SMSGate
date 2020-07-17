package com.dllyal.cmpp.entity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

/**
 * @author
 */
public class CmppTerminateResp extends CmppMessageHeader {

    Logger logger = LoggerFactory.getLogger(CmppTerminateResp.class);

    public CmppTerminateResp(byte[] data){
        if(data.length==8){
            ByteArrayInputStream bins=new ByteArrayInputStream(data);
            DataInputStream dins=new DataInputStream(bins);
            try {
                this.setTotal_Length(data.length+4);
                this.setCommand_Id(dins.readInt());
                this.setSequence_Id(dins.readInt());
                //this.reserved=dins.readByte();
                dins.close();
                bins.close();
            } catch (IOException e){}
        }else{
            logger.info("链路拆除,解析数据包出错，包长度不一致。长度为:"+data.length);
            //System.out.println("链路检查,解析数据包出错，包长度不一致。长度为:"+data.length);
        }
    }

    public CmppTerminateResp() {

    }

    @Override
    public byte[] toByteArray(){
        ByteArrayOutputStream bous=new ByteArrayOutputStream();
        DataOutputStream dous=new DataOutputStream(bous);
        try {
            dous.writeInt(this.getTotal_Length());
            dous.writeInt(this.getCommand_Id());
            dous.write(this.getSeq_Id());
            //dous.write(0x00);//Reserved
            dous.close();
        } catch (IOException e) {
            logger.error("封装链接二进制数组失败。",e);
            //System.out.println("封装链接二进制数组失败。");
        }
        return bous.toByteArray();
    }

    private byte[] seq_Id;

    public byte[] getSeq_Id() {
        return seq_Id;
    }
    public void setSeq_Id(byte[] seq_Id) {
        this.seq_Id = seq_Id;
    }
}
