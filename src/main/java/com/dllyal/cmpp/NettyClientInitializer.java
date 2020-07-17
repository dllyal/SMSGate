package com.dllyal.cmpp;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.timeout.IdleStateHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * @author
 */
@Component
public class NettyClientInitializer extends ChannelInitializer<SocketChannel> {

    @Autowired
    CmppHandler cmppHandler;

    private NettyClient client;

    public NettyClientInitializer(){
    }

    @Override
    protected void initChannel(SocketChannel ch){
        ChannelPipeline ph = ch.pipeline();

        //长度编码器，防止粘包拆包
        ph.addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE , 0, 4, -4, 0, true));
        //ph.addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE,0,4,0,0));

        //心跳
        //readerIdleTime:为读超时间(即测试端一定时间内未接收到被测试端消息)；
        //writerIdleTime:为写超时间（即测试端一定时间内向被测试端发送消息）；
        //allIdeTime：所有类型的超时时间
        ph.addLast("idleState handler",new IdleStateHandler(0,0, 10, TimeUnit.SECONDS));
        //心跳包
        ph.addLast( "heart handler",new HeartHandler(client));

        //解析
        ph.addLast("encoder",new CmppEncoder());

        ph.addLast("decoder",new CmppDecoder());

        //客户端的逻辑
        ph.addLast("com.dllyal.cmpp handler", cmppHandler);

    }

    public NettyClient getClient() {
        return client;
    }

    public void setClient(NettyClient client) {
        this.client = client;
    }
}
