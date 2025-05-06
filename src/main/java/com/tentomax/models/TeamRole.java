package com.tentomax.models;

public enum TeamRole {
    MEMBER,
    ELDER,
    GRAND_DUKE,
    OWNER;

    public static TeamRole byString(String pString){
        if(pString.equalsIgnoreCase("member")){
            return MEMBER;
        }
        if(pString.equalsIgnoreCase("elder")){
            return ELDER;
        }
        if(pString.equalsIgnoreCase("grandduke")){
            return GRAND_DUKE;
        }
        if(pString.equalsIgnoreCase("owner")){
            return OWNER;
        }
        //todo replace
        return null;
    }
}

