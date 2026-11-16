"""Resources for the rotten wood blocks (cloned from the vanilla oak ones) and the wood weathering growth (one-off)."""
import json
import os

ROOT = os.path.join(os.path.dirname(__file__), "..")
RES = os.path.join(ROOT, "src", "main", "resources")
A = os.path.join(RES, "assets", "immersive_weathering")
D = os.path.join(RES, "data", "immersive_weathering")
MC = os.path.join(RES, "data", "minecraft")
V = os.path.join(ROOT, "ref", "tex", "assets", "minecraft")
VD = os.path.join(ROOT, "ref", "data", "data", "minecraft")

# vanilla id -> our id; texture names: oak_planks -> rotten_planks, oak_log -> rotten_log, oak_log_top -> rotten_log_top
BLOCKS = {"oak_planks": "rotten_planks", "oak_log": "rotten_log", "oak_stairs": "rotten_stairs",
          "oak_slab": "rotten_slab", "oak_fence": "rotten_fence"}


def load(p):
    with open(p, encoding="utf8") as f:
        return json.load(f)


def w(p, o):
    os.makedirs(os.path.dirname(p), exist_ok=True)
    with open(p, "w", encoding="utf8") as f:
        json.dump(o, f, indent=2)


def ren(o):
    s = json.dumps(o)
    for a, b in (("minecraft:block/oak_log_top", "immersive_weathering:block/rotten_log_top"),
                 ("minecraft:block/oak_log_horizontal", "immersive_weathering:block/rotten_log_horizontal"),
                 ("minecraft:block/oak_log", "immersive_weathering:block/rotten_log"),
                 ("minecraft:block/oak_planks", "immersive_weathering:block/rotten_planks"),
                 ("minecraft:block/oak_stairs", "immersive_weathering:block/rotten_stairs"),
                 ("minecraft:block/oak_slab", "immersive_weathering:block/rotten_slab"),
                 ("minecraft:block/oak_fence", "immersive_weathering:block/rotten_fence"),
                 ("minecraft:oak_planks", "immersive_weathering:rotten_planks"),
                 ("minecraft:oak_log", "immersive_weathering:rotten_log"),
                 ("minecraft:oak_stairs", "immersive_weathering:rotten_stairs"),
                 ("minecraft:oak_slab", "immersive_weathering:rotten_slab"),
                 ("minecraft:oak_fence", "immersive_weathering:rotten_fence")):
        s = s.replace(a, b)
    return json.loads(s)


import re
for van, new in BLOCKS.items():
    bs = load(f"{V}/blockstates/{van}.json")
    item = load(f"{V}/items/{van}.json")
    models = set(re.findall(r'minecraft:block/([a-z0-9_]+)', json.dumps(bs) + json.dumps(item)))
    w(f"{A}/blockstates/{new}.json", ren(bs))
    w(f"{A}/items/{new}.json", ren(item))
    for m in models:
        w(f"{A}/models/block/{ren({'x': 'minecraft:block/' + m})['x'].split('/')[-1]}.json",
          ren(load(f"{V}/models/block/{m}.json")))
    w(f"{D}/loot_table/blocks/{new}.json", ren(load(f"{VD}/loot_table/blocks/{van}.json")))

ids = [f"immersive_weathering:{n}" for n in BLOCKS.values()]


def tag(p, vals):
    d = load(p) if os.path.exists(p) else {"replace": False, "values": []}
    for v in vals:
        if v not in d["values"]:
            d["values"].append(v)
    w(p, d)


tag(f"{MC}/tags/block/mineable/axe.json", ids)
tag(f"{MC}/tags/block/wooden_fences.json", ["immersive_weathering:rotten_fence"])
tag(f"{MC}/tags/item/wooden_fences.json", ["immersive_weathering:rotten_fence"])
tag(f"{MC}/tags/block/stairs.json", ["immersive_weathering:rotten_stairs"])
tag(f"{MC}/tags/item/stairs.json", ["immersive_weathering:rotten_stairs"])
tag(f"{MC}/tags/block/slabs.json", ["immersive_weathering:rotten_slab"])
tag(f"{MC}/tags/item/slabs.json", ["immersive_weathering:rotten_slab"])

# rotten wood -> mulch, like bark
w(f"{D}/recipe/mulch_block_from_rotten_wood.json", {
    "type": "minecraft:crafting_shaped", "pattern": ["C#", "#C"],
    "key": {"#": "#immersive_weathering:rotten_wood", "C": "minecraft:dirt"},
    "result": {"id": "immersive_weathering:mulch_block", "count": 4}})
w(f"{D}/tags/item/rotten_wood.json", {"replace": False, "values": ids})

# the growth itself (block ticks; owners are computed in code)
w(f"{D}/block_growths/wood_weathering.json", {"builtin": "wood_weathering", "tick_sources": ["block_tick"],
                                              "growth_chance": 0.08, "owners": "#minecraft:planks"})

names = {
    "en_us.json": {"rotten_planks": "Rotten Planks", "rotten_log": "Rotten Log", "rotten_stairs": "Rotten Stairs",
                   "rotten_slab": "Rotten Slab", "rotten_fence": "Rotten Fence"},
    "ru_ru.json": {"rotten_planks": "Гнилые доски", "rotten_log": "Гнилое бревно", "rotten_stairs": "Гнилые ступени",
                   "rotten_slab": "Гнилая плита", "rotten_fence": "Гнилой забор"},
}
for fn, n in names.items():
    p = f"{A}/lang/{fn}"
    d = load(p)
    for k, v in n.items():
        d.setdefault(f"block.immersive_weathering.{k}", v)
    with open(p, "w", encoding="utf8") as f:
        json.dump(d, f, ensure_ascii=False, indent=2)
print("done")
