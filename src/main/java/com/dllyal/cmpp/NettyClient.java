package com.dllyal.cmpp;

import com.dllyal.util.SyncFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.dllyal.cmpp.utils.CmppConfig;
import com.dllyal.cmpp.utils.CmppUtils;
import com.dllyal.cmpp.utils.Command;
import com.dllyal.cmpp.entity.*;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;


/**
 * 封装的netty客户端
 * @author
 */
@Component("nettyClient")
public class NettyClient implements CommandLineRunner {

    @Override
    public void run(String... args) throws Exception {
        System.out.println("通过实现CommandLineRunner接口，在spring boot项目启动后自动执行");
        start();
    }

    @Autowired
    NettyClientInitializer nettyClientInitializer;

    private static Logger logger = LoggerFactory.getLogger(NettyClient.class);

    public final static ConcurrentHashMap<String, byte[]> recMsgMap = new ConcurrentHashMap<String, byte[]>();

    private Channel channel;

    //private EventLoopGroup eventLoopGroup;
    private EventLoopGroup eventLoopGroup =  new NioEventLoopGroup();

    private Bootstrap bootstrap;

    private ChannelFuture channelFuture;

    public NettyClient(){
        /*try {
            start();
        } catch (Exception e) {
            logger.error("NettyClient NettyClient start error",e);
        }*/
    }

    /**
     * 启动链接
     */
    public void start() throws Exception {
        try{
            if (!isActive()){
                //加载CMPP配置
                loadCmppConfig();
                //释放原资源
                shutdown();
                //初始化链接
                initClient();
                //执行连接
                CmppConnect cmppConnect = creatCmppConnect();
                doConnect(cmppConnect);
            }else {
                logger.info("NettyClient isActive");
            }
        }catch (Exception e){
            logger.error("start com.dllyal.cmpp error",e);
            shutdown();
            throw e;
        }
    }

    /**
     * 链接是否打开
     * @return
     */
    public boolean isActive(){
        if (channel==null){
            return false;
        }
        if (!channel.isOpen()||!channel.isActive()||!channel.isWritable()){
            //channel没开 或 没激活
            return false;
        }
        return  true;
    }


    /**
     * 重连机制 在 channelInactive 触发时调用
     * @param reConnect
     */
    public void reConnect(int reConnect){
        for (int times = 0 ; times < reConnect ; times++ ){
            try {
                if (!isActive()) {
                    /*shutdown();
                    CmppConnect cmppConnect = creatCmppConnect();
                    doConnect(cmppConnect);*/
                    start();
                }else {
                    try {
                        Thread.sleep(10 * 1000);
                    } catch (InterruptedException e) {
                        logger.error("reConnect isActive Thread.sleep error",e);
                    }
                }
            }catch (Exception ex){
                logger.info("尝试重连异常...:"+ CmppConfig.ServerIp+":"+ CmppConfig.ServerPort+" / "+ CmppConfig.CompanyCode);
                try {
                    Thread.sleep(10 * 1000);
                } catch (InterruptedException e) {
                    logger.error("reConnect Thread.sleep error",e);
                }
            }
        }
    }

    // 初始化客户端
    public void initClient() {
        //set Client
        nettyClientInitializer.setClient(this);
        //创建线程组 - 手动设置线程数,默认为cpu核心数2倍 - 如果此处new NioEventLoopGroup 则每次重连都会无限新增线程组
        //eventLoopGroup =  new NioEventLoopGroup();
        //创建引导程序
        bootstrap = new Bootstrap();
        //保持长连接
        bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
        //将线程加入bootstrap
        bootstrap.group(eventLoopGroup)
                .remoteAddress(CmppConfig.ServerIp, CmppConfig.ServerPort)
                //使用指定通道类
                .channel(NioSocketChannel.class)
                //设置日志
                .handler(new LoggingHandler(LogLevel.INFO))
                //重写通道初始化方法
                .handler(nettyClientInitializer);
    }

