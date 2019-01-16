package com.tallcraft.githubtickets.ticket;

import com.tallcraft.githubtickets.github.GitHubController;
import org.bukkit.Bukkit;
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

    public long createTicket(Ticket ticket) throws IOException {
        return githubControlller.createIssue(ticket);
    }

    public long createTicket(Player player, Date timestamp, String message) throws IOException {
        org.bukkit.Location l = player.getLocation();
        Location playerLocation = new Location(l.getX(), l.getY(), l.getZ());
        return createTicket(timestamp, player.getUniqueId(), player.getName(), Bukkit.getServer().getName(), player.getWorld().getName(), playerLocation, message);
    }

    public long createTicket(Date timestamp, UUID playerUUID, String playerName, String serverName, String worldName, Location location, String body) throws IOException {
        Ticket ticket = new Ticket(timestamp, playerUUID, playerName, serverName, worldName, location, body);
        return createTicket(ticket);
    }
}
