# LeashedMobsTeleport

**Version:** 1.7
### 🚀 Improvements & Bug Fixes
*   **GriefPrevention & WorldGuard Integration:** Fixed a critical bypass where players could leash or unleash mobs in protected areas without trust.
*   **Anti-Dupe System:** Implemented a persistent DNA tracker (`wandLeashKey`) to prevent players from generating free leads when using the Enchanted Wand.
*   **Protection Notifications:** Added real-time chat alerts when players enter/exit claims or have their trust status changed.
*   **Persistence Fix:** Resolved an issue where mobs would lose their "Pacified" state (IA Off) and drop leads after a server restart.
*   **Fence Security:** Players are now blocked from tying mobs to fences in territories where they do not have access permission.
*   **Code Optimization:** Complete refactor for Java 21/Paper 1.21.1 with logic improvements to prevent interaction glitches.

**Description:** Teleport leashed entities across dimensions and allow leashing any type of mob with advanced persistent protection.

## Main Features

*   **/lmt wand:** Give a new enchanted wand to use on mobs.

*   **Leash Any Mob:** Allows the use of leads on monsters (Creeper, Zombie, etc.), Slimes, Ghasts, and Villagers. (Excludes Bosses).

**Dimensional Teleport:** Leashed mobs follow the player through portals (Nether/End) and teleport commands (/tp).

**Mount Support:** Vehicles (horses, pigs, etc.) teleport along with the player and keep the passenger mounted at the destination.

**Persistent Pacify System:** Leashed mobs become invulnerable, stop burning in the sun, and have their AI disabled. Now uses **PersistentDataContainer (PDC)** to ensure protection survives server restarts and chunk unloads.

**Advanced Fence Support (New):** Allows tying any mob (including monsters) to fences. Players can now retrieve tied mobs from fences even after a server restart without losing the lead.

**Lead Anti-Dupe & Anti-Loss (New):** Improved cleanup of dropped leads during teleportation. Added safety checks to return leads to the player's inventory or drop them at their feet if the inventory is full.

**Protection Integration:** Fully respects **WorldGuard** regions (Flags: Interact/Use) and **GriefPrevention** claims (Trust/Access required).

## Commands

`/lmt reload` - Reloads the configurations and the messages file.

`/lmt toggle` - Enables or disables teleport messages for the player.

## Permissions

`leashedmobsteleport.use` - Basic permission to use the lead and teleport mobs.

`leashedmobsteleport.admin` - Permission to reload the plugin and see version info.

`lmt.leash.` - Specific permission per mob (e.g., `lmt.leash.villager`), if enabled in `config.yml`.

## Important Notice

**WorldGuard:** For the plugin to function correctly in 
**Spawn** or administrative regions, the `interact` and `use` flags must be set to `allow`.


