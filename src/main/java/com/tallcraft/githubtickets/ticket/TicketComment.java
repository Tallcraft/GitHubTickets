package com.tallcraft.githubtickets.ticket;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;

import java.util.Date;
import java.util.UUID;

public class TicketComment {
    // Variable passed to append to reset formatting / events
    private static final ComponentBuilder.FormatRetention f = ComponentBuilder.FormatRetention.NONE;
    private Date timestamp;
    private UUID playerUUID;
    private String displayName;
    private String body;

    public TicketComment(Date timestamp, UUID playerUUID, String displayName, String body) {
        setTimestamp(timestamp);
        setPlayerUUID(playerUUID);
        setDisplayName(displayName);
        setBody(body);
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
        // Can be null (github user)
        this.playerUUID = playerUUID;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        if (displayName == null) throw new IllegalArgumentException("displayName must not be null");
        this.displayName = displayName;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        if (body == null) throw new IllegalArgumentException("body must not be null");
        this.body = body;
    }

    /**
     * Convert Ticket comment to BaseComponent used for Minecraft chat output
     *
     * @return BaseComponent representing ticket
     */
    public BaseComponent[] toChat() {
        // Initialize main ticket component builder
        ComponentBuilder builder = new ComponentBuilder("");

        ComponentBuilder commentHoverText = new ComponentBuilder("");

        if (playerUUID != null) {
            commentHoverText.append("UUID: ", f).bold(true).color(Ticket.chatKeyColor);
            commentHoverText.append(playerUUID.toString(), f).append("\n");
        }

        commentHoverText.append("Time: ", f).bold(true).color(Ticket.chatKeyColor);
        commentHoverText.append(Ticket.dateFormat.format(timestamp), f);

        HoverEvent commentHoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, commentHoverText.create());

        builder.append(getDisplayName() + ": ", f).color(Ticket.chatKeyColor).event(commentHoverEvent);
        builder.append(getBody(), f).event(commentHoverEvent);

        return builder.create();
    }
}
