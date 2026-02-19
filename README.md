# 🪢 LeashedMobsTeleport (1.21.1+)

A robust, interdimensional Minecraft plugin that allows you to leash any living entity and teleport them with the player anywhere, ensuring full stability and AI control.

## 🚀 Key Features (Plug & Play)

*   **Interdimensional Teleportation:** Your mobs follow you between the Overworld, Nether, and End without getting lost.
*   **AI Management:** 
    *   Leashing a mob **disables its AI**, turning it into a "statue" that follows the player smoothly.
    *   This prevents Endermen from escaping, Creepers from attacking, or mobs resisting movement.
    *   AI is automatically reactivated once the leash is removed.
*   **Creeper Reset:** If a Creeper is about to explode, leashing it **immediately puts out the fuse** and cancels the explosion.
*   **Universal Leash:** Forced support for entities usually blocked by Vanilla:
    *   **Villagers:** Blocks the trading menu if you are holding a leash.
    *   **Allays:** Protection against item pickup (prevents leash duping).
    *   **Golems:** Full support for Iron and Snow Golems.
    *   **Mounts:** Zombie/Skeleton Horses and the new **Nautilus (1.21.11)** (prevents accidental mounting).
    *   **Hostiles:** Breeze, Bogged, Zombies, Skeletons, etc.
*   **Unbreakable Leads:** Leashes do not break by physical distance. Perfect for use with **Elytra** or Creative mode flight.

## 🛡️ Permissions

The plugin is **unlocked for all players by default** (Plug & Play).

*   `leashedmobsteleport.use`: Allows leashing and teleporting mobs. 
    *   *Default: true* (All players can use it upon installation).

## 🚫 Restrictions
*   **Bosses:** The **Wither** and the **Ender Dragon** remain immune to leashing for balance reasons.

## 🛠️ Installation
1. Ensure you are using **Java 21**.
2. Compile with `mvn clean package`.
3. Drag the `.jar` from the `target` folder to your server's `/plugins` folder.

---
Developed by **comonier** for version 1.21.11.
