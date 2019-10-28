package com.tallcraft.githubtickets;

import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class Util {
    // TODO: globals for date format and text formatting (could be set from main class from cfg)

    /**
     * Test if user has permission with githubtickets prefix
     *
     * @param sender Sender to test permission for
     * @param perm   Permission suffix to test
     * @return true if sender has permission, false otherwise
     */
    public static boolean hasPerm(CommandSender sender, String perm) {
        return sender.hasPermission("githubtickets." + perm);
    }

    public static void run(Plugin plugin, boolean async, Runnable task) {
        BukkitRunnable runnable = new BukkitRunnable() {
            @Override
            public void run() {
                task.run();
            }
        };
        if (async) {
            runnable.runTaskAsynchronously(plugin);
        } else {
            runnable.runTask(plugin);
        }
    }
}
