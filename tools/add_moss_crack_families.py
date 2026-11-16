"""
Mossy / cracked variants for tuff bricks, mud bricks and mossy deepslate bricks / tiles (one-off).
Writes resources, generates textures with TexGen and prints nothing but a summary; the Java side is
patched in place (ModBlocks, Mossable, Crackable, VanillaBlockReplacements).
"""
import json
import os
import re
import subprocess

ROOT = os.path.join(os.path.dirname(__file__), "..")
RES = os.path.join(ROOT, "src", "main", "resources")
A = os.path.join(RES, "assets", "immersive_weathering")
D = os.path.join(RES, "data", "immersive_weathering")
MC = os.path.join(RES, "data", "minecraft")
V = os.path.join(ROOT, "ref", "tex", "assets", "minecraft")
VD = os.path.join(ROOT, "ref", "data", "data", "minecraft")
JAVA = os.path.join(ROOT, "src", "main", "java", "com", "ordana", "immersive_weathering")

FAMILIES = [
    # full, stem, brick item, add mossy, add cracked, en, ru (full / adjective forms)
    dict(full="tuff_bricks", stem="tuff_brick", item="() -> Items.TUFF", mossy=True, cracked=True,
         en="Tuff Brick", ru="туфовые кирпичи"),
    dict(full="mud_bricks", stem="mud_brick", item="() -> Items.PACKED_MUD", mossy=True, cracked=True,
         en="Mud Brick", ru="кирпичи из грязи"),
    dict(full="deepslate_bricks", stem="deepslate_brick", item="() -> ModItems.DEEPSLATE_BRICK.get()", mossy=True,
         cracked=False, en="Deepslate Brick", ru="глубинносланцевые кирпичи"),
    dict(full="deepslate_tiles", stem="deepslate_tile", item="() -> ModItems.DEEPSLATE_TILE.get()", mossy=True,
         cracked=False, en="Deepslate Tile", ru="глубинносланцевая плитка"),
]
SHAPES = ["", "_stairs", "_slab", "_wall"]  # appended to stem ("" means the full block)


def bid(f, shape):
    return f["full"] if shape == "" else f["stem"] + shape


def j(path, obj):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf8") as fh:
        json.dump(obj, fh, indent=2)


def load(path):
    with open(path, encoding="utf8") as fh:
        return json.load(fh)


def tag(path, values):
    d = load(path) if os.path.exists(path) else {"replace": False, "values": []}
    for v in values:
        if v not in d["values"]:
            d["values"].append(v)
    j(path, d)


def rename_json(obj, f, prefix):
    s = json.dumps(obj)
    s = s.replace("minecraft:block/" + f["stem"], f"immersive_weathering:block/{prefix}_{f['stem']}")
    s = s.replace("minecraft:" + f["stem"], f"immersive_weathering:{prefix}_{f['stem']}")
    return json.loads(s)


java_blocks, moss_map, crack_map, repl = [], [], [], []
new_ids = []

