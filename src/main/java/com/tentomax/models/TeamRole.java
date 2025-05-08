package com.tentomax.models;

public enum TeamRole {
    MEMBER(p(), "member"),
    ELDER(p(Privilege.ACCEPTING), "elder"),
    GRAND_DUKE(p(Privilege.ACCEPTING, Privilege.KICKING, Privilege.ALLY), "grandduke"),
    OWNER(p(Privilege.ACCEPTING, Privilege.KICKING, Privilege.ALLY, Privilege.ATTRIBUTES), "owner");

    private final Privilege[] privileges;
    public final String string;
    public final int rank;

    private TeamRole(Privilege[] pPrivileges, String pString){
        this.privileges = pPrivileges;
        this.string = pString;
        rank = this.ordinal();
    }

    public boolean hasPrivilege(Privilege privilege){
        for (Privilege p : privileges) {
            if(p == privilege)return true;
        }
        return false;
    }

    public static TeamRole byRank(int pRank){
        for(TeamRole tr : values()){
            if(tr.rank == pRank)return tr;
        }
        return null;
    }

    public static TeamRole byString(String pString) {
        for(TeamRole tr : values()){
            if(tr.string.equalsIgnoreCase(pString))return tr;
        }
        return null;
    }

    private static Privilege[] p(Privilege... privileges) {
        return privileges;
    }

    /*
    public int getRank(){
        int i = 0;
        for (TeamRole role : values()) {
            if(role == this)return i;
            i++;
        }
        return -1;
    }
     */
}

