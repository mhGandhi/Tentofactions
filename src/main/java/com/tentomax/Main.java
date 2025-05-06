package com.tentomax;

import com.tentomax.commands.AllyChatCommand;
import com.tentomax.commands.DefaultChatCommand;
import com.tentomax.commands.TeamChatCommand;
import com.tentomax.commands.TeamCommand;
import com.tentomax.listeners.ChatListener;
import com.tentomax.listeners.PvPListener;
import com.tentomax.managers.TeamManager;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class Main extends JavaPlugin {

    private static Main instance;

    /*todo
    tablist
    ally commands
    pvp commands

    bessere info
    request, join, leave, kick msg
    persistence
    when owner leaves, transfer ownership
    when leaving, change back to defaultchat
    check whether in team before switching chat
    indicate what chat is used
    check if teams exist on startup

    privilege check

    brigadeer!!!
    */

    @Override
    public void onEnable() {
        instance = this;
        TeamManager.loadTeams();


        getCommand("tentoteam").setExecutor(new TeamCommand());
        getCommand("teamchat").setExecutor(new TeamChatCommand());
        getCommand("allychat").setExecutor(new AllyChatCommand());
        getCommand("defaultchat").setExecutor(new DefaultChatCommand());

        getServer().getPluginManager().registerEvents(new PvPListener(), this);
        getServer().getPluginManager().registerEvents(new ChatListener(), this);

        getLogger().info("Tentofactions enabled.");
    }

    @Override
    public void onDisable() {
        TeamManager.saveTeams();
    }

    public static Main getInstance() {
        return instance;
    }
}
