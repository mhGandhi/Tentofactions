package com.tentomax.commands;

import com.tentomax.managers.TeamManager;
import com.tentomax.models.Team;
import com.tentomax.models.TeamAttributes;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

import static com.tentomax.managers.TeamManager.getPlayersTeam;
import static com.tentomax.managers.TeamManager.isAbove;

public class TeamCommand implements CommandExecutor {


    public TeamCommand() {
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)){
            sender.sendMessage("Must be executed by player");
            return false;
        }

        if (args.length == 0) {
            player.sendMessage(ChatColor.YELLOW + "- create [name]");
            player.sendMessage(ChatColor.YELLOW + "- join [name]");
            player.sendMessage(ChatColor.YELLOW + "- leave");
            player.sendMessage(ChatColor.YELLOW + "- kick [player]");
            player.sendMessage(ChatColor.YELLOW + "- modify [attribute] [value]");
            player.sendMessage(ChatColor.YELLOW + "- accept [player]");
            player.sendMessage(ChatColor.YELLOW + "- reject [player]");
            player.sendMessage(ChatColor.YELLOW + "- promote [player]");
            player.sendMessage(ChatColor.YELLOW + "- demote [player]");
            player.sendMessage(ChatColor.YELLOW + "- info");
            return true;
        }

        switch (args[0].toLowerCase()) {
            //////////////////////////////////////////////////////////////////////////////
            case "create" -> {
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Usage: - create [team name]");
                    return false;
                }
                String name = args[1];
                try {
                    TeamManager.createTeam(player, name);
                } catch (Exception e) {
                    player.sendMessage(ChatColor.RED + e.getMessage());
                    return false;
                }
                player.sendMessage(ChatColor.GREEN + "Team '" + name + "' created.");
            }
            //////////////////////////////////////////////////////////////////////////////
            case "join" -> {
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Usage: - join [team]");
                    return false;
                }
                String name = args[1];
                try{
                    TeamManager.joinTeam(player, name);
                }catch(Exception e){
                    player.sendMessage(ChatColor.RED + e.getMessage());
                    return false;
                }

                Team team = TeamManager.getTeam(name);
                if(team == null){
                    player.sendMessage(ChatColor.RED + "Team " + name + " doesn't exist.");
                    return false;
                }

                if (team.isPrivate()) {
                    player.sendMessage(ChatColor.YELLOW + "Join request sent to " + name + ".");
                } else {
                    player.sendMessage(ChatColor.GREEN + "Joined team " + name + ".");
                }
            }
            //////////////////////////////////////////////////////////////////////////////
            case "leave" -> {
                try{
                    TeamManager.leaveTeam(player);
                }catch (Exception e){
                    player.sendMessage(ChatColor.RED + e.getMessage());
                    return false;
                }

                player.sendMessage(ChatColor.GREEN + "You have left your team.");
            }
            //////////////////////////////////////////////////////////////////////////////
            case "kick" -> {
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Usage: - kick [player]");
                    return false;
                }
                Team team = getPlayersTeam(player.getUniqueId());
                if (team == null) {
                    player.sendMessage(ChatColor.RED + "You are not in a team.");
                    return true;
                }
                Player target = player.getServer().getPlayer(args[1]);
                if(target==null){
                    player.sendMessage(ChatColor.RED + "Player "+args[1]+" not found.");
                    return false;
                }
                if(target.getUniqueId().equals(player.getUniqueId())){
                    player.sendMessage(ChatColor.RED + "You can't kick yourself.");
                    return false;
                }

                if (team.isMember(target.getUniqueId())) {
                    if(!(team.getOwner()==player.getUniqueId()||team.isGrandDuke(player.getUniqueId()))){
                        player.sendMessage(ChatColor.RED + "You do not have kicking privileges");
                        return false;
                    }

                    if(isAbove(team, player.getUniqueId(), target.getUniqueId())){
                        team.removeMember(target.getUniqueId());
                        player.sendMessage(ChatColor.GREEN + target.getName() + " has been kicked.");
                        target.sendMessage(ChatColor.RED + "You were kicked from the team.");
                        return true;
                    }else{
                        player.sendMessage(ChatColor.RED + "You do not outrank "+target);
                        return false;
                    }
                } else {
                    player.sendMessage(ChatColor.RED + "Player not found or not in your team.");
                    return false;
                }
            }
            //////////////////////////////////////////////////////////////////////////////
            case "modify" -> {
                if (args.length < 3) {
                    player.sendMessage(ChatColor.RED + "Usage: - modify [attribute] [value]");
                    return false;
                }
                try{
                    TeamManager.modifyAttribute(player, args[1], args[2]);
                } catch (Exception e) {
                    player.sendMessage(ChatColor.RED + e.getMessage());
                    return false;
                }

                player.sendMessage(ChatColor.GREEN + "Modified " + args[1] + " to " + args[2]);
                return true;
            }
            //////////////////////////////////////////////////////////////////////////////
            case "accept" -> {
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Usage: - accept [player]");
                    return false;
                }
                Team team = TeamManager.getPlayersTeam(player.getUniqueId());
                if (team == null) {
                    player.sendMessage(ChatColor.RED + "You are not in a team.");
                    return false;
                }
                if(team.isElder(player.getUniqueId())){
                    player.sendMessage(ChatColor.RED + "You do not have accepting privileges.");
                    return false;
                }

                Player target = player.getServer().getPlayer(args[1]);
                if(target==null){
                    player.sendMessage(ChatColor.RED + "Player "+args[1]+" not found.");
                    return false;
                }
                if (team.getJoinRequests().contains(target.getUniqueId())) {
                    team.getJoinRequests().remove(target.getUniqueId());

                    try {
                        TeamManager.forceJoinTeam(target, team.getName());
                    } catch (Exception e) {
                        player.sendMessage(ChatColor.RED + e.getMessage());
                        return false;
                    }

                    player.sendMessage(ChatColor.GREEN + "Accepted " + target.getName() + " into the team.");
                    target.sendMessage(ChatColor.GREEN + "You have joined the team " + team.getName() + "!");
                    return true;
                } else {
                    player.sendMessage(ChatColor.RED + "No join request from that player.");
                    return false;
                }
            }
            //////////////////////////////////////////////////////////////////////////////
            case "reject" -> {
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Usage: - reject [player]");
                    return false;
                }
                Team team = TeamManager.getPlayersTeam(player.getUniqueId());
                if (team == null) {
                    player.sendMessage(ChatColor.RED + "You are not in a team.");
                    return false;
                }
                Player target = player.getServer().getPlayer(args[1]);
                if(target==null){
                    player.sendMessage(ChatColor.RED + "Player "+args[1]+" not found.");
                    return false;
                }
                if (team.getJoinRequests().remove(target.getUniqueId())) {
                    player.sendMessage(ChatColor.YELLOW + "Rejected " + target.getName() + "'s join request.");
                    target.sendMessage(ChatColor.RED + "Your join request to " + team.getName() + " was rejected.");
                    return true;
                } else {
                    player.sendMessage(ChatColor.RED + "No join request from that player.");
                    return false;
                }
            }
            //////////////////////////////////////////////////////////////////////////////
            case "promote" -> {
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Usage: - promote [player]");
                    return false;
                }
                Player targetPromote = Bukkit.getPlayer(args[1]);
                if(targetPromote==null){
                    player.sendMessage(ChatColor.RED + "Player "+args[1]+" not found.");
                    return false;
                }
                try{
                    TeamManager.promoteMember(player, targetPromote);
                } catch (Exception e) {
                    player.sendMessage(ChatColor.RED + e.getMessage());
                    return false;
                }

            }
            //////////////////////////////////////////////////////////////////////////////
            case "demote" -> {
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Usage: - demote [player]");
                    return false;
                }
                Player targetDemote = Bukkit.getPlayer(args[1]);
                if(targetDemote==null){
                    player.sendMessage(ChatColor.RED + "Player "+args[1]+" not found.");
                    return false;
                }
                try {
                    TeamManager.demoteMember(player, targetDemote);
                } catch (Exception e) {
                    player.sendMessage(ChatColor.RED + e.getMessage());
                    return false;
                }
            }
            case "info" -> {
                Team team = TeamManager.getPlayersTeam(player.getUniqueId());
                if(team == null){
                    player.sendMessage(ChatColor.GRAY + "Not in any Team");
                    return true;
                }
                String ret = team.getColor()+"Name -> "+team.getName()
                        +"\nPrefix -> "+team.getPrefix()
                        +"\nPrivate -> "+team.isPrivate()
                        +"\nPlayers:\n";

                for(UUID member : team.getMembers()){
                    Player pl = Bukkit.getPlayer(member);
                    if(pl!=null)
                        ret+=pl.getName()+" - "+(team.getOwner()==member?"owner":(team.isGrandDuke(member)?"duke":(team.isElder(member)?"elder":"member")))+"\n";
                }

                player.sendMessage(ret);
                return true;
            }
            default -> {
                player.sendMessage(ChatColor.RED + "Unknown team subcommand.");
                return false;
            }
        }

        return false;
    }
}
