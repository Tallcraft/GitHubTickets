package com.tallcraft.githubtickets.command;

import com.tallcraft.githubtickets.ticket.Ticket;
import org.bukkit.entity.Player;

import java.io.IOException;

public class StatusChangeCmd extends AsyncCommand {

    private boolean newStatus;

    StatusChangeCmd(boolean newStatus) {
        this.newStatus = newStatus;
    }

    @Override
    public void run() {
        if (args.length < 2) {
            reply("Missing ticket id");
            return;
        }

        int id;

        // Parse ticket id
        try {
            id = Integer.parseInt(args[1]);
        } catch (NumberFormatException ex) {
            reply("Invalid ticket id!");
            return;
        }

        try {
            // Fetch ticket object by id
            Ticket ticket = ticketController.getTicket(id);
            if (ticket == null) {
                sender.sendMessage("Ticket not found by id");
                return;
            }

            // Ticket found => permission check
            if (!hasTicketPermissionSync("reply", sender, ticket)) {
                noPerm();
                return;
            }

            reply("Ticket change submitted...");

            Player player = (Player) sender;
            ticket = ticketController.changeTicketStatus(id, newStatus, player.getUniqueId());
            if (ticket == null) {
                reply("Ticket not found.");
                return;
            }
            reply("Ticket #" + id + " " + (newStatus ? "reopened" : "closed") + ".");
        } catch (IOException e) {
            e.printStackTrace();
            reply("Error while changing ticket state");
        }
    }
}
