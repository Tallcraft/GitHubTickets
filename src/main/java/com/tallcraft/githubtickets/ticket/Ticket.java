package com.tallcraft.githubtickets.ticket;

import java.util.Date;
import java.util.LinkedList;
import java.util.UUID;

public class Ticket {

    private static final int maxTitleLength = 50;

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

    /**
     * Transforms ticket data into text for github issue (excluding comments)
     * @return string for github issue body
     */
    public String getIssueBody() {
        return "Player: " + playerName + "\n"
                + "UUID: " + playerUUID + "\n"
                + "Server: " + serverName + "\n"
                + "World: " + worldName + "\n"
                + "Location: " + location + "\n\n"
                + body;
    }

    public String getIssueTitle() {
        String result = playerName + ": " + body;
        // Limit length if needed
        if (result.length() > maxTitleLength) {
            result = result.substring(0, maxTitleLength - 3) + " (...)";
        }
        return result;
    }
}
