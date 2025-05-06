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

import java.util.UUID;

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

        if (attacker.equals(victim)) return;

        Team attackerTeam = TeamManager.getPlayersTeam(attacker.getUniqueId());
        Team victimTeam = TeamManager.getPlayersTeam(victim.getUniqueId());

        if (attackerTeam == null && victimTeam == null){
            return;
        }

        if((attackerTeam!=null && !attackerTeam.isGlobalPvP())||(victimTeam!=null && !victimTeam.isGlobalPvP())){
            event.setCancelled(true);
            return;
        }

        if (attackerTeam == victimTeam && !attackerTeam.isTeamPvP()) {
            event.setCancelled(true);
            return;
        }

        if (attackerTeam.getAllies().contains(victimTeam.getName()) && !attackerTeam.isAllyPvP()) {
            event.setCancelled(true);
            return;
        }

        if (!attackerTeam.isGlobalPvP() || !victimTeam.isGlobalPvP()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onWolfTarget(EntityTargetLivingEntityEvent event) {
        if (!(event.getEntity() instanceof Wolf wolf)) return;
        if (!wolf.isTamed()) return;
        if (!(event.getTarget() instanceof Player targetPlayer)) return;
        if (!(wolf.getOwner() instanceof Player owner)) return;

        UUID ownerUUID = owner.getUniqueId();
        UUID targetUUID = targetPlayer.getUniqueId();

        Team ownerTeam = TeamManager.getPlayersTeam(ownerUUID);
        Team targetTeam = TeamManager.getPlayersTeam(targetUUID);

        // Cancel if PVP is disallowed in this context
        if (ownerTeam == null && targetTeam == null) return;

        if ((ownerTeam != null && !ownerTeam.isGlobalPvP()) ||
                (targetTeam != null && !targetTeam.isGlobalPvP())) {
            event.setCancelled(true);
            return;
        }

        if (ownerTeam == targetTeam && !ownerTeam.isTeamPvP()) {
            event.setCancelled(true);
            return;
        }

        if (ownerTeam != null && targetTeam != null &&
                ownerTeam.getAllies().contains(targetTeam.getName()) &&
                !ownerTeam.isAllyPvP()) {
            event.setCancelled(true);
        }
    }


    private static Player getProjectileShooter(org.bukkit.entity.Entity damager) {
        if (damager instanceof Projectile projectile) {
            if (projectile.getShooter() instanceof Player shooter) {
                return shooter;
            }
        }
        return null;
    }
}
