package com.tallcraft.githubtickets.ticket;

import com.tallcraft.githubtickets.github.GitHubController;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Interfaces between Bukkit plugin side and GitHub
 */
public class TicketController {
    private static TicketController ourInstance = new TicketController();

    public static TicketController getInstance() {
        return ourInstance;
    }

    private static final GitHubController githubController = GitHubController.getInstance();

    private String serverName;

    private TicketController() {
    }

    /**
     * Set server name
     *
     * @param serverName server name use (instead of server name from api)
     */
    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    /**
     * Create Ticket
     * @param ticket Ticket Object
     * @return Ticket ID
     * @throws IOException API error
     */
    public long createTicket(Ticket ticket) throws IOException {
        if (serverName != null) ticket.setServerName(serverName);
        return githubController.createIssue(ticket);
    }

    /**
     * Create Ticket
     * @param player Name of player who created the ticket
     * @param timestamp Time of ticket creation
     * @param message Ticket message
     * @return Ticket ID
     * @throws IOException API error
     */
    public long createTicket(Player player, Date timestamp, String message) throws IOException {
        org.bukkit.Location l = player.getLocation();
        Location playerLocation = new Location(l.getBlockX(), l.getBlockY(), l.getBlockZ());

        return createTicket(timestamp, player.getUniqueId(), player.getName(), Bukkit.getServer().getName(), player.getWorld().getName(), playerLocation, message);
    }

    /**
     * Create Ticket
     * @param timestamp Time of ticket creation
     * @param playerUUID Unique identifier of player who created ticket
     * @param playerName Name of player who created the ticket
     * @param serverName Name of server where ticket was created
     * @param worldName Name of world ticket was created in
     * @param location Location ticket was created in
     * @param body Ticket text
     * @return Ticket ID
     * @throws IOException API error
     */
    public long createTicket(Date timestamp, UUID playerUUID, String playerName, String serverName, String worldName, Location location, String body) throws IOException {
        // If server name is set in ticket controller overwrite server getter
        String serverNameOverride = this.serverName == null ? serverName : this.serverName;
        Ticket ticket = new Ticket(timestamp, playerUUID, playerName, serverNameOverride, worldName, location, body);
        return createTicket(ticket);
    }

    /**
     * Get Ticket by ID
     * @param id ticket id to query for.
     * @return Ticket matching id
     * @throws IOException API error
     */
    public Ticket getTicket(int id) throws IOException {
        return githubController.getTicket(id);
    }

    public List<Ticket> getOpenTickets() throws IOException {
        return githubController.getOpenTickets();
    }
}
