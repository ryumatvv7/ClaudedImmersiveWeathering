# Changelog

## ❄️ v26.3-port.10 — "Four Seasons"

> The year now has seasons: winter comes to plains and forests, spring melts it away.

### ✨ New
- **📅 12 months, 4 seasons.** A month is 8 in-game days by default, so a year is 96 days.
- **❄️ Winter:** temperate biomes (plains, forests, rivers) get snow instead of rain, snow piles up and water freezes. Deserts, savannas, badlands and the Nether stay hot.
- **🌱 Spring:** snow and ice melt in biomes that are normally warm. Grass, moss and weeds grow faster.
- **🍂 Autumn:** leaf piles fall three times as often, wood rots faster.
- **🌾 Crops:** outdoor crops grow slower in autumn and barely in winter. Under a roof (a greenhouse) they grow normally.

### ⌨️ Commands (operators)
- `/iw season` — current season, month and day
- `/iw season on` / `off` — turn seasons on or off completely
- `/iw season set <spring|summer|autumn|winter>` — jump to the start of a season
- `/iw season month <1-12>` — jump to a month (1 = early spring … 12 = late winter)
- `/iw season skip <months>` — move forward, or back with a negative number
- `/iw season pause` / `resume` — stop or restart the calendar
- `/iw season length <days>` — in-game days per month

Settings live in the `seasons` section of the config and are synced to every player.

### ⚠️ Notes
- Ice in warm biomes melts in spring and summer, including ice you placed outdoors in the open.
- `/time set` also moves the calendar, since seasons follow the world day.
- Grass and leaf colours don't change with the seasons yet.

### ✅ Checked
- Dev server self test: 43,900 checks passed, including the season commands and winter snow in plains.
- Client reaches the title screen without mod errors.

## 🏡 v26.3-port.9 — "Your Own Clock"

> Abandonment now tracks each player separately.

### 🐛 Fixes
- **👥 A friend playing alone no longer ages your base.** Every player has their own online-time clock. A chunk only ages while the player who was there last is online.
- **🗺️ Big bases no longer rot at the far end.** Every loaded chunk within view distance of a player counts as visited, instead of only 3 chunks around them.

### ℹ️ Notes
- Chunks stamped by older versions don't decay until a player visits them again. Nothing will suddenly rot after updating.

### ✅ Checked
- Dev server starts without mod errors.

## 🛌 v26.3-port.8 — "Nobody Home"

> A hotfix for abandonment.

### 🐛 Fixes
- **🏚️ An empty server no longer ages chunks.** Absence was measured in game time, which keeps running on a dedicated server with nobody online. One night offline (about 25–30 in-game days) was enough to cover a base in rot and ivy. Absence now counts only time while at least one player is online.
- Existing worlds are safe: old chunk stamps are refreshed on load without triggering decay.

### ✅ Checked
- Dev server starts without mod errors.

## 🩺 v26.3-port.7 — "Health Check"

> No new features. A stability and polish pass over the whole mod.

### 🐛 Fixes
- **🏆 Advancements now load alongside other mods.** They used to hang off vanilla advancement tabs, so mods that replace those tabs (like Matcha Flavoured) blocked all 22 of them. They now live in their own **Immersive Weathering** tab.
- **🪵 Dry wood no longer rots in wet biomes.** Wood under a roof in a swamp or jungle could rot without rain or water nearby. Now it only grows moss there; rot still needs water or rain.
- **🏚️ The abandonment queue is cleared when the server stops**, so nothing stale carries over to the next world load.

### ✅ Checked
- Dev server self test: 43,889 checks passed.
- Client game test passed, with no missing models or textures.
- Server and client logs are free of mod warnings, including with Matcha Flavoured installed.

---

## 🏚️ v26.3-port.6 — "Abandoned"

> Leave a place alone long enough and nature takes it back.

### ✨ New
- **🏚️ Abandonment.** Every chunk remembers when players were last nearby. Come back after **7+ in-game days** and the chunk catches up on everything it missed:
  - 🧱 bricks, cobblestone and other stone blocks turn **mossy and cracked**;
  - 🌿 **ivy** climbs sunlit walls, **moss** creeps over floors and walls;
  - 🕸️ **cobwebs** fill dark corners indoors;
  - 🪵 exposed wood **rots**.
- The longer you stay away, the worse it gets (capped per chunk).
- Only built blocks decay. Natural stone, dirt and grass are left alone.
- 🛡️ **Varnished** blocks stay protected.

### ⚙️ Config
- `abandonment.enabled`: turn the feature on or off.
- `abandonment.days`: in-game days without players before decay starts (default `7`).
- `abandonment.max_steps`: max blocks changed in one chunk at once (default `600`).
- `/iw abandon <days>` (op): age the chunk you're standing in, to try it out.

---

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
