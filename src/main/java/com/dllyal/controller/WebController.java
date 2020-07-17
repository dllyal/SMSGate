package com.dllyal.controller;

import com.alibaba.fastjson.JSONObject;
import com.dllyal.cmpp.entity.CmppConnectResp;
import com.dllyal.cmpp.entity.CmppSubmitResp;
import com.dllyal.cmpp.utils.CmppUtils;
import com.dllyal.service.NettyClientService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;


@RestController
@Api(tags = "API接口")
public class WebController {

    Logger logger = LoggerFactory.getLogger(WebController.class);

    private NettyClientService nettyClientService;

    @Autowired
    public void setNettyClientService(NettyClientService nettyClientService){
        this.nettyClientService = nettyClientService;
    }

    @ApiOperation(value = "启动CMPP连接", notes = "启动CMPP连接（程序默认启动，一般不用操作）")
    @PostMapping(value = {"/service/start"}, produces = {"application/json;charset=UTF-8"})
    public Map start() {
        logger.info("********start cmpp*********");
        Map result = new HashMap();
        try {
            //启动CMPP连接
            nettyClientService.start();
            result.put("respCode", 200);
            result.put("respDesc", "OK");
        } catch (Exception e) {
            result.put("respCode", 500);
            result.put("respDesc", "Exception");
            logger.error("getReceipt err:" + e);
        }
        return result;
    }

    @ApiOperation(value = "启动CMPP连接-同步", notes = "启动CMPP连接-同步返回结果（程序默认启动，一般不用操作）")
    @PostMapping(value = {"/service/startSync"}, produces = {"application/json;charset=UTF-8"})
    public Map startSync() {
        logger.info("********startSync cmpp*********");
        Map result = new HashMap();
        try {
            //启动CMPP连接--同步
            CmppConnectResp connectResp = nettyClientService.startSync(10);
            result.put("result", JSONObject.toJSON(connectResp));
            if (connectResp.getStatus() == 0){
                result.put("respCode", 200);
                result.put("respDesc", "OK");
            }else {
                result.put("respCode", 500);
                result.put("respDesc", "ERR");
            }
        } catch (Exception e) {
            result.put("respCode", 500);
            result.put("respDesc", "Exception");
            logger.error("getReceipt err:" + e);
        }
        return result;
    }

    @ApiOperation(value = "断开CMPP连接", notes = "断开CMPP连接")
    @PostMapping(value = {"/service/shutdown"}, produces = {"application/json;charset=UTF-8"})
    public Map shutdown() {
        logger.info("********shutdown cmpp*********");
        Map result = new HashMap();
        try {
            //断开CMPP连接
            boolean resultFlag = nettyClientService.shutdown();
            if (resultFlag){
                result.put("respCode", 200);
                result.put("respDesc", "OK");
            }else {
                result.put("respCode", 500);
                result.put("respDesc", "ERR");
            }
        } catch (Exception e) {
            result.put("respCode", 500);
            result.put("respDesc", "Exception");
            logger.error("getReceipt err:" + e);
        }
        return result;
    }

    @ApiOperation(value = "发送短信", notes = "提交后不会同步返回提交结果")
    @PostMapping(value = {"/service/sendMsg"}, produces = {"application/json;charset=UTF-8"})
    public Map sendMsg(@RequestBody Map map) {
        logger.info("********cmpp sendMsg*********");
        logger.info(map.toString());
        String smsContent = (String) map.get("smsContent");
        String receiveNum = (String) map.get("receiveNum");

        //发送端口后面添加的拓展码
        String freeCode = "0000";

        Map result = new HashMap();
        try {
            //发送短信
            boolean resultFlag = nettyClientService.sendMsg(CmppUtils.getSequence(),smsContent,freeCode,receiveNum);
            if (resultFlag){
                result.put("respCode", 200);
                result.put("respDesc", "OK");
            }else {
                result.put("respCode", 500);
                result.put("respDesc", "ERR");
            }
        } catch (Exception e) {
            result.put("respCode", 500);
            result.put("respDesc", "Exception");
            logger.error("getReceipt err:" + e);
        }
        return result;
    }

    @ApiOperation(value = "发送短信-同步", notes = "同步返回提交结果")
    @PostMapping(value = {"/service/sendSyncMsg"}, produces = {"application/json;charset=UTF-8"})
    public Map sendSyncMsg(@RequestBody Map map) {
        logger.info("********cmpp sendSyncMsg*********");
        logger.info(map.toString());
        String smsContent = (String) map.get("smsContent");
        String receiveNum = (String) map.get("receiveNum");

        //发送端口后面添加的拓展码
        String freeCode = "0000";

        Map result = new HashMap();
        try {
            //发送短信
            CmppSubmitResp submitResp = nettyClientService.sendSyncMsg(CmppUtils.getSequence(),smsContent,freeCode,receiveNum,10);
            result.put("result", JSONObject.toJSON(submitResp));
            if (submitResp.getResult() == 0){
                result.put("respCode", 200);
                result.put("respDesc", "OK");
            }else {
                result.put("respCode", 500);
                result.put("respDesc", "ERR");
            }
        } catch (Exception e) {
            result.put("respCode", 500);
            result.put("respDesc", "Exception");
            logger.error("getReceipt err:" + e);
        }
        return result;
    }

}
