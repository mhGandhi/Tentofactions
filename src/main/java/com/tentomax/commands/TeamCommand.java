package com.tentomax.commands;

import com.tentomax.managers.ChatManager;
import com.tentomax.managers.TeamManager;
import com.tentomax.models.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.UUID;

import static com.tentomax.managers.TeamManager.*;
import static com.tentomax.models.Team.NAME_MAXLEN;
import static com.tentomax.models.Team.PREF_MAXLEN;

public class TeamCommand {


    public static void createTeam(Player creator, String name) throws CException {
        if(TeamManager.playerInTeam(creator)) throw new CException("Already in a Team");
        if (TeamManager.getTeams().containsKey(name)) throw new CException("Team \""+name+"\" already exists");

        if(name.length()> NAME_MAXLEN) throw new CException("Team name exceeds max length of "+NAME_MAXLEN);

        Team team = new Team(name);
        TeamManager.getTeams().put(name, team);

        team.setColor(CColor.getRandomCol());

        creator.sendMessage(ChatColor.GREEN + "Team '" + name + "' created.");
        addToTeam(creator, team, true);
        team.setRole(creator.getUniqueId(), TeamRole.OWNER);
    }

    public static void joinTeam(Player player, String teamName) throws CException {
        if(TeamManager.playerInTeam(player))throw new CException("Already in a Team");

        Team team = assertTeam(teamName);

        if (team.isPrivate()) {
            team.getJoinRequests().add(player.getUniqueId());
            player.sendMessage(ChatColor.YELLOW + "Join request sent to " + teamName + ".");
            team.sendMessage(ChatColor.GREEN+"Join request from "+player.getName(), Privilege.ACCEPTING);
        } else {
            addToTeam(player, team, true);
        }
    }

    public static void addToTeam(Player player, Team pTeam, boolean broadcastMessage) throws CException {
        if(TeamManager.playerInTeam(player))throw new CException("Already in a Team");
        if(broadcastMessage){
            pTeam.sendMessage(ChatColor.GREEN + player.getName() + " joined team.");
            player.sendMessage(ChatColor.GREEN + "Joined team " + pTeam.getName() + ".");

        }
        pTeam.addMember(player.getUniqueId());
        updateNameTag(player);
    }

    public static void leaveTeam(Player player, boolean broadcastMessage) throws CException {//todo sepereate to tryleave and leave
        UUID uuid = player.getUniqueId();
        Team playerTeam = assertTeam(player);

        playerTeam.removeMember(uuid);
        ChatManager.setChatMode(player.getUniqueId(), ChatMode.PUBLIC);
        playerTeam.setRole(uuid, TeamRole.MEMBER);
        if(broadcastMessage){
            player.sendMessage("Left team");
            playerTeam.sendMessage(player.getName()+" left team");
        }
        updateNameTag(player);
    }

    public static void modifyAttribute(Player player, String attr, String value) throws CException {
        Team playerTeam = assertTeam(player);

        if(!playerTeam.hasPrivilege(player.getUniqueId(), Privilege.ATTRIBUTES))throw new CException("You do not have attribute privileges");

        TeamAttributes ta = TeamAttributes.byCommand(attr.toLowerCase());
        if(ta == null) throw new CException(attr+" not a valid attribute");

        try{
            switch (ta) {
                case PREFIX:
                    if(value.length()>PREF_MAXLEN)throw new CException("Prefix exceeds max length of "+PREF_MAXLEN);
                    playerTeam.setPrefix(value);
                    updateNameTag(playerTeam);
                    break;
                case COLOR:
                    ChatColor col = CColor.byCommand(value);
                    if(col == null) throw new CException("accepts: "+CColor.allColsString());
                    playerTeam.setColor(col);
                    updateNameTag(playerTeam);
                    break;
                case PRIVATE:
                    playerTeam.setPrivate(parseBool(value));
                    //playerTeam.setPrivate(Boolean.parseBoolean(value));
                    break;
                case TEAM_PVP:
                    playerTeam.setTeamPvP(parseBool(value));
                    break;
                case ALLY_PVP:
                    playerTeam.setAllyPvP(parseBool(value));
                    break;
                case GLOBAL_PVP:
                    playerTeam.setGlobalPvP(parseBool(value));
                    break;
                default:
                    throw new CException(attr+" change via command not supported");
            }
            player.sendMessage(ChatColor.GREEN+attr+" set to "+value);
        }catch(Exception e){
            if(e instanceof CException ce){
                throw ce;
            }else{
                throw new CException("Invalid Value");
            }
        }
    }

