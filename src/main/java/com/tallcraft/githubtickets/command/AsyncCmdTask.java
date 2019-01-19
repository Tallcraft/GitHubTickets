package com.tallcraft.githubtickets.command;

import org.bukkit.scheduler.BukkitRunnable;

/**
 * Runs command task (Runnable) async
 */
public class AsyncCmdTask extends BukkitRunnable {

    private Runnable task;

    AsyncCmdTask(Runnable task) {
        this.task = task;
    }

    /**
     * When an object implementing interface <code>Runnable</code> is used
     * to create a thread, starting the thread causes the object's
     * <code>run</code> method to be called in that separately executing
     * thread.
     * <p>
     * The general contract of the method <code>run</code> is that it may
     * take any action whatsoever.
     *
     * @see Thread#run()
     */
    @Override
    public void run() {
        this.task.run();
    }
}
