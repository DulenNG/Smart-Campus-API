package com.smartcampus.filter;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;
import java.util.logging.Logger;

/**
 * API Request & Response Logging Filter
 * 
 * Provides observability by logging the HTTP method and URI for every 
 * incoming request, and the final HTTP status code for every outgoing response.
 *
 * Using a filter for logging is superior to manual logger statements in each
 * method because it encapsulates cross-cutting concerns in one place (DRY).
 */
@Provider
public class ApiLoggingFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private static final Logger LOGGER = Logger.getLogger(ApiLoggingFilter.class.getName());

    @Override
    public void filter(ContainerRequestContext requestContext) {
        // Log incoming request
        String method = requestContext.getMethod();
        String uri = requestContext.getUriInfo().getRequestUri().toString();
        
        LOGGER.info(">>> INCOMING REQUEST: " + method + " " + uri);
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
        // Log outgoing response
        int status = responseContext.getStatus();
        
        LOGGER.info("<<< OUTGOING RESPONSE: Status " + status);
    }
}
