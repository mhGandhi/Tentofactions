package com.tentomax.managers;

import com.tentomax.commands.CException;
import com.tentomax.models.ChatMode;
import com.tentomax.models.Team;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class TeamManager {
    private static final Map<String, Team> teams = new HashMap<>();

    private static final Map<UUID, ChatMode> chatModes = new HashMap<>();

    public static void setChatMode(UUID playerId, ChatMode mode) {
        chatModes.put(playerId, mode);
    }

    public static ChatMode getChatMode(UUID playerId) {
        return chatModes.getOrDefault(playerId, ChatMode.PUBLIC);
    }


    public static Team getPlayersTeam(UUID pUUID){
        for (Team t: teams.values()) {
            if(t.hasMember(pUUID))return t;
        }
        return null;
    }

    public static boolean playerInTeam(Player pPlayer){
        for (Team tm : teams.values()) {
            for(UUID uuid : tm.getMembers()){
                if(uuid == pPlayer.getUniqueId())return true;
            }
        }
        return false;
    }

    public static Team getTeam(String name) { return teams.get(name); }

    public static void deleteTeam(String name) {
        teams.remove(name);
    }

    //todo check
    private static final File dataFile = new File("plugins/TeamAlliance/teams.yml");
    private static final YamlConfiguration dataConfig = YamlConfiguration.loadConfiguration(dataFile);

    public static void saveTeams() {
        // Serialize teams and save to dataConfig
        // Example:
        for (Map.Entry<String, Team> entry : teams.entrySet()) {
            String path = "teams." + entry.getKey();
            Team team = entry.getValue();
            dataConfig.set(path + ".name", team.getName());
            dataConfig.set(path + ".prefix", team.getPrefix());
            dataConfig.set(path + ".color", team.getColor().name());
            dataConfig.set(path + ".private", team.isPrivate());
            // Add other attributes as needed
        }
        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void loadTeams() {
        // Load teams from dataConfig
        ConfigurationSection teamsSection = dataConfig.getConfigurationSection("teams");
        if (teamsSection == null) return;
        for (String key : teamsSection.getKeys(false)) {
            String path = "teams." + key;
            String name = dataConfig.getString(path + ".name");
            String prefix = dataConfig.getString(path + ".prefix");
            ChatColor color = ChatColor.valueOf(dataConfig.getString(path + ".color"));
            boolean isPrivate = dataConfig.getBoolean(path + ".private");
            // Create and add team to teams map
            Team team = new Team(name);
            team.setPrefix(prefix);
            team.setColor(color);
            team.setPrivate(isPrivate);
            teams.put(name, team);
            //todo add ppl
        }
    }

    public static Map<String, Team> getTeams() {
        return teams;
    }
}
