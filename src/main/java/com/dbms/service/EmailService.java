package com.dbms.service;

import com.dbms.model.Employee;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;

public class EmailService {
    

    private static final String FROM_EMAIL = "employeesdbms@gmail.com";  
    private static final String APP_PASSWORD = "zdzhfrskwxbheusj";    
    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final String SMTP_PORT = "587";
    
    // Send welcome email to new employee
    public static boolean sendWelcomeEmail(Employee employee) {
        String subject = "Welcome to Our Company! 🎉";
        String body = String.format(
            "Dear %s,\n\n" +
            "Welcome to our company! We're excited to have you join our %s department as %s.\n\n" +
            "Your employee details:\n" +
            "Employee ID: %d\n" +
            "Department: %s\n" +
            "Position: %s\n" +
            "Email: %s\n" +
            "Phone: %s\n" +
            "Start Date: %s\n\n" +
            "We look forward to working with you!\n\n" +
            "Best regards,\n" +
            "HR Team",
            employee.getName(),
            employee.getDepartment(),
            employee.getPosition(),
            employee.getId(),
            employee.getDepartment(),
            employee.getPosition(),
            employee.getEmail(),
            employee.getPhone(),
            employee.getHireDate()
        );
        
        return sendEmail(employee.getEmail(), subject, body);
    }
    
    // Send update notification
    public static boolean sendUpdateNotification(Employee employee) {
        String subject = "Employee Record Updated";
        String body = String.format(
            "Dear %s,\n\n" +
            "Your employee record has been updated in our system.\n\n" +
            "If you have any questions, please contact HR.\n\n" +
            "Best regards,\n" +
            "HR Team",
            employee.getName()
        );
        
        return sendEmail(employee.getEmail(), subject, body);
    }
    
    // Core email sending method
    private static boolean sendEmail(String toEmail, String subject, String body) {
        try {
            // Setup mail server properties
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", SMTP_HOST);
            props.put("mail.smtp.port", SMTP_PORT);
            
            // Create session with authentication
            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(FROM_EMAIL, APP_PASSWORD);
                }
            });
            
            // Create message
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(FROM_EMAIL));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject(subject);
            message.setText(body);
            
            // Send message
            Transport.send(message);
            
            System.out.println("✅ Email sent successfully to: " + toEmail);
            return true;
            
        } catch (MessagingException e) {
            System.err.println("❌ Failed to send email: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
