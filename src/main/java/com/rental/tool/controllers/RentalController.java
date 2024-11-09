package com.rental.tool.controllers;

import com.rental.tool.config.security.SecurityUtils;
import com.rental.tool.dto.RentalAgreementDTO;
import com.rental.tool.dto.request.RentalRequest;
import com.rental.tool.dto.response.RentalResponse;
import com.rental.tool.entities.Rental;
import com.rental.tool.exception.ResourceNotFoundException;
import com.rental.tool.services.AuditService;
import com.rental.tool.services.RentalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
@RestController
@RequestMapping("/api/rentals")
@Tag(name = "Tool Rental Service", description = "API for renting tools")
@Validated
public class RentalController implements IRentalController {

    private static final Logger logger = LoggerFactory.getLogger(RentalController.class);

    @Autowired
    private RentalService rentalService;
    @Autowired
    private AuditService auditService;
    /**
     * Checkout a tool and generate a rental agreement.
     *
     * @param request - RentalRequest containing the tool code, rental days, discount percent, and checkout date.
     * @return RentalAgreementDTO containing details of the rental agreement
     */
    @PostMapping("/checkout")
    @Operation(summary = "Check Out Rental Tool", description = "Returns the rental agreement instance ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Check Out successful",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = RentalAgreementDTO.class))),
            @ApiResponse(responseCode = "400", description = "Bad Input",
                    content = @Content)
    })
    public RentalResponse<RentalAgreementDTO> checkoutTool(@Valid @RequestBody RentalRequest request) {

        // Use the helper method from SecurityUtils to get the current username
        String username = SecurityUtils.getCurrentUsername();
        // Generate a unique transaction ID
        String transactionId = auditService.generateTransactionId();
        try {
           // Extract values from the request object
            String toolCode = request.getToolCode();
            LocalDate checkoutDate = request.getCheckoutDate();

            // Create the rental object (assuming this exists in your service logic)
            Rental rental = rentalService.createRental(toolCode, request.getRentalDays(), request.getDiscountPercent(), checkoutDate);
            logger.info("rental: {}", rental.toString());
            // Process and retrieve the rental agreement
            RentalAgreementDTO rentalAgreement = rentalService.processRentalAgreement(rental, toolCode, checkoutDate, transactionId, username);

            // Return the rental agreement with a 200 OK status
            return new RentalResponse<>(
                    HttpStatus.OK.value(),
                    "Rental checkout successful",
                    rentalAgreement
            );
        } catch (ResourceNotFoundException ex) {
            logger.error("Error during tool checkout: {}", ex.getMessage());
            // Log the error in the audit service
            auditService.logAudit(transactionId, username, false, ex.getMessage() );

            // Log the exception and return a bad request response
            logger.error("Error during tool checkout", ex);
            // Create an error response
            return new RentalResponse<>(
                    HttpStatus.NOT_FOUND.value(),
                    ex.getMessage(),
                    null  // No data for failure case
            );
        } catch (Exception e) {
            logger.error("Error during tool checkout", e);
            auditService.logAudit(transactionId, username, false, "Transaction failed: " + e.getMessage());
            return new RentalResponse<>(HttpStatus.BAD_REQUEST.value(), "Transaction failed: " + e.getMessage(), null);
        }
        }
    }




