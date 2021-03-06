package com.tallcraft.githubtickets.command;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.stream.Collectors;

public class CreateCmd extends AsyncCommand {

    private final int minWordCount;

    CreateCmd(int minWordCount) {
        this.minWordCount = minWordCount;
    }

    /**
     * Test ticket body for requirements such as non empty and minimum amount of words.
     *
     * @param body Ticket message
     * @return null if validation succeeds or reason for validation failure.
     */
    private String testBody(String body) {
        if (body == null || body.isEmpty()) {
            return "No message provided.";
        }
        int wordCount = body.split("\\s+").length;

        if (wordCount < minWordCount) {
            return "Message too short";
        }

        return null;
    }

    @Override
    public void run() {
        if (!(sender instanceof Player)) {
            reply("Console not supported (yet)");
            return;
        }
        // Join args to form string for ticket message
        String message = Arrays.stream(args).skip(1).collect(Collectors.joining(" "));

        String validationError = testBody(message);
        if (validationError != null) {
            reply("Invalid ticket message: " + validationError);
            return;
        }

        reply("Ticket submitted...");

        long ticketID;
        try {
            ticketID = ticketController.createTicket((Player) sender, new Date(), message);
        } catch (IOException e) {
            reply("Error: Could not create ticket");
            e.printStackTrace();
            return;
        }
        reply(new ComponentBuilder("Created ticket #" + ticketID)
                .bold(true)
                .color(ChatColor.GOLD)
                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        new ComponentBuilder("Click to show ticket").create()))
                .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                        "/ticket show " + ticketID))
                .create());
    }
}
