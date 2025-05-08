package com.tentomax.models;

import com.tentomax.managers.TeamManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class Team {
    private String name;
    private String prefix;
    private ChatColor color;
    private boolean isPrivate;

    private Set<UUID> members = new HashSet<>();
    private Set<Team> allies = new HashSet<>();
    private Set<UUID> joinRequests = new HashSet<>();
    private Map<UUID, TeamRole> roles = new HashMap<>();

    private boolean globalPvP = true;
    private boolean teamPvP = false;
    private boolean allyPvP = false;

    public Team(String name) {
        this.name = name;
        this.prefix = name;
        this.color = ChatColor.WHITE;
        this.isPrivate = true;
    }

    public String getName() { return name; }
    public String getPrefix() { return prefix; }
    public ChatColor getColor() { return color; }
    public boolean isPrivate() { return isPrivate; }
    public boolean isGlobalPvP() { return globalPvP; }
    public boolean isTeamPvP() { return teamPvP; }
    public boolean isAllyPvP() { return allyPvP; }

    public Set<UUID> getMembers() { return members; }
    public Set<Team> getAllies() { return allies; }
    public Set<UUID> getJoinRequests() { return joinRequests; }

    public TeamRole getRole(UUID pUUID){
        return roles.getOrDefault(pUUID, TeamRole.MEMBER);
    }

    public void setRole(UUID pUUID, TeamRole pRole){
        roles.put(pUUID, pRole);
    }

    public void setPrefix(String prefix) { this.prefix = prefix; }
    public void setColor(ChatColor color) { this.color = color; }
    public void setPrivate(boolean aPrivate) { isPrivate = aPrivate; }

    public void setGlobalPvP(boolean globalPvP) { this.globalPvP = globalPvP; }
    public void setTeamPvP(boolean teamPvP) { this.teamPvP = teamPvP; }
    public void setAllyPvP(boolean allyPvP) { this.allyPvP = allyPvP; }

    public boolean isMember(UUID uuid) { return members.contains(uuid); }

    public void demoteCompletely(UUID uuid) {
        setRole(uuid, TeamRole.byRank(0));
    }

    public void addMember(UUID uuid) { members.add(uuid); }
    public void removeMember(UUID uuid) {
        members.remove(uuid);
        if(getRole(uuid)==TeamRole.OWNER){
            setRole(findSecondRankPlayer(),TeamRole.OWNER);
        }
        if(members.isEmpty())TeamManager.deleteTeam(name);
    }

    private UUID findSecondRankPlayer() {
        int highestRank = TeamRole.values().length;

        for (int i = highestRank; i >= 0 ; i--) {
            TeamRole tr = TeamRole.byRank(i);
            for(UUID member : getMembers()){
                if(getRole(member)==tr){
                    return member;
                }
            }
        }
        return null;
    }

    public boolean hasMember(UUID pUUID) {
        return members.contains(pUUID);
    }

    public boolean hasPrivilege(@NotNull UUID uniqueId, Privilege privilege) {
        return getRole(uniqueId).hasPrivilege(privilege);
    }

    public void sendMessage(String s) {
        for(UUID member : getMembers()){
            try{
                Bukkit.getServer().getPlayer(member).sendMessage(s);
            }catch (Exception e){
                //eier lecken
            }
        }
    }

    public void sendMessage(String s, Privilege privilege) {
        for(UUID member : getMembers()){
            if(getRole(member).hasPrivilege(privilege)){
                try{
                    Bukkit.getServer().getPlayer(member).sendMessage(s);
                }catch (Exception e){
                    //eier lecken
                }
            }
        }
    }
}
