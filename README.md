# LeashedMobsTeleport

**Version:** 1.5.2  
**Description:** Teleport leashed entities across dimensions and allow leashing any type of mob with advanced protection.

## Main Features

*   **Leash Any Mob:** Allows the use of leads on monsters (Creeper, Zombie, etc.), Slimes, Ghasts, and Villagers.
*   **Dimensional Teleport:** Leashed mobs follow the player through portals (Nether/End) and teleport commands (/tp).
*   **Mount Support:** Vehicles (horses, pigs, etc.) teleport along with the player and keep the passenger mounted at the destination.
*   **Pacify System:** Leashed mobs become invulnerable, stop burning in the sun, and have their AI disabled while on the lead.
*   **Lead Anti-Dupe:** Automatic cleanup of dropped leads during teleportation to prevent item duplication.
*   **Protection Integration:** Fully respects WorldGuard regions and GriefPrevention claims (requires Trust/Interaction permission).

## Commands

*   `/lmt reload` - Reloads the configurations and the messages file.
*   `/lmt toggle` - Enables or disables teleport messages for the player.

## Permissions

*   `leashedmobsteleport.use` - Basic permission to use the lead and teleport mobs.
*   `leashedmobsteleport.admin` - Permission to reload the plugin.
*   `lmt.leash.` - Specific permission per mob (e.g., lmt.leash.villager), enabled via config.

## Important Notice

For the plugin to function correctly in **Spawn** or administrative **WorldGuard** regions, the `interact` and `use` flags must be set to `allow` in the region. Otherwise, the lead will be blocked silently or by the WorldGuard's native message.
