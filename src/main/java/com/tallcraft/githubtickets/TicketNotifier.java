package com.tallcraft.githubtickets;

import com.tallcraft.githubtickets.ticket.Ticket;
import com.tallcraft.githubtickets.ticket.TicketController;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class TicketNotifier implements Listener {
    private static TicketNotifier ourInstance = new TicketNotifier();

    private static final TicketController ticketController = TicketController.getInstance();
    private static GithubTickets plugin;
    private static FileConfiguration config;

    public static TicketNotifier getInstance() {
        return ourInstance;
    }

    static void setConfig(FileConfiguration config) {
        TicketNotifier.config = config;
    }

    public static void setPlugin(GithubTickets plugin) {
        TicketNotifier.plugin = plugin;
    }

    private ComponentBuilder createTicketMsg(String msg, String hoverMsg, String command) {
        return new ComponentBuilder(msg)
                .bold(true)
                .color(ChatColor.GOLD)
                .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command))
                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        new ComponentBuilder(hoverMsg).create()));
    }

    private List<Player> getPlayersToNotify(boolean notifyStaff, boolean notifyAuthor, UUID authorUUID) {
        if (!notifyStaff && (!notifyAuthor || authorUUID == null)) {
            return null;
        }
        return Bukkit.getServer().getOnlinePlayers().stream()
                .filter(player ->
                        (notifyStaff && Util.hasPerm(player, "notify.all"))
                                || notifyAuthor && authorUUID != null && authorUUID.equals(player.getUniqueId()))
                .collect(Collectors.toList());
    }

    private void notifyPlayers(boolean notifyStaff, boolean notifyAuthor, UUID authorUUID, BaseComponent[] message) {
        List<Player> players = getPlayersToNotify(notifyStaff, notifyAuthor, authorUUID);
        if (players == null || players.size() == 0) {
            return;
        }
        for (Player player : players) {
            player.spigot().sendMessage(message);
        }
    }

    public void onNewTicket(Ticket ticket) {
        if (plugin == null || config == null) {
            throw new IllegalStateException("Plugin config must be set");
        }
        if (ticket == null) {
            throw new IllegalArgumentException("Ticket event without ticket!");
        }
        ComponentBuilder msg = createTicketMsg("New Ticket #" + ticket.getId(),
                "Click to show ticket", "/ticket show " + ticket.getId());
        notifyPlayers(config.getBoolean("notify.onCreate.staff"), false, null, msg.create());
    }

    public void onTicketStatusChange(Ticket ticket) {
        if (plugin == null || config == null) {
            throw new IllegalStateException("Plugin config must be set");
        }
        if (ticket == null) {
            throw new IllegalArgumentException("Ticket event without ticket!");
        }
        ComponentBuilder msg = createTicketMsg(
                "Ticket #" + ticket.getId() + " " + (ticket.isOpen() ? "opened" : "closed"),
                "Click to show ticket", "/ticket show " + ticket.getId());
        notifyPlayers(
                config.getBoolean("notify.onStatusChange.staff"),
                config.getBoolean("notify.onStatusChange.player"),
                ticket.getPlayerUUID(), msg.create());
    }

    public void onTicketComment(Ticket ticket) {
        if (plugin == null || config == null) {
            throw new IllegalStateException("Plugin config must be set");
        }
        if (ticket == null) {
            throw new IllegalArgumentException("Ticket event without ticket!");
        }
        ComponentBuilder msg = createTicketMsg(
                "New comment for Ticket  #" + ticket.getId(),
                "Click to show ticket", "/ticket show " + ticket.getId());
        notifyPlayers(
                config.getBoolean("notify.onComment.staff"),
                config.getBoolean("notify.onComment.player"),
                ticket.getPlayerUUID(), msg.create());
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

                ComponentBuilder builder = createTicketMsg(message,
                        "Click to show tickets", "/ticket list");
                player.spigot().sendMessage(builder.create());
            });
        });
    }
}
