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

        reply("Ticket change submitted...");

        try {
            Ticket ticket = ticketController.changeTicketStatus(id, newStatus);
            if (ticket == null) {
                reply("Ticket not found.");
                return;
            }
            runSync(() -> {
                if (hasPermSync("close.all") || hasPermSync("reopen.all")
                        || !(sender instanceof Player)
                        || ((Player) sender).getUniqueId().equals(ticket.getPlayerUUID())) {
                    replySync("Ticket #" + id + " " + (newStatus ? "reopened" : "closed") + ".");
                } else {
                    noPermSync();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
            reply("Error while changing ticket state");
        }
    }
}
