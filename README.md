# LeashedMobsTeleport
Changelog v1.7 - Security & Persistence Update

[Added]
│➜ Wand DNA Tracker: New persistent metadata (`wandLeashKey`) to identify mobs leashed with the Enchanted Lead.
│➜ Territory Notifications: Real-time chat alerts when entering/leaving claims or when Trust status is changed.
│➜ Unleash Protection: New logic that blocks players from releasing leashed mobs inside protected areas without trust.

[Fixed]
│➜ Leash Dupe Bug: Fixed the issue where the Enchanted Wand would drop a standard Lead item upon releasing a mob.
│➜ GP/WG Bypass: Resolved the bug where players could leash or tie entities to fences in protected territories.
│➜ AI Restoration Bug: Fixed the issue where mobs would remain with AI disabled even after being legally released in foreign claims.
│➜ Persistence Loss: Fixed the bug that caused mobs to be unleashed and their AI restored automatically after a server restart.

[Improved]
│➜ GriefPrevention Logic: Switched to UUID-based `checkPermission` for more reliable trust verification.
│➜ Fence Security: Enhanced distance checks and ownership validation for tying/untying mobs from hitch posts.
│➜ Code Optimization: Refactored for Java 21 and Paper 1.21.1 with maximized performance for high-entity environments.

Note: A full server restart is recommended to apply these persistent DNA optimizations to all active entities.


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


