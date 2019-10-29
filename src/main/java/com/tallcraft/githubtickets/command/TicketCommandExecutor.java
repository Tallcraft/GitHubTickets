package com.tallcraft.githubtickets.command;

import com.tallcraft.githubtickets.GithubTickets;
import com.tallcraft.githubtickets.Util;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class TicketCommandExecutor implements CommandExecutor {

    private GithubTickets plugin;

    private int minWordCount;

    public TicketCommandExecutor(GithubTickets plugin, int minWordCount) {
        this.plugin = plugin;
        this.minWordCount = minWordCount;
    }


    /**
     * Send no permission message to sender
     *
     * @param sender  Sender to send no permission msg to
     * @param command called command
     * @return true for main command method
     */
    boolean noPerm(CommandSender sender, Command command) {
        sender.sendMessage(command.getPermissionMessage());
        return true;
    }


    /**
     * Executes the given command, returning its success.
     * <br>
     * If false is returned, then the "usage" plugin.yml entry for this command
     * (if defined) will be sent to the player.
     *
     * @param sender  Source of the command
     * @param command Command which was executed
     * @param label   Alias of the command which was used
     * @param args    Passed command arguments
     * @return true if a valid command, otherwise false
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Select command task depending on args
        AsyncCommand cmd;

        // No args => show help
        if (args.length < 1) {
            cmd = new HelpCmd();
        } else {
            switch (args[0].toLowerCase()) {
                case "show":
                    if (!Util.hasPerm(sender, "show.self") && !Util.hasPerm(sender, "show.all"))
                        return noPerm(sender, command);
                    cmd = new ShowCmd();
                    break;
                case "tp":
                    if (!Util.hasPerm(sender, "tp")) return noPerm(sender, command);
                    cmd = new TeleportCmd();
                    break;
                case "create":
                    if (!Util.hasPerm(sender, "create")) return noPerm(sender, command);
                    cmd = new CreateCmd(minWordCount);
                    break;
                case "reply":
                    if (!Util.hasPerm(sender, "reply.self") && !Util.hasPerm(sender, "reply.all"))
                        return noPerm(sender, command);
                    cmd = new ReplyCmd(minWordCount);
                    break;
                case "list":
                    if (!Util.hasPerm(sender, "list")) return noPerm(sender, command);
                    cmd = new ListCmd();
                    break;
                case "close":
                    if (!Util.hasPerm(sender, "close.self") && !Util.hasPerm(sender, "close.all"))
                        return noPerm(sender, command);
                    cmd = new StatusChangeCmd(false);
                    break;
                case "reopen":
                    if (!Util.hasPerm(sender, "reopen.self")
                            && !Util.hasPerm(sender, "reopen.all"))
                        return noPerm(sender, command);
                    cmd = new StatusChangeCmd(true);
                    break;
                default:
                    cmd = new HelpCmd();
            }
        }

        cmd.init(plugin, this, sender, command, label, args);
        cmd.runTaskAsynchronously(plugin);

        return true;
    }
}
