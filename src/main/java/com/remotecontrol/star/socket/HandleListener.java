package com.remotecontrol.star.socket;

import com.remotecontrol.star.config.ServerConfig;
import com.remotecontrol.star.model.Online;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.*;

/**
 * @author ：复目
 * @date ：Created in 2022/1/8 18:52
 */

@Slf4j
public class HandleListener implements Runnable {

    /**
     * 处理上线主机连接的线程池
     */
    private final ThreadPoolExecutor executor = new ThreadPoolExecutor(
            ServerConfig.corePoolSize,
            ServerConfig.maximumPoolSize,
            ServerConfig.keepAliveTime,
            ServerConfig.unit,
            ServerConfig.workQueue,
            new ThreadPoolExecutor.CallerRunsPolicy());
    /**
     * 存储连接ip+port对应的socket管道
     */
    public static ConcurrentHashMap<String, Socket> online = new ConcurrentHashMap<>();
    /**
     * 存储在线主机的信息
     */
    public static List<Online> onlineList = new ArrayList<>();

    @Override
    public void run() {
        try{
            // 监听端口
            ServerSocket server = new ServerSocket(ServerConfig.PORT);
            // 等待连接
            while(true){
                // 获取连接对象
                Socket socket = server.accept();
                String client = socket.getInetAddress().toString() + ":" + socket.getPort();
                // 线程池开启一个线程处理连接
                executor.execute(new HandleSocketThread(client, socket));
                log.info(client + "发起了一次tcp链接!");
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    /**
     * 发送json数据到客户端
     * @param target 目标ip地址
     * @param json json字符串
     */
    public static void sendToClient(String target, String json){
        try {
            online.get(target).getOutputStream().write(json.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            closeConnect(target);
        }
    }

    /**
     * 发送字节数据到客户端，上传文件
     * @param target 目标ip地址
     * @param data 字节
     */
    public static void sendToClientBytes(String target, List<Integer> data) {
        for(int i = 0; i < data.size(); i++){
            try {
                online.get(target).getOutputStream().write(data.get(i));
            } catch (IOException e) {
                log.warn("trans byte error ==> {}", e.getMessage());
            }
        }
    }


    public static void closeConnect(String target){
        Iterator<Online> iterator = HandleListener.onlineList.iterator();
        while (iterator.hasNext()){
            if(iterator.next().getIp().equals(target)){
                iterator.remove();
                break;
            }
        }
        WebSocketServer.noticeBreak(target);
        log.info(target + "已下线!");
    }
}
