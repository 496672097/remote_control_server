package com.remotecontrol.star.config;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class ServerConfig {

    // 监听服务端口
    public static final int PORT = 9527;
    // 连接服务器的密码
    public static final String password = "agent";
    // 基本线程数量
    public static final int corePoolSize = 10;
    // 最大线程池数量
    public static final int maximumPoolSize = 20;
    // 保持存活时长
    public static final long keepAliveTime = 1L;
    // 时间单位
    public static final TimeUnit unit = TimeUnit.SECONDS;
    // 队列
    public static final BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<>(100);

}
