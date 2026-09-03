# 🎭 SeriousRP

[![Build & Tests](https://github.com/stanislasbdx/SeriousRP/actions/workflows/tests.yml/badge.svg)](https://github.com/stanislasbdx/SeriousRP/actions)
[![Minecraft Version](https://img.shields.io/badge/Minecraft-1.21%2B-brightgreen.svg)](https://papermc.io)
[![Spigot / Purpur](https://img.shields.io/badge/Platform-Spigot%20%2F%20Purpur-orange.svg)](https://purpurmc.org)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

**SeriousRP** is a modular Minecraft plugin designed to introduce immersive Roleplay mechanics to Spigot, Paper, and Purpur servers.

---

## 🚀 Features

- 🏥 **Medical & RP Death System**:
  - Coma state on fatal damage with bleeding and injury mechanics.
  - Vitals inspection (`/vitals`) to check health, hunger, active effects, and distance.
  - Revive mechanics (`/revive` for medics, `/hrprevive` fallback).
- 💸 **Economy & Cheques**:
  - Sign physical cheque items (`/cheque <amount>`) backed by [Vault](https://www.spigotmc.org/resources/vault.34315/).
  - Right-click to claim money directly into your account.
- 🎲 **Bounded Random Teleportation (RTP)**:
  - Teleport players randomly within a configurable radius around their position (`/srtp`).
- 🪑 **Interactive Chairs**:
  - Right-click stair blocks to sit down smoothly.
- 🏋️ **Player Carrying**:
  - Lift and carry other players around.
- ⚙️ **Modular Configuration**:
  - Enable or disable every feature independently directly from `config.yml`.

---

## 📋 Requirements

- **Server**: Spigot, Paper, or Purpur (1.21+)
- **Java**: Java 21+
- **Dependencies**: [Vault](https://www.spigotmc.org/resources/vault.34315/) + a Vault-compatible economy plugin (e.g. EssentialsX).

---

## ⌨️ Commands & Permissions

| Command | Aliases | Description | Permission |
| :--- | :--- | :--- | :--- |
| `/seriousrp <help\|version\|modules\|reload>` | `/srp` | Core management and module status | `seriousrp.info` |
| `/srtp` | `/rtp` | Randomly teleports you in a radius | `seriousrp.randomtp` |
| `/cheque <amount>` | `/cq` | Issues a signed physical cheque item | `seriousrp.economy.cheques` |
| `/vitals <player>` | `/medinfo` | Displays health, hunger, effects & location | `seriousrp.medics.info` |
| `/revive <player>` | - | Revives a player from coma | `seriousrp.medics.revive` |
| `/hrprevive` | - | Emergency self-revive when no medics are available | `seriousrp.medics.hrprevive` |

---

## 🛠️ Configuration

The plugin generates a `config.yml` on first start allowing you to toggle modules, customize prefixes, and translate messages:

```yaml
Core:
  Modules:
    CustomRecipes: true
    RPDeath: true
    Medics: true
    Chairs: true
    Economy: true

MicroModules:
  RandomBlocks: 1000
```

---

## 💻 Building from Source

```bash
git clone https://github.com/stanislasbdx/SeriousRP.git
cd SeriousRP
./gradlew build
```

The compiled `.jar` file will be located in `build/libs/`.

---

## 👤 Author

- **Stanislas Castaybert** ([@stanislasbdx](https://github.com/stanislasbdx))
- Website: <https://stan1712.com>
