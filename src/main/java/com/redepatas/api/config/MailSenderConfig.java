package com.redepatas.api.config;

import java.util.Properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

@Configuration
public class MailSenderConfig {

    @Value("${hostingermail.noreply.password}")
    private String noreplyPassword;

    @Value("${hostingermail.agendamento.password}")
    private String agendamentoPassword;

    @Bean(name = "agendamentoMailSender")
    public JavaMailSender agendamentoMailSender() {
        return createMailSender("smtp.hostinger.com", 465,
                "agendamentos@redepatas.com.br", agendamentoPassword);
    }

    @Bean(name = "noreplyMailSender")
    public JavaMailSender noreplyMailSender() {
        return createMailSender("smtp.hostinger.com", 465,
                "noreply@redepatas.com.br", noreplyPassword);
    }

    private JavaMailSender createMailSender(String host, int port, String username, String password) {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(host);
        mailSender.setPort(port);
        mailSender.setUsername(username);
        mailSender.setPassword(password);
        mailSender.setProtocol("smtp");

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.ssl.enable", "true");

        return mailSender;
    }
}
