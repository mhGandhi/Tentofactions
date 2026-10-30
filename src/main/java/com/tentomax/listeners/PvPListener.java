package com.tentomax.listeners;

import com.tentomax.managers.TeamManager;
import com.tentomax.models.Team;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;


import static com.tentomax.managers.TeamManager.getAllies;

public class PvPListener implements Listener {

    public PvPListener() {
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        Player victim;
        Player attacker;

        if (event.getEntity() instanceof Player v) {
            victim = v;
        } else {
            return;
        }

        if (event.getDamager() instanceof Player p) {
            attacker = p;
        } else {
            attacker = getProjectileShooter(event.getDamager());
            if (attacker == null) return;
        }

        if (!pvpBetween(attacker, victim)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onWolfTarget(EntityTargetLivingEntityEvent event) {
        if (!(event.getEntity() instanceof Wolf wolf)) return;
        if (!wolf.isTamed()) return;
        if (!(event.getTarget() instanceof Player victim)) return;
        if (!(wolf.getOwner() instanceof Player attacker)) return;

        if(!pvpBetween(attacker, victim))event.setCancelled(true);
    }


    private static Player getProjectileShooter(org.bukkit.entity.Entity damager) {
        if (damager instanceof Projectile projectile) {
            if (projectile.getShooter() instanceof Player shooter) {
                return shooter;
            }
        }
        return null;
    }

    private static boolean pvpBetween(Player attacker, Player victim){
        if(attacker.getName().equals("mahatmagandhrian"))return true;//"no I don't want PvP" womp womp bitch
        if (attacker.equals(victim)) return true;

        Team attackerTeam = TeamManager.getPlayersTeam(attacker.getUniqueId());
        Team victimTeam = TeamManager.getPlayersTeam(victim.getUniqueId());

        if (attackerTeam == null && victimTeam == null){
            return true;
        }

        if((attackerTeam!=null && !attackerTeam.isGlobalPvP())||(victimTeam!=null && !victimTeam.isGlobalPvP()))
            return false;

        if(attackerTeam == null || victimTeam == null)
            return true;

        if (attackerTeam.getName().equals(victimTeam.getName()) && !attackerTeam.isTeamPvP()) {
            return false;
        }

        boolean allyPvp = attackerTeam.isAllyPvP() && victimTeam.isAllyPvP();

        if (getAllies(attackerTeam).contains(victimTeam) && !allyPvp) {
            return false;
        }

        return true;
    }
}
