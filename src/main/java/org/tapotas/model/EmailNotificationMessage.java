package org.tapotas.model;

public record EmailNotificationMessage(String email, String subject, String content) {
}
