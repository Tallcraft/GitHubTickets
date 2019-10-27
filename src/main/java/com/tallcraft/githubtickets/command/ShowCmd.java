package com.tallcraft.githubtickets.command;

import com.tallcraft.githubtickets.Util;
import com.tallcraft.githubtickets.ticket.Ticket;

import java.io.IOException;

public class ShowCmd extends AsyncCommand {

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

        // Get ticket from GitHub by id
        Ticket ticket = null;
        try {
            ticket = ticketController.getTicket(id);
        } catch (IOException e) {
            e.printStackTrace();
            reply("Error while fetching ticket");
        }
        if (ticket == null) {
            reply("Ticket not found.");
            return;
        }
        Ticket finalTicket = ticket;
        Util.run(plugin, false, () -> {
            // Check if player has permission to show specific ticket (own vs all perm)
            if (!hasTicketPermissionSync("show", sender, finalTicket)) {
                noPermSync();
                return;
            }
            replySync(finalTicket.toChat());
            replySync("");
        });
    }
}
