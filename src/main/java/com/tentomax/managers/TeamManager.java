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

    public static Map<String, Team> getTeams() {
        return teams;
    }
}
