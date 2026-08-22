package said.microgest.utils;

import jakarta.activation.DataHandler;
import jakarta.activation.FileDataSource;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;

import said.microgest.entities.Adherent;
import said.microgest.entities.Epargne;
import said.microgest.entities.Pret;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * Point d'entrée unique pour tout ce qui concerne l'envoi d'emails :
 * configuration SMTP, gabarits de messages, envoi asynchrone.
 */
public class EmailUtil {

    // =========================================================
    // CONFIGURATION
    // =========================================================

    private static final Properties config = new Properties();
    private static boolean configChargee = false;

    private static synchronized void chargerConfig() {

        if (configChargee) {
            return;
        }

        try (InputStream input =
                     EmailUtil.class.getClassLoader()
                             .getResourceAsStream("email.properties")) {

            if (input == null) {
                throw new RuntimeException(
                        "Fichier email.properties introuvable dans les ressources."
                );
            }

            config.load(input);
            configChargee = true;

        } catch (IOException e) {
            throw new RuntimeException("Impossible de charger la configuration email.", e);
        }
    }

    // =========================================================
    // ENVOI ASYNCHRONE — un seul thread dédié, jamais le thread JavaFX
    // =========================================================

    private static final ExecutorService executor =
            Executors.newSingleThreadExecutor(r -> {
                Thread t = new Thread(r, "email-sender");
                t.setDaemon(true);
                return t;
            });

