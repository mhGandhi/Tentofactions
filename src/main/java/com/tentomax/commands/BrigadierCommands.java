package com.tentomax.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.tentomax.Main;
import com.tentomax.managers.TeamManager;
import com.tentomax.models.ChatMode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.registrar.ReloadableRegistrarEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.builder.RequiredArgumentBuilder.argument;
import static com.tentomax.commands.ChatCommand.setChat;
import static com.tentomax.commands.Suggestors.*;
import static com.tentomax.commands.TeamCommand.*;

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
        return LiteralArgumentBuilder.<CommandSourceStack>literal("teamchat")
                .executes(ctx -> setChat(ctx.getSource(), ChatMode.TEAM))
                .build();
    }

    private static LiteralCommandNode<CommandSourceStack> buildAllyChatCommand(){
        return LiteralArgumentBuilder.<CommandSourceStack>literal("allychat")
                .executes(ctx -> setChat(ctx.getSource(), ChatMode.ALLY))
                .build();
    }

    private static LiteralCommandNode<CommandSourceStack> buildDefaultChatCommand(){
        return LiteralArgumentBuilder.<CommandSourceStack>literal("defaultchat")
                .executes(ctx -> setChat(ctx.getSource(), ChatMode.PUBLIC))
                .build();
    }

    private static Player assertIsPlayer(CommandSender pSrc){
        if (!(pSrc instanceof Player player)) {
            pSrc.sendMessage(Component.text("Must be a player").color(NamedTextColor.RED));
            return null;
        }
        return player;
    }




    private static final String TCL = "tteam";
    private static LiteralCommandNode<CommandSourceStack> buildTeamCommand() {
        return LiteralArgumentBuilder.<CommandSourceStack>literal(TCL)
                .then(LiteralArgumentBuilder.<CommandSourceStack>literal("create")
                        .then(Commands.argument("name", StringArgumentType.word())
                                .executes(ctx -> {
                                    Main.getInstance().getLogger().info("PARSED NAME: "+ getString(ctx, "name"));
                                    return handleCommand(
                                            ctx.getSource(),
                                            (player)-> createTeam(player, getString(ctx, "name")));
                                })
                        )
                        .executes(ctx -> missingArg(ctx.getSource()))
                )


                .then(LiteralArgumentBuilder.<CommandSourceStack>literal("join")
                        .then(Commands.argument("team", StringArgumentType.word())
                                .suggests(getAllTeamsSuggestor())
                                        .executes(ctx -> {
                                            return handleCommand(
                                            ctx.getSource(),
                                            (player)-> joinTeam(player, getString(ctx, "team")));
                                        })
                        )
                                .executes(ctx -> missingArg(ctx.getSource()))
                        )

                .then(LiteralArgumentBuilder.<CommandSourceStack>literal("leave")
                        .executes(ctx -> {
                            return handleCommand(
                                    ctx.getSource(),
                                    (player)-> leaveTeam(player, true));
                        }))

                .then(LiteralArgumentBuilder.<CommandSourceStack>literal("info")
                        .executes(ctx -> {
                            return handleCommand(
                                    ctx.getSource(),
                                    TeamCommand::info);
                        }))

                .then(LiteralArgumentBuilder.<CommandSourceStack>literal("kick")
                        .then(Commands.argument("player", StringArgumentType.word())//todo player below in team
                                .suggests(getOneRankLessSuggestor())
                                .executes(ctx -> {
                                    return handleCommand(
                                            ctx.getSource(),
                                            (player)-> kick(player, getString(ctx, "player")));
                                })
                        ).executes(ctx -> missingArg(ctx.getSource()))
                        )

                .then(LiteralArgumentBuilder.<CommandSourceStack>literal("accept")
                        .then(Commands.argument("player", StringArgumentType.word())//todo player with request
                                .suggests(getRequestedSuggestor())
                                .executes(ctx -> {
                                    return handleCommand(
                                            ctx.getSource(),
                                            (player)-> accept(player, getString(ctx, "player")));
                                })
                        ).executes(ctx -> missingArg(ctx.getSource()))
                )

                .then(LiteralArgumentBuilder.<CommandSourceStack>literal("reject")
                        .then(Commands.argument("player", StringArgumentType.word())//todo player with request
                                .suggests(getRequestedSuggestor())
                                .executes(ctx -> {
                                    return handleCommand(
                                            ctx.getSource(),
                                            (player)-> reject(player, getString(ctx, "player")));
                                })
                        ).executes(ctx -> missingArg(ctx.getSource()))
                )

                .then(LiteralArgumentBuilder.<CommandSourceStack>literal("promote")
                        .then(Commands.argument("player", StringArgumentType.word())//todo 2xbelow in team
                                .suggests(getTwoRankLessSuggestor())
                                .executes(ctx -> {
                                    return handleCommand(
                                            ctx.getSource(),
                                            (player)-> promoteMember(player, getString(ctx, "player")));
                                })
                        ).executes(ctx -> missingArg(ctx.getSource()))
                )

                .then(LiteralArgumentBuilder.<CommandSourceStack>literal("demote")
                        .then(Commands.argument("player", StringArgumentType.word())//todo below in team
                                .suggests(getOneRankLessSuggestor())
                                .executes(ctx -> {
                                    return handleCommand(
                                            ctx.getSource(),
                                            (player)-> demoteMember(player, getString(ctx, "player")));
                                })
                        ).executes(ctx -> missingArg(ctx.getSource()))
                )

                .then(LiteralArgumentBuilder.<CommandSourceStack>literal("modify")
                        .then(Commands.argument("key", StringArgumentType.word())//todo arg types
                                .then(Commands.argument("value", StringArgumentType.word())
                                        .suggests(getTeamAttributesSuggestor())
                                        .executes(ctx -> {
                                            return handleCommand(
                                                    ctx.getSource(),
                                                    (player)-> modifyAttribute(player, getString(ctx, "key"), getString(ctx, "value"))
                                            );
                                        })
                                )
                                .executes(ctx -> missingArg(ctx.getSource()))
                        )
                        .executes(ctx -> missingArg(ctx.getSource()))
                )

                // Show usage if /team is run with no args
                .executes(ctx -> {
                    sendDetailedSubcommandHelp(ctx.getSource(), ctx.getRootNode(), TCL);
                    return 0;
                })
                .build();
    }

    public static int mustBePlayer(CommandSourceStack pSource){
        pSource.getExecutor().sendMessage(ChatColor.RED +"must be executed by player");
        return 0;
    }

    private static int missingArg(CommandSourceStack pSource){
        pSource.getExecutor().sendMessage(ChatColor.RED + "missing argument(s)");
        return 0;
    }

    private interface Command{
        void execute(Player player) throws CException;
    }



    private static int handleCommand(CommandSourceStack ctx, Command command) {
        if (!(ctx.getSender() instanceof Player player)) {
            return mustBePlayer(ctx);
        }

        try {
            command.execute(player);
        } catch (CException e) {
            return e.respond(ctx);
        }

        return 1;
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
//todo fix
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
