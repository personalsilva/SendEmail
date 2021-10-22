/**
 * Control de paquetes general
 */
package com.jbsistemas;

import com.sun.mail.util.MailConnectException;
import java.io.File;
import java.util.*;
import javax.mail.*;
import javax.mail.internet.*;
import javax.activation.*;

/**
 * Esta clase permite enviar correos electr&oacute;nicos mediante el
 * protocolo SMTP. Es requerido especificar una serie de par&aacute;metros de
 * entrada obligatorios ya sea desde l&iacute;nea de comandos, programa externo
 * o de aplicaci&oacute;n especial a trav&eacute;s de servicios como Office365,
 * Exchange, Outlook (Hotmail), Gmail, Yahoo entre otros.
 *
 * <p>
 * Para obtener mayor informaci&oacute;n sobre la configuraci&oacute;n de envio
 * de correo mediante servicios p&uacute;blicos SMTP, consultar la
 * documentaci&oacute;n de cada sitio.</p>
 *
 * <p>
 * SMTP via Gmail</p>
 * <a href="https://umd.service-now.com/itsupport?id=kb_article_view&sysparm_article=KB0015112">
 * Manage Gmail app password</a>
 *
 * <p>
 * SMTP via Yahoo</p>
 * <a href="https://ladedu.com/how-to-create-an-app-password-for-yahoo-pop-and-imap/">
 * Manage Yahoo app password</a>
 *
 * @author David Cruz Jim&eacute;nez
 * @author Daniel Torres Silva
 * @version 2.0
 * @since 1.0
 */
public class SendEmail {

    private static String error;
    private static String serverMailHost;
    private static String serverMailPort;
    private static boolean serverMailIsSecure;
    private static boolean serverMailAuthRequired;
    private static String serverMailUserName;
    private static String serverMailUserEmail;
    private static String serverMailUserPass;
    private static boolean toMe;
    private static String to;
    private static String cc;
    private static String bcc;
    private static String subject;
    private static String attachFile;
    private static String bodyMessage;
    private static String priority;

    /**
     * No se requieren especificar datos del constructor.
     */
    public SendEmail() {
    }

