package com.tallcraft.githubtickets.ticket;

import java.util.Date;
import java.util.LinkedList;
import java.util.UUID;

public class Ticket {

    // Meta
    private Date timestamp;
    private UUID playerUUID;
    private String playerName;
    private String serverName;
    private String worldName;
    private Location location;

    private int id;
    private boolean isOpen;

    // Content
    private String body;
    private LinkedList<TicketComment> comments;

    public Ticket() {

    }

    /**
     * Construct ticket
     *
     * @param timestamp  Timestamp of ticket creation
     * @param playerUUID Minecraft player UUID (Unique identifier)
     * @param playerName Minecraft player name
     * @param serverName Name of server instance ticket was created in
     * @param worldName  Name of world ticket was created in
     * @param location   Exact location in world of ticket creation
     * @param body       Ticket body text
     */
    public Ticket(Date timestamp, UUID playerUUID, String playerName, String serverName, String worldName, Location location, String body) {
        this(-1, true, timestamp, playerUUID, playerName,serverName, worldName, location, body);
    }

    /**
     * Construct ticket
     * @param isOpen Is ticket open?
     * @param timestamp  Timestamp of ticket creation
     * @param playerUUID Minecraft player UUID (Unique identifier)
     * @param playerName Minecraft player name
     * @param serverName Name of server instance ticket was created in
     * @param worldName  Name of world ticket was created in
     * @param location   Exact location in world of ticket creation
     * @param body       Ticket body text
     */
    public Ticket(int id, boolean isOpen, Date timestamp, UUID playerUUID, String playerName, String serverName, String worldName, Location location, String body) {
        this.id = id;
        this.isOpen = isOpen;
        this.timestamp = timestamp;
        this.playerUUID = playerUUID;
        this.playerName = playerName;
        this.serverName = serverName;
        this.worldName = worldName;
        this.location = location;
        this.body = body;

        this.comments = new LinkedList<>();
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public void setPlayerUUID(UUID playerUUID) {
        this.playerUUID = playerUUID;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public String getWorldName() {
        return worldName;
    }

    public void setWorldName(String worldName) {
        this.worldName = worldName;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public boolean isOpen() {
        return isOpen;
    }

    public void setOpen(boolean open) {
        isOpen = open;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    @Override
    public String toString() {
        return "Player: " + playerName + "\n"
                + "ID: " + id + "\n"
                + "Status: " + (isOpen ? "Open" : "Closed") + "\n"
                + "UUID: " + playerUUID + "\n"
                + "Timestamp: " + timestamp.toString() + "\n"
                + "Server: " + serverName + "\n"
                + "World: " + worldName + "\n"
                + "Location: " + location + "\n\n"
                + body;
    }
}
