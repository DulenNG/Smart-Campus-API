package com.smartcampus.resource;

import com.smartcampus.data.DataStore;
import com.smartcampus.exception.LinkedResourceNotFoundException;
import com.smartcampus.model.Room;
import com.smartcampus.model.Sensor;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * SensorResource — /api/v1/sensors
 *
 * Handles lifecycle operations and filtering for Sensors.
 */
@Path("/sensors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorResource {

    private final DataStore dataStore = DataStore.getInstance();

    /**
     * GET /api/v1/sensors
     * Supports optional query parameter 'type' for filtering.
     */
    @GET
    public Response getSensors(@QueryParam("type") String type) {
        Collection<Sensor> sensors = dataStore.getSensors().values();
        
        if (type != null && !type.trim().isEmpty()) {
            List<Sensor> filtered = sensors.stream()
                    .filter(s -> s.getType().equalsIgnoreCase(type))
                    .collect(Collectors.toList());
            return Response.ok(filtered).build();
        }

        return Response.ok(new ArrayList<>(sensors)).build();
    }

    /**
     * POST /api/v1/sensors
     * Registers a new sensor and links it to a room.
     */
    @POST
    public Response createSensor(Sensor sensor) {
        if (sensor.getId() == null || sensor.getId().trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Sensor ID is required").build();
        }

        if (sensor.getRoomId() == null || sensor.getRoomId().trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Room ID is required for sensor registration").build();
        }

        // Validate Room existence
        Room room = dataStore.getRoom(sensor.getRoomId());
        if (room == null) {
            throw new LinkedResourceNotFoundException("Cannot register sensor: Room " + 
                    sensor.getRoomId() + " does not exist.");
        }

        if (dataStore.sensorExists(sensor.getId())) {
            return Response.status(Response.Status.CONFLICT)
                    .entity("Sensor already exists").build();
        }

        // Register sensor
        dataStore.addSensor(sensor);
        
        // Link sensor to room (maintain integrity)
        if (!room.getSensorIds().contains(sensor.getId())) {
            room.getSensorIds().add(sensor.getId());
        }

        return Response.status(Response.Status.CREATED).entity(sensor).build();
    }

    /**
     * GET /api/v1/sensors/{sensorId}
     * Returns detailed metadata for a specific sensor.
     */
    @GET
    @Path("/{sensorId}")
    public Response getSensor(@PathParam("sensorId") String sensorId) {
        Sensor sensor = dataStore.getSensor(sensorId);
        if (sensor == null) {
            throw new NotFoundException("Sensor not found: " + sensorId);
        }
        return Response.ok(sensor).build();
    }

    /**
     * DELETE /api/v1/sensors/{sensorId}
     * Removes a sensor and cleans up its reference in the parent Room.
     */
    @DELETE
    @Path("/{sensorId}")
    public Response deleteSensor(@PathParam("sensorId") String sensorId) {
        Sensor sensor = dataStore.getSensor(sensorId);
        if (sensor == null) {
            throw new NotFoundException("Sensor not found: " + sensorId);
        }

        // Cleanup room reference
        Room room = dataStore.getRoom(sensor.getRoomId());
        if (room != null) {
            room.getSensorIds().remove(sensorId);
        }

        dataStore.removeSensor(sensorId);
        return Response.status(Response.Status.NO_CONTENT).build();
    }

    /**
     * Sub-resource locator for historical readings.
     * (Part 4 requirement - will be implemented on Day 4)
     */
    @Path("{sensorId}/readings")
    public Object getReadingsResource(@PathParam("sensorId") String sensorId) {
        // This will return SensorReadingResource once created
        return null; 
    }
}
