package com.tallcraft.githubtickets.command;

import com.tallcraft.githubtickets.ticket.Ticket;
import com.tallcraft.githubtickets.ticket.TicketController;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
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
import java.util.List;
import java.util.stream.Collectors;

public class TicketCommand implements CommandExecutor {

    private static final TicketController ticketController = TicketController.getInstance();

    private static final BaseComponent[] ticketListHeading = new ComponentBuilder("Tickets >>>>>>").color(ChatColor.GOLD).bold(true).create();

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

        // No args => show help
        if (args.length < 1) {
            showHelp(sender, label);
            return true;
        }

        // Is user command syntax valid?
        boolean validSyntax = false;

        // Assign to handling methods
        switch (args[0].toLowerCase()) {
            case "show":
                validSyntax = showTicket(sender, args);
                break;
            case "tp":
                validSyntax = teleportTicket(sender, args);
                break;
            case "create":
                validSyntax = createTicket(sender, args);
                break;
            case "list":
                validSyntax = showTicketList(sender, args);
                break;
            case "close":
                validSyntax = changeTicketStatus(sender, args, false);
                break;
            case "reopen":
                validSyntax = changeTicketStatus(sender, args, true);
                break;
        }

        // If syntax is invalid show help message
        if (!validSyntax) {
            showHelp(sender, label);
        }

        return true;
    }

    /**
     * Show command help to user with command list and description
     *
     * @param sender Source of the command
     * @param label  Alias of the command which was used
     */
    private void showHelp(CommandSender sender, String label) {
        ComponentBuilder.FormatRetention f = ComponentBuilder.FormatRetention.NONE;
        ComponentBuilder builder = new ComponentBuilder("");

        String baseCmd = "/" + label;

        // TODO: check if any permission, otherwise return empty
        builder.append("Commands >>>>>>").color(ChatColor.GOLD).bold(true).append("\n");

        // TODO: permission check. Only show to players with permission
        builder.append(baseCmd + " create <Message>", f).color(ChatColor.GOLD);
        builder.append(" Create a ticket", f).append("\n");

        builder.append(baseCmd + " list", f).color(ChatColor.GOLD);
        builder.append(" List open tickets", f).append("\n");

        builder.append(baseCmd + " show <ID>", f).color(ChatColor.GOLD);
        builder.append(" Show ticket details", f).append("\n");

        builder.append(baseCmd + " tp <ID>", f).color(ChatColor.GOLD);
        builder.append(" Teleport to ticket location", f).append("\n");

        builder.append(baseCmd + " close <ID>", f).color(ChatColor.GOLD);
        builder.append(" Close Ticket", f).append("\n");

        builder.append(baseCmd + " reopen <ID>", f).color(ChatColor.GOLD);
        builder.append(" Re-open Ticket", f).append("\n");

        sender.spigot().sendMessage(builder.create());
    }

    private boolean changeTicketStatus(CommandSender sender, String[] args, boolean open) {
        if (args.length < 2) return false;

        int id;

        // Parse ticket id
        try {
            id = Integer.parseInt(args[1]);
        } catch (NumberFormatException ex) {
            sender.sendMessage("Invalid ticket id!");
            return true;
        }

        try {
            if (ticketController.changeTicketStatus(id, open)) {
                sender.sendMessage("Ticket #" + id + " " + (open ? "reopened" : "closed") + ".");
            } else {
                sender.sendMessage("Ticket not found.");
            }
        } catch (IOException e) {
            e.printStackTrace();
            sender.sendMessage("Error while changing ticket state");
        }

        return true;
    }

    /**
     * Get ticket by id and send it to user
     *
     * @param sender Source of the command
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
                sender.spigot().sendMessage(ticket.toChat());
                sender.sendMessage("");
            }
        } catch (IOException e) {
            sender.sendMessage("Error while getting ticket");
            e.printStackTrace();
        }
        return true;
    }

    /**
     * Show list of opened tickets
     *
     * @param sender Source of the command
     * @param args   command arguments
     * @return true on valid syntax, false otherwise
     */
    private boolean showTicketList(CommandSender sender, String[] args) {
        List<Ticket> ticketList = null;
        try {
            ticketList = ticketController.getOpenTickets();
            sender.spigot().sendMessage(ticketListHeading);
            sender.spigot().sendMessage(Ticket.ticketListToChat(ticketList));
        } catch (IOException e) {
            sender.sendMessage("Error while getting ticket list");
            e.printStackTrace();
        }
        return true;
    }

    /**
     * Teleport player to ticket location
     *
     * @param sender Source of the command
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

        sender.sendMessage("Teleporting to ticket #" + ticket.getId());
        player.teleport(location);
        return true;
    }

    /**
     * Create ticket and reply with ticket id
     *
     * @param sender Source of the command
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