    /**
     * CMPP Netty connect
     * @throws Exception
     */
    public void doConnect(CmppConnect cmppConnect) throws Exception {
        logger.info("connect............................");
        channelFuture = bootstrap.connect(CmppConfig.ServerIp, CmppConfig.ServerPort).sync();
        channelFuture.addListener(new ChannelFutureListener(){
            @Override
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                if (!channelFuture.isSuccess()) {
                    final EventLoop loop = channelFuture.channel().eventLoop();
                    loop.schedule(new Runnable() {
                        @Override
                        public void run() {
                            logger.info("服务端链接不上，开始重连操作...");
                            //System.err.println("服务端链接不上，开始重连操作...");
                            try {
                                CmppConnect cmppConnect = creatCmppConnect();
                                doConnect(cmppConnect);
                            } catch (Exception e) {
                                logger.info("服务端链接不上，重连异常",e);
                            }
                        }
                    }, 10, TimeUnit.SECONDS);
                } else {
                    logger.info("服务端链接成功...");
                    //System.err.println("服务端链接成功...");
                }
            }
        });
        //登录
        logger.info("获取通道执行登录...");
        channel = channelFuture.channel();
        //读取流水号
        String msgNo = CmppUtils.bytesToHexString(CmppUtils.getMsgBytes(cmppConnect.toByteArray(),8,12));
        recMsgMap.put(msgNo,cmppConnect.toByteArray());
        channel.writeAndFlush(cmppConnect);
        channelFuture.addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
    }

    /**
     * CMPP Netty doConnectSync
     * @throws Exception
     */
    public CmppConnectResp doConnectSync(CmppConnect cmppConnect, SyncFuture<CmppConnectResp> syncFuture, int timeout) throws Exception {
        logger.info("doConnectSync............................");
        CmppConnectResp result = null;
        //执行链接
        doConnect(cmppConnect);
        //等待获取同步结果
        try {
            // 等待 n 秒
            result = syncFuture.get(timeout, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            logger.error("发送同步消息doConnectSync--syncFuture--error",e);
        }
        return result;
    }

    /**
     * CMPP Netty Connect
     * @throws Exception
     *//*
    public void doConnect1(CmppConnect cmppConnect) throws Exception {
        logger.info("doConnect............................");
        try {
            channelFuture = bootstrap.connect().sync();
            channel = channelFuture.channel();
            //读取流水号
            String msgNo = CmppUtils.bytesToHexString(CmppUtils.getMsgBytes(cmppConnect.toByteArray(),8,12));
            recMsgMap.put(msgNo,cmppConnect.toByteArray());
            channel.writeAndFlush(cmppConnect);
            channelFuture.addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
        } catch (Exception e) {
            logger.error("连接异常",e);
            throw e;
        }
    }

    *//**
     * CMPP Netty Connect 同步连接
     * @throws Exception
     *//*
    public CMPPConnectResp doConnectSync1(CmppConnect cmppConnect, SyncFuture<CMPPConnectResp> syncFuture, int timeout) throws Exception {
        logger.info("doConnectSync............................");
        CMPPConnectResp result = null;
        try {
            channelFuture = bootstrap.connect().sync();
            channel = channelFuture.channel();
            //读取流水号
            String msgNo = CmppUtils.bytesToHexString(CmppUtils.getMsgBytes(cmppConnect.toByteArray(),8,12));
            recMsgMap.put(msgNo,cmppConnect.toByteArray());
            channel.writeAndFlush(cmppConnect);
            channelFuture.addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
            try {
                // 等待 n 秒
                result = syncFuture.get(timeout, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                logger.error("发送同步消息doConnectSync--syncFuture--error",e);
            }

        } catch (Exception e) {
            logger.error("连接异常",e);
            throw e;
        }
        return result;
    }*/

    /**
     * shutdown
     */
    public boolean shutdown() {
        boolean result;
        try {
            logger.info("开始关闭Netty channel...");
            // 关闭当前的管道
            if (channel != null){
                channel.close();
            }
            // 释放资源 不会退出 不用优雅关闭
            /*if (eventLoopGroup != null){
                eventLoopGroup.shutdownGracefully().sync();
            }*/
            logger.info("Netty channel关闭完毕!");
            result = true;
        } catch (Exception e) {
            result = false;
            logger.error("停止netty异常!" + e.getMessage(), e);
        }
        return result;
    }

    /**
     * 提交请求
     * @param submit
     * @return
     */
    public boolean submit(CmppMessageHeader submit){
        if (isActive()){
            //读取流水号
            String msgNo = CmppUtils.bytesToHexString(CmppUtils.getMsgBytes(submit.toByteArray(),8,12));
            recMsgMap.put(msgNo,submit.toByteArray());
            channel.writeAndFlush(submit);
            logger.info("NettyClient submit " + msgNo + " " + Thread.currentThread().getName());
            return true;
        }
        return false;
    }

    /**
     * 发送消息
     * @param sequence
     * @param smsContent
     * @param freeCode
     * @param receiveNum
     * @return
     * @throws UnsupportedEncodingException
     */
    public boolean sendMsg(int sequence,String smsContent,String freeCode,String receiveNum) throws UnsupportedEncodingException {
        logger.info("NettyClient sendMsg " + sequence + " --" + receiveNum);
        //定义返回值
        boolean result = false;
        //通道活动
        if (isActive()){
            //短信提交对象
            CmppSubmit cmppSubmit = new CmppSubmit(CmppDefine.CMPP_SUBMIT, Command.CMPP3_VERSION);
            cmppSubmit.setCommand_Id(CmppDefine.CMPP_SUBMIT);
            cmppSubmit.setSequence_Id(sequence);
            cmppSubmit.setRegisteredDelivery((byte) 0x01);
            cmppSubmit.setMsgLevel((byte) 0x01);
            cmppSubmit.setFeeUserType((byte) 0x02);
            cmppSubmit.setFeeTerminalId(CmppConfig.ServiceCode);
            cmppSubmit.setFeeTerminalType((byte) 0x00);
            cmppSubmit.setTpPId((byte) 0x00);
            cmppSubmit.setMsgSrc(CmppConfig.BusinessCode);
            cmppSubmit.setSrcId(CmppConfig.ServiceCode + freeCode);
            cmppSubmit.setDestTerminalId(receiveNum);
            cmppSubmit.setServiceId(CmppConfig.CompanyCode);
            //短信内容UCS2编码
            byte[] smsContentUCS2;
            smsContentUCS2 = smsContent.getBytes("UnicodeBigUnmarked");
            //System.out.println(smsContent + " -(UCS2)编码: " + CmppUtils.bytesToHexStr(smsContentUCS2));
            //UCS2编码编码后的长度
            int messageUCS2Len = smsContentUCS2.length;
            //长短信长度
            int maxMessageLen = 126;
            //超过长短信长度
            if (messageUCS2Len > maxMessageLen) {
                //长短信发送
                int tpUdhi = 1;
                //msgFmt编码8 表示UCS2编码
                int msgFmt = 0x08;
                //长短信分为多少条发送
                int messageUCS2Count = messageUCS2Len / (maxMessageLen - 6) + 1;
                //长短信内容前6个字节 为协议头编码
                byte[] tp_udhiHead = new byte[6];
                tp_udhiHead[0] = 0x05;
                tp_udhiHead[1] = 0x00;
                tp_udhiHead[2] = 0x03;
                tp_udhiHead[3] = 0x0A;
                tp_udhiHead[4] = (byte) messageUCS2Count;
                tp_udhiHead[5] = 0x01;
                //默认为第一条 开始循环发送
                for (int i = 0; i < messageUCS2Count; i++) {
                    //第几条
                    tp_udhiHead[5] = (byte) (i + 1);
                    //定义短信内容
                    byte[] msgContent;
                    //分割当前这条的内容
                    if (i != messageUCS2Count - 1) {
                        //不为最后一条
                        msgContent = CmppUtils.byteAddLongMsg(tp_udhiHead, smsContentUCS2, i * (maxMessageLen - 6), (i + 1) * (maxMessageLen - 6));
                    } else {
                        msgContent = CmppUtils.byteAddLongMsg(tp_udhiHead, smsContentUCS2, i * (maxMessageLen - 6), messageUCS2Len);
                    }
                    System.out.println("正在发送第" + tp_udhiHead[5] + "条长短信");
                    System.out.println("UCS2:" + CmppUtils.bytesToHexStr(msgContent));
                    System.out.println("总长度：" + msgContent.length);
                    //补充充短信请求信息
                    cmppSubmit.setTotal_Length(12 + 8 + 1 + 1 + 1 + 1 + 10 + 1 + 32 + 1 + 1 + 1 + 1 + 6 + 2 + 6 + 17 + 17 + 21 + 1 + 32 + 1 + 1 + msgContent.length + 20);
                    cmppSubmit.setPkTotal(tp_udhiHead[4]);
                    cmppSubmit.setPkNumber(tp_udhiHead[5]);
                    cmppSubmit.setTpUdhi((byte) tpUdhi);
                    cmppSubmit.setMsgFmt((byte) msgFmt);
                    cmppSubmit.setMsgLength((byte) msgContent.length);
                    cmppSubmit.setMsgContent(msgContent);
                    //读取流水号
                    String msgNo = CmppUtils.bytesToHexString(CmppUtils.getMsgBytes(cmppSubmit.toByteArray(),8,12));
                    recMsgMap.put(msgNo,cmppSubmit.toByteArray());
                    //向通道写入并推送
                    channel.writeAndFlush(cmppSubmit);
                    logger.info("NettyClient sendMsg " + Thread.currentThread().getName());
                }
            }else {
                //补充充短信请求信息
                cmppSubmit.setTotal_Length(12 + 8 + 1 + 1 + 1 + 1 + 10 + 1 + 32 + 1 + 1 + 1 + 1 + 6 + 2 + 6 + 17 + 17 + 21 + 1 + 32 + 1 + 1 + smsContent.length() * 2 + 20);
                cmppSubmit.setPkTotal((byte) 0x01);
                cmppSubmit.setPkNumber((byte) 0x01);
                cmppSubmit.setTpUdhi((byte) 0x00);
                cmppSubmit.setMsgFmt((byte) 0x0F);
                cmppSubmit.setMsgLength(CmppUtils.intToByte(smsContent.length() * 2));
                cmppSubmit.setMsgContent(smsContent.getBytes("gb2312"));
                //读取流水号
                String msgNo = CmppUtils.bytesToHexString(CmppUtils.getMsgBytes(cmppSubmit.toByteArray(),8,12));
                recMsgMap.put(msgNo,cmppSubmit.toByteArray());
                //向通道写入并推送
                channel.writeAndFlush(cmppSubmit);
                logger.info("NettyClient sendMsg " + Thread.currentThread().getName());
            }
            result = true;
        }
        return result;
    }

    /**
     * 发送同步消息
     * @param sequence
     * @param smsContent
     * @param freeCode
     * @param receiveNum
     * @param syncFuture
     * @param timeout
     * @return
     * @throws UnsupportedEncodingException
     */
    public CmppSubmitResp sendSyncMsg(int sequence,String smsContent,String freeCode,String receiveNum,SyncFuture<CmppSubmitResp> syncFuture,int timeout) throws UnsupportedEncodingException {
        logger.info("NettyClient sendSyncMsg " + sequence + " --" + receiveNum);
        //定义返回对象
        CmppSubmitResp result = null;
        //调用发信
        boolean sendFlag = sendMsg( sequence, smsContent, freeCode, receiveNum);
        //发送成功
        if (sendFlag){
            //同步获取发送结果
            try {
                // 等待 n 秒
                result = syncFuture.get(timeout, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                logger.error("发送同步消息sendSyncMsg--syncFuture--error",e);
            }
        }
        return result;
    }

    /**
     * 构建CMPP连接对象
     * @return
     */
    public CmppConnect creatCmppConnect(){
        //构建CMPP连接对象账号登陆
        CmppConnect cmppConnect = new CmppConnect();
        //消息总长度，级总字节数:4+4+4(消息头)+6+16+1+4(消息主体)
        cmppConnect.setTotal_Length(12+6+16+1+4);
        //标识创建连接
        cmppConnect.setCommand_Id(CmppDefine.CMPP_CONNECT);
        //序列，由我们指定
        cmppConnect.setSequence_Id(CmppUtils.getSequence());
        //我们的企业代码
        cmppConnect.setSourceAddr(CmppConfig.CompanyCode);
        //md5(企业代码+密匙+时间戳)
        cmppConnect.setAuthenticatorSource(CmppUtils.md5(CmppConfig.CompanyCode, CmppConfig.Pwd));
        //时间戳(MMDDHHMMSS)
        cmppConnect.setTimestamp(Integer.parseInt(CmppUtils.getTimestamp()));
        //版本号 高4bit为3，低4位为0
        cmppConnect.setVersion((byte)0x30);
        return cmppConnect;
    }

    /**
     * 加载CMPP配置
     */
    public void loadCmppConfig(){

    }

}
