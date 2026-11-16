# Changelog

## 🪵 v26.3-port.5 — "Damp & Decay"

> Wood isn't forever anymore. Docks, bridges and shacks by the water now age on their own.

### ✨ New
- **🍄 Wood rot.** Planks, logs, wood, stairs, slabs and fences (all vanilla wood types, including poplar and bamboo) rot when they're **in water, under water or next to it**. In the rain in wet biomes (swamps, jungles) they rot more slowly.
  - Rotten blocks **keep their shape**: stairs stay stairs with the same rotation, slabs stay slabs.
  - New blocks: *Rotten Planks, Rotten Log, Rotten Stairs, Rotten Slab, Rotten Fence*.
  - Crimson and warped wood don't rot.
- **🌊 Sunken rotten wood falls apart.** Give it time and a flooded dock disappears.
- **🌿 Moss on wood.** Damp wood grows a layer of moss (not under water).
- **🛡️ Varnish.** `Glass Bottle + Resin Clump + Honeycomb`, 32 uses.
  - Right-click a wooden block and it **won't rot or grow moss**, even in water.
  - Varnish is invisible. Click again to check ("This block is already varnished").
  - Breaking the block removes the varnish from that spot.
- **♻️ Put rotten wood to use.** Craft it into mulch or compost it.

### ⚙️ Config
- `wood.rot` / `wood.moss` turn rot and moss on or off.
- `/iw speed wood <value>` sets the rot speed (e.g. `0.3` is about three times slower).

### ⚠️ For servers
Wooden builds near water will start to rot **slowly**. Varnish important builds ahead of time, or slow rot down with `/iw speed wood`.

---

## v26.3-port.4 — "Weather & Rust"
- 🌳 Poplar support (a vanilla 26.3 wood type), including its colored leaves.
- ⛓️ Rusting chains and lanterns.
- 🧱 Moss and cracks on more bricks: tuff, mud, deepslate.
- 💧 Puddles after rain.
- 🍂 Leaf fall depends on the biome.
- 🛠️ `/iw speed` and `/iw reload` commands (operators only, synced to players).

## v26.3-port.3
- 🐛 Fixed broken item names (`moss_clump` and others).

## v26.3-port.2 — "No Moonlight"
- 🚀 Ported to Minecraft **26.3** (Fabric).
- 🔌 Moonlight dependency removed entirely.
- 🔄 Custom config with **server → client sync**.
