package com.tentomax.models;

import org.bukkit.ChatColor;

import java.util.StringJoiner;
import java.util.concurrent.ThreadLocalRandom;

public enum CColor {
    BLACK(ChatColor.BLACK, "black"),
    DARK_BLUE(ChatColor.DARK_BLUE, "dark_blue"),
    DARK_GREEN(ChatColor.DARK_GREEN, "dark_green"),
    DARK_AQUA(ChatColor.DARK_AQUA, "dark_aqua"),
    DARK_RED(ChatColor.DARK_RED, "dark_red"),
    DARK_PURPLE(ChatColor.DARK_PURPLE, "dark_purple"),
    GOLD(ChatColor.GOLD, "gold"),
    GRAY(ChatColor.GRAY, "gray"),
    DARK_GRAY(ChatColor.DARK_GRAY, "dark_gray"),
    BLUE(ChatColor.BLUE, "blue"),
    GREEN(ChatColor.GREEN, "green"),
    AQUA(ChatColor.AQUA, "aqua"),
    RED(ChatColor.RED, "red"),
    LIGHT_PURPLE(ChatColor.LIGHT_PURPLE, "light_purple"),
    YELLOW(ChatColor.YELLOW, "yellow"),
    WHITE(ChatColor.WHITE, "white");

    public final ChatColor bCol;
    public final String command;

    private CColor(ChatColor pBCol, String pCommand) {
        this.bCol = pBCol;
        this.command = pCommand;
    }

    public static ChatColor byCommand(String pCommand) {
        for (CColor col : values()) {
            if (col.command.equalsIgnoreCase(pCommand)) return col.bCol;
        }
        return null;
    }

    public static String allColsString() {
        StringJoiner joiner = new StringJoiner("|", "<", ">");
        for (CColor color : values()) {
            joiner.add(color.command);
        }
        return joiner.toString();
    }

    public static ChatColor getRandomCol() {
        CColor[] colors = values();
        int randomIndex = ThreadLocalRandom.current().nextInt(colors.length);
        return colors[randomIndex].bCol;
    }

}
