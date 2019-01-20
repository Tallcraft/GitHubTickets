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
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class TicketCommand implements CommandExecutor {

    private static final TicketController ticketController = TicketController.getInstance();

    private GithubTickets plugin;

    private static final BaseComponent[] ticketListHeading = new ComponentBuilder("Tickets >>>>>>").color(ChatColor.GOLD).bold(true).create();

    // TODO: for all async / rate limited calls: list of players, add to it if player called something, remove it when result is there. this prevents multiple ongoing calls by player

    public TicketCommand(GithubTickets plugin) {
        this.plugin = plugin;
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
                if (!hasPerm(sender, "show.self") && !hasPerm(sender, "show.all"))
                    return noPerm(sender, command);
                validSyntax = showTicket(sender, command, args);
                break;
            case "tp":
                if (!hasPerm(sender, "tp")) return noPerm(sender, command);
                validSyntax = teleportTicket(sender, args);
                break;
            case "create":
                if (!hasPerm(sender, "create")) return noPerm(sender, command);
                validSyntax = createTicket(sender, args);
                break;
            case "list":
                if (!hasPerm(sender, "list")) return noPerm(sender, command);
                validSyntax = showTicketList(sender, args);
                break;
            case "close":
                if (!hasPerm(sender, "close.self") && !hasPerm(sender, "close.all"))
                    return noPerm(sender, command);
                validSyntax = changeTicketStatus(sender, command, args, false);
                break;
            case "reopen":
                if (!hasPerm(sender, "reopen.self") && !hasPerm(sender, "reopen.all"))
                    return noPerm(sender, command);
                validSyntax = changeTicketStatus(sender, command, args, true);
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
     * @return true if a valid command, otherwise false
     */
    private boolean changeTicketStatus(CommandSender sender, Command command, String[] args, boolean open) {
        if (args.length < 2) return false;

        int id;

        // Parse ticket id
        try {
            id = Integer.parseInt(args[1]);
        } catch (NumberFormatException ex) {
            sender.sendMessage("Invalid ticket id!");
            return true;
        }

        new AsyncCmdTask(() -> {

            try {
                Ticket ticket = ticketController.changeTicketStatus(id, open).get();
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


            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
                sender.sendMessage("Error while changing ticket state");
            }
        }).runTaskAsynchronously(plugin);

        return true;
    }

    /**
     * Get ticket by id and send it to user
     *
     * @param sender Source of the command
     * @param args   command arguments
     * @return true on valid syntax, false otherwise
     */
    private boolean showTicket(CommandSender sender, Command command, String[] args) {
        if (args.length < 2) return false;

        int id;

        // Parse ticket id
        try {
            id = Integer.parseInt(args[1]);
        } catch (NumberFormatException ex) {
            sender.sendMessage("Invalid ticket id!");
            return true;
        }

        new AsyncCmdTask(() -> {
            // Get ticket from GitHub by id
            try {
                Ticket ticket = ticketController.getTicket(id).get();
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

            } catch (IOException | InterruptedException | ExecutionException e) {
                sender.sendMessage("Error while getting ticket");
                e.printStackTrace();
            }
        }).runTaskAsynchronously(plugin);
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
        new AsyncCmdTask(() -> {
            List<Ticket> ticketList = null;
            try {
                ticketList = ticketController.getOpenTickets().get();
                sender.spigot().sendMessage(ticketListHeading);
                sender.spigot().sendMessage(Ticket.ticketListToChat(ticketList));
            } catch (InterruptedException | ExecutionException e) {
                sender.sendMessage("Error while getting ticket list");
                e.printStackTrace();
            }
        }).runTaskAsynchronously(plugin);

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
        if (args.length < 2) return false;

        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players may use this command.");
            return true;
        }

        // Cast to player so we can teleport
        Player player = (Player) sender;

        int id;

        // Parse ticket id
        try {
            id = Integer.parseInt(args[1]);
        } catch (NumberFormatException ex) {
            sender.sendMessage("Invalid ticket id!");
            return true;
        }

        new AsyncCmdTask(() -> {

            Ticket ticket;

            // Get ticket from GitHub by id
            try {
                ticket = ticketController.getTicket(id).get();
                if (ticket == null) {
                    sender.sendMessage("Ticket not found.");
                    return;
                }
            } catch (IOException | InterruptedException | ExecutionException e) {
                sender.sendMessage("Error while getting ticket");
                e.printStackTrace();
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
        }).runTaskAsynchronously(plugin);
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
            return true;
        }
        new AsyncCmdTask(() -> {
            // Join args to form string for ticket message
            String message = Arrays.stream(args).skip(1).collect(Collectors.joining(" "));

            long ticketID;
            try {
                ticketID = ticketController.createTicket((Player) sender, new Date(), message).get();
            } catch (IOException | InterruptedException | ExecutionException e) {
                sender.sendMessage("Error: Could not create ticket");
                e.printStackTrace();
                return;
            }
            sender.sendMessage("Created ticket #" + ticketID);
        }).runTaskAsynchronously(plugin);

        return true;
    }

}