    private static Session creerSession() {

        chargerConfig();

        Properties props = new Properties();

        props.put("mail.smtp.host", config.getProperty("mail.smtp.host"));
        props.put("mail.smtp.port", config.getProperty("mail.smtp.port"));
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        return Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(
                        config.getProperty("mail.smtp.username"),
                        config.getProperty("mail.smtp.password")
                );
            }
        });
    }

    private static void envoyerSync(
            String destinataire,
            String sujet,
            String corpsHtml,
            File pieceJointe
    ) throws MessagingException {

        if (destinataire == null || destinataire.isBlank()) {
            throw new MessagingException("Adresse email du destinataire manquante.");
        }

        chargerConfig();

        MimeMessage message = new MimeMessage(creerSession());

        try {

            message.setFrom(new InternetAddress(
                    config.getProperty("mail.from"),
                    config.getProperty("mail.from.name", "MicroGest")
            ));

        } catch (Exception e) {

            message.setFrom(new InternetAddress(config.getProperty("mail.from")));
        }

        message.setRecipients(
                Message.RecipientType.TO,
                InternetAddress.parse(destinataire)
        );

        message.setSubject(sujet, "UTF-8");

        if (pieceJointe == null) {

            message.setContent(corpsHtml, "text/html; charset=UTF-8");

        } else {

            MimeBodyPart corps = new MimeBodyPart();
            corps.setContent(corpsHtml, "text/html; charset=UTF-8");

            MimeBodyPart attachement = new MimeBodyPart();
            attachement.setDataHandler(new DataHandler(new FileDataSource(pieceJointe)));
            attachement.setFileName(pieceJointe.getName());

            MimeMultipart multipart = new MimeMultipart();
            multipart.addBodyPart(corps);
            multipart.addBodyPart(attachement);

            message.setContent(multipart);
        }

        Transport.send(message);
    }

    public static void envoyerAsync(
            String destinataire,
            String sujet,
            String corpsHtml,
            Runnable onSuccess,
            Consumer<Exception> onError
    ) {

        executor.submit(() -> {

            try {

                envoyerSync(destinataire, sujet, corpsHtml, null);

                if (onSuccess != null) {
                    javafx.application.Platform.runLater(onSuccess);
                }

            } catch (Exception e) {

                System.err.println("Erreur envoi email : " + e.getMessage());

                if (onError != null) {
                    javafx.application.Platform.runLater(() -> onError.accept(e));
                }
            }
        });
    }

    public static void envoyerAsync(String destinataire, String sujet, String corpsHtml) {
        envoyerAsync(destinataire, sujet, corpsHtml, null, null);
    }

    public static void envoyerAvecPieceJointeAsync(
            String destinataire,
            String sujet,
            String corpsHtml,
            File pieceJointe,
            Runnable onSuccess,
            Consumer<Exception> onError
    ) {

        executor.submit(() -> {

            try {

                envoyerSync(destinataire, sujet, corpsHtml, pieceJointe);

                if (onSuccess != null) {
                    javafx.application.Platform.runLater(onSuccess);
                }

            } catch (Exception e) {

                System.err.println("Erreur envoi email : " + e.getMessage());

                if (onError != null) {
                    javafx.application.Platform.runLater(() -> onError.accept(e));
                }
            }
        });
    }

    // =========================================================
    // API METIER — appelée directement par les services
    // =========================================================

    public static void envoyerBienvenue(Adherent adherent) {

        if (adherent.getEmail() == null || adherent.getEmail().isBlank()) {
            return;
        }

        envoyerAsync(
                adherent.getEmail(),
                "Bienvenue chez MicroGest",
                corpsBienvenue(adherent)
        );
    }

    public static void envoyerPretAccorde(Pret pret) {

        Adherent adherent = pret.getAdherent();

        if (adherent == null || adherent.getEmail() == null || adherent.getEmail().isBlank()) {
            return;
        }

        envoyerAsync(
                adherent.getEmail(),
                "Votre prêt a été validé",
                corpsPretAccorde(pret)
        );
    }

    public static void envoyerEcheanceProche(
            Pret pret,
            BigDecimal montantRestant,
            LocalDate dateEcheance
    ) {

        Adherent adherent = pret.getAdherent();

        if (adherent == null || adherent.getEmail() == null || adherent.getEmail().isBlank()) {
            return;
        }

        envoyerAsync(
                adherent.getEmail(),
                "Rappel : échéance de prêt proche",
                corpsEcheanceProche(pret, montantRestant, dateEcheance)
        );
    }

    public static void envoyerReleve(
            Adherent adherent,
            Epargne epargne,
            File pieceJointePdf,
            Runnable onSuccess,
            Consumer<Exception> onError
    ) {

        if (adherent.getEmail() == null || adherent.getEmail().isBlank()) {

            if (onError != null) {
                onError.accept(new RuntimeException("Cet adhérent n'a pas d'adresse email."));
            }

            return;
        }

        envoyerAvecPieceJointeAsync(
                adherent.getEmail(),
                "Votre relevé de compte épargne",
                corpsReleve(adherent, epargne),
                pieceJointePdf,
                onSuccess,
                onError
        );
    }

    // =========================================================
    // GABARITS HTML (privés)
    // =========================================================

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static String corpsBienvenue(Adherent adherent) {

        return enveloppe(
                "Bienvenue " + adherent.getPrenom() + " !",
                """
                <p>Bonjour <strong>%s %s</strong>,</p>
                <p>Votre adhésion à MicroGest a été enregistrée avec succès.</p>
                <table style="width:100%%; border-collapse: collapse; margin: 16px 0;">
                    <tr><td style="padding:6px; color:#7f8c8d;">Numéro d'adhérent</td>
                        <td style="padding:6px; font-weight:bold;">%s</td></tr>
                    <tr><td style="padding:6px; color:#7f8c8d;">Agence</td>
                        <td style="padding:6px; font-weight:bold;">%s</td></tr>
                    <tr><td style="padding:6px; color:#7f8c8d;">Date d'adhésion</td>
                        <td style="padding:6px; font-weight:bold;">%s</td></tr>
                </table>
                <p>Un compte épargne a été automatiquement créé pour vous, avec un solde initial de 0 FCFA.</p>
                <p>Merci de votre confiance.</p>
                """.formatted(
                        adherent.getPrenom(),
                        adherent.getNom().toUpperCase(),
                        adherent.getNumeroAdherent(),
                        adherent.getAgence() != null ? adherent.getAgence().getNom() : "—",
                        adherent.getDateAdhesion() != null
                                ? adherent.getDateAdhesion().format(DATE_FORMAT)
                                : LocalDate.now().format(DATE_FORMAT)
                )
        );
    }

    private static String corpsPretAccorde(Pret pret) {

        Adherent adherent = pret.getAdherent();

        return enveloppe(
                "Prêt validé",
                """
                <p>Bonjour <strong>%s %s</strong>,</p>
                <p>Nous avons le plaisir de vous informer que votre demande de prêt a été
                <strong style="color:#27ae60;">validée</strong>.</p>
                <table style="width:100%%; border-collapse: collapse; margin: 16px 0;">
                    <tr><td style="padding:6px; color:#7f8c8d;">Montant accordé</td>
                        <td style="padding:6px; font-weight:bold;">%,.0f FCFA</td></tr>
                    <tr><td style="padding:6px; color:#7f8c8d;">Taux</td>
                        <td style="padding:6px; font-weight:bold;">%s %%</td></tr>
                    <tr><td style="padding:6px; color:#7f8c8d;">Durée</td>
                        <td style="padding:6px; font-weight:bold;">%d mois</td></tr>
                    <tr><td style="padding:6px; color:#7f8c8d;">Date du prêt</td>
                        <td style="padding:6px; font-weight:bold;">%s</td></tr>
                </table>
                <p>Merci de vous rapprocher de votre agence pour la suite des démarches.</p>
                """.formatted(
                        adherent.getPrenom(),
                        adherent.getNom().toUpperCase(),
                        pret.getMontant(),
                        pret.getTaux().toPlainString(),
                        pret.getDuree(),
                        pret.getDatePret() != null
                                ? pret.getDatePret().format(DATE_FORMAT)
                                : LocalDate.now().format(DATE_FORMAT)
                )
        );
    }

    private static String corpsEcheanceProche(
            Pret pret,
            BigDecimal montantRestant,
            LocalDate dateEcheance
    ) {

        Adherent adherent = pret.getAdherent();

        return enveloppe(
                "Rappel d'échéance",
                """
                <p>Bonjour <strong>%s %s</strong>,</p>
                <p>Nous vous rappelons que l'échéance de votre prêt approche.</p>
                <table style="width:100%%; border-collapse: collapse; margin: 16px 0;">
                    <tr><td style="padding:6px; color:#7f8c8d;">Montant restant dû</td>
                        <td style="padding:6px; font-weight:bold; color:#e67e22;">%,.0f FCFA</td></tr>
                    <tr><td style="padding:6px; color:#7f8c8d;">Date d'échéance prévue</td>
                        <td style="padding:6px; font-weight:bold;">%s</td></tr>
                </table>
                <p>Merci de procéder au remboursement auprès de votre agence dans les meilleurs délais.</p>
                """.formatted(
                        adherent.getPrenom(),
                        adherent.getNom().toUpperCase(),
                        montantRestant,
                        dateEcheance.format(DATE_FORMAT)
                )
        );
    }

    private static String corpsReleve(Adherent adherent, Epargne epargne) {

        return enveloppe(
                "Relevé de compte",
                """
                <p>Bonjour <strong>%s %s</strong>,</p>
                <p>Veuillez trouver ci-joint votre relevé de compte épargne au %s.</p>
                <table style="width:100%%; border-collapse: collapse; margin: 16px 0;">
                    <tr><td style="padding:6px; color:#7f8c8d;">Solde actuel</td>
                        <td style="padding:6px; font-weight:bold; color:#27ae60;">%,.0f FCFA</td></tr>
                </table>
                <p>Le détail de vos opérations figure dans le document joint.</p>
                """.formatted(
                        adherent.getPrenom(),
                        adherent.getNom().toUpperCase(),
                        LocalDate.now().format(DATE_FORMAT),
                        epargne.getSolde()
                )
        );
    }

    private static String enveloppe(String titre, String contenuHtml) {

        return """
                <html>
                <body style="font-family: Arial, sans-serif; color:#2c3e50; background:#ecf0f1; padding:24px;">
                    <div style="max-width:560px; margin:auto; background:white; border-radius:8px;
                                overflow:hidden; box-shadow:0 1px 4px rgba(0,0,0,0.1);">
                        <div style="background:#2c3e50; color:white; padding:18px 24px;">
                            <h2 style="margin:0;">MicroGest</h2>
                        </div>
                        <div style="padding:24px;">
                            <h3 style="margin-top:0;">%s</h3>
                            %s
                        </div>
                        <div style="background:#f5f6f7; color:#95a5a6; font-size:12px; padding:14px 24px;">
                            Cet email a été généré automatiquement par MicroGest. Merci de ne pas y répondre.
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(titre, contenuHtml);
    }
}