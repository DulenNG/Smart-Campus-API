package com.smartcampus;

import org.glassfish.jersey.server.ResourceConfig;

import javax.ws.rs.ApplicationPath;

/**
 * JAX-RS Application entry point.
 *
 * @ApplicationPath sets the base URI for all REST resources: /api/v1
 *
 * ResourceConfig extends javax.ws.rs.core.Application (satisfies coursework requirement)
 * and adds Jersey's package-scanning so every @Path and @Provider class
 * inside com.smartcampus is automatically registered — no manual listing needed.
 *
 * Lifecycle note: By default, JAX-RS creates a new resource class instance
 * per HTTP request (request-scoped). This is why DataStore is a singleton —
 * the in-memory maps must live outside the resource classes to persist data
 * between requests. See README for full discussion.
 */
@ApplicationPath("/api/v1")
public class SmartCampusApplication extends ResourceConfig {

    public SmartCampusApplication() {
        // Scan all classes under com.smartcampus — picks up @Path, @Provider, @PreMatching, etc.
        packages("com.smartcampus");
    }
}
