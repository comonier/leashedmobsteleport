# 🐾 LeashedMobsTeleport [v1.2]

Minecraft 1.21.11 | Java 21 | Paper 1.21.11

**LeashedMobsTeleport** is a high-performance, professional-grade utility for Minecraft servers. It revolutionizes how players transport entities, ensuring **zero-loss** interdimensional travel and a "pacified" experience for even the most dangerous mobs.

__________________________________________________
connect on ip: hu3.org server to test
its a survival server with griefprevention and all my plugins.
__________________________________________________
<img width="400" height="392" alt="image" src="https://github.com/user-attachments/assets/b46af576-5a44-4b21-b508-d714fe47ea09" />


## 🚀 Key Features

### 🌌 The "Soul Ride" Teleportation
Never lose a mob again. Our unique **Passenger-Ride** mechanic forces entities to "hitch a ride" with the player during teleports. 
*   **Works with:** `/spawn`, `/home`, `/tp`.
*   **Zero-Loss:** Mobs won't despawn or be left behind, even on high-latency servers.
*   **Mount Persistence:** Stay mounted on your Horse, Camel, or Donkey after teleporting!

### 🛡️ Universal Pacification & Safety
*   **Universal Leash:** Supports **Iron Golems, Snow Golems, Villagers, Allays, Breeze**, and more.
*   **AI Lockdown:** Leashed mobs have their AI disabled. No more wandering Allays or teleporting Endermen!
*   **Sun Protection:** Zombies and Skeletons are immune to sun combustion while on a lead. ☀️
*   **Creeper Grace Period:** Leashed Creepers are pacified. Once released, they gain a **5-second grace period** before they can explode again.
*   **Total Invulnerability:** Mobs on a lead are immune to Lava, Fall Damage, Fire, and Player attacks.

### 🎒 Quality of Life
*   **Inventory Recovery:** Leads return directly to your **inventory** when a mob is released (no more drops on the ground).
*   **Smart Fence System:** Automatic notifications when tying/untying mobs to fences.
*   **Bilingual Support:** Built-in support for **English (EN)** and **Portuguese (PT-BR)**.

---

## 🛠️ Installation

1.  Ensure your server is running **Java 21**.
2.  Download the latest [Release](https://github.com/comonier/leashedmobsteleport/releases/download/1.2/leashedmobsteleport-1.2.jar)
3.  Drop the `.jar` file into your `/plugins` folder.
4.  Restart your server.
5.  Configure your language in `config.yml`.

---

## 📜 Permissions


| Permission | Description | Default |
| :--- | :--- | :--- |
| `leashedmobsteleport.use` | Allows player to leash, teleport and use all features. | `true` |

---

## 💻 Configuration (`config.yml`)
```yaml
language: "en" # Options: "en" or "pt"