    public static void promoteMember(Player player, String pTarget) throws CException {
        Team playerTeam = assertTeam(player);

        Player target = player.getServer().getPlayer(pTarget);
        if(target==null) throw new CException("Player "+pTarget+" not found!");

        if (!playerTeam.isMember(target.getUniqueId())) throw new CException(pTarget+" not in your team");

        assertOutranksByTwo(playerTeam, player, target);


        int currentRank = playerTeam.getRole(target.getUniqueId()).rank;
        TeamRole targetRole = TeamRole.byRank(currentRank+1);

        if(targetRole == null) throw new CException("Please, it's too much promotion, we can't take it anymore it is too much promotion!");

        playerTeam.setRole(target.getUniqueId(), targetRole);

        player.sendMessage(ChatColor.GREEN + "Promoted " + target.getName() + " to "+targetRole+".");
        target.sendMessage(ChatColor.GREEN + "You have been promoted to "+targetRole+".");
    }

    public static void demoteMember(Player player, String pTarget) throws CException {
        Team playerTeam = assertTeam(player);

        Player target = player.getServer().getPlayer(pTarget);
        if(target==null) throw new CException("Player "+pTarget+" not found!");

        if (!playerTeam.isMember(target.getUniqueId())) throw new CException(pTarget+" not in your team");
        if (player.equals(target)) throw new CException("You can not demote yourself.");

        assertOutranks(playerTeam, player, target);

        playerTeam.demoteCompletely(target.getUniqueId()); // Example promotion
        player.sendMessage(ChatColor.GREEN + "Demoted " + target.getName() + ".");
        target.sendMessage(ChatColor.GREEN + "You have been demoted.");
    }

    public static void kick(Player player, String pTarget)throws CException {
        Team playerTeam = assertTeam(player);

        Player target = player.getServer().getPlayer(pTarget);
        if(target==null) throw new CException("Player "+pTarget+" not found!");

        if(target.equals(player)) throw new CException("You can't kick yourself.");

        if(!playerTeam.isMember(target.getUniqueId())) throw new CException("Player "+pTarget+" not in your team.");

        if(!playerTeam.hasPrivilege(player.getUniqueId(), Privilege.KICKING))throw new CException("You do not have kicking privileges");

        assertOutranks(playerTeam, player, target);

        leaveTeam(player, false);
        playerTeam.sendMessage(ChatColor.GREEN + target.getName() + " has been kicked from the team.");
        target.sendMessage(ChatColor.RED + "You were kicked from the team.");
    }

    public static void info(Player player) throws CException {
        Team playerTeam = assertTeam(player);

        player.sendMessage(playerTeam.info());
    }


    public static void accept(Player player, String pTarget) throws CException{
        Team playerTeam = assertTeam(player);

        if(!playerTeam.hasPrivilege(player.getUniqueId(), Privilege.ACCEPTING)) throw new CException("You do not have accepting privileges.");

        Player target = player.getServer().getPlayer(pTarget);
        if(target==null) throw new CException("Player "+pTarget+" not found.");

        if (playerTeam.getJoinRequests().contains(target.getUniqueId())) {
            player.sendMessage(ChatColor.GREEN + "Accepted " + target.getName() + " into the team.");

            playerTeam.getJoinRequests().remove(target.getUniqueId());
            addToTeam(target, playerTeam, true);
        } else {
            throw new CException("No join request from "+pTarget+".");
        }
    }

