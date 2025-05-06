package com.tentomax.commands;

import com.tentomax.managers.TeamManager;
import com.tentomax.models.ChatMode;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DefaultChatCommand implements CommandExecutor {

    public DefaultChatCommand() {
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)){
            sender.sendMessage("Must be executed by player");
            return false;
        }

        TeamManager.setChatMode(player.getUniqueId(), ChatMode.PUBLIC);
        player.sendMessage(ChatColor.GREEN + "Default Chat enabled.");
        return true;
    }
}
