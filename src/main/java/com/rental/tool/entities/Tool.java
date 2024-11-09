package com.rental.tool.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tool")
@Data
@NoArgsConstructor // Generates a default no-argument constructor required by JPA
@AllArgsConstructor // Generates an all-arguments constructor for convenience
@Builder // Enables the builder pattern for constructing objects
public class Tool {

    @Id
    @Column(name = "tool_code", nullable = false)
    private String toolCode;

    @Column(name = "tool_type", nullable = false)
    private String toolType;

    @Column(name = "brand", nullable = false)
    private String brand;

    // One-to-one relationship with ToolCharge using tool_code as primary key
    @OneToOne(mappedBy = "tool", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @ToString.Exclude // Prevents circular reference in toString()
    private ToolCharge toolCharge;

}
