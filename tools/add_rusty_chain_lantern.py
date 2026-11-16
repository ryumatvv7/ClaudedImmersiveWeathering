"""Resources for the rusting iron chain / lantern blocks (one-off)."""
import json
import os

R = os.path.join(os.path.dirname(__file__), "..", "src", "main", "resources")
A = os.path.join(R, "assets", "immersive_weathering")
D = os.path.join(R, "data", "immersive_weathering")
M = os.path.join(R, "data", "minecraft")
LEVELS = ["exposed", "weathered", "rusted"]


def w(path, obj):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf8") as f:
        json.dump(obj, f, indent=2)


def tag(path, values):
    d = {"replace": False, "values": []}
    if os.path.exists(path):
        with open(path, encoding="utf8") as f:
            d = json.load(f)
    for v in values:
        if v not in d["values"]:
            d["values"].append(v)
    w(path, d)


ids = []
for lv in LEVELS:
    chain, lantern = f"{lv}_iron_chain", f"{lv}_lantern"
    ids += [chain, lantern]
    w(f"{A}/blockstates/{chain}.json", {"variants": {
        "axis=x": {"model": f"immersive_weathering:block/{chain}", "x": 90, "y": 90},
        "axis=y": {"model": f"immersive_weathering:block/{chain}"},
        "axis=z": {"model": f"immersive_weathering:block/{chain}", "x": 90}}})
    w(f"{A}/models/block/{chain}.json", {"parent": "minecraft:block/template_chain",
                                         "textures": {"texture": f"immersive_weathering:block/{chain}"}})
    w(f"{A}/blockstates/{lantern}.json", {"variants": {
        "hanging=false": {"model": f"immersive_weathering:block/{lantern}"},
        "hanging=true": {"model": f"immersive_weathering:block/{lantern}_hanging"}}})
    w(f"{A}/models/block/{lantern}.json", {"parent": "minecraft:block/template_lantern",
                                           "textures": {"lantern": f"immersive_weathering:block/{lantern}"}})
    w(f"{A}/models/block/{lantern}_hanging.json", {"parent": "minecraft:block/template_hanging_lantern",
                                                   "textures": {"lantern": f"immersive_weathering:block/{lantern}"}})
    for i in (chain, lantern):
        w(f"{A}/models/item/{i}.json", {"parent": "minecraft:item/generated",
                                        "textures": {"layer0": f"immersive_weathering:item/{i}"}})
        w(f"{A}/items/{i}.json", {"model": {"type": "minecraft:model", "model": f"immersive_weathering:item/{i}"}})
        w(f"{D}/loot_table/blocks/{i}.json", {"type": "minecraft:block", "pools": [{
            "rolls": 1, "entries": [{"type": "minecraft:item", "name": f"immersive_weathering:{i}"}],
            "condition": {"type": "minecraft:survives_explosion"}}]})
    group = "exposed_iron" if lv != "rusted" else "rusted_iron"
    tag(f"{D}/tags/block/{group}.json", [f"immersive_weathering:{chain}", f"immersive_weathering:{lantern}"])
    tag(f"{D}/tags/item/{group}.json", [f"immersive_weathering:{chain}", f"immersive_weathering:{lantern}"])

full = [f"immersive_weathering:{i}" for i in ids]
tag(f"{M}/tags/block/mineable/pickaxe.json", full)
tag(f"{D}/tags/block/rustable.json", ["minecraft:iron_chain", "minecraft:lantern"] +
    [f"immersive_weathering:{lv}_{k}" for lv in ("exposed", "weathered") for k in ("iron_chain", "lantern")])
tag(f"{D}/tags/block/clean_iron.json", ["minecraft:iron_chain", "minecraft:lantern"])

names = {
    "en_us.json": {"exposed": "Exposed", "weathered": "Weathered", "rusted": "Rusted", "chain": "Iron Chain", "lantern": "Lantern"},
    "ru_ru.json": {"exposed": "Тронутая ржавчиной", "weathered": "Ржавеющая", "rusted": "Ржавая", "chain": "железная цепь",
                   "lantern": None},
}
ru_lantern = {"exposed": "Тронутый ржавчиной фонарь", "weathered": "Ржавеющий фонарь", "rusted": "Ржавый фонарь"}
for fn, n in names.items():
    p = f"{A}/lang/{fn}"
    with open(p, encoding="utf8") as f:
        d = json.load(f)
    for lv in LEVELS:
        d.setdefault(f"block.immersive_weathering.{lv}_iron_chain", f"{n[lv]} {n['chain']}")
        d.setdefault(f"block.immersive_weathering.{lv}_lantern",
                     ru_lantern[lv] if fn == "ru_ru.json" else f"{n[lv]} {n['lantern']}")
    with open(p, "w", encoding="utf8") as f:
        json.dump(d, f, ensure_ascii=False, indent=2)
print("done")