for f in FAMILIES:
    for prefix in [p for p in ("mossy", "cracked") if f[p]]:
        # textures
        op = "moss" if prefix == "mossy" else "crack"
        ref = "mossy_stone_bricks" if prefix == "mossy" else "cracked_stone_bricks"
        subprocess.run(["java", os.path.join(ROOT, "tools", "TexGen.java"), op,
                        f"{V}/textures/block/{f['full']}.png", f"{V}/textures/block/stone_bricks.png",
                        f"{V}/textures/block/{ref}.png", f"{A}/textures/block/{prefix}_{f['full']}.png"], check=True)
        for shape in SHAPES:
            van = bid(f, shape)
            new = f"{prefix}_{van}"
            new_ids.append(new)
            # blockstate + models + item
            bs = load(f"{V}/blockstates/{van}.json")
            models = set(re.findall(r'"minecraft:block/[a-z0-9_]+"', json.dumps(bs)))
            item = load(f"{V}/items/{van}.json")
            models |= set(re.findall(r'"minecraft:block/[a-z0-9_]+"', json.dumps(item)))
            j(f"{A}/blockstates/{new}.json", rename_json(bs, f, prefix))
            j(f"{A}/items/{new}.json", rename_json(item, f, prefix))
            for m in models:
                name = m.strip('"').split("/")[-1]
                src = f"{V}/models/block/{name}.json"
                j(f"{A}/models/block/{prefix}_{name}.json", rename_json(load(src), f, prefix))
            # loot
            j(f"{D}/loot_table/blocks/{new}.json", rename_json(load(f"{VD}/loot_table/blocks/{van}.json"), f, prefix))

            # java
            const = new.upper()
            full_const = f"{prefix}_{f['full']}".upper()
            copy = f"Properties.ofFullCopy(Blocks.{f['full'].upper()})"
            if prefix == "mossy":
                cls = {"": "MossyBlock", "_stairs": "MossyStairsBlock", "_slab": "MossySlabBlock", "_wall": "MossyWallBlock"}[shape]
                args = "Mossable.MossLevel.MOSSY, " + (f"{full_const}, " if shape == "_stairs" else "")
                moss_map.append((f"Blocks.{van.upper()}", f"ModBlocks.{const}.get()"))
            else:
                cls = {"": "CrackedBlock", "_stairs": "CrackedStairsBlock", "_slab": "CrackedSlabBlock", "_wall": "CrackedWallBlock"}[shape]
                args = "Crackable.CrackLevel.CRACKED, " + (f"{full_const}, " if shape == "_stairs" else "") + f"{f['item']}, "
                crack_map.append((f"Blocks.{van.upper()}", f"ModBlocks.{const}.get()"))
            java_blocks.append(f'    public static final Supplier<Block> {const} = regWithItem("{new}", p ->\n'
                               f'        new {cls}({args}p), () -> {copy});')

    # vanilla blocks of the family become mossable + crackable
    for shape in SHAPES:
        van = bid(f, shape)
        cls = {"": "CrackableMossableBlock", "_stairs": "CrackableMossableStairsBlock",
               "_slab": "CrackableMossableSlabBlock", "_wall": "CrackableMossableWallBlock"}[shape]
        extra = f", () -> Blocks.{f['full'].upper()}" if shape == "_stairs" else ""
        repl.append((van, f'        R.put("{van}", p -> new {cls}(Mossable.MossLevel.UNAFFECTED, Crackable.CrackLevel.UNCRACKED, {f["item"]}{extra}, p));'))

# ---- Java patches ----
p = f"{JAVA}/reg/ModBlocks.java"
s = open(p, encoding="utf8").read()
anchor = "    public static final Supplier<Block> TINTED_GLASS_PANE ="
s = s.replace(anchor, "    //port addition: mossy / cracked tuff, mud and deepslate bricks\n" + "\n".join(java_blocks) + "\n\n" + anchor, 1)
open(p, "w", encoding="utf8").write(s)

p = f"{JAVA}/blocks/mossy/Mossable.java"
s = open(p, encoding="utf8").read()
a = "                .put(Blocks.BRICK_WALL, ModBlocks.MOSSY_BRICK_WALL.get());"
s = s.replace(a, a[:-1] + "\n" + "\n".join(f"                .put({k}, {v})" for k, v in moss_map) + ";", 1)
open(p, "w", encoding="utf8").write(s)

p = f"{JAVA}/blocks/cracked/Crackable.java"
s = open(p, encoding="utf8").read()
a = "            .put(Blocks.END_STONE_BRICK_WALL, ModBlocks.CRACKED_END_STONE_BRICK_WALL.get());"
s = s.replace(a, a[:-1] + "\n" + "\n".join(f"            .put({k}, {v})" for k, v in crack_map) + ";", 1)
open(p, "w", encoding="utf8").write(s)

