package com.smartcampus.resource;

import com.smartcampus.data.DataStore;
import com.smartcampus.exception.ResourceNotFoundException;
import com.smartcampus.exception.SensorUnavailableException;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.UUID;

/**
 * SensorReadingResource — Sub-resource for /api/v1/sensors/{sensorId}/readings
 *
 * Handles historical log of readings for a specific sensor context.
 */
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorReadingResource {

    private final String sensorId;
    private final DataStore dataStore = DataStore.getInstance();

    public SensorReadingResource(String sensorId) {
        this.sensorId = sensorId;
    }

    /**
     * GET /api/v1/sensors/{sensorId}/readings
     * Returns the full history of readings for this sensor.
     */
    @GET
    public Response getReadings() {
        if (!dataStore.sensorExists(sensorId)) {
            throw new ResourceNotFoundException("Sensor not found: " + sensorId);
        }
        
        List<SensorReading> readings = dataStore.getReadingsForSensor(sensorId);
        return Response.ok(readings).build();
    }

    /**
     * POST /api/v1/sensors/{sensorId}/readings
     * Appends a new reading and updates the parent sensor's currentValue.
     */
    @POST
    public Response addReading(SensorReading reading) {
        Sensor sensor = dataStore.getSensor(sensorId);
        if (sensor == null) {
            throw new ResourceNotFoundException("Sensor not found: " + sensorId);
        }

        // Logic check: Cannot accept readings from sensors in MAINTENANCE
        if ("MAINTENANCE".equalsIgnoreCase(sensor.getStatus())) {
            throw new SensorUnavailableException("Sensor " + sensorId + 
                    " is currently under maintenance and cannot accept new readings.");
        }

        // Prepare reading
        if (reading.getId() == null) {
            reading.setId(UUID.randomUUID().toString());
        }
        if (reading.getTimestamp() == 0) {
            reading.setTimestamp(System.currentTimeMillis());
        }

        // 1. Appends reading to history
        dataStore.addReading(sensorId, reading);

        // 2. Side Effect: Update currentValue on the corresponding parent Sensor
        sensor.setCurrentValue(reading.getValue());

        return Response.status(Response.Status.CREATED).entity(reading).build();
    }
}
