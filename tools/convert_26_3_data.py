"""
One-off converter of the mod's data files to the 26.3 formats:
- block states: {"Name", "Properties"} -> {"id", "properties"} (or a plain id string)
- worldgen: configured_feature/ -> feature/, "config" objects flattened into the feature,
  "*_state_provider" types renamed, simple providers replaced by the bare state
- the vanilla big-tree overrides are rebuilt from the 26.3 vanilla files with the mod's
  dirt / rooted dirt mix under the trunk

usage: python tools/convert_26_3_data.py <vanilla 26.3 data dir (…/data/minecraft)>
"""
import json
import os
import shutil
import sys

ROOT = os.path.join(os.path.dirname(__file__), "..", "src", "main", "resources", "data")
VANILLA = sys.argv[1]

PROVIDER_RENAMES = {
    "minecraft:simple_state_provider": "minecraft:simple",
    "minecraft:weighted_state_provider": "minecraft:weighted",
    "minecraft:rule_based_state_provider": "minecraft:rule_based",
    "minecraft:rotated_block_provider": "minecraft:rotated",
    "minecraft:randomized_int_state_provider": "minecraft:randomized_int",
    "minecraft:noise_provider": "minecraft:noise",
    "minecraft:noise_threshold_provider": "minecraft:noise_threshold",
    "minecraft:dual_noise_provider": "minecraft:dual_noise",
}


def state(o):
    props = o.get("Properties")
    if props:
        return {"id": o["Name"], "properties": props}
    return {"id": o["Name"]}


def conv(o, worldgen):
    if isinstance(o, list):
        return [conv(x, worldgen) for x in o]
    if not isinstance(o, dict):
        return o
    if "Name" in o and isinstance(o["Name"], str) and set(o) <= {"Name", "Properties"}:
        return state(o)
    o = {k: conv(v, worldgen) for k, v in o.items()}
    if worldgen:
        t = o.get("type")
        if t in PROVIDER_RENAMES:
            o["type"] = PROVIDER_RENAMES[t]
            t = o["type"]
        if t == "minecraft:simple" and set(o) == {"type", "state"}:
            return o["state"]
        if isinstance(o.get("config"), dict) and "type" in o:
            cfg = o.pop("config")
            o.update(cfg)
    return o


def rewrite(path, worldgen):
    with open(path, encoding="utf8") as f:
        d = json.load(f)
    d2 = conv(d, worldgen)
    if d2 != d:
        with open(path, "w", encoding="utf8") as f:
            json.dump(d2, f, indent=2)
        return True
    return False


changed = 0
for ns in os.listdir(ROOT):
    for sub in ("block_growths", "fluid_generators"):
        p = os.path.join(ROOT, ns, sub)
        if os.path.isdir(p):
            for dp, _, fs in os.walk(p):
                for fn in fs:
                    if fn.endswith(".json"):
                        changed += rewrite(os.path.join(dp, fn), False)

# mod features
cf = os.path.join(ROOT, "immersive_weathering", "worldgen", "configured_feature")
if os.path.isdir(cf):
    dst = os.path.join(ROOT, "immersive_weathering", "worldgen", "feature")
    os.makedirs(dst, exist_ok=True)
    for fn in os.listdir(cf):
        shutil.move(os.path.join(cf, fn), os.path.join(dst, fn))
        rewrite(os.path.join(dst, fn), True)
        changed += 1
    os.rmdir(cf)

# vanilla tree overrides
vcf = os.path.join(ROOT, "minecraft", "worldgen", "configured_feature")
if os.path.isdir(vcf):
    dst = os.path.join(ROOT, "minecraft", "worldgen", "feature")
    os.makedirs(dst, exist_ok=True)
    rooted = {
        "type": "minecraft:rule_based",
        "rules": [{
            "if_true": {"type": "minecraft:not", "predicate": {
                "type": "minecraft:matching_block_tag", "tag": "minecraft:cannot_replace_below_tree_trunk"}},
            "then": {"type": "minecraft:weighted", "entries": [
                {"data": {"id": "minecraft:dirt"}, "weight": 1},
                {"data": {"id": "minecraft:rooted_dirt"}, "weight": 1}]}
        }]
    }
    for fn in os.listdir(vcf):
        with open(os.path.join(VANILLA, "worldgen", "feature", fn), encoding="utf8") as f:
            v = json.load(f)
        v["below_trunk_provider"] = rooted
        with open(os.path.join(dst, fn), "w", encoding="utf8") as f:
            json.dump(v, f, indent=2)
        os.remove(os.path.join(vcf, fn))
        changed += 1
    os.rmdir(vcf)

print("changed", changed)
