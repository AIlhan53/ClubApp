package com.example.clubappv1.services;

import com.example.clubappv1.models.Tournaments;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

/**
 * Service for sending account activation emails.
 * Uses MailHog for development/testing.
 *
 * @author Club App team
 */
@Service
public class MailService {

    private final JavaMailSender mailSender;

    @Value("${app.frontend.base-url:https://localhost:4200}")
    private String frontendUrl;

    @Value("${spring.mail.username:noreply@clubapp.com}")
    private String fromEmail;

    public MailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * Sends an account activation email with activation link.
     *
     * @param token Account activation token
     */
    public void sendActivationEmail(
            String email,
            String FirstName,
            String token
    ) {
        String activationLink = frontendUrl + "/auth/activate?token=" + token;

        String subject = "ClubApp - Account Activation Required";

        String htmlContent = buildActivationEmailHtml(FirstName, activationLink);

        try {
            sendHtmlEmail(email, subject, htmlContent);
        } catch (MessagingException e) {
            // Fallback to simple email if HTML fails
            sendSimpleEmail(
                    email,
                    subject,
                    buildActivationEmailText(FirstName, activationLink)
            );
        }
    }

    /**
     * Sends a tournament invitation email.
     */
    public void sendInvitationEmail(String email, String memberFirstName, Tournaments tournament, String invitationToken) {

        String acceptLink = frontendUrl + "/tournaments/invitation/" + tournament.getId() + "/accept?token=" + invitationToken;
        String declineLink = frontendUrl + "/tournaments/invitation/" + tournament.getId() + "/decline?token=" + invitationToken;

        String subject = "New tournament invitation - " + tournament.getTournamentName();

        String htmlContent = buildTournamentInvitationEmailHtml(
                memberFirstName,
                tournament,
                acceptLink,
                declineLink
        );

        try {
            sendHtmlEmail(email, subject, htmlContent);
        } catch (MessagingException e) {
            sendSimpleEmail(
                    email,
                    subject,
                    buildTournamentInvitationEmailText(
                            memberFirstName,
                            tournament,
                            acceptLink,
                            declineLink)
            );
        }
    }


    /**
     * Sends a simple text email.
     */
    public void sendSimpleEmail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        mailSender.send(message);
    }

    /**
     * Sends an HTML email.
     */
    public void sendHtmlEmail(String to, String subject, String htmlContent) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);

        mailSender.send(message);
    }

    /**
     * Builds the HTML content for account activation email.
     */
    private String buildActivationEmailHtml(String firstName, String activationLink) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #f4f4f4; margin: 0; padding: 20px; }
                    .container { max-width: 600px; margin: 0 auto; background: white; border-radius: 16px; overflow: hidden; box-shadow: 0 4px 20px rgba(0,0,0,0.1); }
                    .header { background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); padding: 40px 20px; text-align: center; }
                    .header h1 { color: white; margin: 0; font-size: 28px; }
                    .content { padding: 40px 30px; }
                    .content h2 { color: #333; margin-top: 0; }
                    .content p { color: #666; line-height: 1.6; }
                    .button { display: inline-block; background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); color: white !important; text-decoration: none; padding: 15px 40px; border-radius: 8px; font-weight: bold; margin: 20px 0; }
                    .button:hover { opacity: 0.9; }
                    .footer { background: #f9f9f9; padding: 20px; text-align: center; color: #999; font-size: 12px; }
                    .warning { background: #fff3cd; border: 1px solid #ffc107; padding: 15px; border-radius: 8px; margin-top: 20px; }
                    .warning p { color: #856404; margin: 0; font-size: 14px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>ClubApp</h1>
                    </div>
                    <div class="content">
                        <h2>Hello %s,</h2>
                        <p>Thank you for registering with ClubApp!</p>
                        <p>Your account needs to be activated before you can start using it.</p>
                        <p>Click the button below to activate your account:</p>
                        <center>
                            <a href="%s" class="button">Activate My Account</a>
                        </center>
                        <div class="warning">
                            <p>⚠️ This link will expire in <strong>24 hours</strong>.</p>
                            <p>If you didn't create this account, you can safely ignore this email.</p>
                        </div>
                        <p style="margin-top: 30px; font-size: 14px; color: #999;">
                            If the button doesn't work, copy and paste this link into your browser:<br>
                            <a href="%s" style="color: #667eea;">%s</a>
                        </p>
                    </div>
                    <div class="footer">
                        <p>&copy; 2025 ClubApp. All rights reserved.</p>
                        <p>This is an automated message, please do not reply.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(firstName, activationLink, activationLink, activationLink);
    }

    /**
     * Builds the plain text content for activation email (fallback).
     */
    private String buildActivationEmailText(String firstName, String activationLink) {
        return """
            Hello %s,
            
            Thank you for registering with ClubApp!
            
            Your account needs to be activated before you can start using it.
            
            Click the link below to activate your account:
            %s
            
            This link will expire in 24 hours.
            
            If you didn't create this account, you can safely ignore this email.
            
            Best regards,
            The ClubApp Team
            """.formatted(firstName, activationLink);
    }

    /**
     * Builds the HTML content for invitation email.
     */
    private String buildTournamentInvitationEmailHtml(String firstName, Tournaments tournament,
                                                      String acceptLink, String declineLink) {
        return """
                <html>
                <body>
                    <h2>Hello %s,</h2>
                    <p>You have been invited to participate in a tournament.</p>
                    <h3>%s</h3>
                    <p>%s</p>
                    <p>Date: %s</p>
                    <center>
                        <a href="%s" class="button">Accept invitation !</a>
                    </center>
                    <center>
                        <a href="%s" class="button">Refuse invitation !</a>
                    </center>
                </body>
                </html>
                """.formatted(firstName,
                tournament.getTournamentName(),
                tournament.getTournamentDescription(),
                tournament.getTournamentDate(),
                acceptLink,
                declineLink);
    }

    /**
     * Builds the plain text content for invitation email (fallback).
     */
    private String buildTournamentInvitationEmailText(String firstName, Tournaments tournament,
                                                      String acceptLink, String declineLink)
    {
        return """
            Hello %s,

            You have been invited to participate in a tournament.

            %s
            %s
            Date: %s

            Accept invitation: %s
            Refuse invitation: %s
            """.formatted(firstName,
                tournament.getTournamentName(),
                tournament.getTournamentDescription(),
                tournament.getTournamentDate(),
                acceptLink,
                declineLink);
    }
}
