# LeashedMobsTeleport

**Version:** 1.8.1
**Description:** Advanced dimensional teleportation for leashed entities, featuring multi-plugin protection hooks, persistent DNA tracking (PDC), and the exclusive Enchanted Lead system.

## 🚀 Main Features

*   **Enchanted Wand (LMT Wand):** A soul-bound "Enchanted Lead" that allows instant leashing and rescuing mobs from fences. Features high-security anti-drop and anti-theft (cannot be stored in chests or dropped on death).
*   **Universal Leashing:** Leash any entity, including Monsters (Creeper, Zombie, etc.), Villagers, Slimes, and even Ghasts. (Bosses are excluded for stability).
*   **Dimensional & Long-Distance Teleport:** Mobs follow the player through portals (Nether/End) and long-distance teleports. Includes an anti-stuck "Mob Ticker" that forces teleport if the mob is too far or trapped.
*   **Smart Mount Support:** Vehicles (Horses, Pigs, Strider, etc.) teleport with the player and automatically re-mount the passenger at the destination.
*   **Persistent Pacify System (DNA):** Leashed mobs become invulnerable, stop burning in the sun, and have their AI/Aggro disabled. Using **PersistentDataContainer (PDC)**, this protection survives server restarts and chunk unloads.
*   **Advanced Fence Interaction:** Tie any mob to fences. Ownership is tracked via DNA, allowing only the owner (or admins) to retrieve them.
*   **Triple Protection Hook:** Fully respects area protections from **WorldGuard** (Interact flag), **GriefPrevention** (Access/Trust), and **RedProtect** (Build permission).

## 🛠 Commands

*   `/lmt` - Displays plugin info and permission status.
*   `/lmt wand` - Toggles the Enchanted Lead in your inventory.
*   `/lmt reload` - Reloads configurations and all translation files (EN, PT, ES, RU).
*   `/lmt toggle` - Enables or disables teleport status messages for the player.

## 🔑 Permissions

*   `leashedmobsteleport.use` - Allows leashing mobs and using the /lmt wand and toggle commands.
*   `leashedmobsteleport.admin` - Bypass all area protections, use /lmt reload, and view debug info.
*   `lmt.leash.<mob_name>` - Specific permission per mob (e.g., `lmt.leash.creeper`), if `use-permission-per-mob` is enabled in `config.yml`.

## 🌍 Supported Languages
The plugin automatically detects and loads:
*   English (`en`)
*   Portuguese (`pt`)
*   Spanish (`es`)
*   Russian (`ru`)

## ⚙️ Technical Compatibility

**Automatic Hook System:** The plugin is designed to be plug-and-play. It automatically detects if **WorldGuard**, **GriefPrevention**, or **RedProtect** are installed. 
*   If a protection plugin is missing, LMT will simply skip its check without any console errors or performance impact.
*   For administrative regions (like Spawn), ensure the `interact` flag (WorldGuard) or "Access/Build" trust is granted.
