package com.tallcraft.githubtickets.command;

import com.tallcraft.githubtickets.Util;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;

public class HelpCmd extends AsyncCommand {

    @Override
    public void run() {
        Util.run(plugin, false, () -> {
            ComponentBuilder.FormatRetention f = ComponentBuilder.FormatRetention.NONE;
            ComponentBuilder builder = new ComponentBuilder("");

            String baseCmd = "/" + label;

            builder.append("Commands >>>>>>").color(ChatColor.GOLD).bold(true).append("\n");

            if (Util.hasPerm(sender, "create")) {
                builder.append(baseCmd + " create <Message>", f).color(ChatColor.GOLD)
                        .event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/ticket create "));
                builder.append(" Create a ticket", f).append("\n");
            }

            if (Util.hasPerm(sender, "reply")) {
                builder.append(baseCmd + " reply <ID> <Message>", f).color(ChatColor.GOLD)
                        .event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/ticket reply "));
                builder.append(" Reply to a ticket", f).append("\n");
            }

            if (Util.hasPerm(sender, "list")) {
                builder.append(baseCmd + " list", f).color(ChatColor.GOLD)
                        .event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/ticket list"));
                builder.append(" List open tickets", f).append("\n");
            }

            if (Util.hasPerm(sender, "show.self") || Util.hasPerm(sender, "show.all")) {
                builder.append(baseCmd + " show <ID>", f).color(ChatColor.GOLD)
                        .event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/ticket show "));
                builder.append(" Show ticket details", f).append("\n");
            }

            if (Util.hasPerm(sender, "tp")) {
                builder.append(baseCmd + " tp <ID>", f).color(ChatColor.GOLD)
                        .event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/ticket tp "));
                builder.append(" Teleport to ticket location", f).append("\n");
            }

            if (Util.hasPerm(sender, "close.self") || Util.hasPerm(sender, "close.all")) {
                builder.append(baseCmd + " close <ID>", f).color(ChatColor.GOLD)
                        .event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/ticket close "));
                builder.append(" Close Ticket", f).append("\n");
            }

            if (Util.hasPerm(sender, "reopen.self") || Util.hasPerm(sender, "reopen.all")) {
                builder.append(baseCmd + " reopen <ID>", f).color(ChatColor.GOLD)
                        .event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/ticket reopen "));
                builder.append(" Re-open Ticket", f).append("\n");
            }

            replySync(builder.create());
        });
    }
}
