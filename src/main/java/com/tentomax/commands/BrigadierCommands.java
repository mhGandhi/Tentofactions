package com.tentomax.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.registrar.ReloadableRegistrarEvent;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class BrigadierCommands {
    public static void register(ReloadableRegistrarEvent<Commands> pDispatcher){

        Set<LiteralCommandNode<CommandSourceStack>> commands = new LinkedHashSet<>();

        commands.add(buildTeamCommand());

        for(LiteralCommandNode<CommandSourceStack> com : commands){
            pDispatcher.registrar().register(com);
        }
    }

    private static LiteralCommandNode<CommandSourceStack> buildTeamCommand(){
        return null;//todo
    }
}
