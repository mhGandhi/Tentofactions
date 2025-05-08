package com.tentomax.models;

import java.util.LinkedList;
import java.util.List;
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

    public static List<String> getCommandValues(){
        List<String> ret = new LinkedList<>();

        for(TeamAttributes ta : values()){
            ret.add(ta.command);
        }

        return ret;
    }
}
