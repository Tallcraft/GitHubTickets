package com.tallcraft.githubtickets.command;

import com.tallcraft.githubtickets.GithubTickets;
import com.tallcraft.githubtickets.ticket.Ticket;
import com.tallcraft.githubtickets.ticket.TicketController;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
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
    private GithubTickets plugin;

    private int minWordCount;

    public TicketCommand(GithubTickets plugin, int minWordCount) {
        this.plugin = plugin;
        this.minWordCount = minWordCount;
    }

    /**
     * Test if user has permission with githubtickets prefix
     *
     * @param sender Sender to test permission for
     * @param perm   Permission suffix to test
     * @return true if sender has permission, false otherwise
     */
    private boolean hasPerm(CommandSender sender, String perm) {
        return sender.hasPermission("githubtickets." + perm);
    }

    /**
     * Send no permission message to sender
     *
     * @param sender  Sender to send no permission msg to
     * @param command called command
     * @return true for main command method
     */
    private boolean noPerm(CommandSender sender, Command command) {
        sender.sendMessage(command.getPermissionMessage());
        return true;
    }

    /**
     * Test ticket body for requirements such as non empty and minimum amount of words
     *
     * @param body Ticket message
     * @return true if requirements met, false otherwise
     */
    private boolean testBody(String body) {
        if (body == null || body.isEmpty()) return false;
        int wordCount = body.split(" ").length;
        return wordCount >= minWordCount;
    }

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

        switch (args[0].toLowerCase()) {
            case "show":
                if (!hasPerm(sender, "show.self") && !hasPerm(sender, "show.all"))
                    return noPerm(sender, command);
                showTicket(sender, command, args);
                break;
            case "tp":
                if (!hasPerm(sender, "tp")) return noPerm(sender, command);
                teleportTicket(sender, args);
                break;
            case "create":
                if (!hasPerm(sender, "create")) return noPerm(sender, command);
                createTicket(sender, args);
                break;
            case "list":
                if (!hasPerm(sender, "list")) return noPerm(sender, command);
                showTicketList(sender, args);
                break;
            case "close":
                if (!hasPerm(sender, "close.self") && !hasPerm(sender, "close.all"))
                    return noPerm(sender, command);
                changeTicketStatus(sender, command, args, false);
                break;
            case "reopen":
                if (!hasPerm(sender, "reopen.self") && !hasPerm(sender, "reopen.all"))
                    return noPerm(sender, command);
                changeTicketStatus(sender, command, args, true);
                break;
            default:
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

        builder.append("Commands >>>>>>").color(ChatColor.GOLD).bold(true).append("\n");

        if (hasPerm(sender, "create")) {
            builder.append(baseCmd + " create <Message>", f).color(ChatColor.GOLD)
                    .event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/ticket create "));
            builder.append(" Create a ticket", f).append("\n");
        }

        if (hasPerm(sender, "list")) {
            builder.append(baseCmd + " list", f).color(ChatColor.GOLD)
                    .event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/ticket list"));
            builder.append(" List open tickets", f).append("\n");
        }

        if (hasPerm(sender, "show.self") || hasPerm(sender, "show.all")) {
            builder.append(baseCmd + " show <ID>", f).color(ChatColor.GOLD)
                    .event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/ticket show "));
            builder.append(" Show ticket details", f).append("\n");
        }

        if (hasPerm(sender, "tp")) {
            builder.append(baseCmd + " tp <ID>", f).color(ChatColor.GOLD)
                    .event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/ticket tp "));
            builder.append(" Teleport to ticket location", f).append("\n");
        }

        if (hasPerm(sender, "close.self") || hasPerm(sender, "close.all")) {
            builder.append(baseCmd + " close <ID>", f).color(ChatColor.GOLD)
                    .event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/ticket close "));
            builder.append(" Close Ticket", f).append("\n");
        }

        if (hasPerm(sender, "reopen.self") || hasPerm(sender, "reopen.all")) {
            builder.append(baseCmd + " reopen <ID>", f).color(ChatColor.GOLD)
                    .event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/ticket reopen "));
            builder.append(" Re-open Ticket", f).append("\n");
        }

        sender.spigot().sendMessage(builder.create());
    }

    /**
     * Update ticket status to open or closed
     *
     * @param sender command sender
     * @param args   Passed command arguments
     * @param open   true = open ticket, false = close ticket
     * @return async task
     */
    private void changeTicketStatus(CommandSender sender, Command command, String[] args, boolean open) {
        if (args.length < 2) {
            sender.sendMessage("Missing ticket id");
            return;
        }

        int id;

        // Parse ticket id
        try {
            id = Integer.parseInt(args[1]);
        } catch (NumberFormatException ex) {
            sender.sendMessage("Invalid ticket id!");
            return;
        }

        sender.sendMessage("Ticket change submitted...");

        try {
            Ticket ticket = ticketController.changeTicketStatus(id, open);
            if (ticket == null) {
                sender.sendMessage("Ticket not found.");
                return;
            }
            if (hasPerm(sender, "close.all") || hasPerm(sender, "reopen.all")
                    || !(sender instanceof Player)
                    || ((Player) sender).getUniqueId().equals(ticket.getPlayerUUID())) {
                sender.sendMessage("Ticket #" + id + " " + (open ? "reopened" : "closed") + ".");
            } else {
                noPerm(sender, command);
            }
        } catch (IOException e) {
            e.printStackTrace();
            sender.sendMessage("Error while changing ticket state");
        }
    }

    /**
     * Get ticket by id and send it to user
     *
     * @param sender Source of the command
     * @param args   command arguments
     * @return async task
     */
    private void showTicket(CommandSender sender, Command command, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("Missing ticket id");
            return;
        }

        int id;

        // Parse ticket id
        try {
            id = Integer.parseInt(args[1]);
        } catch (NumberFormatException ex) {
            sender.sendMessage("Invalid ticket id!");
            return;
        }

        // Get ticket from GitHub by id
        Ticket ticket = null;
        try {
            ticket = ticketController.getTicket(id);
        } catch (IOException e) {
            e.printStackTrace();
            sender.sendMessage("Error while fetching ticket");
        }
        if (ticket == null) {
            sender.sendMessage("Ticket not found.");
            return;
        }
        // Check if player has permission to show specific ticket (own vs all perm)
        if (hasPerm(sender, "show.all")
                || !(sender instanceof Player)
                || ((Player) sender).getUniqueId().equals(ticket.getPlayerUUID())) {
            sender.spigot().sendMessage(ticket.toChat());
            sender.sendMessage("");
        } else {
            noPerm(sender, command);
        }
    }

    /**
     * Show list of opened tickets
     *
     * @param sender Source of the command
     * @param args   command arguments
     */
    private void showTicketList(CommandSender sender, String[] args) {
        List<Ticket> ticketList;
        try {
            ticketList = ticketController.getOpenTickets();
        } catch (IOException e) {
            e.printStackTrace();
            sender.sendMessage("Error while fetching ticket list");
            return;
        }
        sender.spigot().sendMessage(ticketListHeading);
        sender.spigot().sendMessage(Ticket.ticketListToChat(ticketList));
    }

    /**
     * Teleport player to ticket location
     *
     * @param sender Source of the command
     * @param args   Command arguments
     * @return async task
     */
    private void teleportTicket(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("Missing ticket id");
            return;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players may use this command.");
            return;
        }

        // Cast to player so we can teleport
        Player player = (Player) sender;

        int id;

        // Parse ticket id
        try {
            id = Integer.parseInt(args[1]);
        } catch (NumberFormatException ex) {
            sender.sendMessage("Invalid ticket id!");
            return;
        }

        Bukkit.getScheduler().runTask(plugin, () -> {
            Ticket ticket;

            // Get ticket from GitHub by id
            try {
                ticket = ticketController.getTicket(id);
            } catch (IOException e) {
                e.printStackTrace();
                sender.sendMessage("Error while fetching ticket");
                return;
            }
            if (ticket == null) {
                sender.sendMessage("Ticket not found.");
                return;
            }

            World world = Bukkit.getWorld(ticket.getWorldName());
            if (world == null) {
                sender.sendMessage("World " + ticket.getWorldName() + " not found!");
                return;
            }
            Location location = new Location(
                    world,
                    ticket.getLocation().getX(),
                    ticket.getLocation().getY(),
                    ticket.getLocation().getZ());

            sender.sendMessage("Teleporting to ticket #" + ticket.getId());
            player.teleport(location);
        });
    }

    /**
     * Create ticket and reply with ticket id
     *
     * @param sender Source of the command
     * @param args   command arguments
     * @return async task
     */
    private void createTicket(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Console not supported (yet)");
            return;
        }
        // Join args to form string for ticket message
        String message = Arrays.stream(args).skip(1).collect(Collectors.joining(" "));

        if (!testBody(message)) {
            sender.sendMessage("Invalid ticket message");
            return;
        }

        sender.sendMessage("Ticket submitted...");

        long ticketID;
        try {
            ticketID = ticketController.createTicket((Player) sender, new Date(), message);
        } catch (IOException e) {
            sender.sendMessage("Error: Could not create ticket");
            e.printStackTrace();
            return;
        }
        sender.sendMessage("Created ticket #" + ticketID);
    }

}
