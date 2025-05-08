package com.tentomax.commands;

import com.tentomax.Main;
import com.tentomax.managers.ChatManager;
import com.tentomax.managers.TeamManager;
import com.tentomax.models.*;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.UUID;

import static com.tentomax.commands.BrigadierCommands.mustBePlayer;

public class TeamCommand {


    public static void createTeam(Player creator, String name) throws CException {

        if(TeamManager.playerInTeam(creator)) throw new CException("Already in a Team");
        if (TeamManager.getTeams().containsKey(name)) throw new CException("Team \""+name+"\" already exists");

        Team team = new Team(name);
        TeamManager.getTeams().put(name, team);

        creator.sendMessage(ChatColor.GREEN + "Team '" + name + "' created.");
        addToTeam(creator, team, true);
        team.setRole(creator.getUniqueId(), TeamRole.OWNER);
    }

    public static void joinTeam(Player player, String teamName) throws CException {
        if(TeamManager.playerInTeam(player))throw new CException("Already in a Team");

        Team team = TeamManager.getTeams().get(teamName);
        if (team == null) throw new CException("Team \""+teamName+"\" doesn't exists");

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
    }

    public static void leaveTeam(Player player, boolean broadcastMessage) throws CException {//todo sepereate to tryleave and leave
        UUID uuid = player.getUniqueId();
        Team playerTeam = TeamManager.getPlayersTeam(uuid);
        if (playerTeam == null) throw new CException("Player not in any team");

        playerTeam.removeMember(uuid);
        ChatManager.setChatMode(player.getUniqueId(), ChatMode.PUBLIC);
        playerTeam.setRole(uuid, TeamRole.MEMBER);
        if(broadcastMessage){
            player.sendMessage("Left team");
            playerTeam.sendMessage(player.getName()+" left team");
        }
    }

    public static void modifyAttribute(Player player, String attr, String value) throws CException {
        Team team = TeamManager.getPlayersTeam(player.getUniqueId());
        if (team == null) throw new CException("Player not in any team");

        TeamAttributes ta = TeamAttributes.byCommand(attr.toLowerCase());

        if(ta == null) throw new CException(attr+" not a valid attribute");

        try{
            switch (ta) {
                case PREFIX:
                    team.setPrefix(value);
                    break;
                case COLOR:
                    ChatColor col = CColor.byCommand(value);
                    if(col == null) throw new CException("accepts: "+CColor.allColsString());
                    team.setColor(col);
                    break;
                case PRIVATE:
                    if(value.equalsIgnoreCase("false")){
                        team.setPrivate(false);
                    } else if (value.equalsIgnoreCase("true")) {
                        team.setPrivate(true);
                    }else{
                        throw new Exception();
                    }
                    //team.setPrivate(Boolean.parseBoolean(value));
                    break;
                default:
                    throw new CException(attr+" change via command not supported");
            }
        }catch(Exception e){
            if(e instanceof CException ce){
                throw ce;
            }else{
                throw new CException("Invalid Value");
            }
        }
    }

    public static void promoteMember(Player player, String pTarget) throws CException {
        Team team = TeamManager.getPlayersTeam(player.getUniqueId());
        if (team == null) throw new CException("Not in a Team");

        Player target = player.getServer().getPlayer(pTarget);
        if(target==null) throw new CException("Player "+pTarget+" not found!");

        if (!team.isMember(target.getUniqueId())) throw new CException(pTarget+" not in your team");

        assertOutranksByTwo(team, player, target);


        int currentRank = team.getRole(target.getUniqueId()).rank;
        TeamRole targetRole = TeamRole.byRank(currentRank+1);

        if(targetRole == null) throw new CException("Please, it's too much promotion, we can't take it anymore it is too much promotion!");

        team.setRole(target.getUniqueId(), targetRole);

        player.sendMessage(ChatColor.GREEN + "Promoted " + target.getName() + " to "+targetRole+".");
        target.sendMessage(ChatColor.GREEN + "You have been promoted to "+targetRole+".");
    }

