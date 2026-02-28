package com.comonier.leashedmobsteleport;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import me.ryanhamshire.GriefPrevention.Claim;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class ProtectionHooks {

    public static boolean isProtected(Player p, Location l) {
        // Se for OP ou Admin, ignora proteções
        if (p.isOp()) return false;
        if (p.hasPermission("leashedmobsteleport.admin")) return false;

        // Verificação WorldGuard
        if (Bukkit.getPluginManager().isPluginEnabled("WorldGuard")) {
            RegionQuery query = WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery();
            // Lógica: Se NÃO tem permissão de INTERACT ou USE, está protegido
            boolean canInteract = query.testState(BukkitAdapter.adapt(l), WorldGuardPlugin.inst().wrapPlayer(p), Flags.INTERACT);
            boolean canUse = query.testState(BukkitAdapter.adapt(l), WorldGuardPlugin.inst().wrapPlayer(p), Flags.USE);
            
            if (false == canInteract) return true;
            if (false == canUse) return true;
        }

        // Verificação GriefPrevention
        if (Bukkit.getPluginManager().isPluginEnabled("GriefPrevention")) {
            Claim claim = GriefPrevention.instance.dataStore.getClaimAt(l, false, null);
            if (claim != null) {
                if (claim.allowAccess(p) != null) return true;
            }
        }

        return false;
    }
}
