package com.tentomax.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.tentomax.Main;
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

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
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




    public static final String TCL = "tentoteam";
    private static LiteralCommandNode<CommandSourceStack> buildTeamCommand() {
        return LiteralArgumentBuilder.<CommandSourceStack>literal(TCL)

                .then(LiteralArgumentBuilder.<CommandSourceStack>literal("help")
                        .executes(ctx -> help(ctx.getSource()))
                )

                .then(LiteralArgumentBuilder.<CommandSourceStack>literal("create")
                        .then(Commands.argument("name", StringArgumentType.word())
                                .executes(ctx -> {
                                    Main.getInstance().getLogger().info("PARSED NAME: "+ getString(ctx, "name"));
                                    return handleCommand(
                                            ctx.getSource(),
                                            (player)-> createTeam(player, getString(ctx, "name")));
                                })
                        )
                        //.executes(ctx -> missingArg(ctx.getSource()))
                )


                .then(LiteralArgumentBuilder.<CommandSourceStack>literal("join")
                        .then(Commands.argument("team", StringArgumentType.word())
                                .suggests(getOtherTeamsSuggestor())
                                        .executes(ctx -> {
                                            return handleCommand(
                                            ctx.getSource(),
                                            (player)-> joinTeam(player, getString(ctx, "team")));
                                        })
                        )
                                //.executes(ctx -> missingArg(ctx.getSource()))
                )

                .then(LiteralArgumentBuilder.<CommandSourceStack>literal("leave")
                        .executes(ctx -> {
                            return handleCommand(
                                    ctx.getSource(),
                                    (player)-> leaveTeam(player, true));
                        })
                )

                .then(LiteralArgumentBuilder.<CommandSourceStack>literal("info")
                        .executes(ctx -> {
                            return handleCommand(
                                    ctx.getSource(),
                                    TeamCommand::info);//todo for other teams aswell
                        }))

                .then(LiteralArgumentBuilder.<CommandSourceStack>literal("kick")
                        .then(Commands.argument("player", StringArgumentType.word())
                                .suggests(getOneRankLessSuggestor())
                                .executes(ctx -> {
                                    return handleCommand(
                                            ctx.getSource(),
                                            (player)-> kick(player, getString(ctx, "player")));
                                })
                        )//.executes(ctx -> missingArg(ctx.getSource()))
                        )

                .then(LiteralArgumentBuilder.<CommandSourceStack>literal("accept")
                        .then(Commands.argument("player", StringArgumentType.word())
                                .suggests(getRequestedSuggestor())
                                .executes(ctx -> {
                                    return handleCommand(
                                            ctx.getSource(),
                                            (player)-> accept(player, getString(ctx, "player")));
                                })
                        )//.executes(ctx -> missingArg(ctx.getSource()))
                )

                .then(LiteralArgumentBuilder.<CommandSourceStack>literal("reject")
                        .then(Commands.argument("player", StringArgumentType.word())
                                .suggests(getRequestedSuggestor())
                                .executes(ctx -> {
                                    return handleCommand(
                                            ctx.getSource(),
                                            (player)-> reject(player, getString(ctx, "player")));
                                })
                        )//.executes(ctx -> missingArg(ctx.getSource()))
                )

                .then(LiteralArgumentBuilder.<CommandSourceStack>literal("promote")
                        .then(Commands.argument("player", StringArgumentType.word())
                                .suggests(getTwoRankLessSuggestor())
                                .executes(ctx -> {
                                    return handleCommand(
                                            ctx.getSource(),
                                            (player)-> promoteMember(player, getString(ctx, "player")));
                                })
                        )//.executes(ctx -> missingArg(ctx.getSource()))
                )

                .then(LiteralArgumentBuilder.<CommandSourceStack>literal("demote")
                        .then(Commands.argument("player", StringArgumentType.word())
                                .suggests(getOneRankLessSuggestor())
                                .executes(ctx -> {
                                    return handleCommand(
                                            ctx.getSource(),
                                            (player)-> demoteMember(player, getString(ctx, "player")));
                                })
                        )//.executes(ctx -> missingArg(ctx.getSource()))
                )

                .then(LiteralArgumentBuilder.<CommandSourceStack>literal("ally")
                                .then(Commands.argument("team", StringArgumentType.word())
                                        .suggests(getOtherTeamsSuggestor())
                                        .executes(ctx -> {
                                            return handleCommand(
                                                    ctx.getSource(),
                                                    (player)-> allyTeam(player, getString(ctx, "team")));
                                        })
                                )
                                .executes(ctx -> {
                                    ctx.getSource().getExecutor().sendMessage(ChatColor.YELLOW+"Use to add a team to your List of allies - for anything to take effect, both teams must add the other to their allies");
                                    return 0;
                                })
                        //.executes(ctx -> missingArg(ctx.getSource()))
                )

                .then(LiteralArgumentBuilder.<CommandSourceStack>literal("unally")
                                .then(Commands.argument("team", StringArgumentType.word())
                                        .suggests(getOtherTeamsSuggestor())
                                        .executes(ctx -> {
                                            return handleCommand(
                                                    ctx.getSource(),
                                                    (player)-> unAllyTeam(player, getString(ctx, "team")));
                                        })
                                )
                        //.executes(ctx -> missingArg(ctx.getSource()))
                )

                .then(LiteralArgumentBuilder.<CommandSourceStack>literal("modify")
                        .then(Commands.argument("key", StringArgumentType.word())
                                .suggests(getTeamAttributesSuggestor())
                                .then(Commands.argument("value", StringArgumentType.word())
                                        .executes(ctx -> {
                                            return handleCommand(
                                                    ctx.getSource(),
                                                    (player)-> modifyAttribute(player, getString(ctx, "key"), getString(ctx, "value"))
                                            );
                                        })
                                )
                                //.executes(ctx -> missingArg(ctx.getSource()))
                        )
                        //.executes(ctx -> missingArg(ctx.getSource()))
                )

                // Show usage if /team is run with no args
                .executes(ctx -> help(ctx.getSource()))
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

    private static int help(CommandSourceStack source){
        source.getExecutor().sendMessage(ChatColor.YELLOW
                +"create [team] --> creates a new team"
                +"\njoin [team] --> join a team or send request"
                +"\nleave --> leave your current team"
                +"\nkick [player] --> kick a player from your team"
                +"\nmodify [attribute] [value] --> modify a team attribute"
                +"\naccept [player] --> accept a player join-request"
                +"\nreject [player] --> reject a player join-request"
                +"\npromote [player] --> promotes a player"
                +"\ndemote [player] --> demotes a player"
                +"\nally [team] --> add a team to your teams allies"
                +"\nunally [team] --> remove a team from your teams allies"
                +"\ninfo --> shows info about your team"
        );

        return 0;
    }
}
