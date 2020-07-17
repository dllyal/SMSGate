package com.dllyal.cmpp;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.dllyal.cmpp.utils.CmppUtils;
import com.dllyal.cmpp.entity.*;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

import static com.dllyal.cmpp.NettyClient.recMsgMap;

/**
 * 解码
 * @author
 */
public class CmppDecoder extends ByteToMessageDecoder {

    Logger logger = LoggerFactory.getLogger(CmppDecoder.class);

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list){
        //字节数组
        byte[] buf = new byte[byteBuf.readableBytes()];
        //读取数据到字节数组
        byteBuf.readBytes(buf);

        System.out.println();
        logger.info("SMG->SP  服务端->客户端");
        //获取字节数组
        byte[] returnData = CmppUtils.getInData(buf);
        logger.info("===读取到字节流:" + CmppUtils.bytesToHexString(returnData));
        //System.out.println();
        if(returnData!=null && returnData.length>=8) {
            CmppMessageHeader head = new CmppMessageHeader(returnData);
            switch(head.getCommand_Id()) {
                case CmppDefine.CMPP_CONNECT_RESP:
                    //注册连接返回
                    logger.info("===CMPP_CONNECT_RESP===");
                    CmppConnectResp connectResp=new CmppConnectResp(returnData);
                    //读取流水号
                    String recmsgNo = CmppUtils.bytesToHexString(CmppUtils.getMsgBytes(returnData,4,8));
                    byte[] msgmsg = recMsgMap.get(recmsgNo);
                    if(msgmsg ==null) {
                        logger.info("===序号可能已被注销！响应消息丢弃" + recmsgNo);
                        recMsgMap.remove(recmsgNo);
                    }
                    logger.info("===CMPP_CONNECT_RESP---END===");
                    list.add(connectResp);
                    System.out.println();
                    break;
                case CmppDefine.CMPP_ACTIVE_TEST:
                    //接收到短信网关连接检查请求
                    logger.info("===CMPP_ACTIVE_TEST===");
                    byte[] seqs = new byte[4];
                    System.arraycopy(returnData, 4, seqs, 0, 4);
                    CmppActiveTestResp activeTestResp = new CmppActiveTestResp();
                    activeTestResp.setTotal_Length(12+1);
                    activeTestResp.setCommand_Id(CmppDefine.CMPP_ACTIVE_TEST_RESP);
                    activeTestResp.setSeq_Id(seqs);

                    String fss = CmppUtils.bytesToHexString(activeTestResp.toByteArray());
                    logger.info("===CMPP_ACTIVE_TEST_RESP 回复网关："+fss);
                    //进行回复
                    channelHandlerContext.writeAndFlush(activeTestResp);
                    logger.info("===CMPP_ACTIVE_TEST_RESP 回复网关完毕...");
                    logger.info("===CMPP_ACTIVE_TEST---END===");
                    System.out.println();
                    break;
                case CmppDefine.CMPP_ACTIVE_TEST_RESP:
                    //发送给短信网关进行连接检查的返回
                    logger.info("===CMPP_ACTIVE_TEST_RESP===");
                    CmppActiveTestResp activeResp=new CmppActiveTestResp(returnData);
                    //logger.info("===发送给短信网关进行连接检查的返回---序列号："+activeResp.getSequence_Id());
                    logger.info("===CMPP_ACTIVE_TEST_RESP---END===");
                    list.add(activeResp);
                    System.out.println();
                    break;
                case CmppDefine.CMPP_TERMINATE_RESP:
                    //发送给短信网关进行连接拆除的返回
                    logger.info("===CMPP_TERMINATE_RESP===");
                    CmppTerminateResp cmppTerminateResp=new CmppTerminateResp(returnData);
                    //logger.info("===发送给短信网关进行连接拆除的返回---序列号："+cmppTerminateResp.getSequence_Id());
                    logger.info("===CMPP_TERMINATE_RESP---END===");
                    list.add(cmppTerminateResp);
                    System.out.println();
                    break;
                case CmppDefine.CMPP_SUBMIT_RESP:
                    //向用户下发短信，返回状态
                    logger.info("===CMPP_SUBMIT_RESP===");
                    CmppSubmitResp submitResp=new CmppSubmitResp(returnData);
                    /*logger.info("===向用户下发短信，返回状态:" + submitResp.getResult());
                    logger.info("===序列号:" + submitResp.getSequence_Id());
                    logger.info("===Result:" + submitResp.getResult());
                    if(submitResp.getResult()==0){
                        //b = true;
                        logger.info("===msgId:"+submitResp.getMsgIdStr());
                    }*/
                    list.add(submitResp);
                    logger.info("===CMPP_SUBMIT_RESP---END===");
                    System.out.println();
                    break;
                case CmppDefine.CMPP_DELIVER:
                    //CMPP_DELIVER接收短信
                    logger.info("===CMPP_DELIVER===");
                    byte[] b = new byte[8];
                    System.arraycopy(returnData, 8, b, 0, 8);

                    byte[] seq = new byte[4];
                    System.arraycopy(returnData, 4, seq, 0, 4);

                    CmppDeliver msgDeliver=new CmppDeliver(returnData);
                    //解析正确才进行处理
                    if(msgDeliver.getResult()==0){
                        /*logger.info("===CMPP_DELIVER接收短信");
                        logger.info("===序列号："+head.getSequence_Id());
                        if (msgDeliver.getRegistered_Delivery()==0){
                            logger.info("===是上行短信,消息内容:" + msgDeliver.getMsg_Content());
                            logger.info("===手机号码是:" + msgDeliver.getSrc_terminal_Id());
                        }else {
                            logger.info("===是状态报告");
                        }*/
                        CmppDeliverResp msgDeliverResp=new CmppDeliverResp();
                        msgDeliverResp.setTotal_Length(12+8+4);
                        msgDeliverResp.setCommand_Id(CmppDefine.CMPP_DELIVER_RESP);
                        msgDeliverResp.setSeq_Id(seq);
                        msgDeliverResp.setMsg_Id(b);
                        msgDeliverResp.setResult(msgDeliver.getResult());

                        String fs = CmppUtils.bytesToHexString(msgDeliverResp.toByteArray());
                        logger.info("===CMPP_DELIVER_RESP 回复网关："+fs);
                        //进行回复
                        channelHandlerContext.writeAndFlush(msgDeliverResp);
                        list.add(msgDeliver);
                    }
                    logger.info("===CMPP_DELIVER---END===");
                    System.out.println();
                    break;
                case CmppDefine.CMPP_TERMINATE:
                    logger.info("===ISMG请求断开连接===");
                    System.out.println();
                    break;
                default:
                    System.out.println("===无法解析IMSP返回的包结构：包长度为" + head.getTotal_Length());
                    System.out.println();
                    break;
            }
        } else {
            logger.info("===没有读取到数据,或长度不正确====");
            System.out.println();
        }
    }
}
