package com.tallcraft.githubtickets.command;

import com.tallcraft.githubtickets.ticket.Ticket;
import com.tallcraft.githubtickets.ticket.TicketComment;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Collectors;

// TODO: Ticket body requirements
public class ReplyCmd extends AsyncCommand {

    private int minWordCount;

    ReplyCmd(int minWordCount) {
        this.minWordCount = minWordCount;
    }

    @Override
    public void run() {
        if (args.length < 2) {
            reply("Missing ticket id");
            return;
        }

        if (args.length < 3) {
            reply("Missing reply message");
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

        // Join args to form string for reply message
        String message = Arrays.stream(args).skip(2).collect(Collectors.joining(" "));


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

            reply("Ticket reply submitted ...");

            // Trigger action
            ticket = ticketController.replyTicket(id, (Player) sender, message);
            if (ticket == null) {
                reply("Ticket #" + id + " not found.");
                return;
            }
            reply(ticket.toChat());
        } catch (IOException e) {
            e.printStackTrace();
            reply("Error while adding reply to ticket #" + id);
        }
    }
}
