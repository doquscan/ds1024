package com.rental.tool.services;

import com.rental.tool.dto.AuditDTO;
import com.rental.tool.entities.Audit;
import com.rental.tool.mapper.AuditMapper;
import com.rental.tool.repository.AuditRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AuditService {
    private static final Logger logger = LoggerFactory.getLogger(AuditService.class);

    @Autowired
    private AuditRepository auditRepository;

    // Method to log audit details and map to DTO
    public void logAudit(String transactionId, String username, boolean success, String message) {
        // Create AuditDTO
        AuditDTO auditDTO = new AuditDTO(null, transactionId, username, LocalDateTime.now(), success, message);

        // Convert AuditDTO to Audit entity and save to the database
        Audit audit = AuditMapper.toEntity(auditDTO);
        auditRepository.save(audit);

        // Optionally log the audit details
        String logMessage = String.format("Transaction ID: %s | User: %s | Time: %s | Success: %b | Message: %s",
                auditDTO.getTransactionId(), auditDTO.getUsername(), auditDTO.getTimestamp(), auditDTO.isSuccess(), auditDTO.getMessage());

        if (success) {
            logger.info(logMessage);
        } else {
            logger.error(logMessage);
        }
    }

    // Method to generate a unique transaction ID
    public String generateTransactionId() {
        return UUID.randomUUID().toString();
    }
}
