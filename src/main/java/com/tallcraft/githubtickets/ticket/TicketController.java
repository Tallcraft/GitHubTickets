package com.tallcraft.githubtickets.ticket;

import com.tallcraft.githubtickets.TicketNotifier;
import com.tallcraft.githubtickets.github.GitHubController;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Interfaces between Bukkit plugin side and GitHub
 */
public class TicketController {
    private static final TicketNotifier ticketNotifier = TicketNotifier.getInstance();

    private static final GitHubController githubController = GitHubController.getInstance();
    private static TicketController ourInstance = new TicketController();

    // Server name overwrite variable
    private String serverName;


    public static TicketController getInstance() {
        return ourInstance;
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
     *
     * @param ticket Ticket Object
     * @return Ticket ID
     */
    private int createTicket(Ticket ticket) throws IOException {
        ticket = githubController.createTicket(ticket);
        ticketNotifier.onNewTicket(ticket);
        return ticket.getId();
    }

    /**
     * Create Ticket
     *
     * @param player    Name of player who created the ticket
     * @param timestamp Time of ticket creation
     * @param message   Ticket message
     * @return Ticket ID
     */
    public int createTicket(Player player, Date timestamp, String message) throws IOException {
        org.bukkit.Location l = player.getLocation();
        Location playerLocation = new Location(l.getBlockX(), l.getBlockY(), l.getBlockZ());

        return createTicket(timestamp, player.getUniqueId(), player.getName(), Bukkit.getServer().getName(), player.getWorld().getName(), playerLocation, message);
    }

    /**
     * Create Ticket
     *
     * @param timestamp  Time of ticket creation
     * @param playerUUID Unique identifier of player who created ticket
     * @param playerName Name of player who created the ticket
     * @param serverName Name of server where ticket was created
     * @param worldName  Name of world ticket was created in
     * @param location   Location ticket was created in
     * @param body       Ticket text
     * @return Ticket ID
     */
    private int createTicket(Date timestamp, UUID playerUUID, String playerName, String serverName, String worldName, Location location, String body) throws IOException {
        // If server name is set in ticket controller overwrite server getter
        String serverNameOverride = this.serverName == null ? serverName : this.serverName;
        Ticket ticket = new Ticket(timestamp, playerUUID, ChatColor.stripColor(playerName), serverNameOverride, worldName, location, body);
        return createTicket(ticket);
    }

    public Ticket replyTicket(int id, Player player, String message) throws IOException {
        TicketComment comment = new TicketComment(null, player.getUniqueId(), ChatColor.stripColor(player.getDisplayName()), message);
        Ticket ticket = githubController.addTicketComment(id, comment);
        if (ticket != null) {
            ticketNotifier.onTicketComment(ticket, comment);
        }
        return ticket;
    }

    /**
     * Change ticket status
     *
     * @param id    Ticket ID
     * @param open  true = open, false = closed
     * @param actor UUID of player who changed the status, can be null if actor is not a player.
     * @return ticket object modified, or null if not found
     */
    public Ticket changeTicketStatus(int id, boolean open, UUID actor) throws IOException {
        Ticket ticket = githubController.changeTicketStatus(id, open);
        if (ticket != null) {
            ticketNotifier.onTicketStatusChange(ticket, actor);
        }
        return ticket;
    }


    public Ticket getTicket(int id) throws IOException {
        return githubController.getTicket(id);
    }

    /**
     * Get a list of open tickets sorted by id *without* comments
     *
     * @return List of open tickets
     */
    public List<Ticket> getOpenTickets() throws IOException {
        return getTickets(true, true, true, null, false);
    }

    public List<Ticket> getTickets(boolean sorted, boolean filterStatus,
                                   boolean status, UUID filterPlayerUUID, boolean includeComments) throws IOException {
        List<Ticket> tickets;
        if (filterStatus) {
            tickets = githubController.getTickets(status, includeComments);
        } else {
            tickets = githubController.getTickets(includeComments);
        }
        if (!sorted && filterPlayerUUID == null) {
            return tickets;
        }
        Stream<Ticket> ticketStream = tickets.stream();
        if (sorted) {
            ticketStream = ticketStream.sorted(Comparator.comparing(Ticket::getId).reversed());
        }
        if (filterPlayerUUID != null) {
            ticketStream = ticketStream.filter((ticket) -> ticket.getPlayerUUID()
                    .equals(filterPlayerUUID));
        }
        return ticketStream.collect(Collectors.toCollection(LinkedList::new));
    }
}
