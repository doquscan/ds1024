package com.rental.tool.mapper;


import com.rental.tool.entities.Audit;
import com.rental.tool.dto.AuditDTO;
import org.springframework.stereotype.Component;

@Component
public class AuditMapper {

    // Maps an Audit entity to AuditDTO
    public static AuditDTO toDTO(Audit audit) {
        return new AuditDTO(
                audit.getId(),
                audit.getTransactionId(),
                audit.getUsername(),
                audit.getTimestamp(),
                audit.isSuccess(),
                audit.getMessage()
        );
    }

    // Maps AuditDTO to Audit entity
    public static Audit toEntity(AuditDTO auditDTO) {
        Audit audit = new Audit();
        audit.setTransactionId(auditDTO.getTransactionId());
        audit.setUsername(auditDTO.getUsername());
        audit.setTimestamp(auditDTO.getTimestamp());
        audit.setSuccess(auditDTO.isSuccess());
        audit.setMessage(auditDTO.getMessage());
        return audit;
    }
}

