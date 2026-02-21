<div align="center">

# 🚀 Release v1.5.1 - The Stability Update (1.21.1)

This is a **CRITICAL** update that fixes several physics and synchronization bugs introduced by the Minecraft 1.21.1 engine. All server owners are highly encouraged to update immediately.

### 🛠️ Key Fixes & Improvements

> 🐎 Anti-Rocket Mounts: Fixed a major physics bug where entities would fly into the stratosphere when tamed or mounted while the teleport system was active.
> 🛡️ Fence Persistence: Fixed a "Ghost Leash" issue where leashed entities (like horses) would teleport back to the player's hand even after being tied to a fence.
> 👹 Aggressive Mob Leashing: Fully restored the ability to leash monsters (Creepers, Zombies, etc.) which was being blocked by the native 1.21.1 engine.
> 🧹 Triple-Tick Anti-Dupe: Implemented a high-precision lead cleaning system (ticks 2, 4, and 6) to prevent lead items from dropping/duplicating during cross-dimension teleports.
> 🔇 Teleport Anti-Spam: Messages no longer spam the chat during short-distance teleports (e.g., while mounting or adjusting positions).
> 🔒 WG/GP Full Sync: Improved interaction logic with WorldGuard and GriefPrevention to ensure custom leashing rules are respected in protected areas.
  
# 🛡️ Proteção de Mobs & Integração WG/GP (v1.5)
  
# ⚠️EXTREMELY IMPORTANT⚠️
This release (v1.5) introduces breaking changes in the configuration structure. Follow these steps to ensure the protection system works as intended.

  ### 🛑 1. RESET CONFIGURATION
Due to the new **Mob Permission System**, the language files and messages have been overhauled.
   **ACTION REQUIRED:** You **MUST** delete the old plugin folder before installing v1.5. 
   **WHY?** Failure to do so will cause new permission messages to be read incorrectly or not at all.

### 🛡️ 2. WORLDGUARD OVERRIDE SETUP
For the **Leash Protection** (allow/deny logic) to function correctly across Server and Player claims, you must bypass global interaction blocks. Our plugin will handle the specific security logic, but it needs WorldGuard to "let it pass" first.

**Run these commands in your console immediately:**

#### 🌍 Overworld
> `/rg flag __global__ use allow`
> `/rg flag __global__ interact allow`

#### 🔥 The Nether
> `/rg flag __global__ -w world_nether use allow`
> `/rg flag __global__ -w world_nether interact allow`

#### 🌌 The End
> `/rg flag __global__ -w world_the_end use allow`
> `/rg flag __global__ -w world_the_end interact allow`

### ⚠️ WHY IS THIS NECESSARY?
If these flags are not set to `allow` in the `__global__` region, **WorldGuard** and **GriefPrevention** will hard-block the action before our plugin's logic can even process it. Setting these to `allow` delegates the final decision to our plugin's advanced filtering system.

---

# 🐾 LeashedMobsTeleport [v1.4]
DUPE LEAD ON CROSS WORLD TP FIXED

**Minecraft 1.21.1 | Java 21 | Paper 1.21.1**

**LeashedMobsTeleport** is a high-performance, professional-grade utility for Minecraft servers. It revolutionizes how players transport entities, ensuring **zero-loss** travel between worlds and a "pacified" experience for even the most dangerous mobs.

__________________________________________________

### 🌐 Test now on IP: **hu3.org**
*Survival server with GriefPrevention and custom plugins.*

[Discord](https://discord.gg/yyKQTFGVfR)
__________________________________________________

<img width="503" height="602" alt="image" src="https://github.com/user-attachments/assets/b2eb5312-8146-4c14-b22f-72a06b41b988" />


## 🚀 Key Features

### 🌌 The "Golden Rescue" Teleportation
Never lose a mob again. Our unique **Golden-Rescue** mechanic ensures entities stay bonded to the player during teleports and world changes.
Works with: `/spawn`, `/home`, `/tp` and Portals.
Zero-Loss: Mobs won't despawn or be left behind, even on high-latency servers.
Mount Persistence: Stay mounted on your Horse, Camel, or even a Ghast after teleporting!

### 🌈 Dynamic Smart Colors
Our new intelligent messaging system provides visual feedback:
Passive Mobs: Displayed in **Light Green**.
Hostile Mobs: Displayed in **Light Red**.
Mounts: Displayed in **White**.
Worlds: Themed colors for **Overworld**, **Nether**, and **End**.

### 🛡️ Universal Pacification & Safety
Universal Leash: Supports **Iron Golems, Villagers, Allays, Breeze**, and more.
AI Lockdown: Leashed mobs have their AI disabled. No more wandering Allays!
Sun Protection: Zombies and Skeletons are immune to sun combustion while leashed. ☀️
Creeper Grace Period: Leashed Creepers are pacified. Once released, they gain a **5-second grace period**.
Total Invulnerability: Mobs on a lead are immune to Lava, Fall Damage, Fire, and Player attacks.

### 🎒 Quality of Life
Inventory Recovery: Leads return directly to your **inventory** (no more drops on the ground).
Smart Commands: Use `/lmt` to toggle teleport messages in real-time.
Bilingual Support: Built-in support for **English (EN)** and **Portuguese (PT-BR)**.

---

## 🛠️ Installation

⚠️ **IMPORTANT:** ⚠️

If updating, **delete the plugin folder** (`/plugins/LeashedMobsTeleport`) to prevent file conflicts.

1. Ensure your server is running **Java 21**.
2. Download the latest **v1.3 Release**.
3. Drop the `.jar` file into your `/plugins` folder.
4. Restart your server.
5. Configure your language in `config.yml`.

---

## 📜 Permissions


| Permission | Description | Default |
| :---: | :---: | :---: |
| `leashedmobsteleport.use` | Allows player to use all features. | `true` |

---

## 💻 Configuration (`config.yml`)
```yaml
language: "en" # Options: "en" or "pt"
