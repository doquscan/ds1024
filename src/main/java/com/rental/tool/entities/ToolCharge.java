package com.rental.tool.entities;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "tool_charge")
@Data
@NoArgsConstructor // Generates a default no-argument constructor required by JPA
@AllArgsConstructor // Generates an all-arguments constructor for convenience
@Builder // Enables the builder pattern for constructing objects
public class ToolCharge {

    @Id
    @Column(name = "tool_charge_id", nullable = false)
    private String toolChargeId;

    @OneToOne
    @JoinColumn(name = "tool_code", referencedColumnName = "tool_code", nullable = false)
    @ToString.Exclude // Prevents circular reference in toString()
    private Tool tool;

    @Column(name = "daily_rental_charge", nullable = false)
    private BigDecimal dailyRentalCharge;

    @Column(name = "weekday_charge", nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    private boolean weekdayCharge;

    @Column(name = "weekend_charge", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean weekendCharge;

    @Column(name = "holiday_charge", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean holidayCharge;

}

