package com.tallcraft.githubtickets;

import com.tallcraft.githubtickets.ticket.Ticket;
import com.tallcraft.githubtickets.ticket.TicketController;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

public class TicketNotifier implements Listener {

    private static final TicketController ticketController = TicketController.getInstance();
    private static TicketNotifier ourInstance = new TicketNotifier();
    private static GithubTickets plugin;
    private static FileConfiguration config;

    static TicketNotifier getInstance() {
        return ourInstance;
    }

    static void setConfig(FileConfiguration config) {
        TicketNotifier.config = config;
    }

    public static void setPlugin(GithubTickets plugin) {
        TicketNotifier.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (plugin == null || config == null) {
            throw new IllegalStateException("Plugin config must be set");
        }
        boolean cfgNotifyStaff = config.getBoolean("notify.onLogin.staff");
        boolean cfgNotifyPlayer = config.getBoolean("notify.onLogin.player");

        if (!cfgNotifyStaff && !cfgNotifyPlayer) {
            // Notify not enabled
            return;
        }

        Player player = event.getPlayer();
        UUID filterUUID = null;
        boolean notifyAll = player.hasPermission("githubtickets.notify.all")
                && cfgNotifyStaff;
        boolean notifySelf = player.hasPermission("githubtickets.notify.self")
                && cfgNotifyPlayer;

        if (!notifyAll && !notifySelf) {
            // No notify permissions set / config disables notifications
            return;
        }

        if (!notifyAll) {
            // If the player doesn't have permission to get global ticket notifications
            // or config disables staff notifications
            // only notify them for their own tickets
            filterUUID = player.getUniqueId();
        }

        UUID finalFilterUUID = filterUUID;
        Util.run(plugin, true, () -> {
            List<Ticket> tickets;
            try {
                tickets = ticketController.getTickets(false, true, true,
                        finalFilterUUID);
            } catch (IOException e) {
                e.printStackTrace();
                // Don't show anything to the player, ticket fetch failed.
                return;
            }
            int ticketCount = tickets.size();
            if (ticketCount == 0) {
                // No open tickets to notify for
                return;
            }
            Util.run(plugin, false, () -> {
                String message;
                if (finalFilterUUID == null) {
                    message = "There "
                            + (ticketCount == 1 ? "is" : "are")
                            + " "
                            + ticketCount
                            + " open ticket"
                            + (ticketCount == 1 ? "" : "s")
                            + ".";
                } else {
                    message = "You have " + ticketCount + " open tickets";
                }

                ComponentBuilder builder = new ComponentBuilder(message)
                        .bold(true).color(ChatColor.GOLD)
                        .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/ticket list"))
                        .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                new ComponentBuilder("Click to show tickets").create()));
                player.spigot().sendMessage(builder.create());
            });
        });
    }
}
