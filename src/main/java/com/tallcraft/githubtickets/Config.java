package com.tallcraft.githubtickets;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.file.FileConfiguration;

public class Config {
    private static Config ourInstance = new Config();
    private FileConfiguration configStore;

    public static Config getInstance() {
        return ourInstance;
    }
    public FileConfiguration store() {
        return configStore;
    }

    /**
     * Initialize config, set defaults if unset
     */
    public void initConfigStore(GithubTickets plugin) {
        configStore = plugin.getConfig();

        configStore.setDefaults(getDefaults());
        configStore.options().copyDefaults(true);

        plugin.saveConfig();
    }

    private MemoryConfiguration getDefaults() {
        MemoryConfiguration defaultConfig = new MemoryConfiguration();

        defaultConfig.set("serverName", "");
        defaultConfig.set("ticketMinWordCount", 2);

        ConfigurationSection github = defaultConfig.createSection("github");
        ConfigurationSection githubAuth = github.createSection("auth");
        ConfigurationSection githubRepo = github.createSection("repository");
        githubAuth.set("username", "");
        githubAuth.set("password", "");
        githubAuth.set("oauth", "");
        githubRepo.set("user", "");
        githubRepo.set("repoName", "");

        // TODO
//        ConfigurationSection chat = defaultConfig.createSection("chat");
//        ConfigurationSection locale = chat.createSection("locale");
//        locale.set("language", "en");
//        locale.set("country", "US");

        ConfigurationSection notify = defaultConfig.createSection(("notify"));
        ConfigurationSection notifyOnLogin = notify.createSection("onLogin");
        notifyOnLogin.set("staff", true);
        notifyOnLogin.set("player", true);
        ConfigurationSection notifyOnCreate = notify.createSection("onCreate");
        notifyOnCreate.set("staff", true);
        ConfigurationSection notifyOnStatusChange = notify.createSection("onStatusChange");
        notifyOnStatusChange.set("staff", true);
        notifyOnStatusChange.set("player", true);
        ConfigurationSection notifyOnComment = notify.createSection("onComment");
        notifyOnComment.set("staff", true);
        notifyOnComment.set("player", true);

        return defaultConfig;
    }
}
