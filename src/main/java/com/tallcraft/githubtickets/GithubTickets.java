package com.tallcraft.githubtickets;

import com.tallcraft.githubtickets.github.GitHubController;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.logging.Logger;

//import java.util.ArrayList;
//import java.util.Date;
//import java.util.UUID;

public final class GithubTickets extends JavaPlugin {
    private final Logger logger = Logger.getLogger(this.getName());

    private static final GitHubController gitHubController = GitHubController.getInstance();
//    private static final TicketController ticketController = TicketController.getInstance();


    private FileConfiguration config;


//    // Test method
//    public static void main(String[] args) {
//
//
//        try {
//            gitHubController.connect(user, password, ,repositoryName);
//
//            // Test ticket creation
//            ticketController.createTicket(new Date(), UUID.randomUUID(), "Tallcraft", "Survival", "survival2", "Help, I've been griefed");
//        } catch(IOException ex) {
//            ex.printStackTrace();
//        }
//    }


    @Override
    public void onEnable() {
        // Initialize with defaults
        initConfig();

        // Read config values
        String user = config.getString("github.auth.username");
        String password = config.getString("github.auth.password");
        String repositoryUser = config.getString("github.repository.user");
        String repositoryName = config.getString("github.repository.repoName");

        // TODO: disable plugin if config values are empty / invalid

        try {
            // Connect to github repo
            gitHubController.connect(user, password, repositoryUser, repositoryName);
        } catch(IOException ex) {
            logger.info("Error while connecting to GitHub");
            ex.printStackTrace();
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        // Initialize and register commands
        TicketCommand ticketCommand = new TicketCommand();
        this.getCommand("ticket").setExecutor(ticketCommand);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    private void initConfig() {
        config = this.getConfig();

        MemoryConfiguration defaultConfig = new MemoryConfiguration();

        ConfigurationSection github = defaultConfig.createSection("github");
        ConfigurationSection githubAuth = github.createSection("auth");
        ConfigurationSection githubRepo = github.createSection("repository");

        githubAuth.set("username", "");
        githubAuth.set("password", "");

        githubRepo.set("user", "");
        githubRepo.set("repoName", "");


        config.setDefaults(defaultConfig);
        config.options().copyDefaults(true);
        saveConfig();
    }
}
