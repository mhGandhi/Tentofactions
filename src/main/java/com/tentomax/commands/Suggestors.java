package com.tentomax.commands;

import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.tentomax.managers.TeamManager;
import com.tentomax.models.Privilege;
import com.tentomax.models.Team;
import com.tentomax.models.TeamAttributes;
import com.tentomax.models.TeamRole;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class Suggestors {

    private static final SuggestionProvider<CommandSourceStack> otherTeamsSuggestor = (context, builder) -> {
        Team team = null;
        CommandSourceStack source = context.getSource();
        if ((source.getExecutor() instanceof Player player)) {
            team = TeamManager.getPlayersTeam(player.getUniqueId());
        }

        if (team == null) {
            for(String key : TeamManager.getTeams().keySet()){
                if(key.toLowerCase().startsWith(builder.getRemaining().toLowerCase()))
                    builder.suggest(key);
            }
        }else{
            for(String key : TeamManager.getTeams().keySet()){
                if((!key.equals(team.getName())) && key.toLowerCase().startsWith(builder.getRemaining().toLowerCase()))
                    builder.suggest(key);
            }
        }

        return CompletableFuture.completedFuture(builder.build());
    };

    private static final SuggestionProvider<CommandSourceStack> oneRankLess = (context, builder) -> {
        CommandSourceStack source = context.getSource();
        if (!(source.getExecutor() instanceof Player player)) {
            return (Suggestions.empty());
        }

        Team team = TeamManager.getPlayersTeam(player.getUniqueId());
        if (team == null) {
            return (Suggestions.empty());
        }

        TeamRole playerRole = team.getRole(player.getUniqueId());
        if (playerRole == null) {
            return (Suggestions.empty());
        }

        TeamRole demotionTargetRole = TeamRole.byRank(playerRole.rank - 1);
        if (demotionTargetRole == null) {
            return (Suggestions.empty());
        }

        for(UUID member : team.getMembers()){
            Player mem = Bukkit.getServer().getPlayer(member);
            if(member != null && team.getRole(member).rank < playerRole.rank && mem.getName().toLowerCase().startsWith(builder.getRemaining().toLowerCase()))
                builder.suggest(mem.getName());

        }

        return CompletableFuture.completedFuture(builder.build());
    };

    private static final SuggestionProvider<CommandSourceStack> twoRankLess = (context, builder) -> {
        CommandSourceStack source = context.getSource();
        if (!(source.getExecutor() instanceof Player player)) {
            return (Suggestions.empty());
        }

        Team team = TeamManager.getPlayersTeam(player.getUniqueId());
        if (team == null) {
            return (Suggestions.empty());
        }

        TeamRole playerRole = team.getRole(player.getUniqueId());
        if (playerRole == null) {
            return (Suggestions.empty());
        }

        TeamRole promotionTargetRole = TeamRole.byRank(playerRole.rank + 1);
        if (promotionTargetRole == null) {
            return (Suggestions.empty());
        }

        for(UUID member : team.getMembers()){
            Player mem = Bukkit.getServer().getPlayer(member);
            if(member != null && team.getRole(member).rank+1 < playerRole.rank && mem.getName().toLowerCase().startsWith(builder.getRemaining().toLowerCase()))
                builder.suggest(mem.getName());

        }

        return CompletableFuture.completedFuture(builder.build());
    };

    private static final SuggestionProvider<CommandSourceStack> requested = (context, builder) -> {
        CommandSourceStack source = context.getSource();
        if (!(source.getExecutor() instanceof Player player)) {
            return (Suggestions.empty());
        }

        Team team = TeamManager.getPlayersTeam(player.getUniqueId());
        if (team == null) {
            return (Suggestions.empty());
        }

        if(!team.hasPrivilege(player.getUniqueId(), Privilege.ACCEPTING)){
            return (Suggestions.empty());
        }

        List<SuggestionsBuilder> requestedPlayers = team.getJoinRequests().stream()
                .map(Bukkit::getPlayer)
                .filter(p -> p != null && p.getName().toLowerCase().startsWith(builder.getRemaining().toLowerCase()))
                .map(p -> builder.suggest(p.getName()))
                .collect(Collectors.toList());

        for(UUID request : team.getJoinRequests()){
            Player req = Bukkit.getServer().getPlayer(request);
            if(req!=null && req.getName().toLowerCase().startsWith(builder.getRemaining().toLowerCase()))
                builder.suggest(req.getName());
        }

        return CompletableFuture.completedFuture(builder.build());
    };

    private static final SuggestionProvider<CommandSourceStack> teamAttributes = (context, builder) -> {

        for(String teamAtt : TeamAttributes.getCommandValues()){
            if(teamAtt.toLowerCase().startsWith(builder.getRemaining().toLowerCase()))
                builder.suggest(teamAtt);
        }

        return CompletableFuture.completedFuture(builder.build());
    };


    public static SuggestionProvider<CommandSourceStack> getOtherTeamsSuggestor() {
        return otherTeamsSuggestor;
    }

    public static SuggestionProvider<CommandSourceStack> getOneRankLessSuggestor() {
        return oneRankLess;
    }

    public static SuggestionProvider<CommandSourceStack> getTwoRankLessSuggestor() {
        return twoRankLess;
    }

    public static SuggestionProvider<CommandSourceStack> getRequestedSuggestor() {
        return requested;
    }

    public static SuggestionProvider<CommandSourceStack> getTeamAttributesSuggestor() {
        return teamAttributes;
    }
}
