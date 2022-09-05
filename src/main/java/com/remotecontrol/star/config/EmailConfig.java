package com.remotecontrol.star.config;

/**
 * 邮箱配置
 */
public class EmailConfig {
    public static final String emailHost = "smtp.qq.com";       //发送邮件的主机
    public static final String transportType = "smtp";           //邮件发送的协议
    public static final String fromUser = "send_name";           //发件人名称
    public static final String fromEmail = "your email@email.com";  //发件人邮箱
    public static final String authCode = "abababaabab";    //发件人邮箱授权码
    public static final String toEmail = "your notice email@email.com";   //收件人邮箱
    public static final String subject = "online notice";           //主题信息

}
