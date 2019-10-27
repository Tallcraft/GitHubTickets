package com.tallcraft.githubtickets;

import com.tallcraft.githubtickets.command.TicketCommandExecutor;
import com.tallcraft.githubtickets.github.GitHubController;
import com.tallcraft.githubtickets.ticket.TicketController;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.logging.Logger;

public final class GithubTickets extends JavaPlugin implements Listener {
    private static final GitHubController gitHubController = GitHubController.getInstance();
    private static final TicketController ticketController = TicketController.getInstance();
    private static final TicketNotifier ticketNotifier = TicketNotifier.getInstance();
    private final Logger logger = Logger.getLogger(this.getName());
    private FileConfiguration config;

    private boolean isUnset(String str) {
        return str == null || str.isEmpty();
    }

    @Override
    public void onEnable() {
        // Initialize with defaults
        initConfig();

        // Read config values
        String user = config.getString("github.auth.username");
        String password = config.getString("github.auth.password");
        String oauth = config.getString("github.auth.oauth");
        String repositoryUser = config.getString("github.repository.user");
        String repositoryName = config.getString("github.repository.repoName");
        int minWordCount = config.getInt("ticketMinWordCount");

        // Validate config options

        if (isUnset(oauth) && ((isUnset(user)) || isUnset(password)) || isUnset(repositoryUser) || isUnset(repositoryName)) {
            logger.info("Missing GitHub configuration. Please add it in config.yml. Disabling.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        // Servername overwrite
        String serverName = config.getString("serverName");
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
        TicketNotifier.setPlugin(this);
        TicketNotifier.setConfig(config);
        Bukkit.getServer().getPluginManager().registerEvents(ticketNotifier, this);

        // Initialize and register commands
        TicketCommandExecutor ticketCommandExecutor = new TicketCommandExecutor(this, minWordCount);
        this.getCommand("ticket").setExecutor(ticketCommandExecutor);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    /**
     * Initialize config, set defaults if unset
     */
    private void initConfig() {
        config = this.getConfig();

        MemoryConfiguration defaultConfig = new MemoryConfiguration();

        defaultConfig.set("serverName", "");
        defaultConfig.set("ticketMinWordCount", 2);

        ConfigurationSection notify = defaultConfig.createSection(("notify"));
        ConfigurationSection notifyOnLogin = notify.createSection("onLogin");
        notifyOnLogin.set("staff", true);
        notifyOnLogin.set("player", true);

        ConfigurationSection github = defaultConfig.createSection("github");
        ConfigurationSection githubAuth = github.createSection("auth");
        ConfigurationSection githubRepo = github.createSection("repository");

        githubAuth.set("username", "");
        githubAuth.set("password", "");
        githubAuth.set("oauth", "");

        githubRepo.set("user", "");
        githubRepo.set("repoName", "");


        config.setDefaults(defaultConfig);
        config.options().copyDefaults(true);
        saveConfig();
    }
}
