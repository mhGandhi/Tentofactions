package com.tentomax.managers;

import com.tentomax.models.ChatMode;
import com.tentomax.models.Team;
import com.tentomax.models.TeamAttributes;
import com.tentomax.models.TeamRole;
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

    public static void createTeam(Player creator, String name) throws Exception {

        if(playerInTeam(creator)) throw new Exception("Already in a Team");
        if (teams.containsKey(name)) throw new Exception("Team \""+name+"\" already exists");

        Team team = new Team(name, creator.getUniqueId());
        teams.put(name, team);

        forceJoinTeam(creator, name);
    }

    public static void joinTeam(Player player, String teamName) throws Exception {
        if(playerInTeam(player))throw new Exception("Already in a Team");

        Team team = teams.get(teamName);
        if (team == null) throw new Exception("Team \""+teamName+"\" doesn't exists");

        if (team.isPrivate()) {
            team.getJoinRequests().add(player.getUniqueId());
        } else {
            team.addMember(player.getUniqueId());
        }
    }

    public static void forceJoinTeam(Player player, String teamName) throws Exception {
        if(playerInTeam(player))throw new Exception("Already in a Team");

        Team team = teams.get(teamName);
        team.addMember(player.getUniqueId());
    }

    public static void leaveTeam(Player player) throws Exception {
        UUID uuid = player.getUniqueId();
        Team playerTeam = getPlayersTeam(uuid);
        if (playerTeam == null) throw new Exception("Player not in any team");

        playerTeam.removeMember(uuid);
    }

    public static void modifyAttribute(Player player, String attr, String value) throws Exception {
        Team team = getPlayersTeam(player.getUniqueId());
        if (team == null) throw new Exception("Player not in any team");

        TeamAttributes ta = TeamAttributes.byCommand(attr.toLowerCase());

        if(ta == null){
            String ex = "";
            for (TeamAttributes teamAttribute: TeamAttributes.values()) {
                ex += teamAttribute.command+"\n";
            }
            ex += (attr+" not a valid attribute");
            throw new Exception(ex);
        }

        try{
            switch (ta) {
                case PREFIX: team.setPrefix(value); break;
                case COLOR: team.setColor(org.bukkit.ChatColor.valueOf(value.toUpperCase())); break;
                case PRIVATE: team.setPrivate(Boolean.parseBoolean(value)); break;
            }
        }catch(Exception e){
            throw new Exception("Invalid Value");
        }

    }

    public static Team getTeam(String name) { return teams.get(name); }

    public static void promoteMember(Player executor, Player target) throws Exception {
        Team team = getPlayersTeam(executor.getUniqueId());
        if (team == null || !team.isMember(target.getUniqueId())) {
            throw new Exception("both players must be in the same team");
        }

        if(isAbove(team, executor.getUniqueId(), target.getUniqueId())){
            TeamRole targetRole;
            if(team.isElder(target.getUniqueId())){
                targetRole = TeamRole.GRAND_DUKE;
            }else{
                targetRole = TeamRole.ELDER;
            }

            team.promote(target.getUniqueId(), targetRole); // Example promotion
            executor.sendMessage(ChatColor.GREEN + "Promoted " + target.getName() + " to "+targetRole+".");
            target.sendMessage(ChatColor.GREEN + "You have been promoted to "+targetRole+" Elder.");
        }else{
            throw new Exception("Player must outrank Target");
        }
    }

    public static void demoteMember(Player executor, Player target) throws Exception {
        Team team = getPlayersTeam(executor.getUniqueId());
        if (team == null || !team.isMember(target.getUniqueId())) {
            throw new Exception("both players must be in the same team");
        }

        if(isAbove(team, executor.getUniqueId(), target.getUniqueId())){

            team.demote(target.getUniqueId()); // Example promotion
            executor.sendMessage(ChatColor.GREEN + "Demoted " + target.getName() + ".");
            target.sendMessage(ChatColor.GREEN + "You have been demoted.");
        }else{
            throw new Exception("Player must outrank Target");
        }
    }

    public static boolean isAbove(Team team, UUID exec, UUID target){//todo
        return team.getOwner() == exec
                || (team.isGrandDuke(exec) && team.getOwner() != target && !team.isGrandDuke(exec));
    }

    private static final Map<UUID, ChatMode> chatModes = new HashMap<>();

    public static void setChatMode(UUID playerId, ChatMode mode) {
        chatModes.put(playerId, mode);
    }

    public static ChatMode getChatMode(UUID playerId) {
        return chatModes.getOrDefault(playerId, ChatMode.PUBLIC);
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
            Team team = new Team(name, UUID.randomUUID()); // Replace with actual owner UUID
            team.setPrefix(prefix);
            team.setColor(color);
            team.setPrivate(isPrivate);
            teams.put(name, team);
        }
    }


    public static void deleteTeam(String name) {
        teams.remove(name);
    }
}
