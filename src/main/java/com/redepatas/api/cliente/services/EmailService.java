package com.redepatas.api.cliente.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.activation.DataHandler;
import jakarta.activation.URLDataSource;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMultipart;

import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.net.URL;

@Service
public class EmailService {

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    @Qualifier("agendamentoMailSender")
    private JavaMailSender agendamentoMailSender;

    @Autowired
    @Qualifier("noreplyMailSender")
    private JavaMailSender noreplyMailSender;

    @SuppressWarnings("deprecation")
    public void enviarEmail(String para, String assunto, String corpoHtml, String personal, String from)
            throws MessagingException {

        JavaMailSender sender;
        if ("agendamentos".equalsIgnoreCase(from)) {
            sender = agendamentoMailSender;
        } else if ("noreply".equalsIgnoreCase(from)) {
            sender = noreplyMailSender;
        } else {
            throw new IllegalArgumentException("Remetente inválido: " + from);
        }

        jakarta.mail.internet.MimeMessage mensagem = sender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mensagem, true);

        helper.setTo(para);
        helper.setSubject(assunto);
        try {
            helper.setFrom(from + "@redepatas.com.br", personal);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        helper.setText(corpoHtml, true);

        try {
            URL urlImagem = new URL("https://redepatasbucket.s3.sa-east-1.amazonaws.com/public/RedePatasLogo.png");
            URLDataSource fds = new URLDataSource(urlImagem);

            MimeBodyPart imagePart = new MimeBodyPart();
            imagePart.setDataHandler(new DataHandler(fds));
            imagePart.setHeader("Content-ID", "<imagemID>");
            imagePart.setFileName("imagem.jpg");

            MimeMultipart multipart = new MimeMultipart();
            multipart.addBodyPart(imagePart);
            multipart.addBodyPart(new MimeBodyPart() {
                {
                    setContent(corpoHtml, "text/html");
                }
            });

            mensagem.setContent(multipart);

            sender.send(mensagem);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Async
    public void enviarAgendamento(String para, String nomeParceiro, String nomePet, LocalDate dataAgendamento,
            String intervaloAgendamento,
            String idAgendamento) throws MessagingException {
        Context context = new Context();
        context.setVariable("nomeParceiro", nomeParceiro);
        context.setVariable("nomePet", nomePet);
        context.setVariable("dataAgendamento", dataAgendamento.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        context.setVariable("horarioAgendamento", intervaloAgendamento);
        context.setVariable("idAgendamento", idAgendamento);

        String corpoHtml = templateEngine.process("agendamentoTemplate", context);

        try (FileWriter writer = new FileWriter("email-debug.html")) {
            writer.write(corpoHtml);
        } catch (IOException e) {
            e.printStackTrace();
        }
        enviarEmail(para, "CONFIRME AGENDAMENTO", corpoHtml, "NOVO AGENDAMENTO", "agendamentos");
    }

    @Async
    public void enviarConfirmacao(String para, String nomeUsuario, String linkConfirmacao) throws MessagingException {
        Context context = new Context();
        context.setVariable("nomeUsuario", nomeUsuario);
        context.setVariable("linkConfirmacao", linkConfirmacao);

        String corpoHtml = templateEngine.process("confirmacaoTemplate", context);

        enviarEmail(para, "Confirme seu cadastro", corpoHtml, "Rede Patas", "noreply");
    }

    @Async
    public void enviarRecovery(String para, String token, boolean parceiro) throws MessagingException {
        String baseUrl = parceiro ? "https://parceiro.redepatas.com/" : "https://cliente.redepatas.com/";
        Context context = new Context();
        context.setVariable("linkRecuperacao", baseUrl + "newPassword/" + token);
        String corpoHtml = templateEngine.process("recovery-password", context);
        enviarEmail(para, "Redefinição de Senha", corpoHtml, "Rede Patas", "noreply");
    }
}

