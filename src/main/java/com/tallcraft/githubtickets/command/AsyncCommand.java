package com.tallcraft.githubtickets.command;

import com.tallcraft.githubtickets.GithubTickets;
import com.tallcraft.githubtickets.Util;
import com.tallcraft.githubtickets.ticket.Ticket;
import com.tallcraft.githubtickets.ticket.TicketController;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

abstract class AsyncCommand extends BukkitRunnable {
    static final TicketController ticketController = TicketController.getInstance();

    protected GithubTickets plugin;
    protected TicketCommandExecutor executor;
    protected CommandSender sender;
    protected Command command;
    protected String label;
    protected String[] args;


    void init(GithubTickets plugin, TicketCommandExecutor executor, CommandSender sender, Command command, String label, String[] args) {
        this.plugin = plugin;
        this.executor = executor;
        this.sender = sender;
        this.command = command;
        this.label = label;
        this.args = args;
    }

    // Methods which can run in an async context

    protected void reply(String msg) {
        Util.run(plugin, false, () -> replySync(msg));
    }

    protected void reply(BaseComponent[] msg) {
        Util.run(plugin, false, () -> replySync(msg));
    }

    protected void noPerm() {
        Util.run(plugin, false, this::noPermSync);
    }

    // Methods which must only be used in a sync context

    protected void replySync(String msg) {
        sender.sendMessage(msg);
    }

    protected void replySync(BaseComponent[] msg) {
        sender.spigot().sendMessage(msg);
    }

    protected void noPermSync() {
        executor.noPerm(sender, command);
    }

    protected boolean hasPermSync(String perm) {
        return executor.hasPerm(sender, perm);
    }

    protected boolean hasTicketPermissionSync(String basePermission, CommandSender sender, Ticket ticket) {
        return
                // Players with all permission
                executor.hasPerm(sender, basePermission + ".all")
                        // Console
                        || !(sender instanceof Player)
                        // Players with matching id and self permission
                        || (executor.hasPerm(sender, basePermission + ".self")
                        && ((Player) sender).getUniqueId().equals(ticket.getPlayerUUID()));
    }
}