    /**
     * M&eacute;todo main para env&iacute;o de correos. Requiere especificar una
     * serie de par&aacute;metros tipo {@code String} o cadena de caracteres
     * separados por un espacio en blanco.
     *
     * <p>
     * Si alg&uacute;n dato es ingresado de manera incorrecta o el servidor de
     * correo falla en la conexi&oacute;n con el servicio especificado, el
     * programa lanzar&aacute; una excepci&oacute;n de tipo
     * {@code MessagingException} o en su defecto {@code MailConnectException}
     * previo a validaci&oacute;n de sintaxis de los par&aacute;metros que se
     * especifican.</p>
     *
     * @param args Array de {@code String} de entrada de datos
     * @see javax.mail.MessagingException
     * @see com.sun.mail.util.MailConnectException
     * @since 1.0
     */
    public static void main(String[] args) {

        if (validaParametros(args)) {

            Properties properties = new Properties();
            properties.setProperty("mail.smtp.host", serverMailHost);
            properties.setProperty("mail.smtp.port", serverMailPort);
            properties.setProperty("mail.mime.charset", "ISO-8859-1");

            //Establece autenticación de usuario
            if (serverMailAuthRequired) {
                properties.setProperty("mail.smtp.auth", "true");
                properties.setProperty("mail.smtp.user", serverMailUserEmail);
                properties.setProperty("mail.smtp.password", serverMailUserPass);
            } else {
                properties.setProperty("mail.smtp.auth", "false");
            }

            //Establece correo seguro
            if (serverMailIsSecure) {
                properties.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
                properties.setProperty("mail.smtp.starttls.enable", "true");
            }

            try {

                Session session = Session.getInstance(properties);
                MimeMessage message = new MimeMessage(session);

                //Establece remitente
                String from;
                if (!serverMailUserName.isEmpty()) {
                    from = serverMailUserName + "<" + serverMailUserEmail + ">";
                } else {
                    from = serverMailUserEmail;
                }
                message.setFrom(new InternetAddress(from));

                //Agrega al remitente tambien como destinatario
                if (toMe) {
                    to += ";" + serverMailUserEmail;
                }

                //Destinatarios
                for (String _to : to.split(";")) {
                    System.out.println("to: " + _to);
                    message.addRecipient(Message.RecipientType.TO, new InternetAddress(_to));
                }

                //Destinatarios con copia
                for (String _cc : cc.split(";")) {
                    System.out.println("cc: " + _cc);
                    message.addRecipients(Message.RecipientType.CC, _cc);
                }

                //Destinatarios con copia oculta
                for (String _bcc : bcc.split(";")) {
                    System.out.println("bcc: " + _bcc);
                    message.addRecipients(Message.RecipientType.BCC, _bcc);
                }

                //Establece prioridad del mensaje
                message.setHeader("X-Priority", priority);

                //Agrega asunto y cuerpo de mensaje
                message.setSubject(subject);
                BodyPart messageBodyPart = new MimeBodyPart();
                messageBodyPart.setContent(bodyMessage, "text/html; charset=utf-8");
                Multipart multipart = new MimeMultipart();
                multipart.addBodyPart(messageBodyPart);

                //Adjunta archivos
                if (!attachFile.isEmpty()) {
                    for (String attach : attachFile.split(";")) {
                        System.out.println("adjunto: " + attach);
                        messageBodyPart = new MimeBodyPart();
                        DataSource source = new FileDataSource(attach.trim());
                        messageBodyPart.setDataHandler(new DataHandler(source));
                        File attached = new File(attach);
                        messageBodyPart.setFileName(attached.getName());
                        multipart.addBodyPart(messageBodyPart);
                    }
                }

                message.setContent(multipart);
                message.saveChanges();

                //Establece conexión con el servidor
                Transport tr = session.getTransport("smtp");
                tr.connect(serverMailHost, serverMailUserEmail, serverMailUserPass);

                //Envia correo
                tr.sendMessage(message, message.getAllRecipients());
                tr.close();

                Singleton.getInstance().writeToFile("Correo enviado satisfactoriamente.", true);
                System.out.println("Correo enviado satisfactoriamente.");
            } catch (MailConnectException mce) {
                System.out.println("Error: \r\n" + mce.getMessage());
                Singleton.getInstance().writeToFile("[Error]: \r\n" + mce, true);
            } catch (MessagingException mex) {
                System.out.println(mex);
                Singleton.getInstance().writeToFile("[Error]: \r\n" + mex, true);
            }

        } else {
            System.out.println("Error: " + error);
            Singleton.getInstance().writeToFile("[Error]: " + error, true);
        }

    }

