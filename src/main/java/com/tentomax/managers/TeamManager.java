package com.tentomax.managers;

import com.tentomax.models.Team;
import org.bukkit.entity.Player;

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
}
