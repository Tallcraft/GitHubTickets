package com.tallcraft.githubtickets.ticket;

import com.tallcraft.githubtickets.github.GitHubController;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

public class TicketFetcher extends BukkitRunnable {

    private ConcurrentHashMap<Integer, Ticket> tickets;
    private GitHubController gitHubController = GitHubController.getInstance();

    /**
     * @param tickets Ticket map to populate every call
     */
    TicketFetcher(ConcurrentHashMap<Integer, Ticket> tickets) {
        this.tickets = tickets;
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
        // We can only fetch open tickets if gitHubController is ready
        if (gitHubController.isConnected()) {
            fetchTickets();
        }
    }

    private void fetchTickets() {
        // Get list of open tickets from controller
        List<Ticket> ticketList;
        try {
            ticketList = gitHubController.getTickets().get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return;
        }
        // Empty map
        tickets.clear();

        // Convert ticket list to map
        for (Ticket ticket : ticketList) {
            tickets.put(ticket.getId(), ticket);
        }
    }
}
