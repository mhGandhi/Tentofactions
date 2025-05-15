package com.tentomax.managers;

import com.tentomax.models.Team;
import com.tentomax.models.TeamRole;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class PersistenceManager {
    //todo check (GPT work)
    private static final File dataFile = new File("plugins/Tentofactions/teams.yml");
    private static final YamlConfiguration dataConfig = YamlConfiguration.loadConfiguration(dataFile);

    public static void saveTeams() {
        // Ensure directory and file exist
        try {
            if (!dataFile.exists()) {
                dataFile.getParentFile().mkdirs();
                dataFile.createNewFile();
            }
        } catch (IOException e) {
            Bukkit.getLogger().severe("Failed to create data file: " + e.getMessage());
            return;
        }

        // Clear previous teams section if exists
        dataConfig.set("teams", null);
        ConfigurationSection teamsSection = dataConfig.createSection("teams");

        for (Map.Entry<String, Team> entry : TeamManager.getTeams().entrySet()) {
            String key = entry.getKey();
            Team team = entry.getValue();

            String path = "teams." + key;
            ConfigurationSection teamSection = teamsSection.createSection(key);

            teamSection.set("name", team.getName());
            teamSection.set("prefix", team.getPrefix());
            teamSection.set("color", team.getColor().name());
            teamSection.set("private", team.isPrivate());
            teamSection.set("globalPvP", team.isGlobalPvP());
            teamSection.set("teamPvP", team.isTeamPvP());
            teamSection.set("allyPvP", team.isAllyPvP());

            // Save members and roles
            ConfigurationSection membersSection = teamSection.createSection("members");
            for (Map.Entry<UUID, TeamRole> member : team.getRoles().entrySet()) {
                membersSection.set(member.getKey().toString(), member.getValue().name());
            }

            // Save allies
            teamSection.set("allies", new ArrayList<>(team.getAlliesByName()));

            // Save join requests
            List<String> requestList = new ArrayList<>();
            for (UUID req : team.getJoinRequests()) {
                requestList.add(req.toString());
            }
            teamSection.set("joinRequests", requestList);
        }

        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            Bukkit.getLogger().severe("Failed to save teams to file: " + e.getMessage());
            e.printStackTrace();
        }
    }


    public static void loadTeams() {
        TeamManager.getTeams().clear();
        Set<UUID> addedUsers = new HashSet<>();

        // Reload configuration from file
        YamlConfiguration loadedConfig = YamlConfiguration.loadConfiguration(dataFile);
        ConfigurationSection teamsSection = loadedConfig.getConfigurationSection("teams");
        if (teamsSection == null) {
            Bukkit.getLogger().warning("No teams found in file.");
            return;
        }

        for (String key : teamsSection.getKeys(false)) {
            ConfigurationSection teamSection = teamsSection.getConfigurationSection(key);
            if (teamSection == null) continue;

            String teamName = teamSection.getString("name", key);
            String prefix = teamSection.getString("prefix", teamName);
            String colorString = teamSection.getString("color", "WHITE");
            boolean isPrivate = teamSection.getBoolean("private", true);
            boolean globalPvP = teamSection.getBoolean("globalPvP", true);
            boolean teamPvP = teamSection.getBoolean("teamPvP", false);
            boolean allyPvP = teamSection.getBoolean("allyPvP", false);

            Team team = new Team(teamName);
            team.setPrefix(prefix);
            try {
                team.setColor(ChatColor.valueOf(colorString.toUpperCase()));
            } catch (IllegalArgumentException e) {
                Bukkit.getLogger().warning("Invalid color '" + colorString + "' for team " + teamName + ". Defaulting to WHITE.");
                team.setColor(ChatColor.WHITE);
            }
            team.setPrivate(isPrivate);
            team.setGlobalPvP(globalPvP);
            team.setTeamPvP(teamPvP);
            team.setAllyPvP(allyPvP);

            // Load members and roles
            ConfigurationSection membersSection = teamSection.getConfigurationSection("members");
            if (membersSection != null) {
                for (String uuidString : membersSection.getKeys(false)) {
                    try {
                        UUID uuid = UUID.fromString(uuidString);
                        if (!addedUsers.add(uuid)) {
                            throw new IllegalStateException("Player is already in another team");
                        }

                        String roleName = membersSection.getString(uuidString, "MEMBER");
                        TeamRole role = TeamRole.valueOf(roleName.toUpperCase());
                        team.addMember(uuid);
                        team.setRole(uuid, role);
                    } catch (Exception e) {
                        Bukkit.getLogger().warning("Failed to load member " + uuidString + " in team " + teamName + ": " + e.getMessage());
                    }
                }
            }

            // Load allies
            List<String> allies = teamSection.getStringList("allies");
            for (String ally : allies) {
                if (ally.equals(teamName)) {
                    Bukkit.getLogger().warning("Team " + teamName + " cannot ally with itself.");
                    continue;
                }
                team.getAlliesByName().add(ally);
            }

            // Load join requests
            List<String> requests = teamSection.getStringList("joinRequests");
            for (String uuidString : requests) {
                try {
                    team.getJoinRequests().add(UUID.fromString(uuidString));
                } catch (IllegalArgumentException e) {
                    Bukkit.getLogger().warning("Invalid UUID in joinRequests for team " + teamName + ": " + uuidString);
                }
            }

            // Only add team if it has members
            if (team.getMembers().isEmpty()) {
                Bukkit.getLogger().warning("Team " + teamName + " has no members and will be skipped.");
            } else {
                TeamManager.getTeams().put(teamName, team);
            }
        }
    }


}
