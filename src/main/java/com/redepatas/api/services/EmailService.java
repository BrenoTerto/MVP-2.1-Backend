package com.redepatas.api.services;

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
import java.time.LocalDateTime;
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

        // Escolher o JavaMailSender certo conforme o "from"
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

        // Definir os detalhes do e-mail
        helper.setTo(para);
        helper.setSubject(assunto);
        try {
            helper.setFrom(from + "@redepatas.com.br", personal);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        helper.setText(corpoHtml, true);

        // URL da imagem externa
        try {
            URL urlImagem = new URL("https://redepatasbucket.s3.sa-east-1.amazonaws.com/public/RedePatasLogo.png");
            URLDataSource fds = new URLDataSource(urlImagem);

            // Criar a parte de imagem
            MimeBodyPart imagePart = new MimeBodyPart();
            imagePart.setDataHandler(new DataHandler(fds));
            imagePart.setHeader("Content-ID", "<imagemID>");
            imagePart.setFileName("imagem.jpg");

            // Criar a parte de multipart
            MimeMultipart multipart = new MimeMultipart();
            multipart.addBodyPart(imagePart);
            multipart.addBodyPart(new MimeBodyPart() {
                {
                    setContent(corpoHtml, "text/html");
                }
            });

            mensagem.setContent(multipart);

            // Enviar o e-mail
            sender.send(mensagem);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Async
    public void enviarAgendamento(String para, String nomeParceiro, String nomePet, LocalDateTime dataAgendamento,
            String idAgendamento) throws MessagingException {
        Context context = new Context();
        context.setVariable("nomeParceiro", nomeParceiro);
        context.setVariable("nomePet", nomePet);
        context.setVariable("dataAgendamento", dataAgendamento.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        context.setVariable("idAgendamento", idAgendamento);

        String corpoHtml = templateEngine.process("agendamentoTemplate", context);

        try (FileWriter writer = new FileWriter("email-debug.html")) {
            writer.write(corpoHtml);
            System.out.println("HTML exportado com sucesso para 'email-debug.html'");
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

        try (FileWriter writer = new FileWriter("email-confirmacao-debug.html")) {
            writer.write(corpoHtml);
            System.out.println("HTML de confirmação exportado para 'email-confirmacao-debug.html'");
        } catch (IOException e) {
            e.printStackTrace();
        }

        enviarEmail(para, "Confirme seu cadastro", corpoHtml, "Confirmação cadastral", "noreply");
    }

}
