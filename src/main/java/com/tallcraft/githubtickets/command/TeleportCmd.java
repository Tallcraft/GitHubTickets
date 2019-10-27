package com.tallcraft.githubtickets.command;

import com.tallcraft.githubtickets.Util;
import com.tallcraft.githubtickets.ticket.Ticket;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.io.IOException;

public class TeleportCmd extends AsyncCommand {

    @Override
    public void run() {
        if (args.length < 2) {
            reply("Missing ticket id");
            return;
        }

        if (!(sender instanceof Player)) {
            reply("Only players may use this command.");
            return;
        }

        // Cast to player so we can teleport
        Player player = (Player) sender;

        int id;

        // Parse ticket id
        try {
            id = Integer.parseInt(args[1]);
        } catch (NumberFormatException ex) {
            reply("Invalid ticket id!");
            return;
        }

        Ticket ticket;

        // Get ticket from GitHub by id
        try {
            ticket = ticketController.getTicket(id);
        } catch (IOException e) {
            e.printStackTrace();
            reply("Error while fetching ticket");
            return;
        }
        Util.run(plugin, false, () -> {
            if (ticket == null) {
                replySync("Ticket not found.");
                return;
            }

            World world = Bukkit.getWorld(ticket.getWorldName());
            if (world == null) {
                replySync("World " + ticket.getWorldName() + " not found!");
                return;
            }
            Location location = new Location(
                    world,
                    ticket.getLocation().getX(),
                    ticket.getLocation().getY(),
                    ticket.getLocation().getZ());

            replySync("Teleporting to ticket #" + ticket.getId());
            player.teleport(location);
        });
    }
}
