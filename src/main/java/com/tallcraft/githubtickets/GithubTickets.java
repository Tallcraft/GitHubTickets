package com.tallcraft.githubtickets;

import com.tallcraft.githubtickets.github.GitHubController;
import com.tallcraft.githubtickets.ticket.Location;
import com.tallcraft.githubtickets.ticket.Ticket;
import com.tallcraft.githubtickets.ticket.TicketController;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.Date;
import java.util.UUID;
import java.util.logging.Logger;

public final class GithubTickets extends JavaPlugin {
    private final Logger logger = Logger.getLogger(this.getName());

    private static final GitHubController gitHubController = GitHubController.getInstance();



    private FileConfiguration config;

    public static void main(String[] args) {
        try {
            gitHubController.connect(args[0], args[1], args[2], args[3]);
        } catch (IOException e) {
            System.err.println("Error while connect to github ticket repo");
            e.printStackTrace();
            return;
        }

        TicketController ticketController = TicketController.getInstance();
        Ticket ticket = new Ticket(new Date(), UUID.randomUUID(), "Steve", "Survival", "world", new Location(1, 2, 3), "Help, zombies!");
        try {
            ticketController.createTicket(ticket);
        } catch (IOException e) {
            System.err.println("Error while creating ticket");
            e.printStackTrace();
        }
    }


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
