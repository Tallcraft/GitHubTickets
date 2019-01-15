package com.tallcraft.githubtickets;

//import org.bukkit.plugin.java.JavaPlugin;

import com.tallcraft.githubtickets.github.GitHubController;
import com.tallcraft.githubtickets.ticket.TicketController;

import java.io.IOException;
import java.util.Date;
import java.util.UUID;
import java.util.logging.Logger;

public final class GithubTickets/* extends JavaPlugin*/ {
//    private final Logger logger = Logger.getLogger(this.getName());

    private static final GitHubController gitHubController = GitHubController.getInstance();
    private static final TicketController ticketController = TicketController.getInstance();

    // FIXME
    private static final String user = "";
    private static final String password = "";
    private static final String repositoryName = "ticket-test";


    public static void main(String[] args) {

        try {
            gitHubController.connect(user, password, repositoryName);

            // Test ticket creation
            ticketController.createTicket(new Date(), UUID.randomUUID(), "Tallcraft", "Survival", "survival2", "Help, I've been griefed");
        } catch(IOException ex) {
            ex.printStackTrace();
        }
    }


//    @Override
//    public void onEnable() {
//        try {
//            this.gitHubController = new GitHubController();
//        } catch(IOException ex) {
//            logger.info("Could not initialize GitHubController connection");
//            ex.printStackTrace();
//            this.onDisable(); // Disable plugin
//        }
//
//        TicketCommand ticketCommand = new TicketCommand(gitHubController);
//        this.getCommand("ticket").setExecutor(ticketCommand);
//
//
//        // Plugin startup logic
//
//    }

//    @Override
//    public void onDisable() {
//        // Plugin shutdown logic
//    }
}
