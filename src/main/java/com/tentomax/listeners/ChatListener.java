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
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.UUID;

import static com.tentomax.managers.TeamManager.getAllies;
import static com.tentomax.managers.TeamManager.updateNameTag;

public class ChatListener implements Listener {
    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        Team team = TeamManager.getPlayersTeam(player.getUniqueId());
        if (team == null) return;
        event.setCancelled(true);

        String message = team.getColor() + "[" + team.getPrefix() + "]"+ChatColor.RESET+" <"+ player.getName() +"> "+ event.getMessage();

        ChatMode mode = ChatManager.getChatMode(player.getUniqueId());

        if (mode == ChatMode.PUBLIC){
            for(Player p : Bukkit.getServer().getOnlinePlayers()){
                p.sendMessage(message);
            }
        }

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

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        updateNameTag(player);
    }

}
