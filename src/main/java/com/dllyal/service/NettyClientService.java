package com.dllyal.service;

import com.dllyal.cmpp.entity.CmppConnectResp;
import com.dllyal.cmpp.entity.CmppSubmitResp;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;

@Service
public interface NettyClientService {

    /**
     * 启动
     * @throws Exception
     */
    public void start() throws Exception;

    /**
     * 启动--同步返回响应
     * @param timeout
     * @return
     * @throws Exception
     */
    public CmppConnectResp startSync(int timeout) throws Exception;

    /**
     * 中断连接
     * @return
     */
    public boolean shutdown();

    /**
     * 发送短信消息
     * @param sequence 当前消息序号
     * @param smsContent 消息内容
     * @param freeCode 自定义拓展码
     * @param receiveNum 接收方手机号码
     * @return
     * @throws UnsupportedEncodingException
     */
    public boolean sendMsg(int sequence, String smsContent, String freeCode, String receiveNum) throws UnsupportedEncodingException;

    /**
     * 发送短信消息--同步返回提交结果
     * @param sequence 当前消息序号
     * @param smsContent 消息内容
     * @param freeCode 自定义拓展码
     * @param receiveNum 接收方手机号码
     * @param timeout 同步等待时间-超时时间
     * @return
     * @throws UnsupportedEncodingException
     */
    public CmppSubmitResp sendSyncMsg(int sequence, String smsContent, String freeCode, String receiveNum, int timeout) throws UnsupportedEncodingException;

    /**
     * CMPP回调
     * @param msg
     */
    public void ackSyncMsg(Object msg);
}
