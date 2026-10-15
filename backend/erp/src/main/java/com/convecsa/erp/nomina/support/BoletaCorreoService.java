package com.convecsa.erp.nomina.support;

import jakarta.mail.internet.MimeMessage;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class BoletaCorreoService {

    private final JavaMailSender mailSender;
    private final boolean habilitado;
    private final String remitente;

    public BoletaCorreoService(
            JavaMailSender mailSender,
            @Value("${app.nomina.mail-enabled:false}") boolean habilitado,
            @Value("${app.nomina.mail-from:no-reply@convecsa.pe}") String remitente) {
        this.mailSender = mailSender;
        this.habilitado = habilitado;
        this.remitente = remitente;
    }

    public void enviar(
            String destinatario,
            String trabajador,
            String empresa,
            int anio,
            int mes,
            String filename,
            byte[] pdf) {
        if (!habilitado) {
            throw new IllegalStateException(
                    "El envío de boletas por correo está deshabilitado. Configure SMTP y APP_NOMINA_MAIL_ENABLED=true");
        }
        try {
            String periodo = Month.of(mes).getDisplayName(TextStyle.FULL, Locale.forLanguageTag("es-PE")) + " " + anio;
            MimeMessage mensaje = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mensaje, true, "UTF-8");
            helper.setFrom(remitente);
            helper.setTo(destinatario);
            helper.setSubject("Boleta de pago - " + periodo);
            helper.setText(
                    "Hola " + trabajador + ",\n\nAdjuntamos tu boleta de pago correspondiente a " + periodo
                            + ".\n\nAtentamente,\n" + empresa,
                    false);
            helper.addAttachment(filename, new ByteArrayResource(pdf), "application/pdf");
            mailSender.send(mensaje);
        } catch (IllegalStateException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalStateException("No se pudo enviar el correo: " + ex.getMessage(), ex);
        }
    }
}
