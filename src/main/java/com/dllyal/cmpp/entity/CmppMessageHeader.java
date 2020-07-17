package com.dllyal.cmpp.entity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

/**
 * @author
 */
public class CmppMessageHeader implements Serializable{

    Logger logger = LoggerFactory.getLogger(CmppMessageHeader.class);

    private int Total_Length; // 消息总长度(消息头和消息体)
    private int Command_Id; // 命令或者响应类型
    private int Sequence_Id; // 流水号，顺序累加，一对请求回应，流水号相同

    public int getTotal_Length() {
        return Total_Length;
    }

    public void setTotal_Length(int total_Length) {
        Total_Length = total_Length;
    }

    public int getCommand_Id() {
        return Command_Id;
    }

    public void setCommand_Id(int command_Id) {
        Command_Id = command_Id;
    }

    public int getSequence_Id() {

        return Sequence_Id;
    }

    public void setSequence_Id(int sequence_Id) {
        Sequence_Id = sequence_Id;
    }

    public byte[] toByteArray() {
        ByteArrayOutputStream bous = new ByteArrayOutputStream();
        DataOutputStream dous = new DataOutputStream(bous);
        try {
            dous.writeInt(this.getTotal_Length());
            dous.writeInt(this.getCommand_Id());
            dous.writeInt(this.getSequence_Id());
            dous.close();
        } catch (IOException e) {
            logger.error("封装CMPP消息头二进制数组失败。",e);
            //System.out.println("封装CMPP消息头二进制数组失败。");
        }
        return bous.toByteArray();
    }

    public CmppMessageHeader(byte[] data) {
        ByteArrayInputStream bins = new ByteArrayInputStream(data);
        DataInputStream dins = new DataInputStream(bins);
        try {
            this.setTotal_Length(data.length + 4);
            this.setCommand_Id(dins.readInt());
            this.setSequence_Id(dins.readInt());
            dins.close();
            bins.close();
        } catch (IOException e) {

        }
    }

    public CmppMessageHeader() {
        super();
    }
}

