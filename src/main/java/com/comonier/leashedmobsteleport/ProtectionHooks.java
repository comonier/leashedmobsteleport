package com.comonier.leashedmobsteleport;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class ProtectionHooks {
    public static boolean isProtected(Player p, Location l) {
        // Bypass para administradores
        if (p.isOp() || p.hasPermission("leashedmobsteleport.admin")) return false;

        // Auto-Hook: WorldGuard
        if (Bukkit.getPluginManager().isPluginEnabled("WorldGuard")) {
            try {
                RegionQuery q = WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery();
                if (false == q.testState(BukkitAdapter.adapt(l), WorldGuardPlugin.inst().wrapPlayer(p), Flags.INTERACT)) return true;
            } catch (Throwable ignored) {} // Captura qualquer erro de versão ou classe ausente
        }

        // Auto-Hook: GriefPrevention
        if (Bukkit.getPluginManager().isPluginEnabled("GriefPrevention")) {
            try {
                me.ryanhamshire.GriefPrevention.Claim c = me.ryanhamshire.GriefPrevention.GriefPrevention.instance.dataStore.getClaimAt(l, false, null);
                if (null != c && null != c.checkPermission(p, me.ryanhamshire.GriefPrevention.ClaimPermission.Access, null)) return true;
            } catch (Throwable ignored) {}
        }

        // Auto-Hook: RedProtect
        if (Bukkit.getPluginManager().isPluginEnabled("RedProtect")) {
            try {
                br.net.fabiozumbi12.RedProtect.Bukkit.Region r = br.net.fabiozumbi12.RedProtect.Bukkit.RedProtect.get().getAPI().getRegion(l);
                if (null != r && false == r.canBuild(p)) return true;
            } catch (Throwable ignored) {}
        }

        return false;
    }
}
