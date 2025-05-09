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
        ConfigurationSection teamsSection = dataConfig.createSection("teams");

        for (Map.Entry<String, Team> entry : TeamManager.getTeams().entrySet()) {
            String key = entry.getKey();
            Team team = entry.getValue();

            String path = "teams." + key;
            ConfigurationSection teamSection = dataConfig.createSection(path);

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
            Bukkit.getLogger().severe("Failed to save teams: " + e.getMessage());
            e.printStackTrace();
        }
    }


    public static void loadTeams() {
        Set<UUID> addedUsers = new HashSet<>();


        TeamManager.getTeams().clear();

        ConfigurationSection teamsSection = dataConfig.getConfigurationSection("teams");
        if (teamsSection == null) return;

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
                team.setColor(ChatColor.valueOf(colorString));
            } catch (IllegalArgumentException e) {
                Bukkit.getLogger().warning("Invalid color '" + colorString + "' for team " + teamName + ", defaulting to WHITE.");
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
                        if(addedUsers.contains(uuid))throw new Exception("Already in another team");
                        String roleName = membersSection.getString(uuidString, "MEMBER");
                        TeamRole role = TeamRole.valueOf(roleName);
                        team.addMember(uuid);
                        team.setRole(uuid, role);
                        addedUsers.add(uuid);
                    } catch (Exception e) {
                        Bukkit.getLogger().warning("Error loading member " + uuidString + " in team " + teamName + ": " + e.getMessage());
                    }
                }
            }

            // Load allies
            List<String> alliesList = teamSection.getStringList("allies");
            for(String ally : alliesList){
                if(ally.equals(teamName)){
                    Bukkit.getLogger().warning(teamName+ " allied to itself in files");
                    continue;
                }
                team.getAlliesByName().add(ally);
            }

            // Load join requests
            List<String> requestList = teamSection.getStringList("joinRequests");
            for (String uuidString : requestList) {
                try {
                    team.getJoinRequests().add(UUID.fromString(uuidString));
                } catch (IllegalArgumentException e) {
                    Bukkit.getLogger().warning("Invalid UUID in joinRequests: " + uuidString + " for team " + teamName);
                }
            }

            if(team.getMembers().isEmpty()){
                Bukkit.getLogger().warning("Team "+teamName+" is empty and will not be loaded");
            }else{
                TeamManager.getTeams().put(teamName, team);
            }
        }
    }

}
