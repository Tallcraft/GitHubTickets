package com.tallcraft.githubtickets.ticket;

import com.tallcraft.githubtickets.GithubTickets;
import com.tallcraft.githubtickets.github.GitHubController;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/**
 * Interfaces between Bukkit plugin side and GitHub
 */
public class TicketController {
    private static final GitHubController githubController = GitHubController.getInstance();
    private static TicketController ourInstance = new TicketController();

    // Server name overwrite variable
    private String serverName;

    // Stores open tickets
    private ConcurrentHashMap<Integer, Ticket> tickets = new ConcurrentHashMap<>();

    // Task that fetches new tickets from github
    private BukkitTask fetchTickets;


    public static TicketController getInstance() {
        return ourInstance;
    }

    /**
     * Initialize TicketController
     * Starts ticket fetcher
     *
     * @param plugin Plugin object to register runnables with
     */
    public void init(GithubTickets plugin) {
        // Only start once
        if (fetchTickets != null) {
            throw new RuntimeException("Controller init was called but already initialized");
        }
        fetchTickets = new TicketFetcher(tickets).runTaskTimerAsynchronously(plugin, 20, 10 * 20);
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
    public Future<Integer> createTicket(Ticket ticket) {
        if (serverName != null) ticket.setServerName(serverName);
        return githubController.createTicket(ticket);
    }

    /**
     * Create Ticket
     *
     * @param player    Name of player who created the ticket
     * @param timestamp Time of ticket creation
     * @param message   Ticket message
     * @return Future with Ticket ID
     */
    public Future<Integer> createTicket(Player player, Date timestamp, String message) {
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
     * @return Future with Ticket ID
     */
    public Future<Integer> createTicket(Date timestamp, UUID playerUUID, String playerName, String serverName, String worldName, Location location, String body) {
        // If server name is set in ticket controller overwrite server getter
        String serverNameOverride = this.serverName == null ? serverName : this.serverName;
        Ticket ticket = new Ticket(timestamp, playerUUID, playerName, serverNameOverride, worldName, location, body);
        return createTicket(ticket);
    }

    /**
     * Open Ticket
     *
     * @param id Ticket ID
     * @return ticket object modified, or null if not found
     */
    public Future<Ticket> openTicket(int id) {
        return changeTicketStatus(id, true);
    }

    /**
     * Close Ticket
     *
     * @param id Ticket ID
     * @return ticket object modified, or null if not found
     */
    public Future<Ticket> closeTicket(int id) {
        return changeTicketStatus(id, false);
    }

    /**
     * Change ticket status
     *
     * @param id   Ticket ID
     * @param open true = open, false = closed
     * @return ticket object modified, or null if not found
     */
    public Future<Ticket> changeTicketStatus(int id, boolean open) {
        // TODO: also wait for future and update ticket in local cache
        return githubController.changeTicketStatus(id, open);
    }

    /**
     * Get Ticket by ID
     *
     * @param id ticket id to query for.
     * @return Ticket matching id
     */
    private Future<Ticket> getTicketNoCache(int id) {
        return githubController.getTicket(id);
    }

    /**
     * Get a list of open tickets
     *
     * @return Future which resolves with ticket list or exception
     */
    private Future<List<Ticket>> getOpenTicketsNoCache() {
        return githubController.getTickets();
    }

    public Ticket getTicket(int id) {
        return tickets.get(id);
    }

    /**
     * Get a list of open tickets from cache
     *
     * @return List of open tickets
     */
    public List<Ticket> getOpenTickets() {
        return tickets.values().stream()
                .filter(Ticket::isOpen)
                .collect(Collectors.toCollection(LinkedList::new));
    }
}
