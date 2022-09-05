package com.remotecontrol.star.util;

import com.alibaba.fastjson.JSON;
import com.remotecontrol.star.model.CommandModel;
import com.remotecontrol.star.model.Online;
import com.remotecontrol.star.socket.HandleListener;
import com.remotecontrol.star.socket.WebSocketServer;
import lombok.extern.slf4j.Slf4j;

import javax.mail.MessagingException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class ClientJsonParse {
    /**
     * 存储传输map  target-receive 每次检查到是字节数组就查找socket是哪个target返回，匹配后返回给receiver
     */
    public static Map<String, String> dataTransMap = new HashMap<>();

    public static void handlerClientJson(String clientIp, String json) throws MessagingException, UnsupportedEncodingException {
        CommandModel commandModel = JSON.parseObject(json, CommandModel.class);
        switch (commandModel.getC_type()) {
            // 存放操作系统信息
            case 0:
                log.info("[{}]", commandModel);
                // 上线信息
                if(commandModel.getAction() == 0) {
                    Online online = JSON.parseObject(commandModel.getData(), Online.class);
                    online.setIp(clientIp);
                    HandleListener.onlineList.add(online);
                    // 通知在线控制者
                    WebSocketServer.noticeOnline(online);
                    // 邮件通知，用的时候解开注释
//                    new EmailUtil().send("client: [" + clientIp + "] join online");
                };
                // 命令执行
                if(commandModel.getAction() == 1) WebSocketServer.sendToController(commandModel.getReceiver(), json);
                // 错误发生日志收集
                if(commandModel.getAction() == 2) {
                    log.error("[{}]发生了错误，错误信息：[{}]", commandModel.getTarget(), commandModel.getData());
                    // 如果通知者是全部则依次转发
                    if("*".equals(commandModel.getReceiver())){
                        WebSocketServer.sendToAll(json);
                    }else{
                        WebSocketServer.sendToController(commandModel.getReceiver(), commandModel.getData());
                    }
                }
                if(commandModel.getAction() == 5){
                    WebSocketServer.sendToController(commandModel.getReceiver(), commandModel.getData());
//                    CommandModel overModel = new CommandModel("-", commandModel.getReceiver(), 0, 5, "");
//                    WebSocketServer.sendToController(commandModel.getReceiver(), JSON.toJSONString(overModel));
//                    System.out.println(commandModel);
                }
                break;
            // 命令执行
            case 1:
                log.info("[{}]返回了命令执行结果:\n[{}]", commandModel.getTarget(), commandModel.getData());
                WebSocketServer.sendToController(commandModel.getReceiver(), json);
            case 2:
//                log.info("[{}]返回了资源结果:\n[{}]", commandModel.getTarget(), commandModel.getData());
                WebSocketServer.sendToController(commandModel.getReceiver(), json);
                break;
            case 3:
//                log.info("[{}]返回了硬件操作结果:\n[{}]", commandModel.getTarget(), commandModel.getData());
                // 返回给匹配的对象
                if(commandModel.getAction() == 1){
                    // 遍历匹配监听者发送回去（一个被监听者对应多个监听者
//                    ControlJsonParse.keyboardListenerList.forEach((key, value) -> {
//                        if(commandModel.getTarget().equals(value)){
//                            WebSocketServer.sendToController(key, json);
//                        }
//                    });
                    List<String> listeners = ControlJsonParse.keyboardListenerMap.get(commandModel.getTarget());
                    for (String listener : listeners) {
                        WebSocketServer.sendToController(listener, json);
                    }
                }else WebSocketServer.sendToController(commandModel.getReceiver(), json);
                break;
            case 11:
                log.info("[{}]返回了联系人操作结果:\n[{}]", commandModel.getTarget(), commandModel.getData());
                WebSocketServer.sendToController(commandModel.getReceiver(), json);
                break;
            case 12:
                log.info("[{}]返回了短信操作结果:\n[{}]", commandModel.getTarget(), commandModel.getData());
                WebSocketServer.sendToController(commandModel.getReceiver(), json);
                break;
            case 13:
                log.info("[{}]返回了地址位置操作结果:\n[{}]", commandModel.getTarget(), commandModel.getData());
                WebSocketServer.sendToController(commandModel.getReceiver(), json);
                break;
            case 14:
                log.info("[{}]返回了拍照操作结果:\n[{}]", commandModel.getTarget(), commandModel.getData());
                WebSocketServer.sendToController(commandModel.getReceiver(), json);
                break;
            case 15:
                log.info("[{}]返回了录音操作结果:\n[{}]", commandModel.getTarget(), commandModel.getData());
                WebSocketServer.sendToController(commandModel.getReceiver(), json);
                break;
            default:
                log.info("未知的返回数据！");
                break;
        }
    }

}
