package com.smartcampus.data;

import com.smartcampus.model.Room;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory data store — the single source of truth for the entire API.
 *
 * DESIGN RATIONALE:
 * -----------------
 * JAX-RS creates a NEW resource class instance for every incoming HTTP request
 * (per-request lifecycle). This means any data stored as an instance field on
 * a resource class would be lost the moment the request completes.
 *
 * To persist data across requests DataStore is implemented as a thread-safe
 * singleton using the "eagerly-initialised static final" pattern. The single
 * instance is created once when the class is loaded by the JVM and shared by
 * all resource instances and all threads for the lifetime of the application.
 *
 * THREAD SAFETY:
 * --------------
 * ConcurrentHashMap is used instead of HashMap to prevent race conditions
 * when multiple requests access or modify the maps simultaneously. This avoids
 * data corruption without the performance penalty of full synchronisation blocks.
 */
public class DataStore {

    // Eagerly initialised singleton — thread-safe without synchronisation
    private static final DataStore INSTANCE = new DataStore();

    /** All campus rooms keyed by room ID */
    private final Map<String, Room> rooms = new ConcurrentHashMap<>();

    /** All sensors keyed by sensor ID */
    private final Map<String, Sensor> sensors = new ConcurrentHashMap<>();

    /** Historical readings keyed by sensor ID → list of readings */
    private final Map<String, List<SensorReading>> readings = new ConcurrentHashMap<>();

    // Private constructor — prevents external instantiation
    private DataStore() {}

    /**
     * Returns the single shared DataStore instance.
     * Called from every resource class method instead of using a field.
     */
    public static DataStore getInstance() {
        return INSTANCE;
    }

    // -------------------------------------------------------------------------
    // Room operations
    // -------------------------------------------------------------------------

    public Map<String, Room> getRooms() {
        return rooms;
    }

    public Room getRoom(String id) {
        return rooms.get(id);
    }

    public void addRoom(Room room) {
        rooms.put(room.getId(), room);
    }

    public boolean roomExists(String id) {
        return rooms.containsKey(id);
    }

    public Room removeRoom(String id) {
        return rooms.remove(id);
    }

    // -------------------------------------------------------------------------
    // Sensor operations
    // -------------------------------------------------------------------------

    public Map<String, Sensor> getSensors() {
        return sensors;
    }

    public Sensor getSensor(String id) {
        return sensors.get(id);
    }

    public void addSensor(Sensor sensor) {
        sensors.put(sensor.getId(), sensor);
    }

    public boolean sensorExists(String id) {
        return sensors.containsKey(id);
    }

    public Sensor removeSensor(String id) {
        return sensors.remove(id);
    }

    // -------------------------------------------------------------------------
    // Reading operations
    // -------------------------------------------------------------------------

    /**
     * Returns the readings list for a sensor, creating an empty list if none exists.
     * Uses computeIfAbsent for thread-safe lazy initialisation.
     */
    public List<SensorReading> getReadingsForSensor(String sensorId) {
        return readings.computeIfAbsent(sensorId,
                k -> Collections.synchronizedList(new ArrayList<>()));
    }

    public void addReading(String sensorId, SensorReading reading) {
        getReadingsForSensor(sensorId).add(reading);
    }
}