    public static void reject(Player player, String pTarget) throws CException{
        Team playerTeam = assertTeam(player);

        if(!playerTeam.hasPrivilege(player.getUniqueId(), Privilege.ACCEPTING)) throw new CException("You do not have accepting privileges.");

        Player target = player.getServer().getPlayer(pTarget);
        if(target==null) throw new CException("Player "+pTarget+" not found.");

        if (playerTeam.getJoinRequests().remove(target.getUniqueId())) {
            player.sendMessage(ChatColor.YELLOW + "Rejected " + target.getName() + "'s join request.");
            target.sendMessage(ChatColor.RED + "Your join request to " + playerTeam.getName() + " was rejected.");
        } else {
            throw new CException("No join request from "+pTarget+".");
        }
    }

    public static void allyTeam(Player player, String pTeam) throws CException{
        Team playerTeam = assertTeam(player);
        Team targetTeam = assertTeam(pTeam);

        if(!playerTeam.hasPrivilege(player.getUniqueId(), Privilege.ALLY))throw new CException("You do not have allying privileges.");
        if(playerTeam.getAlliesByName().contains(targetTeam.getName()))throw new CException(targetTeam+" is already on ally list");

        if(targetTeam.getName().equals(playerTeam.getName()))throw new CException("Teams may not ally themselves");

        playerTeam.getAlliesByName().add(targetTeam.getName());
        player.sendMessage("Added "+targetTeam+" to allies");

        if(isAlly(playerTeam, targetTeam)){
            playerTeam.sendMessage(targetTeam+" is now an ally");
            targetTeam.sendMessage(playerTeam+" is now an ally");
        }else{
            targetTeam.sendMessage(playerTeam+" added you to their allies - add them back to become their ally ["+BrigadierCommands.TCL+" ally "+playerTeam+"]", Privilege.ALLY);
        }
    }

    public static void unAllyTeam(Player player, String pTeam) throws CException{
        Team playerTeam = assertTeam(player);
        Team targetTeam = assertTeam(pTeam);

        if(!playerTeam.hasPrivilege(player.getUniqueId(), Privilege.ALLY))throw new CException("You do not have allying privileges.");
        if(!playerTeam.getAlliesByName().contains(targetTeam.getName()))throw new CException(targetTeam+" is not on ally list");

        boolean alliesBefore = isAlly(playerTeam,targetTeam);

        playerTeam.getAlliesByName().remove(targetTeam);
        player.sendMessage("Removed "+targetTeam+" from allies");

        if(alliesBefore){
            playerTeam.sendMessage(targetTeam+" is no longer an ally");
            targetTeam.sendMessage(playerTeam+" is no longer an ally");
        }
    }

    private static boolean parseBool(String pBool)throws Exception{
        if(pBool.equalsIgnoreCase("false")){
            return false;
        } else if (pBool.equalsIgnoreCase("true")) {
            return true;
        }else{
            throw new Exception();
        }
    }

    private static Team assertTeam(Player pPlayer)throws CException{
        Team team = TeamManager.getPlayersTeam(pPlayer.getUniqueId());
        if (team == null) throw new CException("Not in a team");
        return team;
    }

    private static Team assertTeam(String pTeam)throws CException{
        Team targetTeam = TeamManager.getTeams().get(pTeam);
        if (targetTeam == null) throw new CException("Team \""+pTeam+"\" doesn't exists");
        return targetTeam;
    }

    private static void assertOutranks(Team pTeam, Player p1, Player p2) throws CException {
        int rankP1 = pTeam.getRole(p1.getUniqueId()).rank;
        int rankP2 = pTeam.getRole(p2.getUniqueId()).rank;

        if(!(rankP1>rankP2)) throw new CException("You do not outrank "+p2.getName());
    }

    private static void assertOutranksByTwo(Team pTeam, Player p1, Player p2) throws CException {
        int rankP1 = pTeam.getRole(p1.getUniqueId()).rank;
        int rankP2 = pTeam.getRole(p2.getUniqueId()).rank;

        if(!(rankP1>rankP2+1)) throw new CException("You do not outrank "+p2.getName()+" enough");
    }
}
