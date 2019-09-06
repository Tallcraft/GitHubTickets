package com.tallcraft.githubtickets.command;

import com.tallcraft.githubtickets.ticket.Ticket;
import org.bukkit.entity.Player;

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
        runSync(() -> {
            // Check if player has permission to show specific ticket (own vs all perm)
            if (hasPermSync("show.all")
                    || !(sender instanceof Player)
                    || ((Player) sender).getUniqueId().equals(finalTicket.getPlayerUUID())) {
                replySync(finalTicket.toChat());
                replySync("");
            } else {
                noPermSync();
            }
        });
    }
}
