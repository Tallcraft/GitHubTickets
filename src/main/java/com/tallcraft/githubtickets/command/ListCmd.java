package com.tallcraft.githubtickets.command;

import com.tallcraft.githubtickets.ticket.Ticket;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

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
            if (sender instanceof Player && !hasPermSync("show.all")) {
                Player player = (Player) sender;
                List<Ticket> ticketListFiltered = ticketList.stream()
                        .filter((ticket) -> ticket.getPlayerUUID()
                                .equals(player.getUniqueId())).collect(Collectors.toList());
                if (ticketListFiltered.isEmpty()) {
                    replySync("You don't have any open tickets");
                    return;
                }
                replySync(Ticket.ticketListToChat(ticketListFiltered));
                return;
            }
            if (ticketList.isEmpty()) {
                replySync("No open tickets");
                return;
            }
            replySync(Ticket.ticketListToChat(ticketList));
        });
    }
}
