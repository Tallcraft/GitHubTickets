package com.tallcraft.githubtickets.ticket;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;

import java.text.DateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.Locale;
import java.util.UUID;

public class Ticket {

    // format locale and color settings
    private static Locale locale = new Locale("en", "US");
    private static DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, locale);
    private static ChatColor chatKeyColor = ChatColor.GOLD;
    private static ChatColor ticketOpenColor = ChatColor.DARK_GREEN;
    private static ChatColor ticketClosedColor = ChatColor.DARK_RED;

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
        this(-1, true, timestamp, playerUUID, playerName, serverName, worldName, location, body);
    }

    /**
     * Construct ticket
     *
     * @param isOpen     Is ticket open?
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
                + "Status: " + isOpenString() + "\n"
                + "UUID: " + playerUUID + "\n"
                + "Timestamp: " + timestamp.toString() + "\n"
                + "Server: " + serverName + "\n"
                + "World: " + worldName + "\n"
                + "Location: " + location + "\n\n"
                + body;
    }

    /**
     * Get ticket open state
     *
     * @return string representation of ticket open state
     */
    private String isOpenString() {
        return isOpen ? "OPEN" : "CLOSED";
    }

    /**
     * Conver Ticket to BaseComponent used for Minecraft chat output
     *
     * @return BaseComponent representing ticket
     */
    public BaseComponent[] toMCComponent() {
        // Initialize main ticket component builder
        ComponentBuilder builder = new ComponentBuilder("");

        // Variable passed to append to reset formatting / events
        ComponentBuilder.FormatRetention f = ComponentBuilder.FormatRetention.NONE;


        builder.bold(true).color(chatKeyColor).append("Ticket #" + id).append(" >>>>>>").append("\n");

        // Hover text of player nametag
        ComponentBuilder uuidText = new ComponentBuilder("UUID").bold(true).append("\n").append(playerUUID.toString(), f);
        HoverEvent playerNameHover = new HoverEvent(HoverEvent.Action.SHOW_TEXT, uuidText.create());

        // Teleport Click event for location
        HoverEvent locationHover = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Teleport to ticket location").create());
        ClickEvent locationClick = new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/ticket tp " + id);

        // Metadata
        builder.append("Player: ", f).bold(true).color(chatKeyColor).event(playerNameHover);
        builder.append(playerName, f).event(playerNameHover).append("\n");

        // Status
        ChatColor statusColor = isOpen ? ticketOpenColor : ticketClosedColor;
        builder.append("Status: ", f).bold(true).color(chatKeyColor);
        builder.append(isOpenString(), f).color(statusColor).append("\n");

        // Date
        builder.append("Date: ", f).bold(true).color(chatKeyColor);
        builder.append(dateFormat.format(timestamp), f).append("\n");

        // Server
        builder.append("Server: ", f).bold(true).color(chatKeyColor);
        builder.append(serverName, f).append("\n");

        // Location
        builder.append("Location: ", f).bold(true).color(chatKeyColor).event(locationHover).event(locationClick);
        builder.append(worldName + " [" + location + "]", f).event(locationHover).event(locationClick).append("\n");

        // Message
        builder.append("\n" + body, f);

        // Build and return as component
        return builder.create();
    }
}
