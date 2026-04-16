package com.smartcampus.model;

/**
 * Represents a single historical reading recorded by a Sensor.
 * Readings are immutable data points — they are never updated after creation.
 *
 * A successful POST to /api/v1/sensors/{sensorId}/readings:
 *   1. Appends a new SensorReading to the sensor's history list.
 *   2. Updates sensor.currentValue to reflect the latest measurement.
 */
public class SensorReading {

    /** Unique reading event ID — use UUID.randomUUID().toString() on creation */
    private String id;

    /** Epoch time in milliseconds when the reading was captured */
    private long timestamp;

    /** The actual metric value recorded by the hardware */
    private double value;

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    public SensorReading() {
        // Required for Jackson JSON deserialisation
    }

    public SensorReading(String id, long timestamp, double value) {
        this.id = id;
        this.timestamp = timestamp;
        this.value = value;
    }

    // -------------------------------------------------------------------------
    // Getters & Setters
    // -------------------------------------------------------------------------

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "SensorReading{id='" + id + "', timestamp=" + timestamp + ", value=" + value + "}";
    }
}
