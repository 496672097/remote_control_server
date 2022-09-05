package com.remotecontrol.star.socket;

import com.alibaba.fastjson.JSON;
import com.remotecontrol.star.model.CommandModel;
import com.remotecontrol.star.util.ClientJsonParse;
import lombok.extern.slf4j.Slf4j;

import javax.mail.MessagingException;
import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * @author ：复目
 * @date ：Created in 2021/11/10 16:05
 */
@Slf4j
class HandleSocketThread implements Runnable {

    private final String client;
    private final Socket socket;
    InputStreamReader inputStreamReader;
    BufferedReader bufferedReader;

    public HandleSocketThread(String client, Socket socket) {
        this.client = client;
        this.socket = socket;
        HandleListener.online.put(client, socket);
    }

    @Override
    public void run() {
        try {
            char[] buf = new char[65536];
            int len;
            // 保证一直可以接收消息
            while (true) {
                // 规定编码
                inputStreamReader = new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8);
                // 接收请求的流
                bufferedReader = new BufferedReader(inputStreamReader);
                while ((len = bufferedReader.read(buf)) != -1) {
                    String string = new String(buf, 0, len);
//                    System.out.println(string);
                    if(string.startsWith("{")) ClientJsonParse.handlerClientJson(client, string);
                    else {
//                        System.out.println(string);
                        if(string.endsWith("}")){
                            string = string.substring(0, string.indexOf("{"));
                            WebSocketServer.sendToController(ClientJsonParse.dataTransMap.get(client), string);
                            CommandModel commandModel = new CommandModel("-", ClientJsonParse.dataTransMap.get(client), 0, 5, "");
                            WebSocketServer.sendToController(ClientJsonParse.dataTransMap.get(client), JSON.toJSONString(commandModel));
                        }else if(string.endsWith("=")){
                            WebSocketServer.sendToController(ClientJsonParse.dataTransMap.get(client), string);
                            CommandModel commandModel = new CommandModel("-", ClientJsonParse.dataTransMap.get(client), 0, 5, "");
                            WebSocketServer.sendToController(ClientJsonParse.dataTransMap.get(client), JSON.toJSONString(commandModel));
                        } else{
                            WebSocketServer.sendToController(ClientJsonParse.dataTransMap.get(client), string);
                        }
//                        System.out.println(string);
                    };
                }
            }
        }catch (IOException e){
            e.printStackTrace();
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}