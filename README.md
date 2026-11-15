# Claude'd Immersive Weathering

**Unofficial Fabric port of [Immersive Weathering](https://github.com/Silversmith-Mods/Immersive-Weathering) to Minecraft 26.1.2.**

> 🤖 This port was made with the help of **Claude** (Anthropic's AI model, via Claude Code).
> The code was migrated, fixed and tested by Claude; gameplay was checked in game by a human.

> ⚠️ This is **not** an official release. All credit for the mod itself goes to the original authors:
> **Ordana, MehVahdJukaar, Keyberries** and the other contributors of
> [Immersive Weathering](https://github.com/Silversmith-Mods/Immersive-Weathering).
> Please don't report bugs of this port to them.

Русская версия — [ниже](#русский).

---

## What is it

Immersive Weathering adds natural systems that make the world feel alive: moss and cracks spread over
stone and bricks, iron rusts, fire chars wood and leaves soot, leaves fall into leaf piles, frost and icicles
form in the cold, sand piles up in deserts, new soils, bark, mulch, weeds and more.

This port is based on the original **1.20.1 – 2.0.5** sources (commit `1bcc03b6`) and brings them to:

| | |
|---|---|
| Minecraft | **26.1.2** |
| Loader | **Fabric Loader 0.19.3+** |
| Fabric API | **0.155.3+26.1.2** |
| Moonlight Lib | **26.1.2-4.2.2 (Fabric)** — required |
| Java | **25** |

Works in single player, LAN and on a dedicated server. The mod must be installed on the server **and** on every client.

## What's new compared to the original

- **Ash** is back: fire leaves ash layers on burned blocks and where it goes out; rain washes it away;
  ash works as fertilizer; blazes drop ash.
- **Speed config**: global and per-category multipliers (vegetation, frost & ice, fire & soot, sand, coral,
  lightning, leaf piles, nether, weeds, moss, cracks, rust). Applies to datapack block growths too.
- Leaf piles and bark for **pale oak** (and dynamically for modded wood types).
- Server-side config synced to clients.

Not ported: EMI, ModMenu config screen, Quark / Serene Seasons / Trinkets compat.
Full details: [PORTING_NOTES.md](PORTING_NOTES.md) (Russian).

## Install

1. Install Fabric Loader 0.19.3+ for Minecraft 26.1.2.
2. Put into `mods`: Fabric API, Moonlight Lib (Fabric, 26.1.2) and the jar of this port.
3. Same three files on the server. Server guide: [SERVER_SETUP.md](SERVER_SETUP.md) (Russian).

Config: `config/immersive_weathering-common.json` (server, synced) and `immersive_weathering-client.json` (per player).

## Build

```
gradlew build
```
The jar appears in `build/libs/`. Dev server: `gradlew runServer`, client: `gradlew runClient`.

## License

- Code: **LGPL-3.0**, same as the original — see [LICENSE](LICENSE). Sound attributions: [ATTRIBUTIONS](ATTRIBUTIONS).
- Assets belong to the original authors.
- **Moonlight Lib** is a separate mod under its own license and is **not** included here; this port only calls its public API.

---

## Русский

**Неофициальный порт мода [Immersive Weathering](https://github.com/Silversmith-Mods/Immersive-Weathering) на Fabric для Minecraft 26.1.2.**

> 🤖 Порт сделан с помощью **Claude** (ИИ от Anthropic, через Claude Code): перенос кода, исправления и
> автотесты выполнил Claude, игру проверял человек.

Все заслуги за сам мод принадлежат оригинальным авторам (**Ordana, MehVahdJukaar, Keyberries** и др.).
Это не официальная версия — об ошибках порта не пишите им.

**Нужно:** Minecraft 26.1.2, Fabric Loader 0.19.3+, Fabric API 0.155.3+26.1.2, Moonlight 26.1.2-4.2.2 (Fabric), Java 25.
Мод ставится и на сервер, и всем игрокам.

**Нового по сравнению с оригиналом:** пепел (остаётся после огня, смывается дождём, работает как удобрение),
множители скорости всех механик в конфиге, кучи листьев и кора бледного дуба.

Подробно: [PORTING_NOTES.md](PORTING_NOTES.md), сервер: [SERVER_SETUP.md](SERVER_SETUP.md).
