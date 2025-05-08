package com.tentomax.managers;

import com.tentomax.models.Team;
import com.tentomax.models.TeamRole;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;

public class PersistenceManager {
    //todo check
    private static final File dataFile = new File("plugins/Tentofactions/teams.yml");
    private static final YamlConfiguration dataConfig = YamlConfiguration.loadConfiguration(dataFile);

    public static void saveTeams() {
        // Serialize teams and save to dataConfig
        // Example:
        for (Map.Entry<String, Team> entry : TeamManager.getTeams().entrySet()) {
            String path = "teams." + entry.getKey();
            Team team = entry.getValue();
            dataConfig.set(path + ".name", team.getName());
            dataConfig.set(path + ".prefix", team.getPrefix());
            dataConfig.set(path + ".color", team.getColor().name());
            dataConfig.set(path + ".private", team.isPrivate());

            // Save members and roles
            ConfigurationSection membersSection = dataConfig.createSection(path + ".members");
            for (Map.Entry<UUID, TeamRole> memberEntry : team.getRoles().entrySet()) {
                membersSection.set(memberEntry.getKey().toString(), memberEntry.getValue().name());
            }
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
            String colorString = dataConfig.getString(path + ".color");
            boolean isPrivate = dataConfig.getBoolean(path + ".private");
            // Create and add team to teams map
            Team team = new Team(name);
            team.setPrefix(prefix);
            team.setColor(colorString != null ? ChatColor.valueOf(colorString) : ChatColor.WHITE); // Default to WHITE if color is null
            team.setPrivate(isPrivate);

            // Load members and roles
            ConfigurationSection membersSection = dataConfig.getConfigurationSection(path + ".members");
            if (membersSection != null) {
                for (String memberKey : membersSection.getKeys(false)) {
                    try {
                        UUID memberUUID = UUID.fromString(memberKey);
                        String roleName = membersSection.getString(memberKey);
                        TeamRole role = TeamRole.valueOf(roleName); //changed to valueOf
                        team.addMember(memberUUID); // Ensure the member is added to the team.
                        team.setRole(memberUUID, role);
                    } catch (IllegalArgumentException e) {
                        Bukkit.getLogger().warning("Invalid role '" + membersSection.getString(memberKey) + "' for player " + memberKey + " in team " + name + ". Skipping.");
                        // Handle the error, e.g., log a warning, skip the player, or set a default role.
                    } catch (NullPointerException e){
                        Bukkit.getLogger().warning("Invalid player UUID  " +  memberKey + " in team " + name + ". Skipping.");
                    }
                }
            }
            TeamManager.getTeams().put(name, team);
        }
    }
}
