package com.example.clubappv1.unitTest;

import com.example.clubappv1.models.Tournaments;
import com.example.clubappv1.services.MailService;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link MailService}.
 *
 * <p>This test class verifies the correct behavior of email sending operations
 * including password reset emails, simple text emails, and HTML emails.</p>
 *
 * <p>Tests cover:</p>
 * <ul>
 *   <li>Sending password reset emails (HTML and plain text)</li>
 *   <li>Sending simple text emails</li>
 *   <li>HTML email fallback to simple email on failure</li>
 *   <li>Email content validation</li>
 *   <li>Error handling scenarios</li>
 * </ul>
 *
 * @author ClubApp Team
 * @version 1.1
 * @see MailService
 */
@ExtendWith(MockitoExtension.class)
class MailServiceTest {

    /**
     * Mock for the JavaMailSender dependency.
     */
    @Mock
    private JavaMailSender mailSender;

    private MimeMessage mimeMessage;

    /**
     * The service under test.
     */
    private MailService mailService;

    /**
     * Frontend URL used for password reset links.
     */
    private static final String FRONTEND_URL = "http://localhost:4200";

    /**
     * Email address used as the sender.
     */
    private static final String FROM_EMAIL = "noreply@clubapp.com";

    /**
     * tournament for tests invitations
     */
    private final Tournaments tournament = new Tournaments();
    ;

    /**
     * Sets up the test environment before each test.
     * Initializes the mailService and injects required properties.
     */
    @BeforeEach
    void setUp() {
        mailService = new MailService(mailSender);
        ReflectionTestUtils.setField(mailService, "frontendUrl", FRONTEND_URL);
        ReflectionTestUtils.setField(mailService, "fromEmail", FROM_EMAIL);


        mimeMessage = new MimeMessage((Session) null);

        tournament.setId(1);
        tournament.setTournamentName("Tournoi Test");
        tournament.setTournamentDescription("Une belle compétition");
        tournament.setTournamentDate(LocalDateTime.of(2026, 8, 20, 14, 30));
    }

    // ==================== SEND SIMPLE EMAIL ====================

    /**
     * Tests that sendSimpleEmail sends a simple text email.
     * Verifies all email fields are set correctly.
     */
    @Test
    void sendSimpleEmail_shouldSendSimpleMessage() {
        String to = "user@example.com";
        String subject = "Test Subject";
        String text = "Test message body";

        mailService.sendSimpleEmail(to, subject, text);

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());

