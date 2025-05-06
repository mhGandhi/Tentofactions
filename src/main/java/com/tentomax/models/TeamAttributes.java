package com.tentomax.models;

import java.util.Objects;

public enum TeamAttributes {
    PREFIX("prefix"),
    COLOR("color"),
    PRIVATE("private");

    public final String command;
    private TeamAttributes(String pCommand){
        this.command = pCommand;
    }

    public static TeamAttributes byCommand(String pCom){
        for (TeamAttributes ta : values() ) {
            if(Objects.equals(ta.command, pCom))return ta;
        }
        return null;
    }
}