p = f"{JAVA}/reg/VanillaBlockReplacements.java"
s = open(p, encoding="utf8").read()
for van, line in repl:
    s = re.sub(r'        R\.put\("' + van + r'", [^\n]*\n', "", s)
a = "        R.put(\"stone\", "
s = s.replace(a, "\n".join(l for _, l in repl) + "\n" + a, 1)
open(p, "w", encoding="utf8").write(s)

# ---- tags ----
ns = [f"immersive_weathering:{i}" for i in new_ids]
tag(f"{MC}/tags/block/mineable/pickaxe.json", ns)
tag(f"{MC}/tags/block/walls.json", [i for i in ns if i.endswith("_wall")])
tag(f"{MC}/tags/item/walls.json", [i for i in ns if i.endswith("_wall")])
tag(f"{MC}/tags/block/stairs.json", [i for i in ns if i.endswith("_stairs")])
tag(f"{MC}/tags/item/stairs.json", [i for i in ns if i.endswith("_stairs")])
tag(f"{MC}/tags/block/slabs.json", [i for i in ns if i.endswith("_slab")])
tag(f"{MC}/tags/item/slabs.json", [i for i in ns if i.endswith("_slab")])
tag(f"{D}/tags/block/mossy.json", [i for i in ns if ":mossy_" in i])
tag(f"{D}/tags/block/cracked.json", [i for i in ns if ":cracked_" in i])
mossable, crackable = [], []
for f in FAMILIES:
    for shape in SHAPES:
        van = "minecraft:" + bid(f, shape)
        if f["mossy"]:
            mossable.append(van)
        if f["cracked"]:
            crackable.append(van)
tag(f"{D}/tags/block/mossable.json", mossable)
tag(f"{D}/tags/block/crackable.json", crackable)

# ---- lang ----
shape_en = {"": "s", "_stairs": " Stairs", "_slab": " Slab", "_wall": " Wall"}
for f in FAMILIES:
    pass
en = load(f"{A}/lang/en_us.json")
ru = load(f"{A}/lang/ru_ru.json")
ru_shape = {"": "{adj} {noun}", "_stairs": "Ступени из {gen}", "_slab": "Плита из {gen}", "_wall": "Стена из {gen}"}
RU = {  # adjective (plural) for full block, genitive for the shapes
    ("mossy", "tuff_bricks"): ("Замшелые туфовые кирпичи", "замшелых туфовых кирпичей"),
    ("cracked", "tuff_bricks"): ("Потрескавшиеся туфовые кирпичи", "потрескавшихся туфовых кирпичей"),
    ("mossy", "mud_bricks"): ("Замшелые кирпичи из грязи", "замшелых кирпичей из грязи"),
    ("cracked", "mud_bricks"): ("Потрескавшиеся кирпичи из грязи", "потрескавшихся кирпичей из грязи"),
    ("mossy", "deepslate_bricks"): ("Замшелые глубинносланцевые кирпичи", "замшелых глубинносланцевых кирпичей"),
    ("mossy", "deepslate_tiles"): ("Замшелая глубинносланцевая плитка", "замшелой глубинносланцевой плитки"),
}
for f in FAMILIES:
    for prefix in [p for p in ("mossy", "cracked") if f[p]]:
        full_ru, gen_ru = RU[(prefix, f["full"])]
        for shape in SHAPES:
            new = f"{prefix}_{bid(f, shape)}"
            noun_en = f["en"] + ("s" if shape == "" else shape_en[shape])
            en.setdefault(f"block.immersive_weathering.{new}", f"{prefix.capitalize()} {noun_en}")
            ru.setdefault(f"block.immersive_weathering.{new}", full_ru if shape == "" else
                          {"_stairs": "Ступени из ", "_slab": "Плита из ", "_wall": "Стена из "}[shape] + gen_ru)
j(f"{A}/lang/en_us.json", en)
with open(f"{A}/lang/ru_ru.json", "w", encoding="utf8") as fh:
    json.dump(ru, fh, ensure_ascii=False, indent=2)
print("blocks:", len(new_ids))
