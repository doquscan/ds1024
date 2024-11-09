package com.rental.tool.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AuditDTO {

    private Long id;
    private String transactionId;
    private String username;
    private LocalDateTime timestamp;
    private boolean success;
    private String message;

    // Constructors
    public AuditDTO() {}

    public AuditDTO(Long id, String transactionId, String username, LocalDateTime timestamp, boolean success, String message) {
        this.id = id;
        this.transactionId = transactionId;
        this.username = username;
        this.timestamp = timestamp;
        this.success = success;
        this.message = message;
    }

}

