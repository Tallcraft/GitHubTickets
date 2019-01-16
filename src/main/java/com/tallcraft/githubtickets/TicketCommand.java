package com.tallcraft.githubtickets;

import com.tallcraft.githubtickets.ticket.Ticket;
import com.tallcraft.githubtickets.ticket.TicketController;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.Date;

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
                sender.sendMessage(ticket.toString());
            }
        } catch (IOException e) {
            sender.sendMessage("Error while getting ticket");
            e.printStackTrace();
        }
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

        String message = String.join(" ", args);

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
