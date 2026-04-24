package com.example.authadmin.service;

import com.example.authadmin.entity.InterviewSlot;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;

import java.io.File;

@Service
public class EmailService {
	private final JavaMailSender mailSender;

	@Value("${app.base-url}")
	private String baseUrl;

	public EmailService(JavaMailSender mailSender) {
		this.mailSender = mailSender;
	}

	public void sendVerificationEmail(String toEmail, String username, String token) {
		String verifyUrl = baseUrl + "/api/users/verify?token=" + token;
		String subject = "Verify your email";
		String text = "Hello " + username + ",\n\n" +
			"Thanks for registering. Please verify your email by clicking the link below:\n" +
			verifyUrl + "\n\n" +
			"If you did not request this, please ignore this email.";
		sendSafe(toEmail, subject, text);
	}

	public void sendInterviewBookedEmail(String toEmail, String username, InterviewSlot slot) {
		String subject = "Interview booked: " + slot.getTitle();
		String text = "Hello " + username + ",\n\n" +
			"Your interview has been booked.\n\n" +
			"Title: " + slot.getTitle() + "\n" +
			"When: " + slot.getScheduledAt() + "\n" +
			"Duration: " + slot.getDurationMinutes() + " minutes\n\n" +
			"If you need to change this booking, please contact the administrator.\n";
		sendSafe(toEmail, subject, text);
	}

	public void sendInterviewCancelledEmail(String toEmail, String username, InterviewSlot slot) {
		String subject = "Interview booking cancelled: " + slot.getTitle();
		String text = "Hello " + username + ",\n\n" +
			"Your interview booking has been cancelled for the slot:\n\n" +
			"Title: " + slot.getTitle() + "\n" +
			"When: " + slot.getScheduledAt() + "\n\n";
		sendSafe(toEmail, subject, text);
	}

	public void sendMeetingStartedEmail(String toEmail, String username) {
		String dashboardUrl = baseUrl + "/user.html";
		String subject = "Meeting Started";
		String text = "Hello " + username + ",\n\n" +
			"A video meeting has been started.\n\n" +
			"Join from your dashboard: " + dashboardUrl + "\n\n" +
			"Click the 'Join Meeting' button when it appears.\n";
		sendSafe(toEmail, subject, text);
	}

	public void sendAdminEmailWithAttachment(String toEmail, String subject, String text, File attachmentFile) {
		try {
			MimeMessage message = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, attachmentFile != null);
			helper.setTo(toEmail);
			helper.setSubject(subject);
			helper.setText(text == null ? "" : text, false);
			if (attachmentFile != null && attachmentFile.exists()) {
				helper.addAttachment(attachmentFile.getName(), attachmentFile);
			}
			mailSender.send(message);
		} catch (Exception ignored) {
			// Don't fail admin flows if SMTP/attachment sending fails in local/dev setups.
		}
	}

	public void sendUserPositionEmail(String toEmail, String username, int position, int total, double finalScore) {
		String subject = "Your Interview Ranking Position";
		String text = "Hello " + username + ",\n\n" +
			"Your current position in the final report is:\n" +
			"Rank: #" + position + " out of " + total + " candidates\n" +
			"Final Score: " + String.format("%.2f", finalScore) + "/100\n\n" +
			"Keep improving your communication, technical, and behavioral scores.\n";
		sendSafe(toEmail, subject, text);
	}

	public void sendUserPositionEmailWithAttachment(String toEmail, String username, int position, int total, double finalScore, File attachmentFile) {
		String subject = "Your Interview Ranking Position";
		String text = "Hello " + username + ",\n\n" +
			"Your current position in the final report is:\n" +
			"Rank: #" + position + " out of " + total + " candidates\n" +
			"Final Score: " + String.format("%.2f", finalScore) + "/100\n\n" +
			"Final report is attached.\n";
		sendAdminEmailWithAttachment(toEmail, subject, text, attachmentFile);
	}

	private void sendSafe(String toEmail, String subject, String text) {
		try {
			SimpleMailMessage message = new SimpleMailMessage();
			message.setTo(toEmail);
			message.setSubject(subject);
			message.setText(text);
			mailSender.send(message);
		} catch (Exception ignored) {
			// In dev or if SMTP is unreachable, don't fail the flow
		}
	}
}

