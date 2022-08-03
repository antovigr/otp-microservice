package com.vigery.otpmicroservice.service;

import org.hibernate.service.spi.ServiceException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.mail.*;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

@Service
public class EmailService {

    @Value("${smtp.ip}")
    private String ip;
    @Value("${smtp.port}")
    private String port;
    @Value("${smtp.tls}")
    private Boolean tls;
    @Value("${smtp.user}")
    private String user;
    @Value("${smtp.password}")
    private String password;
    @Value("${smtp.address}")
    private String senderAddress;

    public void sendToken(String receiverAddress, String object, String content) throws AddressException {
        InternetAddress sender= new InternetAddress(senderAddress);
        InternetAddress receiver= new InternetAddress(receiverAddress);

        Properties props = new Properties();

        props.put("mail.smtp.host", ip);
        props.put("mail.smtp.port", port);
        if (tls) {
            props.put("mail.smtp.starttls.enable", "true");
        }

        Session sess;
        if (user != null && password != null) {

            props.put("mail.smtp.auth", "true");
            sess = Session.getInstance(props,
                    new javax.mail.Authenticator() {
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(user, password);
                        }
                    });
        } else {
            sess = Session.getDefaultInstance(props);
        }

        Message msg = new MimeMessage(sess);
        try {
            msg.setFrom(new InternetAddress(senderAddress));
            msg.setRecipients(Message.RecipientType.TO, new InternetAddress[]{sender, receiver});
            msg.setSubject(object);
            msg.setText(content);
            Transport.send(msg);
        } catch (MessagingException e) {
            throw new ServiceException("error", e);
        }
    }
}


