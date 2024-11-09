package com.rental.tool.controllers;

import com.rental.tool.dto.RentalAgreementDTO;
import com.rental.tool.dto.request.RentalRequest;
import com.rental.tool.dto.response.RentalResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface IRentalController {

    /**
     * Checkout a tool and generate a rental agreement.
     *
     * @param request - RentalRequest containing the tool code, rental days, discount percent, and checkout date.
     * @return RentalAgreementDTO containing details of the rental agreement
     */
    RentalResponse<RentalAgreementDTO> checkoutTool(
            @RequestBody RentalRequest request);
}

