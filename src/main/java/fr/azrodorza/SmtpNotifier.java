package fr.azrodorza;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class SmtpNotifier implements Notifier {
    private final MailConfig mailConfig;
    private final Session session;

    public SmtpNotifier(MailConfig mailConfig) {
        this.mailConfig = mailConfig;

        Properties prop = new Properties();
        prop.put("mail.smtp.host", mailConfig.host());
        prop.put("mail.smtp.port", String.valueOf(mailConfig.port()));
        prop.put("mail.smtp.auth", "true");
        prop.put("mail.smtp.starttls.enable", "true");

        this.session = Session.getInstance(prop, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(mailConfig.username(), mailConfig.password());
            }
        });
    }

    @Override
    public void notify(List<Assignment> assignments) {
        List<Player> failedGivers = new ArrayList<>();

        for (Assignment assignment : assignments) {
            try {
                sendEmail(assignment.giver(), assignment.receiver());
                IO.println("Email envoyé à " + assignment.giver().name());
            } catch (MessagingException e) {
                IO.println("Erreur lors de l'envoi à " + assignment.giver().name() + " : " + e.getMessage());
                failedGivers.add(assignment.giver());
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Envoi des mails interrompu", e);
            }
        }

        if (!failedGivers.isEmpty()) {
            throw new NotificationException(failedGivers);
        }
    }

    private void sendEmail(Player giver, Player receiver) throws MessagingException {
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(mailConfig.username()));
        message.setRecipients(
                Message.RecipientType.TO,
                InternetAddress.parse(giver.email())
        );
        message.setSubject("🎄 Secret Santa : Ton tirage !");
        message.setText("Bonjour " + giver.name() + ",\n\n"
                + "Pour ce Secret Santa, tu devras offrir un cadeau à : " + receiver.name() + " !\n\n"
                + "Pour rappel, le budget défini est de " + mailConfig.budget() + "€.\n\n"
                + "Joyeuses fêtes ! 🎅");

        Transport.send(message);
    }
}
