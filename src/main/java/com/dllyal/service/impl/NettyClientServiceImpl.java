package com.dllyal.service.impl;

import com.alibaba.fastjson.JSON;
import com.dllyal.cmpp.NettyClient;
import com.dllyal.cmpp.utils.CmppConfig;
import com.dllyal.cmpp.entity.CmppConnect;
import com.dllyal.cmpp.entity.CmppConnectResp;
import com.dllyal.cmpp.entity.CmppSubmitResp;
import com.google.common.cache.*;
import com.dllyal.service.NettyClientService;
import com.dllyal.util.SyncFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.util.concurrent.TimeUnit;

@Service
public class NettyClientServiceImpl implements NettyClientService {

    private static final Logger logger = LoggerFactory.getLogger(NettyClientServiceImpl.class);

    //缓存接口这里是LoadingCache，LoadingCache在缓存项不存在时可以自动加载缓存
    private static LoadingCache<String, SyncFuture> futureCache = CacheBuilder.newBuilder()
            //设置缓存容器的初始容量为10
            .initialCapacity(100)
            // maximumSize 设置缓存大小
            .maximumSize(10000)
            //设置并发级别为20，并发级别是指可以同时写缓存的线程数
            .concurrencyLevel(20)
            // expireAfterWrite设置写缓存后15秒钟过期
            .expireAfterWrite(15, TimeUnit.SECONDS)
            //设置缓存的移除通知
            .removalListener(new RemovalListener<Object, Object>() {
                @Override
                public void onRemoval(RemovalNotification<Object, Object> notification) {
                    logger.debug("LoadingCache: {} was removed, cause is {}",notification.getKey(), notification.getCause());
                }
            })
            //build方法中可以指定CacheLoader，在缓存不存在时通过CacheLoader的实现自动加载缓存
            .build(new CacheLoader<String, SyncFuture>() {
                @Override
                public SyncFuture load(String key) throws Exception {
                    // 当获取key的缓存不存在时，不需要自动添加
                    return null;
                }
            });


    @Autowired
    private NettyClient nettyClient;

    /*@Autowired
    private CacheManager cacheManager;*/

    @Override
    public void start() throws Exception{
        logger.info("检测启动NettyClient客户端");
        /*//重新加载CMPP配置
        nettyClient.loadCmppConfig();*/
        nettyClient.start();
    }

    @Override
    public CmppConnectResp startSync(int timeout) throws Exception{
        CmppConnectResp resp = null;
        logger.info("检测启动NettyClient客户端");
        CmppConnect cmppConnect = nettyClient.creatCmppConnect();
        SyncFuture<CmppConnectResp> syncFuture = new SyncFuture<CmppConnectResp>();
        // 放入缓存中
        futureCache.put(String.valueOf(cmppConnect.getSequence_Id()), syncFuture);
        try{
            if (!nettyClient.isActive()){
                //重新加载CMPP配置
                nettyClient.loadCmppConfig();
                //释放原资源
                nettyClient.shutdown();
                //初始化链接
                nettyClient.initClient();
                //执行连接
                resp = nettyClient.doConnectSync(cmppConnect,syncFuture,timeout);
                //返回信息
                logger.info("startSync doConnectSync" + JSON.toJSONString(resp));
            }else {
                logger.info("NettyClient isActive");
            }
        }catch (Exception e){
            logger.error("start com.dllyal.cmpp error");
            nettyClient.shutdown();
            throw e;
        }
        return resp;
    }

    @Override
    public boolean shutdown() {
        logger.info("停止NettyClient客户端");
        CmppConfig.ReCount = 0;
        return nettyClient.shutdown();
    }

    @Override
    public boolean sendMsg(int sequence,String smsContent,String freeCode,String receiveNum) throws UnsupportedEncodingException {
        // 发送异步消息
        boolean result = nettyClient.sendMsg(sequence,smsContent,freeCode,receiveNum);
        return result;
    }

    @Override
    public CmppSubmitResp sendSyncMsg(int sequence,String smsContent,String freeCode,String receiveNum,int timeout) throws UnsupportedEncodingException {

        SyncFuture<CmppSubmitResp> syncFuture = new SyncFuture<CmppSubmitResp>();
        // 放入缓存中
        futureCache.put(String.valueOf(sequence), syncFuture);
        // 发送同步消息
        CmppSubmitResp result = nettyClient.sendSyncMsg(sequence, smsContent, freeCode, receiveNum, syncFuture,timeout);

        return result;
    }

    @Override
    public void ackSyncMsg(Object msg) {

        if (msg instanceof CmppConnectResp){
            CmppConnectResp connectResp = (CmppConnectResp)msg;
            // ID
            String dataId = String.valueOf(connectResp.getSequence_Id());
            // 从缓存中获取数据
            SyncFuture<CmppConnectResp> syncFuture = futureCache.getIfPresent(dataId);
            // 如果不为null, 则通知返回
            if(syncFuture != null) {
                //String s = JSON.toJSONString(msg);
                syncFuture.setResponse(connectResp);
                //主动释放
                futureCache.invalidate(dataId);
            }
            //登录失败shutdown
            if (connectResp.getStatus() != 0){
                nettyClient.shutdown();
            }
        }

        if (msg instanceof CmppSubmitResp) {
            CmppSubmitResp resp = (CmppSubmitResp) msg;
            // ID
            String dataId = String.valueOf(resp.getSeqId());
            // 从缓存中获取数据
            SyncFuture<CmppSubmitResp> syncFuture = futureCache.getIfPresent(dataId);
            // 如果不为null, 则通知返回
            if(syncFuture != null) {
                //String s = JSON.toJSONString(msg);
                syncFuture.setResponse(resp);
                //主动释放
                futureCache.invalidate(dataId);
            }
        }
    }

}