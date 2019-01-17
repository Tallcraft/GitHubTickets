package com.tallcraft.githubtickets.command;

import com.tallcraft.githubtickets.ticket.Ticket;
import com.tallcraft.githubtickets.ticket.TicketController;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.stream.Collectors;

public class TicketCommand implements CommandExecutor {

    private static final TicketController ticketController = TicketController.getInstance();

    // TODO: Permission checks

    /**
     * Executes the given command, returning its success.
     * <br>
     * If false is returned, then the "usage" plugin.yml entry for this command
     * (if defined) will be sent to the player.
     *
     * @param sender  Source of the command
     * @param command Command which was executed
     * @param label   Alias of the command which was used
     * @param args    Passed command arguments
     * @return true if a valid command, otherwise false
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1) {
            return false;
        }
        switch (args[0].toLowerCase()) {
            case "show":
                return showTicket(sender, args);
            case "tp":
                return teleportTicket(sender, args);
            case "create":
                return createTicket(sender, args);
        }
        return false;
    }

    /**
     * Get ticket by id and send it to user
     *
     * @param sender Requesting user
     * @param args   command arguments
     * @return true on valid syntax, false otherwise
     */
    private boolean showTicket(CommandSender sender, String[] args) {
        if (args.length < 2) return false;
        int id;

        // Parse ticket id
        try {
            id = Integer.parseInt(args[1]);
        } catch (NumberFormatException ex) {
            sender.sendMessage("Invalid ticket id!");
            return true;
        }

        // Get ticket from GitHub by id
        try {
            Ticket ticket = ticketController.getTicket(id);
            if (ticket == null) {
                sender.sendMessage("Ticket not found.");
            } else {
                sender.spigot().sendMessage(ticket.toMCComponent());
            }
        } catch (IOException e) {
            sender.sendMessage("Error while getting ticket");
            e.printStackTrace();
        }
        return true;
    }

    /**
     * Teleport player to ticket location
     *
     * @param sender Requesting user
     * @param args   Command arguments
     * @return true on valid syntax, false otherwise
     */
    private boolean teleportTicket(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players may use this command.");
            return false;
        }

        // Cast to player so we can teleport
        Player player = (Player) sender;

        if (args.length < 2) return false;
        int id;

        // Parse ticket id
        try {
            id = Integer.parseInt(args[1]);
        } catch (NumberFormatException ex) {
            sender.sendMessage("Invalid ticket id!");
            return true;
        }

        Ticket ticket;

        // Get ticket from GitHub by id
        try {
            ticket = ticketController.getTicket(id);
            if (ticket == null) {
                sender.sendMessage("Ticket not found.");
                return true;
            }
        } catch (IOException e) {
            sender.sendMessage("Error while getting ticket");
            e.printStackTrace();
            return true;
        }

        World world = Bukkit.getWorld(ticket.getWorldName());
        if (world == null) {
            sender.sendMessage("World " + ticket.getWorldName() + " not found!");
            return true;
        }
        Location location = new Location(
                world,
                ticket.getLocation().getX(),
                ticket.getLocation().getY(),
                ticket.getLocation().getZ());

        sender.sendMessage("Teleporting to ticket..."); // TODO: print ticket id
        player.teleport(location);
        return true;
    }

    /**
     * Create ticket and reply with ticket id
     *
     * @param sender Requesting user
     * @param args   command arguments
     * @return true on valid syntax, false otherwise
     */
    private boolean createTicket(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Console not supported (yet)");
            return false;
        }

        // Join args to form string for ticket message
        String message = Arrays.stream(args).skip(1).collect(Collectors.joining(" "));

        long ticketID;
        try {
            ticketID = ticketController.createTicket((Player) sender, new Date(), message);
        } catch (IOException e) {
            sender.sendMessage("Error: Could not create ticket");
            e.printStackTrace();
            return false;
        }
        sender.sendMessage("Created ticket #" + ticketID);
        return true;
    }

}
