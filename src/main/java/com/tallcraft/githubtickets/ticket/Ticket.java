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

    // Content
    private String body;
    private LinkedList<TicketComment> comments;

    public Ticket() {

    }

    public Ticket(Date timestamp, UUID playerUUID, String playerName, String serverName, String worldName, Location location, String body) {
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

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    @Override
    public String toString() {
        return "Player: " + playerName + "\n"
                + "UUID: " + playerUUID + "\n"
                + "Timestamp: " + timestamp.toString() + "\n"
                + "Server: " + serverName + "\n"
                + "World: " + worldName + "\n"
                + "Location: " + location + "\n\n"
                + body;
    }
}
