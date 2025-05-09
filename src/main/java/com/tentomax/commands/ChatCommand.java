package com.tentomax.commands;

import com.tentomax.managers.ChatManager;
import com.tentomax.managers.TeamManager;
import com.tentomax.models.ChatMode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import static com.tentomax.commands.BrigadierCommands.mustBePlayer;

public class ChatCommand {

    public static int setChat(CommandSourceStack pContext, ChatMode chatMode){
        if(!(pContext.getSender() instanceof Player player)){
            return mustBePlayer(pContext);
        }

        if(TeamManager.playerInTeam(player)){
            ChatManager.setChatMode(player.getUniqueId(), chatMode);
            player.sendMessage("chat mode switched to "+chatMode);
            return 1;
        }else{
            player.sendMessage(ChatColor.RED+"You are not in a team.");
            return 0;
        }
    }
}
