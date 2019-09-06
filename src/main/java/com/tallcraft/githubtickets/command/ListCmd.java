package com.tallcraft.githubtickets.command;

import com.tallcraft.githubtickets.ticket.Ticket;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;

import java.io.IOException;
import java.util.List;

public class ListCmd extends AsyncCommand {

    private static final BaseComponent[] ticketListHeading = new ComponentBuilder("Tickets >>>>>>").color(ChatColor.GOLD).bold(true).create();

    @Override
    public void run() {
        List<Ticket> ticketList;
        try {
            ticketList = ticketController.getOpenTickets();
        } catch (IOException e) {
            e.printStackTrace();
            reply("Error while fetching ticket list");
            return;
        }
        runSync(() -> {
            replySync(ticketListHeading);
            replySync(Ticket.ticketListToChat(ticketList));
        });
    }
}
