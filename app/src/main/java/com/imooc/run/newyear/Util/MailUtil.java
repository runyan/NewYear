package com.imooc.run.newyear.Util;

import java.util.Date;
import java.util.Properties;

import javax.activation.CommandMap;
import javax.activation.MailcapCommandMap;
import javax.mail.BodyPart;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class MailUtil extends javax.mail.Authenticator {

    private String user;
    private String password;

    private String[] to;
    private String from;

    private String port;
    private String sport;

    private String host;

    private String subject;
    private String body;

    private boolean auth;

    private boolean debuggable;

    private Multipart multipart;

    public MailUtil() {
        host = "smtp.gmail.com"; // 默认smtp服务器
        port = "465"; // 默认smtp端口
        sport = "465"; // 默认SocketFactory端口

        user = ""; // 用户名
        password = ""; // 密码
        from = ""; // 邮件发送者
        subject = ""; // 邮件主题
        body = ""; // 邮件主题

        debuggable = false; // 调试模式是否打开 - 默认关闭
        auth = true; // smtp 授权 - 默认已授权

        multipart = new MimeMultipart();

        // There is something wrong with MailCap, JavaMail can not find a handler for the multipart/mixed part, so this bit needs to be added.
        MailcapCommandMap mc = (MailcapCommandMap) CommandMap.getDefaultCommandMap();
        mc.addMailcap("text/html;; x-java-content-handler=com.sun.mail.handlers.text_html");
        mc.addMailcap("text/xml;; x-java-content-handler=com.sun.mail.handlers.text_xml");
        mc.addMailcap("text/plain;; x-java-content-handler=com.sun.mail.handlers.text_plain");
        mc.addMailcap("multipart/*;; x-java-content-handler=com.sun.mail.handlers.multipart_mixed");
        mc.addMailcap("message/rfc822;; x-java-content-handler=com.sun.mail.handlers.message_rfc822");
        CommandMap.setDefaultCommandMap(mc);
    }

    public MailUtil(String user, String pass) {
        this();
        this.user = user;
        password = pass;
    }

    public boolean send() throws Exception {
        Properties props = _setProperties();

        if (!user.equals("") && !password.equals("") && to.length > 0 && !from.equals("") && !subject.equals("") && !body.equals("")) {
            Session session = Session.getInstance(props, this);

            MimeMessage msg = new MimeMessage(session);

            msg.setFrom(new InternetAddress(from));

            InternetAddress[] addressTo = new InternetAddress[to.length];
            for (int i = 0; i < to.length; i++) {
                addressTo[i] = new InternetAddress(to[i]);
            }
            msg.setRecipients(MimeMessage.RecipientType.TO, addressTo);

            msg.setSubject(subject);
            msg.setSentDate(new Date());

            BodyPart messageBodyPart = new MimeBodyPart();// 设置消息主体
            messageBodyPart.setText(body);
            multipart.addBodyPart(messageBodyPart);

            msg.setContent(multipart); // 将内容设置进消息

            Transport.send(msg);// 发送邮件

            return true;
        } else {
            return false;
        }
    }

//    public void addAttachment(String filename) throws Exception {
//        BodyPart messageBodyPart = new MimeBodyPart();
//        DataSource source = new FileDataSource(filename);
//        messageBodyPart.setDataHandler(new DataHandler(source));
//        messageBodyPart.setFileName(filename);
//
//        multipart.addBodyPart(messageBodyPart);
//    }

    @Override
    public PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(user, password);
    }

    private Properties _setProperties() {
        Properties props = new Properties();
        props.put("mail.smtp.host", host);

        if (debuggable) {
            props.put("mail.debug", "true");
        }

        if (auth) {
            props.put("mail.smtp.auth", "true");
        }

        props.put("mail.smtp.port", port);
        props.put("mail.smtp.socketFactory.port", sport);
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.socketFactory.fallback", "false");

        return props;
    }

//    public String getUser() {
//        return user;
//    }
//
//    public void setUser(String user) {
//        this.user = user;
//    }
//
//    public String getPassword() {
//        return password;
//    }
//
//    public void setPassword(String password) {
//        this.password = password;
//    }

    public String[] getTo() {
        return to;
    }

    public void setTo(String[] to) {
        this.to = to;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

//    public String getPort() {
//        return port;
//    }

    public void setPort(String port) {
        this.port = port;
    }

//    public String getSport() {
//        return sport;
//    }

//    public void setSport(String sport) {
//        this.sport = sport;
//    }
//
//    public String getHost() {
//        return host;
//    }

    public void setHost(String host) {
        this.host = host;
    }

//    public String getSubject() {
//        return subject;
//    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

//    public String getBody() {
//        return body;
//    }

    public void setBody(String body) {
        this.body = body;
    }

//    public boolean isAuth() {
//        return auth;
//    }

//    public void setAuth(boolean auth) {
//        this.auth = auth;
//    }

//    public boolean isDebuggable() {
//        return debuggable;
//    }
//
//    public void setDebuggable(boolean debuggable) {
//        this.debuggable = debuggable;
//    }
//
//    public Multipart getMultipart() {
//        return multipart;
//    }
//
//    public void setMultipart(Multipart multipart) {
//        this.multipart = multipart;
//    }
}