    public static void demoteMember(Player player, String pTarget) throws CException {
        Team team = TeamManager.getPlayersTeam(player.getUniqueId());
        if (team == null) throw new CException("Not in a Team");

        Player target = player.getServer().getPlayer(pTarget);
        if(target==null) throw new CException("Player "+pTarget+" not found!");

        if (!team.isMember(target.getUniqueId())) throw new CException(pTarget+" not in your team");

        assertOutranks(team, player, target);

        team.demoteCompletely(target.getUniqueId()); // Example promotion
        player.sendMessage(ChatColor.GREEN + "Demoted " + target.getName() + ".");
        target.sendMessage(ChatColor.GREEN + "You have been demoted.");
    }

    public static void kick(Player player, String pTarget)throws CException {
        Team team = TeamManager.getPlayersTeam(player.getUniqueId());
        if (team == null) throw new CException("Not in a Team");

        Player target = player.getServer().getPlayer(pTarget);
        if(target==null) throw new CException("Player "+pTarget+" not found!");

        if(target.getUniqueId().equals(player.getUniqueId())) throw new CException("You can't kick yourself.");

        if(!team.isMember(target.getUniqueId())) throw new CException("Player "+pTarget+" not in your team.");

        if(!team.hasPrivilege(player.getUniqueId(), Privilege.KICKING))throw new CException("You do not have kicking privileges");

        assertOutranks(team, player, target);

        leaveTeam(player, false);
        team.sendMessage(ChatColor.GREEN + target.getName() + " has been kicked from the team.");
        target.sendMessage(ChatColor.RED + "You were kicked from the team.");
    }

    public static void info(Player player) throws CException {
        Team team = TeamManager.getPlayersTeam(player.getUniqueId());
        if(team == null) throw new CException(ChatColor.GRAY + "Not in any Team");

        String ret = team.getColor()+"Name -> "+team.getName()
                +"\nPrefix -> "+team.getPrefix()
                +"\nPrivate -> "+team.isPrivate()
                +"\nPlayers:\n";

        for(UUID member : team.getMembers()){
            Player pl = Bukkit.getPlayer(member);
            if(pl!=null)
                ret+=pl.getName()+" - "+team.getRole(pl.getUniqueId());
        }

        player.sendMessage(ret);
    }

    public static void accept(Player player, String pTarget) throws CException{
        Team team = TeamManager.getPlayersTeam(player.getUniqueId());
        if (team == null) throw new CException("Not in a team");

        if(!team.hasPrivilege(player.getUniqueId(), Privilege.ACCEPTING)) throw new CException("You do not have accepting privileges.");

        Player target = player.getServer().getPlayer(pTarget);
        if(target==null) throw new CException("Player "+pTarget+" not found.");

        if (team.getJoinRequests().contains(target.getUniqueId())) {
            player.sendMessage(ChatColor.GREEN + "Accepted " + target.getName() + " into the team.");

            team.getJoinRequests().remove(target.getUniqueId());
            addToTeam(target, team, true);
        } else {
            throw new CException("No join request from "+pTarget+".");
        }
    }

    public static void reject(Player player, String pTarget) throws CException{
        Team team = TeamManager.getPlayersTeam(player.getUniqueId());
        if (team == null) throw new CException("Not in a team");

        if(!team.hasPrivilege(player.getUniqueId(), Privilege.ACCEPTING)) throw new CException("You do not have accepting privileges.");

        Player target = player.getServer().getPlayer(pTarget);
        if(target==null) throw new CException("Player "+pTarget+" not found.");

        if (team.getJoinRequests().remove(target.getUniqueId())) {
            player.sendMessage(ChatColor.YELLOW + "Rejected " + target.getName() + "'s join request.");
            target.sendMessage(ChatColor.RED + "Your join request to " + team.getName() + " was rejected.");
        } else {
            throw new CException("No join request from "+pTarget+".");
        }
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