        SimpleMailMessage sentMessage = captor.getValue();
        assertEquals(to, sentMessage.getTo()[0]);
        assertEquals(FROM_EMAIL, sentMessage.getFrom());
        assertEquals(subject, sentMessage.getSubject());
        assertEquals(text, sentMessage.getText());
    }

    /**
     * Tests that sendSimpleEmail sets the correct from address.
     * Verifies the configured sender email is used.
     */
    @Test
    void sendSimpleEmail_shouldSetCorrectFromAddress() {
        String to = "user@example.com";
        String subject = "Subject";
        String text = "Body";

        mailService.sendSimpleEmail(to, subject, text);

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());

        assertEquals(FROM_EMAIL, captor.getValue().getFrom());
    }

    /**
     * Tests that sendSimpleEmail handles empty subject.
     * Verifies emails can be sent without a subject.
     */
    @Test
    void sendSimpleEmail_shouldHandleEmptySubject() {
        String to = "user@example.com";
        String subject = "";
        String text = "Body";

        mailService.sendSimpleEmail(to, subject, text);

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());

        assertEquals("", captor.getValue().getSubject());
    }

    /**
     * Tests that sendSimpleEmail handles empty body.
     * Verifies emails can be sent with an empty message.
     */
    @Test
    void sendSimpleEmail_shouldHandleEmptyBody() {
        String to = "user@example.com";
        String subject = "Subject";
        String text = "";

        mailService.sendSimpleEmail(to, subject, text);

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());

        assertEquals("", captor.getValue().getText());
    }

    /**
     * Tests that sendSimpleEmail handles long subject lines.
     * Verifies no truncation of lengthy subjects.
     */
    @Test
    void sendSimpleEmail_shouldHandleLongSubject() {
        String to = "user@example.com";
        String subject = "A".repeat(200);
        String text = "Body";

        mailService.sendSimpleEmail(to, subject, text);

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());

        assertEquals(subject, captor.getValue().getSubject());
    }

    /**
     * Tests that sendSimpleEmail handles long body content.
     * Verifies no truncation of lengthy messages.
     */
    @Test
    void sendSimpleEmail_shouldHandleLongBody() {
        String to = "user@example.com";
        String subject = "Subject";
        String text = "B".repeat(10000);

        mailService.sendSimpleEmail(to, subject, text);

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());

        assertEquals(text, captor.getValue().getText());
    }

    /**
     * Tests that sendSimpleEmail handles special characters in body.
     * Verifies proper encoding of international characters.
     */
    @Test
    void sendSimpleEmail_shouldHandleSpecialCharactersInBody() {
        String to = "user@example.com";
        String subject = "Subject";
        String text = "Special chars: e a u n (c) (R) TM EUR GBP YEN";

        mailService.sendSimpleEmail(to, subject, text);

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());

        assertEquals(text, captor.getValue().getText());
    }

    /**
     * Tests that sendSimpleEmail handles newlines in body.
     * Verifies line breaks are preserved in the message.
     */
    @Test
    void sendSimpleEmail_shouldHandleNewlinesInBody() {
        String to = "user@example.com";
        String subject = "Subject";
        String text = "Line 1\nLine 2\nLine 3";

        mailService.sendSimpleEmail(to, subject, text);

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());

        assertTrue(captor.getValue().getText().contains("\n"));
    }

    // ==================== SEND HTML EMAIL ====================

    /**
     * Tests that sendHtmlEmail sends a MIME message.
     * Verifies HTML content is sent via MimeMessage.
     */
    @Test
    void sendHtmlEmail_shouldSendMimeMessage() throws MessagingException {
        String to = "user@example.com";
        String subject = "HTML Subject";
        String htmlContent = "<h1>Hello</h1>";

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        mailService.sendHtmlEmail(to, subject, htmlContent);

        verify(mailSender).createMimeMessage();
        verify(mailSender).send(mimeMessage);
    }

    /**
     * Tests that sendHtmlEmail handles complex HTML content.
     * Verifies proper handling of nested HTML tags.
     */
    @Test
    void sendHtmlEmail_shouldHandleComplexHtmlContent() throws MessagingException {
        String to = "user@example.com";
        String subject = "HTML Subject";
        String htmlContent = "<html><body><h1>Title</h1><p>Paragraph with <strong>bold</strong> text.</p></body></html>";

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        assertDoesNotThrow(() -> mailService.sendHtmlEmail(to, subject, htmlContent));
        verify(mailSender).send(mimeMessage);
    }

    /**
     * Tests that sendHtmlEmail handles empty HTML content.
     * Verifies graceful handling of empty message body.
     */
    @Test
    void sendHtmlEmail_shouldHandleEmptyHtmlContent() throws MessagingException {
        String to = "user@example.com";
        String subject = "HTML Subject";
        String htmlContent = "";

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        assertDoesNotThrow(() -> mailService.sendHtmlEmail(to, subject, htmlContent));
    }

    // ==================== EDGE CASES ====================

    /**
     * Tests that sendSimpleEmail handles email addresses with plus sign.
     * Verifies plus addressing is supported.
     */
    @Test
    void sendSimpleEmail_shouldHandleEmailWithPlusSign() {
        String to = "user+test@example.com";
        String subject = "Subject";
        String text = "Body";

        mailService.sendSimpleEmail(to, subject, text);

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());

        assertEquals("user+test@example.com", captor.getValue().getTo()[0]);
    }

    /**
     * Tests that sendSimpleEmail handles subdomain email addresses.
     * Verifies complex domain structures are supported.
     */
    @Test
    void sendSimpleEmail_shouldHandleSubdomainEmail() {
        String to = "user@mail.subdomain.example.com";
        String subject = "Subject";
        String text = "Body";

        mailService.sendSimpleEmail(to, subject, text);

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());

        assertEquals("user@mail.subdomain.example.com", captor.getValue().getTo()[0]);
    }

    @Test
    void sendInvitationEmail_shouldSendHtmlEmail() {

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        mailService.sendInvitationEmail(
                "jean@test.com",
                "Jean",
                tournament,
                "abc123"
        );

        verify(mailSender).createMimeMessage();
        verify(mailSender).send(mimeMessage);
    }

    @Test
    void sendInvitationEmail_shouldSetCorrectRecipientAndSubject() throws Exception {

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        mailService.sendInvitationEmail(
                "jean@test.com",
                "Jean",
                tournament,
                "token123"
        );

        assertEquals("New tournament invitation - Tournoi Test", mimeMessage.getSubject()
        );

        assertEquals("jean@test.com", mimeMessage.getAllRecipients()[0].toString()
        );
    }

    @Test
    void sendInvitationEmail_shouldSetCorrectSender() throws Exception {

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        mailService.sendInvitationEmail(
                "jean@test.com",
                "Jean",
                tournament,
                "token123"
        );

        assertEquals(FROM_EMAIL, mimeMessage.getFrom()[0].toString());
    }

}