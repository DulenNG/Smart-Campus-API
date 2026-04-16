package com.smartcampus.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Discovery Endpoint — GET /api/v1
 *
 * Provides API metadata and navigation links (HATEOAS).
 * This is the entry point for any client exploring the API.
 *
 * HATEOAS note: By embedding resource links in the response, clients can
 * navigate the entire API from this single root endpoint without needing
 * external documentation. This is a core REST maturity principle (Richardson
 * Maturity Model — Level 3). See README for full explanation.
 */
@Path("/")
public class DiscoveryResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response discover() {

        // Top-level metadata
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("api",         "Smart Campus Sensor & Room Management API");
        response.put("version",     "1.0");
        response.put("description", "RESTful API for managing campus rooms and IoT sensors");
        response.put("contact",     "admin@smartcampus.westminster.ac.uk");
        response.put("module",      "5COSC022W — Client-Server Architectures");

        // HATEOAS links — clients use these to navigate the API
        Map<String, String> resources = new LinkedHashMap<>();
        resources.put("rooms",   "/api/v1/rooms");
        resources.put("sensors", "/api/v1/sensors");
        response.put("resources", resources);

        return Response.ok(response).build();
    }
}
