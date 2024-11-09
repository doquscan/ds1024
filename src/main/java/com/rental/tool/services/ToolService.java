package com.rental.tool.services;

import com.rental.tool.entities.Tool;
import com.rental.tool.entities.ToolCharge;
import com.rental.tool.exception.ResourceNotFoundException;
import com.rental.tool.repository.ToolChargeRepository;
import com.rental.tool.repository.ToolRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Service class responsible for managing tools and their charges,
 * providing caching support for efficient retrieval.
 */
@Service
public class ToolService {
    private static final Logger logger = LoggerFactory.getLogger(ToolService.class);

    /**
     * Repository for accessing Tool entities in the database.
     */
    @Autowired
    private ToolRepository toolRepository;
    /**
     * Repository for accessing Tool Charge entities in the database.
     */
    @Autowired
    private ToolChargeRepository toolChargeRepository;

    /**
     * Preloads all tools and tool charges into the cache at application startup.
     * This method iterates through all Tool entities in the database,
     * fetching associated ToolCharge entries if they exist and loading both
     * into the cache for quick access during runtime.
     */
    @PostConstruct
    public void preloadCache() {
        try {
            logger.info("Preloading Tool and ToolCharge entities into cache.");
            toolRepository.findAll().forEach(tool -> {
                // Load Tool into cache
                this.loadToolToCache(tool.getToolCode());

                // Load ToolCharge into cache if it exists
                if (toolChargeRepository.existsById(tool.getToolCode())) {
                    this.loadToolChargeToCache(tool.getToolCode());
                }
            });
            logger.info("Tool and ToolCharge entities successfully cached.");
        } catch (Exception e) {
            logger.error("Error during Tool and ToolCharge cache preloading: ", e);
            throw e;
        }
    }
    /**
     * Retrieves a Tool entity by its tool code from the cache. If the Tool is not present in the cache,
     * it fetches it from the database, caches it for future accesses, and returns it.
     *
     * @param toolCode The unique identifier of the tool to retrieve.
     * @return The Tool entity corresponding to the provided tool code.
     * @throws ResourceNotFoundException if no Tool is found for the given tool code.
     */
    @Cacheable("tools")
    public Tool getToolFromCache(String toolCode) {
        return loadToolToCache(toolCode);
    }

    /**
     * Retrieves a ToolCharge entity by its tool code from the cache. If the ToolCharge is not present
     * in the cache, it fetches it from the database, caches it for future accesses, and returns it.
     *
     * @param toolCode The unique identifier of the tool charge (same as the tool code).
     * @return The ToolCharge entity corresponding to the provided tool code.
     * @throws ResourceNotFoundException if no ToolCharge is found for the given tool code.
     */
    @Cacheable("toolCharges")
    public ToolCharge getToolChargeFromCache(String toolCode) {
        return loadToolChargeToCache(toolCode);
    }

    /**
     * Loads a Tool entity directly from the database without relying on the cache.
     * This method is used internally by both `preloadCache` and `@Cacheable` methods
     * to fetch and cache Tool entities.
     *
     * @param toolCode The unique identifier of the tool to load.
     * @return The Tool entity corresponding to the provided tool code.
     * @throws ResourceNotFoundException if no Tool is found for the given tool code.
     */
    private Tool loadToolToCache(String toolCode) {
    try {
        return toolRepository.findById(toolCode)
                .orElseThrow(() -> new ResourceNotFoundException("Tool not found with code: " + toolCode));
    } catch (ResourceNotFoundException ex) {
        // Handle the exception by logging and returning a structured response
        logger.error("Error loading tool to cache: {}", ex.getMessage());
        throw ex; // Re-throw the exception so that it can be handled globally in the controller
    }
    }

    /**
     * Loads a ToolCharge entity directly from the database without relying on the cache.
     * This method is used internally by both `preloadCache` and `@Cacheable` methods
     * to fetch and cache ToolCharge entities.
     *
     * @param toolCode The unique identifier of the tool charge (same as the tool code).
     * @return The ToolCharge entity corresponding to the provided tool code.
     * @throws ResourceNotFoundException if no ToolCharge is found for the given tool code.
     */
    private ToolCharge loadToolChargeToCache(String toolCode) {
        return toolChargeRepository.findById(toolCode)
                .orElseThrow(() -> new ResourceNotFoundException("ToolCharge not found for tool code: " + toolCode));
    }
    // New method to clear all Tool cache entries
    @CacheEvict(value = "tools", allEntries = true)
    public void evictToolCache() {
        logger.info("Tool cache has been cleared.");
    }

    // New method to clear all ToolCharge cache entries
    @CacheEvict(value = "toolCharges", allEntries = true)
    public void evictToolChargeCache() {
        logger.info("ToolCharge cache has been cleared.");
    }

}

