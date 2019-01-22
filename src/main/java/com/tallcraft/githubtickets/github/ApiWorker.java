package com.tallcraft.githubtickets.github;

import org.bukkit.scheduler.BukkitRunnable;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Works on api tasks while respecting api limits
 */
public class ApiWorker extends BukkitRunnable {

    private LinkedBlockingQueue<Runnable> tasks;
    private int apiCallDelay;

    /**
     * Create api worker
     *
     * @param tasks        queue of api tasks
     * @param apiCallDelay delay between each api call in ms
     */
    ApiWorker(LinkedBlockingQueue<Runnable> tasks, int apiCallDelay) {
        this.tasks = tasks;

        if (apiCallDelay <= 0)
            throw new IllegalArgumentException("apiCallDelay must be greater than zero");
        this.apiCallDelay = apiCallDelay;
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
        while (true) {
            try {
                Runnable task = tasks.poll(1, TimeUnit.DAYS);
                if (task != null) task.run();
                // Time to wait between each api call
                // GitHub API is limited to 5000 calls per hour, that's 1.3889 per second, thus 2 second delay
                // should be sufficient
                Thread.sleep(apiCallDelay);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
