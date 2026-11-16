"""
One-off converter of loot tables and advancements to the 26.3 formats:
- "conditions": [..] -> "condition": {..}  (several -> minecraft:all_of)
- {"condition": "minecraft:x", ...} -> {"type": "minecraft:x", ...}
- "functions": [..] -> "modifier": {..} or [..]; "function" -> "type"
- block_state_property {block, properties} -> match_block {blocks, state}
- number providers {min, max} without type -> minecraft:uniform
- advancement trigger conditions given as a list of predicates -> one condition
- recipe_unlocked "recipe" -> "recipes"

usage: python tools/convert_26_3_loot.py
"""
import glob
import json
import os

ROOT = os.path.join(os.path.dirname(__file__), "..", "src", "main", "resources", "data")


def is_cond(o):
    return isinstance(o, dict) and isinstance(o.get("condition"), str)


def join(conds):
    conds = [conv(c) for c in conds]
    if len(conds) == 1:
        return conds[0]
    return {"type": "minecraft:all_of", "terms": conds}


def number(o):
    if isinstance(o, dict) and "type" not in o and set(o) <= {"min", "max"} and o:
        return {"type": "minecraft:uniform", **o}
    return o


def conv(o):
    if isinstance(o, list):
        if o and all(is_cond(x) for x in o):
            return join(o)
        return [conv(x) for x in o]
    if not isinstance(o, dict):
        return o
    out = {}
    for k, v in o.items():
        if k == "condition" and isinstance(v, str):
            out["type"] = v
        elif k == "function" and isinstance(v, str):
            out["type"] = v
        elif k == "conditions" and isinstance(v, list):
            out["condition"] = join(v)
        elif k == "functions" and isinstance(v, list):
            fs = [conv(x) for x in v]
            out["modifier"] = fs[0] if len(fs) == 1 else fs
        elif k == "terms" and isinstance(v, list):
            out[k] = [conv(x) for x in v]
        elif k in ("count", "rolls", "bonus_rolls", "chance", "levels") and isinstance(v, dict):
            out[k] = conv(number(v))
        else:
            out[k] = conv(v)
    if out.get("type") == "minecraft:block_state_property":
        out["type"] = "minecraft:match_block"
        if "block" in out:
            out["blocks"] = out.pop("block")
        if "properties" in out:
            out["state"] = out.pop("properties")
    if out.get("trigger") == "minecraft:recipe_unlocked":
        c = out.get("conditions", {})
        if "recipe" in c:
            c["recipes"] = c.pop("recipe")
    return out


def run(pattern, advancement):
    n = 0
    for p in glob.glob(os.path.join(ROOT, pattern), recursive=True):
        with open(p, encoding="utf8") as f:
            d = json.load(f)
        if advancement:
            # criteria "conditions" are an object here, only their inner predicate lists change
            d2 = dict(d)
            crit = {}
            for name, c in d.get("criteria", {}).items():
                c = dict(c)
                if "conditions" in c:
                    c["conditions"] = {k: conv(v) for k, v in c["conditions"].items()}
                c = conv({k: v for k, v in c.items() if k != "conditions"}) | ({"conditions": c["conditions"]} if "conditions" in c else {})
                if c.get("trigger") == "minecraft:recipe_unlocked" and "recipe" in c.get("conditions", {}):
                    c["conditions"]["recipes"] = c["conditions"].pop("recipe")
                crit[name] = c
            if crit:
                d2["criteria"] = crit
        else:
            d2 = conv(d)
        if d2 != d:
            with open(p, "w", encoding="utf8") as f:
                json.dump(d2, f, indent=2)
            n += 1
    return n


print("loot tables", run("*/loot_table/**/*.json", False))
print("advancements", run("*/advancement/**/*.json", True))
