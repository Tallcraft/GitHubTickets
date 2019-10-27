package com.tallcraft.githubtickets.ticket;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;

import java.text.DateFormat;
import java.util.*;

public class Ticket {

    // Variable passed to append to reset formatting / events
    private static final ComponentBuilder.FormatRetention f = ComponentBuilder.FormatRetention.NONE;
    private static final int ticketListTextLength = 60;
    // format locale and color settings
    static Locale locale = new Locale("en", "US");
    static DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, locale);
    static ChatColor chatKeyColor = ChatColor.GOLD;
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
        setId(id);
        setOpen(isOpen);
        setTimestamp(timestamp);
        setPlayerUUID(playerUUID);
        setPlayerName(playerName);
        setServerName(serverName);
        setWorldName(worldName);
        setLocation(location);
        setBody(body);

        this.comments = new LinkedList<>();
    }

    /**
     * Convert list of tickets to formatted message for MC chat
     *
     * @param tickets list of tickets
     * @return BaseComponent holding ticket list as mc chat message
     */
    public static BaseComponent[] ticketListToChat(List<Ticket> tickets) {
        ComponentBuilder builder = new ComponentBuilder("");

        for (Ticket ticket : tickets) {

            // Hover text: Playername and ticket body

            HoverEvent ticketHover = ticket.getTicketListHoverEvent();
            ClickEvent ticketClick = ticket.getTicketListClickEvent();

            // Ticket ID
            builder.append(Integer.toString(ticket.getId()), f)
                    .bold(true).color(chatKeyColor)
                    .event(ticketHover)
                    .event(ticketClick);

            // Ticket body
            // Limit ticket body for list view
            String ticketBody = ticket.getBody();
            String ticketPlayer = ticket.getPlayerName();

            if (ticketBody == null) ticketBody = "INVALID";
            if (ticketPlayer == null) ticketPlayer = "INVALID";

            // Calculate length of list entry, constant is extra space
            int entryLength = ticketBody.length() + ticketPlayer.length() + Integer.toString(ticket.getId()).length() + 1;
            int delta = ticketListTextLength - entryLength;

            // We have to cut because entryLength is too long
            if (delta < 0) {
                int lastIndex = ticketBody.length() + delta - 4;
                if (lastIndex < 0) lastIndex = 0;
                ticketBody = ticketBody.substring(0, lastIndex) + " ...";
            }
            builder.append(" " + ticketPlayer + ": ", f).color(chatKeyColor).event(ticketHover).event(ticketClick);
            builder.append(ticketBody, f).event(ticketHover).event(ticketClick).append("\n");
        }
        return builder.create();
    }

    /**
     * Generate minecraft chat hover event for ticket list
     *
     * @return event for ticket mouse hover
     */
    private HoverEvent getTicketListHoverEvent() {
        ComponentBuilder ticketHoverText = new ComponentBuilder("");
        ticketHoverText.append("Ticket #" + id + "\n").color(chatKeyColor)
                .append(playerName, f).bold(true).append("\n")
                .append(body, f);
        int commentCount = comments.size();
        if (commentCount > 0) {
            ticketHoverText.append("\n\nComments: ", f).color(chatKeyColor)
                    .append(Integer.toString(commentCount), f);
        }
        ticketHoverText.append("\n\nClick to show details", f).color(ChatColor.DARK_PURPLE);
        return new HoverEvent(HoverEvent.Action.SHOW_TEXT, ticketHoverText.create());
    }

    /**
     * Generate minecraft chat click event for ticket list
     *
     * @return event for ticket click
     */
    private ClickEvent getTicketListClickEvent() {
        return new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/ticket show " + id);
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        if (serverName == null) throw new IllegalArgumentException("serverName must not be null");
        this.serverName = serverName;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        if (timestamp == null) throw new IllegalArgumentException("timestamp must not be null");
        this.timestamp = timestamp;
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public void setPlayerUUID(UUID playerUUID) {
        if (playerUUID == null) throw new IllegalArgumentException("playerUUID must not be null");
        this.playerUUID = playerUUID;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        if (playerName == null) throw new IllegalArgumentException("playerName must not be null");
        this.playerName = playerName;
    }

    public String getWorldName() {
        return worldName;
    }

    public void setWorldName(String worldName) {
        if (worldName == null) throw new IllegalArgumentException("worldName must not be null");
        this.worldName = worldName;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        if (location == null) throw new IllegalArgumentException("location must not be null");
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
        if (body == null) throw new IllegalArgumentException("body must not be null");
        this.body = body;
    }

    public LinkedList<TicketComment> getComments() {
        return comments;
    }

    public void setComments(LinkedList<TicketComment> comments) {
        this.comments = comments;
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

    public BaseComponent[] toChat() {
        return toChat(true);
    }

    /**
     * Conver Ticket to BaseComponent used for Minecraft chat output
     *
     * @return BaseComponent representing ticket
     */
    public BaseComponent[] toChat(boolean includeComments) {
        // Initialize main ticket component builder
        ComponentBuilder builder = new ComponentBuilder("");

        builder.bold(true).color(chatKeyColor).append("Ticket #" + id).append(" >>>>>>").append("\n");

        // Hover text of player nametag
        ComponentBuilder uuidText = new ComponentBuilder("").append("UUID", f).bold(true).color(chatKeyColor).append("\n").append(playerUUID.toString(), f);
        HoverEvent playerNameHover = new HoverEvent(HoverEvent.Action.SHOW_TEXT, uuidText.create());

        // Location Hover Message & click for teleport
        HoverEvent locationHover = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Teleport to ticket location").create());
        ClickEvent locationClick = new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/ticket tp " + id);

        // Status Hover message & click for status  toggle
        HoverEvent statusHover = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to " + (isOpen ? "CLOSE" : "OPEN")).create());
        ClickEvent statusClick = new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/ticket " + (isOpen ? "close" : "reopen") + " " + id);

        // Metadata
        builder.append("Player: ", f).bold(true).color(chatKeyColor).event(playerNameHover);
        builder.append(playerName, f).event(playerNameHover).append("\n");

        // Status
        ChatColor statusColor = isOpen ? ticketOpenColor : ticketClosedColor;
        builder.append("Status: ", f).bold(true).color(chatKeyColor).event(statusHover).event(statusClick);
        builder.append(isOpenString(), f).color(statusColor).event(statusHover).event(statusClick).append("\n");

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

        // Comments
        if (includeComments) {
            builder.append("\nComments >>>\n", f).bold(true).color(chatKeyColor)
                    .event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND,
                            "/ticket reply " + id + " "))
                    .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                            new ComponentBuilder("Click to reply").create()));
            if(comments.isEmpty()) {
                builder.append("None", f).color(chatKeyColor);
            } else {
                comments.forEach(comment -> {
                    builder.append(comment.toChat(), f);
                    builder.append("\n", f);
                });
            }
        }

        // Build and return as component
        return builder.create();
    }
}
