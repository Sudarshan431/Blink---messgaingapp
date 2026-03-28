package com.blink.taskmanager.service.notification;

import com.blink.taskmanager.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailNotificationService implements NotificationService {

    private static final Logger log = LoggerFactory.getLogger(EmailNotificationService.class);

    private final JavaMailSender mailSender;

    public EmailNotificationService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void sendTaskAssignedNotification(Task task) {
        if (task.getAssignee() == null || task.getAssignee().getEmail() == null) {
            return;
        }

        String subject = "Task Assigned: " + task.getTitle();
        String text = "You have been assigned task #" + task.getId() + " with status " + task.getStatus() + ".";
        sendMail(task.getAssignee().getEmail(), subject, text);
    }

    @Override
    public void sendTaskStatusChangedNotification(Task task) {
        if (task.getAssignee() == null || task.getAssignee().getEmail() == null) {
            return;
        }

        String subject = "Task Status Updated: " + task.getTitle();
        String text = "Task #" + task.getId() + " status changed to " + task.getStatus() + ".";
        sendMail(task.getAssignee().getEmail(), subject, text);
    }

    private void sendMail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);

        try {
            mailSender.send(message);
        } catch (MailException ex) {
            log.warn("Failed to send email notification to {}: {}", to, ex.getMessage());
        }
    }
}
