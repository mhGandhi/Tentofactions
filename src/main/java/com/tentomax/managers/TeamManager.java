package com.tentomax.managers;

import com.tentomax.models.Team;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;

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
            if(tm.getMembers().contains(pPlayer.getUniqueId()))return true;
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

    public static boolean isAlly(Team t1, Team t2){
        return (t1.getAlliesByName().contains(t2.getName()) && t2.getAlliesByName().contains(t1.getName()));
    }

    public static Set<Team> getAllies(Team pTeam){
        Set<Team> ret = new HashSet<>();

        for(Team t : getTeams().values()){
            if(isAlly(pTeam, t))ret.add(t);
        }

        return ret;
    }


    //todo dumm, so viele teams aber hihi
    public static void updateNameTag(Player player) {
        Team team = getPlayersTeam(player.getUniqueId());
        String prefix;
        if(team == null){
            prefix = "";
        }else{
            prefix = team.getColor()+"["+team.getPrefix()+"] "+ChatColor.RESET;
        }

        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        String iTeamName = "prefix_" + player.getUniqueId(); // Unique team name for each player

        org.bukkit.scoreboard.Team iTeam = scoreboard.getTeam(iTeamName);
        if (iTeam == null) {
            iTeam = scoreboard.registerNewTeam(iTeamName);
        }

        iTeam.setPrefix(prefix);
        iTeam.addEntry(player.getName());

        // Make sure the team is applied to the player (though usually the main scoreboard is active)
        player.setScoreboard(scoreboard);
    }

    public static void updateNameTag(Team pTeam) {
        pTeam.getMembers().stream().forEach((uuid)->{
            Player p = Bukkit.getServer().getPlayer(uuid);
            if(p!=null)updateNameTag(p);
        });
    }
}
