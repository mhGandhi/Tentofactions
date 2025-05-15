package com.tentomax.models;

import com.tentomax.managers.TeamManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static com.tentomax.managers.TeamManager.getAllies;
import static com.tentomax.managers.TeamManager.updateNameTag;

public class Team {//todo vanilla team system for better persistence
    private String name;
    private String prefix;
    private ChatColor color;
    private boolean isPrivate;

    private Set<UUID> members = new HashSet<>();
    private Set<String> alliesByName = new HashSet<>();
    private Set<UUID> joinRequests = new HashSet<>();
    private Map<UUID, TeamRole> roles = new HashMap<>();
    public Map<UUID,TeamRole> getRoles(){
        return this.roles;
    }

    private boolean globalPvP = true;
    private boolean teamPvP = true;
    private boolean allyPvP = true;

    public Team(String name) {
        this.name = name;
        setPrefix(name);
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
    public Set<String> getAlliesByName() { return alliesByName; }
    public Set<UUID> getJoinRequests() { return joinRequests; }

    public TeamRole getRole(UUID pUUID){
        return roles.getOrDefault(pUUID, TeamRole.MEMBER);
    }

    //todo show role in tag
    public void setRole(UUID pUUID, TeamRole pRole){
        roles.put(pUUID, pRole);
    }

    public static final int NAME_MAXLEN = 25;
    public static final int PREF_MAXLEN = 15;

    public void setPrefix(String prefix) {
        if(prefix.length()>PREF_MAXLEN)
            prefix = prefix.substring(0,PREF_MAXLEN);

        this.prefix = prefix;
    }

    public void setColor(ChatColor color) {
        this.color = color;

        for(String al : ALBANIEN){
            if(al.equalsIgnoreCase(getPrefix())||al.equalsIgnoreCase(getName())){
                this.color = ChatColor.DARK_RED;//todo schwarz rot
            }
        }
    }
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

    public String info(){
        StringBuilder ret = new StringBuilder();
        ret.append("--------------------------------------------\n");
        ret.append("Team Info:\n");
        ret.append("Name: ").append(this).append("\n");
        ret.append(this.isPrivate()?"Private":"Public").append("\n");
        ret.append("Prefix -> ").append(this.getPrefix()).append("\n");
        ret.append("PVP: [global: ").append(this.isGlobalPvP())
                .append("] [team: ").append(this.isTeamPvP())
                .append("] [ally: ").append(this.isAllyPvP()).append("]\n");

        ret.append("Players:\n");
        for (UUID member : this.getMembers()) {
            Player pl = Bukkit.getPlayer(member);
            if (pl != null) {
                ret.append(" - ").append(pl.getName())
                        .append(" (").append(this.getRole(pl.getUniqueId())).append(")\n");
            }
        }

        Set<UUID> joinRequests = this.getJoinRequests();
        if(!joinRequests.isEmpty()){
            ret.append("Join Requests:\n");
            for (UUID request : this.getJoinRequests()) {
                Player pl = Bukkit.getPlayer(request);
                if (pl != null) {
                    ret.append(" - ").append(pl.getName()).append("\n");
                }
            }
        }

        Set<Team> allies = getAllies(this);
        if(!allies.isEmpty()){
            ret.append("Allies:\n");
            for (Team team : getAllies(this)) {
                ret.append(" - ").append(team).append("\n");
            }
        }

        Set<String> alliesByName = this.getAlliesByName();
        if(!alliesByName.isEmpty()){
            ret.append("Pending Allies:\n");
            for (String team : alliesByName) {
                ret.append(" - ").append(team).append("\n");
            }
        }
        ret.append("--------------------------------------------\n");

        return ret.toString();
    }

    @Override
    public String toString() {
        return getColor()+getName()+ChatColor.RESET;
    }


    public void beeFix(){

    }

    private static final List<String> ALBANIEN = List.of(
            // Standard country names
            "albania", "albanien", "shqipëria", "shqiperia",

            // ISO and international codes
            "al", "alb", "sqi", "sqp", "sq",

            // Language and nationality
            "albanian", "albanians", "shqiptar", "shqiptare", "gjuha shqipe",

            // Kosovo & regional overlap
            "kosovar", "kosovare", "kosovari", "dardani", "dardanian",

            // Dialectal, poetic, or historical forms
            "shqipni", "shqypni", "arbëri", "arbëria", "arberia", "arbani", "arbana", "arbnesh",

            // Phonetic spellings (from misheard or spoken form)
            "albenia", "albaina", "albaniya", "albahnia", "albanija", "albanía", "albanía", "albaania",

            // Common typos and misspellings
            "albnia", "alabania", "albainia", "alabaina", "albanina", "albani", "albaini", "albanea",

            // Slang and diaspora lingo
            "albo", "albi", "albos", "albs", "alboz", "albking", "albqueen", "albprincess",
            "albanianz", "albania4life", "teamalbania", "alblife", "proudalbanian", "albfam",
            "albdude", "albchick", "albcru", "albcrew", "albunit", "redblack", "rednblack",

            // Social media & emoji use
            "albania🇦🇱", "🇦🇱", ":albania:", "#albania", "#shqip", "#shqiperia", "albania_forever",

            // Cultural identifiers
            "eagle", "eagles", "doubleeagle", "besa", "kuqezi", "kuq e zi", "kuqezinjte", "illyrian", "illyrians",

            // Common domain-style or game tags
            "albania123", "xalbaniax", "albania_king", "xxalbxx", "albaniax", "albania.pro",

            // National identifiers from old documents
            "republika e shqipërisë", "rpsh", "rsa", "rs", "al-shqipëri", "repubblica d’albania"
    );
}
