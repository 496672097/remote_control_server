package com.remotecontrol.star.socket;
import com.alibaba.fastjson.JSON;
import com.remotecontrol.star.config.ServerConfig;
import com.remotecontrol.star.model.CommandModel;
import com.remotecontrol.star.model.Online;
import com.remotecontrol.star.util.ControlJsonParse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author ：复目
 * @date ：Created in 2021/11/9 17:53
 */
@Component
@Service
@ServerEndpoint("/remoteController/{username}/{password}")
@Slf4j
public class WebSocketServer {
    public static ConcurrentHashMap<String,Session> webSocketMap = new ConcurrentHashMap<>();
    /**与某个控制端的连接会话，需要通过它来给客户端发送数据*/
    private Session session;
    /**接收username*/
    private String username = "";

    private static final Integer AUTH_TYPE = 0;

    private static final Integer AUTH_FAIL = 0;
    private static final Integer AUTH_SUCCESS = 1;

    /**
     * 连接建立成功调用的方法
     * @param username 用户名
     * @param password 密码
     * */
    @OnOpen
    public void onOpen(Session session,
                       @PathParam("username") String username,
                       @PathParam("password") String password) throws IOException {
        log.info("用户连接:" + username + "\tauth:" + password);
        // TODO:校验密码
        this.username = username;
        this.session = session;
        if(!ServerConfig.password.equals(password)){
            CommandModel loginError = new CommandModel(username, "*", AUTH_TYPE, AUTH_FAIL, "fail: check your username/password");
            session.getBasicRemote().sendText(JSON.toJSONString(loginError));
            session.close();
        }
        // 检查是否重复用户名
        if(webSocketMap.get(username) != null){
            CommandModel loginError = new CommandModel(username, "*", AUTH_TYPE, AUTH_FAIL, "fail: username useage!!!");
            session.getBasicRemote().sendText(JSON.toJSONString(loginError));
            session.close();
        }else{
            CommandModel loginSuccess = new CommandModel(username, "*", AUTH_TYPE, AUTH_SUCCESS, "success");
            session.getBasicRemote().sendText(JSON.toJSONString(loginSuccess));
            webSocketMap.put(username, session);
        }
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose() {
        webSocketMap.remove(this.username);
        System.out.println(this.username + "退出");
    }

    /**
     * 收到客户端消息后调用的方法
     *
     * @param commandJson 客户端发送过来的消息*/
    @OnMessage
    public void onMessage(String commandJson, Session session) throws IOException {
        // 传输的是字节数据
        if (!commandJson.startsWith("{") && !ControlJsonParse.fileTransMap.isEmpty()) {
//            List<Integer> bytes = JSON.parseArray(commandJson, int.class);
//            HandleListener.sendToClientBytes(ControlJsonParse.fileTransMap.get(username), bytes);
//            // 返回成功提示
//            CommandModel success = new CommandModel(username, ControlJsonParse.fileTransMap.get(username), 2, 4, "upload success");
//            session.getBasicRemote().sendText(JSON.toJSONString(success));
            // 不需要手动删除管道匹配，因为后续会覆盖(map特性)
//            System.out.println("len:" + commandJson.length());
//            System.out.println(commandJson);
            HandleListener.sendToClient(ControlJsonParse.fileTransMap.get(username), commandJson);
            CommandModel success = new CommandModel(username, ControlJsonParse.fileTransMap.get(username), 2, 4, "upload success");
            session.getBasicRemote().sendText(JSON.toJSONString(success));
        }else{
            ControlJsonParse.commandHandler(commandJson, session);
        }
    }

    public static void sendToController(String target, String json){
        try {
            webSocketMap.get(target).getBasicRemote().sendText(json);
        } catch (IOException e) {
            log.warn("sendToController error ==> [\n{}]", e.getMessage());
        }
    }

    /**
     * 通知全部人的信息
     * @param json 原始数据
     */
    public static void sendToAll(String json) {
        for (Session session : webSocketMap.values()) {
            try {
                session.getBasicRemote().sendText(json);
            } catch (IOException e) {
                log.warn("sendToAll error ==> [\n{}]", e.getMessage());
            }
        }
    }

    /**
     * 被控机下线通知
     * @param ip 下线主机ip
     */
    public static void noticeBreak(String ip) {
        CommandModel commandModel = new CommandModel(
            "*",
            "*",
            0,
            4,
            ip
        );
        String json = JSON.toJSONString(commandModel);
        sendToAll(json);
    }

    /**
     * 被控机上线通知
     * @param online 上线主机
     */
    public static void noticeOnline(Online online) {
        CommandModel commandModel = new CommandModel(
            "*",
            "*",
            0,
            3,
            JSON.toJSONString(online)
        );
        String json = JSON.toJSONString(commandModel);
        sendToAll(json);
    }
}