    /**
     * Validaci&oacute;n de par&aacute;metros de entrada. Este proceso
     * permite identificar cada uno de los par&aacute;metros ingresados de
     * acuerdo con el orden que se requieren y validar si cumplen con el formato
     * para poder generar y enviar el correo electr&oacute;nico.
     *
     * <p>
     * Esta clase admite el ingreso &uacute;nicamente de 15 posibles
     * par&aacute;metros los cuales deber&aacute;n especificarse estrictamente
     * en el orden que se indica.</p>
     *
     * <p>
     * Los par&aacute;metros marcados con asterisco (*) son requeridos; los que
     * no lo tengan, se puede declarar vac&iacute;o ("") con comillas dobles sin
     * datos si no se requiere especificar dicho par&aacute;metro, como ejemplo
     * un correo sin copia</p>
     *
     * <pre>SendEmail.jar "server.smtp.com" "1" "" "micorreo@dominio.com" "password" "etc. etc."..</pre>
     *
     * <p>
     * Es importante notar que si un par&aacute;metro no requerido no es
     * especificado, de manera obligatoria deber&aacute; respetar la secuencia
     * dejando las comillas dobles vac&iacute;as (""), ya que al no especificar
     * el par&aacute;metro, la secuencia se pierde y solicitar&aacute; ingresar
     * los 15 par&aacute;metros de entrada.</p>
     *
     * <p>
     * El orden de secuencia de los par&aacute;metros es la siguiente</p>
     * <ol><li>parametros[0]* Host Server SMTP (smtp.mail.com)</li>
     * <li>parametros[1]* Requiere autenticar usuario (1 o 0)</li>
     * <li>parametros[2] Nombre de usuario que autentifica (Juan Perez)</li>
     * <li>parametros[3]* Correo de usuario que autentifica
     * (mail@dominio.com)</li>
     * <li>parametros[4]* Password de usuario que autentifica</li>
     * <li>parametros[5] Enviar a mi (1 agrega al remitente como destinatario, 0
     * no enviar)</li>
     * <li>parametros[6]* Correos destinatarios separados por punto y coma
     * (;)</li>
     * <li>parametros[7] Correos con copia separados por punto y coma (;)</li>
     * <li>parametros[8] Correos con copia oculta separados por punto y coma
     * (;)</li>
     * <li>parametros[9]* Asunto del correo</li>
     * <li>parametros[10] Lista de archivos adjuntos separados por punto y coma
     * (;) especificar ruta absoluta (Windows: c:\mailpath\file.pdf)</li>
     * <li>parametros[11]* Cuerpo del mensaje en formato HTML</li>
     * <li>parametros[12]* Correo seguro (1 para habilitar TLS SSL Secure o 0
     * para desactivar)</li>
     * <li>parametros[13]* Puerto de servicio</li>
     * <li>parametros[14] Importancia del correo (1 importante, 3 normal, 5
     * bajo)</li></ol>
     *
     * @param parametros Array de tipo {@code String} el cual se especifica la
     * secuencia de datos a ingresar
     *
     * @return Dato de tipo {@code boolean} el cual indica si la
     * validaci&oacute;n fu&eacute; exitosa o encontr&oacute; un error durante
     * la validaci&oacute;n de los par&aacute;metros
     *
     * @since 2.0
     */
    protected static boolean validaParametros(String[] parametros) {
        boolean ok = false;
        error = "";
        String param = "\r\n\r\nArgumentos:\r\n"
                + "parametros[0] Host Server SMTP (smtp.mail.com)\n"
                + "parametros[1] Requiere autenticar usuario (1 o 0)\n"
                + "parametros[2] Nombre de usuario que autentifica (Juan Perez)\n"
                + "parametros[3] Correo de usuario que autentifica (mail@dominio.com)\n"
                + "parametros[4] Password de usuario que autentifica\n"
                + "parametros[5] Enviar a mi (1 agrega al remitente como\n"
                + "               destinatario, 0 no enviar)\n"
                + "parametros[6] Correos destinatarios separados por punto y coma (;)\n"
                + "parametros[7] Correos con copia separados por punto y coma (;)\n"
                + "parametros[8] Correos con copia oculta separados por punto y coma (;)\n"
                + "parametros[9] Asunto del correo\n"
                + "parametros[10] Lista de archivos adjuntos separados por punto y\n"
                + "               coma (;) especificar ruta absoluta (Windows: c:\\mailpath\\file.pdf)\n"
                + "parametros[11] Cuerpo del mensaje en formato HTML\n"
                + "parametros[12] Correo seguro (1 para habilitar TLS SSL Secure o 0\n"
                + "               para desactivar)\n"
                + "parametros[13] Puerto de servicio\n"
                + "parametros[14] Importancia del correo (1 importante, 3 normal, 5 bajo)\n";
        Singleton.getInstance().writeToFile(new Date() + "", false);
        Singleton.getInstance().writeToFile("Num. parametros recibidos: " + parametros.length, true);

        //Evalua si todos los parametros son especificados
        if (parametros.length != 15) {
            error = "Especificar los argumentos requeridos" + param;
        } else {
            serverMailHost = parametros[0].trim();
            serverMailPort = parametros[13].matches("[0-9]+") ? parametros[13].trim() : "0";
            serverMailIsSecure = parametros[12].equals("1");
            serverMailAuthRequired = parametros[1].equals("1");
            serverMailUserName = parametros[2].trim();
            serverMailUserEmail = parametros[3].trim();
            serverMailUserPass = parametros[4].trim();
            toMe = parametros[5].equals("1");
            to = parametros[6].replace(',', ';').trim();
            cc = parametros[7].replace(',', ';').trim();
            bcc = parametros[8].replace(',', ';').trim();
            subject = parametros[9].isEmpty() ? "Aviso de correo" : parametros[9].trim();
            attachFile = parametros[10].replace(',', ';').trim();
            bodyMessage = parametros[11];
            priority = parametros[14].matches("[0-9]+") ? parametros[14] : "3";
            if (serverMailHost.isEmpty()) {
                error = "Especificar Host de correo";
            } else if (serverMailUserEmail.isEmpty()) {
                error = "Especificar usuario de correo";
            } else if (serverMailUserPass.isEmpty()) {
                error = "Especificar password de usuario";
            } else if (to.isEmpty()) {
                error = "Especificar destinatarios";
            } else if (bodyMessage.isEmpty()) {
                error = "Especificar cuerpo de correo";
            } else {
                ok = true;
            }
        }
        return ok;
    }

}
