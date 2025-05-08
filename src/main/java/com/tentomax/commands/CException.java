package com.tentomax.commands;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.ChatColor;

public class CException extends Exception {
    public CException(String message) {
        super(message);
    }

    public int respond(CommandSourceStack pSender){
        pSender.getExecutor().sendMessage(ChatColor.RED +getMessage());
        return 0;
    }
}
