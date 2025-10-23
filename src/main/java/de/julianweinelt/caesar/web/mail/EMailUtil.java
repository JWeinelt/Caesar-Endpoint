package de.julianweinelt.caesar.web.mail;

import de.julianweinelt.caesar.CaesarEndpoint;
import de.julianweinelt.caesar.storage.Configuration;
import lombok.extern.slf4j.Slf4j;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Date;
import java.util.Properties;

@Slf4j
public class EMailUtil {
    private final Session session;
    private final Configuration c;

    public EMailUtil(Configuration c) {
        this.c = c;
        session = createSession();
    }

    public static EMailUtil getInstance() {
        return CaesarEndpoint.getInstance().getMailUtil();
    }


    public void sendEmail(String toEmail, String subject, String body) {
        try {
            MimeMessage msg = new MimeMessage(session);
            msg.addHeader("Content-type", "text/HTML; charset=UTF-8");
            msg.addHeader("format", "flowed");
            msg.addHeader("Content-Transfer-Encoding", "8bit");

            msg.setFrom(new InternetAddress(c.getMailConfiguration().getUsername(), c.getMailConfiguration().getSenderName()));
            msg.setSubject(subject, "UTF-8");
            msg.setText(body, "UTF-8");

            msg.setSentDate(new Date());

            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail, false));
            log.info("Message is ready");
            Transport.send(msg);

            log.info("EMail Sent Successfully!");
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    public Session createSession() {
        final String fromEmail = c.getMailConfiguration().getUsername();
        final String password = c.getMailConfiguration().getPassword();

        log.info("TLSEmail Start");
        Properties props = new Properties();
        props.put("mail.smtp.host", c.getMailConfiguration().getSmtpHost());
        props.put("mail.smtp.port", "" + c.getMailConfiguration().getPort());
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        Authenticator auth = new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(fromEmail, password);
            }
        };
        return Session.getInstance(props, auth);

    }
}

