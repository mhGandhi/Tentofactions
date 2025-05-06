package com.tentomax.models;

import com.tentomax.managers.TeamManager;
import org.bukkit.ChatColor;

import java.util.*;

public class Team {
    private String name;
    private String prefix;
    private ChatColor color;
    private boolean isPrivate;

    private UUID owner;
    private Set<UUID> members = new HashSet<>();
    private Set<UUID> elders = new HashSet<>();
    private Set<UUID> grandDukes = new HashSet<>();
    private Set<Team> allies = new HashSet<>();
    private Set<UUID> joinRequests = new HashSet<>();

    private boolean globalPvP = true;
    private boolean teamPvP = false;
    private boolean allyPvP = false;

    public Team(String name, UUID owner) {
        this.name = name;
        this.prefix = name;
        this.color = ChatColor.WHITE;
        this.isPrivate = true;
        this.owner = owner;
        members.add(owner);
    }

    public String getName() { return name; }
    public String getPrefix() { return prefix; }
    public ChatColor getColor() { return color; }
    public boolean isPrivate() { return isPrivate; }
    public UUID getOwner() { return owner; }
    public boolean isGlobalPvP() { return globalPvP; }
    public boolean isTeamPvP() { return teamPvP; }
    public boolean isAllyPvP() { return allyPvP; }

    public Set<UUID> getMembers() { return members; }
    public Set<UUID> getElders() { return elders; }
    public Set<UUID> getGrandDukes() { return grandDukes; }
    public Set<Team> getAllies() { return allies; }
    public Set<UUID> getJoinRequests() { return joinRequests; }

    public void setPrefix(String prefix) { this.prefix = prefix; }
    public void setColor(ChatColor color) { this.color = color; }
    public void setPrivate(boolean aPrivate) { isPrivate = aPrivate; }

    public void setGlobalPvP(boolean globalPvP) { this.globalPvP = globalPvP; }
    public void setTeamPvP(boolean teamPvP) { this.teamPvP = teamPvP; }
    public void setAllyPvP(boolean allyPvP) { this.allyPvP = allyPvP; }

    public boolean isMember(UUID uuid) { return members.contains(uuid); }

    public void promote(UUID uuid, TeamRole role) {
        if (role == TeamRole.ELDER) elders.add(uuid);
        if (role == TeamRole.GRAND_DUKE) grandDukes.add(uuid);
    }

    public void demote(UUID uuid) {
        elders.remove(uuid);
        grandDukes.remove(uuid);
    }

    public void addMember(UUID uuid) { members.add(uuid); }
    public void removeMember(UUID uuid) {
        members.remove(uuid);
        elders.remove(uuid);
        grandDukes.remove(uuid);
        if(owner == uuid){
            owner = null;
            TeamManager.deleteTeam(name);
        }
    }

    public boolean hasMember(UUID pUUID) {
        if(pUUID == owner)return true;

        for(UUID m : members){
            if(m == pUUID)return true;
        }

        return false;
    }

    public boolean isElder(UUID uniqueId) {
        return getElders().contains(uniqueId);
    }

    public boolean isGrandDuke(UUID exec) {
        return getGrandDukes().contains(exec);
    }
}
