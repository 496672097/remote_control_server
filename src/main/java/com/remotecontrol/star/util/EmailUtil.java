package com.remotecontrol.star.util;

import com.remotecontrol.star.config.EmailConfig;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;
import java.io.UnsupportedEncodingException;
import java.util.Properties;

public class EmailUtil {

    public void send(String content) throws UnsupportedEncodingException, javax.mail.MessagingException {
        //初始化默认参数
        Properties props = new Properties();
        props.setProperty("mail.transport.protocol", EmailConfig.transportType);
        props.setProperty("mail.host", EmailConfig.emailHost);
        props.setProperty("mail.user", EmailConfig.fromUser);
        props.setProperty("mail.from", EmailConfig.fromEmail);
        Session session = Session.getInstance(props, null);
        MimeMessage message = new MimeMessage(session);
        String formName = MimeUtility.encodeWord("机器上线通知") + " <" + EmailConfig.fromEmail + ">";
        InternetAddress from = new InternetAddress(formName);
        message.setFrom(from);
        //设置收件人：
        InternetAddress to = new InternetAddress(EmailConfig.toEmail);
        message.setRecipient(Message.RecipientType.TO, to);
        //设置邮件主题
        message.setSubject(EmailConfig.subject);
        //纯文本"text/plain"
        message.setContent(content, "text/plain");
        //保存上面设置的邮件内容
        message.saveChanges();
        //获取Transport对象
        Transport transport = session.getTransport();
        //smtp验证，就是你用来发邮件的邮箱用户名密码（若在之前的properties中指定默认值，这里可以不用再次设置）
        transport.connect(EmailConfig.emailHost, EmailConfig.fromEmail, EmailConfig.authCode);
        //发送邮件
        transport.sendMessage(message, message.getAllRecipients()); // 发送
    }
}