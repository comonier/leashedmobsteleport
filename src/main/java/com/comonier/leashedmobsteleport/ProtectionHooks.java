package com.comonier.leashedmobsteleport;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.ClaimPermission;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
public class ProtectionHooks {
    public static boolean isProtected(Player p, Location l) {
        if (p.isOp() || p.hasPermission("leashedmobsteleport.admin")) return false;
        if (Bukkit.getPluginManager().isPluginEnabled("WorldGuard")) {
            RegionQuery q = WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery();
            if (false == q.testState(BukkitAdapter.adapt(l), WorldGuardPlugin.inst().wrapPlayer(p), Flags.INTERACT)) return true;
        }
        if (Bukkit.getPluginManager().isPluginEnabled("GriefPrevention")) {
            Claim c = GriefPrevention.instance.dataStore.getClaimAt(l, false, null);
            if (null != c) {
                if (null != c.checkPermission(p, ClaimPermission.Access, null)) return true;
            }
        }
        return false;
    }
}
