"""Clones the pale oak leaf pile / bark resources for the 26.3 poplar wood (one-off)."""
import json
import os
import re

R = os.path.join(os.path.dirname(__file__), "..", "src", "main", "resources")
A = os.path.join(R, "assets", "immersive_weathering")
D = os.path.join(R, "data", "immersive_weathering")
LEAVES = ["red_poplar", "orange_poplar", "yellow_poplar"]


def clone(src, dst, subs):
    with open(src, encoding="utf8") as f:
        s = f.read()
    for a, b in subs:
        s = s.replace(a, b)
    os.makedirs(os.path.dirname(dst), exist_ok=True)
    with open(dst, "w", encoding="utf8") as f:
        f.write(s)


# leaf piles: one per poplar leaves colour
for c in LEAVES:
    subs = [("pale_oak_leaf_pile", c + "_leaf_pile"), ("pale_oak_leaves", c + "_leaves")]
    clone(f"{A}/blockstates/pale_oak_leaf_pile.json", f"{A}/blockstates/{c}_leaf_pile.json", subs)
    clone(f"{A}/items/pale_oak_leaf_pile.json", f"{A}/items/{c}_leaf_pile.json", subs)
    clone(f"{A}/models/item/pale_oak_leaf_pile.json", f"{A}/models/item/{c}_leaf_pile.json", subs)
    for h in (1, 2, 4, 6, 8, 10, 12, 14, 16):
        clone(f"{A}/models/block/leaf_piles/pale_oak_leaf_pile_height{h}.json",
              f"{A}/models/block/leaf_piles/{c}_leaf_pile_height{h}.json", subs)
    clone(f"{D}/loot_table/blocks/pale_oak_leaf_pile.json", f"{D}/loot_table/blocks/{c}_leaf_pile.json", subs)
    clone(f"{D}/recipe/pale_oak_leaf_pile.json", f"{D}/recipe/{c}_leaf_pile.json", subs)

# bark
subs = [("pale_oak", "poplar")]
for p in ("items/pale_oak_bark.json", "models/item/pale_oak_bark.json"):
    clone(f"{A}/{p}", f"{A}/{p.replace('pale_oak', 'poplar')}", subs)
for p in ("recipe/pale_oak_log_unstrip.json", "recipe/pale_oak_wood_from_bark.json",
          "recipe/pale_oak_wood_unstrip.json", "advancement/recipes/pale_oak_bark.json"):
    clone(f"{D}/{p}", f"{D}/{p.replace('pale_oak', 'poplar')}", subs)


# tags: add a poplar entry next to every pale oak entry
def add_tag(path, fn):
    with open(path, encoding="utf8") as f:
        d = json.load(f)
    vals = d["values"]
    out = []
    for v in vals:
        out.append(v)
        s = v if isinstance(v, str) else v.get("id", "")
        if "pale_oak" in s:
            for n in fn(s):
                if n not in vals and n not in out:
                    out.append(n if isinstance(v, str) else {**v, "id": n})
    d["values"] = out
    with open(path, "w", encoding="utf8") as f:
        json.dump(d, f, indent=2)


for t in ("block/leaf_piles", "block/leaves_leafy", "block/vanilla_leaves"):
    add_tag(f"{D}/tags/{t}.json", lambda s: [s.replace("pale_oak", c) for c in LEAVES])
for t in ("block/raw_logs", "block/stripped_logs", "item/bark", "item/flammable_fence_gates", "item/flammable_fences",
          "item/flammable_planks", "item/flammable_slabs", "item/flammable_stairs"):
    add_tag(f"{D}/tags/{t}.json", lambda s: [s.replace("pale_oak", "poplar")])

# translations
names = {
    "en_us.json": {"red_poplar": "Red Poplar Leaf Pile", "orange_poplar": "Orange Poplar Leaf Pile",
                   "yellow_poplar": "Yellow Poplar Leaf Pile", "bark": "Poplar Bark"},
    "ru_ru.json": {"red_poplar": "Куча красных листьев тополя", "orange_poplar": "Куча оранжевых листьев тополя",
                   "yellow_poplar": "Куча жёлтых листьев тополя", "bark": "Кора тополя"},
}
for fn, n in names.items():
    p = f"{A}/lang/{fn}"
    with open(p, encoding="utf8") as f:
        d = json.load(f)
    for c in LEAVES:
        d.setdefault(f"block.immersive_weathering.{c}_leaf_pile", n[c])
    d.setdefault("item.immersive_weathering.poplar_bark", n["bark"])
    with open(p, "w", encoding="utf8") as f:
        json.dump(d, f, ensure_ascii=False, indent=2)
print("done")
