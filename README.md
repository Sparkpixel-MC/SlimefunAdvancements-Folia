# 🏆 SlimefunAdvancements

> A configurable advancement system for Slimefun, bringing vanilla‑style challenges and progression to your server.

[![Build Status](https://builds.guizhanss.com/api/badge/SlimefunGuguProject/SlimefunAdvancements/main/latest)](https://builds.guizhanss.com/SlimefunGuguProject/SlimefunAdvancements/main)  
[![Java 21](https://img.shields.io/badge/Java-21-blue?logo=openjdk)](https://adoptium.net/)
[![Paper 26.2](https://img.shields.io/badge/Paper-26.2-informational?logo=paper)](https://papermc.io/)
[![Folia Ready](https://img.shields.io/badge/Folia-Compatible-brightgreen?logo=paper)](https://folia.papermc.io/) <!-- Adapted by me -->

---

## ✨ Features

- 📜 Fully configurable advancement trees with custom conditions, rewards, and displays
- 🧩 Native Slimefun item and recipe integration
- 🔄 Seamless integration with the vanilla advancement UI (shown in the Minecraft progress screen)
- ⚡ **Fully compatible with Folia's threading model** (adapted)
- 🔧 Ready to use out of the box, yet highly customisable

---

## 📋 Requirements

- **Java 21** (or newer) – your server JVM must meet this
- **Paper 26.2 +** or a fork (e.g. Purpur)
- **Slimefun** main plugin (latest stable build)

> ⚠️ **Note**: Vanilla advancement injection must be called on the **main thread**. Do not trigger it from asynchronous tasks.

---

## 📥 Download

Click the badge below to go to the latest build:

[![Build Status (click to download)](https://builds.guizhanss.com/api/badge/SlimefunGuguProject/SlimefunAdvancements/main/latest)](https://builds.guizhanss.com/SlimefunGuguProject/SlimefunAdvancements/main)

---

## 📖 Configuration

All configuration options and customisation tutorials are available on the [**Wiki**](https://slimefun-addons-wiki.guizhanss.cn/slimefun-advancements) (includes complete examples).

---

## 🔐 Permissions

| Permission Node | Description |
|-----------------|-------------|
| `sfa.command.<command name>` | Allows execution of the corresponding SlimefunAdvancements admin command |

(Replace `<command name>` with the actual sub‑command, e.g. `sfa.command.reload`)

---

## 🛠 Developer API

To extend advancements, create custom conditions or rewards via the API, see [**api.md**](./api.md).

---

## 🙏 Acknowledgements

- Thanks to [Slimefun](https://github.com/Slimefun/Slimefun4) for the powerful plugin framework
- This project is maintained by [SlimefunGuguProject](https://github.com/SlimefunGuguProject)
- Folia adaptation: **made by Qingha821** ✅

---

**Enjoy your new progression system!** 🎮