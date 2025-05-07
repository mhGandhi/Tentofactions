package com.tentomax.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.tentomax.managers.TeamManager;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.registrar.ReloadableRegistrarEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;

public class BrigadierCommands {
    public static void register(ReloadableRegistrarEvent<Commands> pDispatcher){

        Set<LiteralCommandNode<CommandSourceStack>> commands = new LinkedHashSet<>();

        commands.add(buildTeamCommand());
        commands.add(buildTeamChatCommand());
        commands.add(buildAllyChatCommand());
        commands.add(buildDefaultChatCommand());

        for(LiteralCommandNode<CommandSourceStack> com : commands){
            pDispatcher.registrar().register(com);
        }
    }

    private static LiteralCommandNode<CommandSourceStack> buildTeamChatCommand(){
        return null;//todo
    }

    private static LiteralCommandNode<CommandSourceStack> buildAllyChatCommand(){
        return null;//todo
    }

    private static LiteralCommandNode<CommandSourceStack> buildDefaultChatCommand(){
        return null;//todo
    }

    private static Player assertIsPlayer(CommandSender pSrc){
        if (!(pSrc instanceof Player player)) {
            pSrc.sendMessage(Component.text("Must be a player").color(NamedTextColor.RED));
            return null;
        }
        return player;
    }

    private static final String TCL = "tteam";
    private static LiteralCommandNode<CommandSourceStack> obuildTeamCommand() {
        return LiteralArgumentBuilder.<CommandSourceStack>literal("team")
                .then(LiteralArgumentBuilder.<CommandSourceStack>literal("create")
                        .then(argument("name", StringArgumentType.word()))
                        .executes(ctx -> handleCreate(ctx.getSource(), getString(ctx, "name"))))

                .then(LiteralArgumentBuilder.<CommandSourceStack>literal("join")
                        .then(argument("team", StringArgumentType.word()))
                        .executes(ctx -> handleJoin(ctx.getSource(), getString(ctx, "team"))))

                .then(LiteralArgumentBuilder.<CommandSourceStack>literal("leave")
                        .executes(ctx -> handleLeave(ctx.getSource())))

                .then(LiteralArgumentBuilder.<CommandSourceStack>literal("info")
                        .executes(ctx -> handleInfo(ctx.getSource())))

                .then(LiteralArgumentBuilder.<CommandSourceStack>literal("kick")
                        .then(argument("player", StringArgumentType.word()))
                        .executes(ctx -> handleKick(ctx.getSource(), getString(ctx, "player"))))

                .then(LiteralArgumentBuilder.<CommandSourceStack>literal("accept")
                        .then(argument("player", StringArgumentType.word()))
                        .executes(ctx -> handleAccept(ctx.getSource(), getString(ctx, "player"))))

                .then(LiteralArgumentBuilder.<CommandSourceStack>literal("reject")
                        .then(argument("player", StringArgumentType.word()))
                        .executes(ctx -> handleReject(ctx.getSource(), getString(ctx, "player"))))

                .then(LiteralArgumentBuilder.<CommandSourceStack>literal("promote")
                        .then(argument("player", StringArgumentType.word()))
                        .executes(ctx -> handlePromote(ctx.getSource(), getString(ctx, "player"))))

                .then(LiteralArgumentBuilder.<CommandSourceStack>literal("demote")
                        .then(argument("player", StringArgumentType.word()))
                        .executes(ctx -> handleDemote(ctx.getSource(), getString(ctx, "player"))))

                // Show usage if /team is run with no args
                .executes(ctx -> {
                    CommandHelpUtil.sendDetailedSubcommandHelp(ctx.getSource(), ctx.getNode(), "team");
                    return 1;
                })
                .build();
    }


    private static LiteralCommandNode<CommandSourceStack> buildTeamCommand() {
        return LiteralArgumentBuilder.<CommandSourceStack>literal(TCL)
                .executes(ctx -> {
                    Player player = assertIsPlayer(ctx.getSource().getSender());
                    if(player==null)return 0;

                    player.sendMessage(Component.text("Use /team [subcommand]").color(NamedTextColor.YELLOW));
                    return 1;
                })
                .then(LiteralArgumentBuilder.<CommandSourceStack>literal("create")
                        .then(RequiredArgumentBuilder.<CommandSourceStack, String>argument("name", StringArgumentType.word())
                                .executes(ctx -> {
                                    Player player = (Player) ctx.getSource().getSender();
                                    String name = StringArgumentType.getString(ctx, "name");

                                    try {
                                        TeamManager.createTeam(player, name);
                                        player.sendMessage(Component.text("Team '" + name + "' created.").color(NamedTextColor.GREEN));
                                        return 1;
                                    } catch (Exception e) {
                                        player.sendMessage(Component.text(e.getMessage()).color(NamedTextColor.RED));
                                        return 0;
                                    }
                                })))
                .build();
    }



    private static final Map<String, String> TEAM_COMMAND_DESCRIPTIONS = new HashMap<>();

    static {
        TEAM_COMMAND_DESCRIPTIONS.put("create", "Creates a new team");
        TEAM_COMMAND_DESCRIPTIONS.put("join", "Join a team or send join request");
        TEAM_COMMAND_DESCRIPTIONS.put("leave", "Leave your current team");
        TEAM_COMMAND_DESCRIPTIONS.put("kick", "Kick a member from your team");
        TEAM_COMMAND_DESCRIPTIONS.put("modify", "Modify a team attribute");
        TEAM_COMMAND_DESCRIPTIONS.put("accept", "Accept a join request");
        TEAM_COMMAND_DESCRIPTIONS.put("reject", "Reject a join request");
        TEAM_COMMAND_DESCRIPTIONS.put("promote", "Promote a team member");
        TEAM_COMMAND_DESCRIPTIONS.put("demote", "Demote a team member");
        TEAM_COMMAND_DESCRIPTIONS.put("info", "View your team's info");
    }

    public static void sendDetailedSubcommandHelp(CommandSourceStack source, CommandNode<CommandSourceStack> root, String commandName) {
        StringBuilder sb = new StringBuilder("Usage: /").append(commandName).append("\n");

        for (CommandNode<CommandSourceStack> child : root.getChildren()) {
            if (child instanceof LiteralCommandNode<CommandSourceStack> literal) {
                String name = literal.getName();
                sb.append(" - ").append(name);

                for (CommandNode<CommandSourceStack> arg : literal.getChildren()) {
                    sb.append(" <").append(arg.getName()).append(">");
                }

                String description = TEAM_COMMAND_DESCRIPTIONS.getOrDefault(name, "");
                if (!description.isEmpty()) {
                    sb.append(" : ").append(description);
                }

                sb.append("\n");
            }
        }

        source.getSender().sendMessage(Component.text(sb.toString()).color(NamedTextColor.YELLOW));
    }


}
