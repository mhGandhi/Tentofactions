package com.tentomax;

import com.tentomax.commands.*;
import com.tentomax.listeners.ChatListener;
import com.tentomax.listeners.PvPListener;
import com.tentomax.managers.PersistenceManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.plugin.java.JavaPlugin;


public final class Main extends JavaPlugin {

    private static Main instance;

    /*todo
    modify better
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
