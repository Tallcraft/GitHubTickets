package com.tallcraft.githubtickets.ticket;

import com.tallcraft.githubtickets.github.GitHubController;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.Date;
import java.util.UUID;

public class TicketController {
    private static TicketController ourInstance = new TicketController();

    public static TicketController getInstance() {
        return ourInstance;
    }

    private static final GitHubController githubControlller = GitHubController.getInstance();

    private TicketController() {
    }

    public long createTicket(Player player, Date timestamp, String message) throws IOException {
        Ticket ticket = new Ticket(timestamp, player.getUniqueId(), player.getName(), "todo", player.getWorld().getName(), message);
        return githubControlller.createIssue(ticket);
    }

    public long createTicket(Date timestamp, UUID playerUUID, String playerName, String serverName, String worldName, String body) throws IOException {
        Ticket ticket = new Ticket(timestamp, playerUUID, playerName, serverName, worldName, body);
        return githubControlller.createIssue(ticket);
    }
}
