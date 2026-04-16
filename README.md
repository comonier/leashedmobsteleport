# LeashedMobsTeleport (LMT)

LeashedMobsTeleport is an essential tool for Minecraft servers that want to enhance leash (lead) mechanics. It allows teleporting leashed entities across dimensions and worlds, while offering a robust protection system, persistent DNA, and deep integration with land claim plugins.

## 🚀 Key Features

*   **Interdimensional Teleport:** Carry your mobs through Nether/End portals or teleport commands without the lead breaking.
*   **Persistent DNA:** The plugin records the mob's owner in the PDC (Persistent Data Container), ensuring only the person who leashed it can interact with or release the mob.
*   **Automatic Pacify Mode:** When leashed, mobs become invulnerable, stop burning in sunlight, and lose aggressive AI to ensure a safe journey.
*   **Hostile Mob Support:** Allows leashing Monsters, Villagers, and Slimes (configurable).
*   **Enchanted Lead (Wand):** A special soul-bound item that cannot be dropped or stored in chests, allowing you to rescue mobs from fences.
*   **Protection Hooks:** Native support for WorldGuard, GriefPrevention, and RedProtect.

## 🛠 Commands & Usage

### Player Commands
*   /lmt chat - Toggles territory entry/exit notifications in Chat.
*   /lmt screen - Toggles notifications (Title/Subtitle) on the screen.
*   /lmt action - Toggles notifications on the Action Bar.
*   /lmt boss - Toggles notifications on the Boss Bar.
*   /lmt wand - Receives or removes the Enchanted Lead (Wand) from your inventory.

### Administrator Commands
*   /lmt reload - Reloads configuration and translation files.

## 📋 Permissions

*   leashedmobsteleport.use: Allows using leads and basic player commands (Default: Everyone).
*   leashedmobsteleport.admin: Allows reloading the plugin and bypassing area protections.

### Entity-Specific Control (Optional)
If use-permission-per-mob is set to true in config.yml, players will need specific permissions for each mob:
*   Example: lmt.leash.cow, lmt.leash.skeleton, lmt.leash.villager.

## ⚙️ Notification System

The administrator has full control over which notification systems are active on the server via config.yml:

notifications:
  chat: true
  screen: true
  actionbar: false
  bossbar: false

*   Admin Control: If the administrator sets an option to false, it is disabled server-wide.
*   Player Choice: If an option is true in the config, players can individually decide to enable or disable it using /lmt <type>. If they try to toggle a disabled system, they will be notified it's blocked by the admin.

## 🌐 Supported Languages
The plugin automatically loads messages based on the language setting:
*   English (en), Portuguese (pt), Spanish (es), Russian (ru).
