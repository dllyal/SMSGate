package com.dllyal.cmpp;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.dllyal.cmpp.utils.CmppConfig;
import com.dllyal.cmpp.entity.CmppActiveTest;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;


/**
 * 心跳Handler
 * @author
 */
public class HeartHandler extends ChannelInboundHandlerAdapter {

    Logger logger = LoggerFactory.getLogger(HeartHandler.class);

    private NettyClient client;

    public HeartHandler(NettyClient client){
        this.client = client;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.info("exceptionCaught关闭客户端连接");
        cause.printStackTrace();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        logger.info("服务端链接断裂...");
        logger.info("CmppConfig.ReCount~~~~~~ "+ CmppConfig.ReCount);
        if (CmppConfig.ReCount > 0){
            if (CmppConfig.ReCount > 0){
                logger.info("CmppConfig.ReCount>0，开启重连机制");
                client.reConnect(CmppConfig.ReCount);
            }
            /*ctx.channel().eventLoop().schedule(new Runnable() {
                @Override
                public void run() {
                    logger.info("开始关闭重连操作...");
                    //System.err.println("服务端链接不上，开始重连操作...");
                    try {
                        client.shutdown();
                        CmppConnect cmppConnect = client.creatCmppConnect();
                        client.doConnect(cmppConnect);
                    } catch (Exception e) {
                        logger.error("服务端重连出现异常",e);
                    }
                }
            }, 10, TimeUnit.SECONDS);*/
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println();
        logger.info("初始化创建HeartHandler连接。。。");
        super.channelActive(ctx);
    }


    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            IdleState state = event.state();
            if (state == IdleState.WRITER_IDLE || state == IdleState.ALL_IDLE) {
                CmppActiveTest cmppActiveTest = new CmppActiveTest();
                System.out.println();
                logger.info("===心跳启动!===");
                logger.info("===序号：" + cmppActiveTest.getSequence_Id());
                client.submit(cmppActiveTest);
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    /*@Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        logger.info("掉线了~~~~~~ ");
        logger.info("CmppConfig.ReCount~~~~~~ "+ CmppConfig.ReCount);
        if (CmppConfig.ReCount > 0){
            logger.info("CmppConfig.ReCount>0，开启重连机制");
            client.reConnect(CmppConfig.ReCount);
        }
    }*/
}
