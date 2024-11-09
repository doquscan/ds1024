package com.rental.tool.controllers;

import com.rental.tool.services.ToolService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cache")
public class CacheController {
    private static final Logger logger = LoggerFactory.getLogger(CacheController.class);

    @Autowired
    private ToolService toolService;

    /**
     * Clears the cache for Tool entities.
     */
    @DeleteMapping("/invalidate/tools")
    public ResponseEntity<String> clearToolCache() {
        toolService.evictToolCache();
        return ResponseEntity.ok("Tool cache has been successfully cleared.");
    }

    /**
     * Clears the cache for ToolCharge entities.
     */
    @DeleteMapping("/invalidate/toolCharges")
    public ResponseEntity<String> clearToolChargeCache() {
        logger.info("Clearing ToolCharge cache...");
        toolService.evictToolChargeCache();
        logger.info("ToolCharge cache has been cleared.");
        return ResponseEntity.ok("ToolCharge cache has been successfully cleared.");
    }

    /**
     * Clears all caches.
     */
    @DeleteMapping("/invalidate/all")
    public ResponseEntity<String> clearAllCaches() {
        toolService.evictToolCache();
        toolService.evictToolChargeCache();
        return ResponseEntity.ok("All caches (Tool and ToolCharge) have been successfully cleared.");
    }
}

