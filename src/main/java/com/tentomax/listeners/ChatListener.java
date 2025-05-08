package com.tentomax.listeners;

import com.tentomax.Main;
import com.tentomax.managers.ChatManager;
import com.tentomax.managers.TeamManager;
import com.tentomax.models.ChatMode;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import com.tentomax.models.Team;

import java.util.UUID;

import static com.tentomax.managers.TeamManager.getAllies;

public class ChatListener implements Listener {
    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        Team team = TeamManager.getPlayersTeam(player.getUniqueId());
        if (team == null) return;
        String message = team.getColor() + "[" + team.getPrefix() + "] " + event.getMessage();

        ChatMode mode = ChatManager.getChatMode(player.getUniqueId());

        if (mode == ChatMode.PUBLIC){
            event.setMessage(message);
            return;
        }

        message = "<"+ player.getName() +"> "+ message;

        event.setCancelled(true);

        if (mode == ChatMode.TEAM) {
            message = "(Team)"+message;
            for (UUID memberId : team.getMembers()) {
                Player member = Bukkit.getPlayer(memberId);
                if (member != null) member.sendMessage(message);
            }
        } else if (mode == ChatMode.ALLY) {
            message = "(Ally)"+message;
            for (UUID memberId : team.getMembers()) {
                Player member = Bukkit.getPlayer(memberId);
                if (member != null) member.sendMessage(message);
            }
            for (Team allyTeam : getAllies(team)) {
                if (allyTeam != null) {
                    for (UUID memberId : allyTeam.getMembers()) {
                        Player member = Bukkit.getPlayer(memberId);
                        if (member != null) member.sendMessage(message);
                    }
                }
            }
        }
    }

}
