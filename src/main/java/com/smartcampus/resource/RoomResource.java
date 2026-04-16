package com.smartcampus.resource;

import com.smartcampus.data.DataStore;
import com.smartcampus.exception.RoomNotEmptyException;
import com.smartcampus.model.Room;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Collection;

/**
 * SensorRoom Resource — /api/v1/rooms
 *
 * Handles lifecycle operations for campus Rooms.
 */
@Path("/rooms")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RoomResource {

    private final DataStore dataStore = DataStore.getInstance();

    /**
     * GET /api/v1/rooms
     * Returns a list of all registered rooms.
     */
    @GET
    public Response getAllRooms() {
        Collection<Room> rooms = dataStore.getRooms().values();
        return Response.ok(new ArrayList<>(rooms)).build();
    }

    /**
     * POST /api/v1/rooms
     * Registers a new room in the system.
     */
    @POST
    public Response createRoom(Room room) {
        if (room.getId() == null || room.getId().trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Room ID is required").build();
        }

        if (dataStore.roomExists(room.getId())) {
            return Response.status(Response.Status.CONFLICT)
                    .entity("Room already exists").build();
        }

        dataStore.addRoom(room);
        return Response.status(Response.Status.CREATED).entity(room).build();
    }

    /**
     * GET /api/v1/rooms/{roomId}
     * Returns detailed metadata for a specific room.
     */
    @GET
    @Path("/{roomId}")
    public Response getRoom(@PathParam("roomId") String roomId) {
        Room room = dataStore.getRoom(roomId);
        if (room == null) {
            throw new com.smartcampus.exception.ResourceNotFoundException("Room not found: " + roomId);
        }
        return Response.ok(room).build();
    }

    /**
     * DELETE /api/v1/rooms/{roomId}
     * Decommissions a room if it has no active sensors.
     */
    @DELETE
    @Path("/{roomId}")
    public Response deleteRoom(@PathParam("roomId") String roomId) {
        Room room = dataStore.getRoom(roomId);
        
        if (room == null) {
            throw new com.smartcampus.exception.ResourceNotFoundException("Room not found: " + roomId);
        }

        // Business Logic Constraint: Room cannot be deleted if it has sensors assigned.
        if (!room.getSensorIds().isEmpty()) {
            throw new RoomNotEmptyException("Cannot delete room " + roomId + 
                    " because it still has " + room.getSensorIds().size() + " sensors assigned to it.");
        }

        dataStore.removeRoom(roomId);
        return Response.status(Response.Status.NO_CONTENT).build();
    }
}
