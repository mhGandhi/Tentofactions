package com.tentomax;

import com.mojang.brigadier.CommandDispatcher;
import com.tentomax.commands.*;
import com.tentomax.listeners.ChatListener;
import com.tentomax.listeners.PvPListener;
import com.tentomax.managers.PersistenceManager;
import com.tentomax.managers.TeamManager;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.Bukkit;
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

    persistence
    check if teams exist on startup
    */

    @Override
    public void onEnable() {
        instance = this;
        PersistenceManager.loadTeams();

        getServer().getPluginManager().registerEvents(new PvPListener(), this);
        getServer().getPluginManager().registerEvents(new ChatListener(), this);

        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, BrigadierCommands::register);

        getLogger().info("Tentofactions enabled.");
    }

    @Override
    public void onDisable() {
        PersistenceManager.saveTeams();
    }

    public static Main getInstance() {
        return instance;
    }
}
