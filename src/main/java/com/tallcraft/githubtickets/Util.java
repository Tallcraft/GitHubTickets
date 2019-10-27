package com.tallcraft.githubtickets;

import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class Util {
    // TODO: globals for date format and text formatting (could be set from main class from cfg)

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
