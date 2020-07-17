package com.dllyal.cmpp;

import com.dllyal.service.NettyClientService;
import com.dllyal.cmpp.utils.CmppUtils;
import com.dllyal.cmpp.entity.*;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 主要业务 handler
 * @author
 */
@Component
@ChannelHandler.Sharable
public class CmppHandler extends SimpleChannelInboundHandler {

    Logger logger = LoggerFactory.getLogger(CmppHandler.class);

    @Autowired
    private NettyClientService service;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) {

        //CmppMessageHeader resp0 = (CmppMessageHeader)msg;
        //logger.info("CmppHandler 接收 " + Thread.currentThread().getName());

        if (msg instanceof CmppConnectResp){
            CmppConnectResp connectResp = (CmppConnectResp) msg;
            logger.info("-------------接收到链接短信网关返回-------------");
            logger.info("----注册序列号：" + connectResp.getSequence_Id());
            logger.info("----注册返回值：" + connectResp.getStatus());
            logger.info("----注册返状态：" + connectResp.getStatusStr());
            System.out.println();
            // 同步消息返回
            service.ackSyncMsg(msg);
        }

        if (msg instanceof CmppActiveTestResp){
            CmppActiveTestResp activeTestResp = (CmppActiveTestResp) msg;
            logger.info("-------------接收到短信网关链接检查返回-------------");
            logger.info("----返回序列号：" + activeTestResp.getSequence_Id());
            System.out.println();
        }

        if (msg instanceof CmppTerminateResp){
            CmppTerminateResp terminateResp = (CmppTerminateResp) msg;
            logger.info("-------------接收到请求短信网关进行连接拆除的返回-------------");
            logger.info("----返回序列号：" + terminateResp.getSequence_Id());
            System.out.println();
        }

        if (msg instanceof CmppSubmitResp){
            CmppSubmitResp resp = (CmppSubmitResp) msg;
            logger.info("-------------接收到短信提交应答-------------");
            logger.info("----请求序列号：" + resp.getSeqId());
            logger.info("----提交的状态：" + resp.getResult());
            logger.info("----第一次响应：" + resp.getMsgIdStr());
            System.out.println();
            // 同步消息返回
            service.ackSyncMsg(msg);
        }

        if (msg instanceof CmppDeliver){
            CmppDeliver resp=(CmppDeliver)msg;
            // 是否为状态报告 0：非状态报告1：状态报告
            if (resp.getRegistered_Delivery() == 1) {
                // 如果是状态报告的话
                resp.setDest_terminal_Id(CmppUtils.trimPhoneNum(resp.getDest_terminal_Id()));
                logger.info("-------------短信下发状态报告---------------");
                logger.info("----第二次响应：" + resp.getMsg_Id_report());
                logger.info("----下发手机号：" + resp.getDest_terminal_Id());
                logger.info("----送达的状态：" + resp.getStat());
                System.out.println();
            } else {
                //用户回复会打印在这里
                resp.setSrc_terminal_Id(CmppUtils.trimPhoneNum(resp.getSrc_terminal_Id()));
                logger.info("-------------短信上行回复---------------");
                logger.info("----Msg_Id：" + resp.getMsg_Id());
                logger.info("----Dest_Id：" + resp.getDest_Id());
                logger.info("----Src_terminal_Id：" + resp.getSrc_terminal_Id());
                logger.info("----Msg_Content：" + resp.getMsg_Content());
                System.out.println();

                //处理
            }
        }
    }

}
