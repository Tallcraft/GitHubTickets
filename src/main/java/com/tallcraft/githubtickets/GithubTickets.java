package com.tallcraft.githubtickets;

import com.tallcraft.githubtickets.command.TicketCommandExecutor;
import com.tallcraft.githubtickets.github.GitHubController;
import com.tallcraft.githubtickets.ticket.TicketController;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.logging.Logger;

public final class GithubTickets extends JavaPlugin implements Listener {
    private static final Config config = Config.getInstance();
    private static final GitHubController gitHubController = GitHubController.getInstance();
    private static final TicketController ticketController = TicketController.getInstance();
    private static final TicketNotifier ticketNotifier = TicketNotifier.getInstance();
    private final Logger logger = Logger.getLogger(this.getName());

    private boolean isUnset(String str) {
        return str == null || str.isEmpty();
    }

    @Override
    public void onEnable() {
        // Initialize config with defaults
        config.initConfigStore(this);

        // Read config values
        String user = config.store().getString("github.auth.username");
        String password = config.store().getString("github.auth.password");
        String oauth = config.store().getString("github.auth.oauth");
        String repositoryUser = config.store().getString("github.repository.user");
        String repositoryName = config.store().getString("github.repository.repoName");
        int minWordCount = config.store().getInt("ticketMinWordCount");

        // Validate config options

        if (isUnset(oauth) && ((isUnset(user)) || isUnset(password)) || isUnset(repositoryUser) || isUnset(repositoryName)) {
            logger.info("Missing GitHub configuration. Please add it in config.yml. Disabling.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        // Servername overwrite
        String serverName = config.store().getString("serverName");
        if (serverName != null && !serverName.isEmpty()) {
            ticketController.setServerName(serverName);
        }

        // Connect to github repo
        try {
            if (!isUnset(oauth)) {
                gitHubController.setOauth(oauth);
            } else {
                gitHubController.setCredentials(user, password);
            }
            gitHubController.connect(repositoryUser, repositoryName);
            logger.info("Connected to GitHub repository: " + repositoryUser + ":" + repositoryName);
            logger.info(gitHubController.getRateLimitInfo());
        } catch (IOException ex) {
            logger.info("Error while connecting to GitHub");
            ex.printStackTrace();
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        // Initialize ticket notifier and register event listener
        TicketNotifier.setTicketController(ticketController);
        TicketNotifier.setPlugin(this);
        Bukkit.getServer().getPluginManager().registerEvents(ticketNotifier, this);

        // Initialize and register commands
        TicketCommandExecutor ticketCommandExecutor = new TicketCommandExecutor(this, minWordCount);
        this.getCommand("ticket").setExecutor(ticketCommandExecutor);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
