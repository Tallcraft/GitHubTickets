package com.tallcraft.githubtickets.ticket;

import java.util.Date;
import java.util.LinkedList;
import java.util.UUID;

public class Ticket {

    private static int maxTitleLength = 20;

    // Meta
    private Date timestamp;
    private UUID playerUUID;
    private String playerName;
    private String serverName;
    private String worldName;

    // Content
    private String body;
    private LinkedList<TicketComment> comments;

    public Ticket(Date timestamp, UUID playerUUID, String playerName, String serverName, String worldName, String body) {
        this.timestamp = timestamp;
        this.playerUUID = playerUUID;
        this.playerName = playerName;
        this.serverName = serverName;
        this.worldName = worldName;
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
                + "World: " + worldName + "\n\n"
                + body;
    }

    public String getIssueTitle() {
        return playerName + ": " + body.substring(0, maxTitleLength);
    }
}
