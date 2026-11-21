"""Split the complete Blockbench item models into the 18 blocks used in-world.

Author the 3x3x2 structure at one-third scale in Blockbench's Java Block format.
North is the front and the controller occupies the front-center bottom cell.
"""
import json
from itertools import product
from pathlib import Path

ASSETS = Path(__file__).resolve().parents[1] / "src/main/resources/assets/jdte"
MACHINES = {
    "large_greenhouse": ("large_greenhouse_controller", "large_greenhouse_part", "wall", "large_greenhouse_roof"),
    "life_synthesis_vat": ("life_synthesis_vat", "life_synthesis_part", "wall", "life_synthesis_roof"),
    "large_mineral_extractor": ("large_mineral_extractor", "large_mineral_extractor_part", "top", None),
}
# Boundary axis, upper boundary, and signed U/V axes (1=X, 2=Y, 3=Z).
FACES = {
    "north": (2, False, -1, -2), "south": (2, True, 1, -2),
    "west": (0, False, 3, -2), "east": (0, True, -3, -2),
    "up": (1, True, 1, 3), "down": (1, False, 1, -3),
}


def write(path, obj):
    path.write_text(json.dumps(obj, indent=2) + "\n", encoding="utf-8")


def clip(element, origin):
    # Snap export precision back to machine units before comparing cell boundaries.
    low, high = ([round(v * 3, 3) for v in element[k]] for k in ("from", "to"))
    a = [max(o, n) for o, n in zip(origin, low)]
    b = [min(o + 16, n) for o, n in zip(origin, high)]
    if any(lo >= hi for lo, hi in zip(a, b)):
        return None

    def fraction(axis):
        i = abs(axis) - 1
        span = high[i] - low[i]
        start, end = (a[i] - low[i]) / span, (b[i] - low[i]) / span
        return (start, end) if axis > 0 else (1 - end, 1 - start)

    faces = {}
    for side, face in element["faces"].items():
        axis, upper, u_axis, v_axis = FACES[side]
        if (upper and b[axis] != high[axis]) or (not upper and a[axis] != low[axis]):
            continue
        u, v, U, V = face["uv"]
        u0, u1 = fraction(u_axis)
        v0, v1 = fraction(v_axis)
        coords = (u + (U-u)*u0, v + (V-v)*v0, u + (U-u)*u1, v + (V-v)*v1)
        faces[side] = {**face, "uv": [round(n, 6) for n in coords]}
    if not faces:
        return None
    return {"name": element["name"], "from": [round(n-o, 4) for n,o in zip(a,origin)],
            "to": [round(n-o, 4) for n,o in zip(b,origin)], "faces": faces}


def generate(machine):
    controller, part_block, upper_layer, legacy_roof = MACHINES[machine]
    source = json.loads((ASSETS / "models/item" / (machine + ".json")).read_text(encoding="utf-8"))
    multipart = []
    for x, y, z in product(range(3), range(2), range(3)):
        layer = ("base", upper_layer)[y]
        depth = ("front", "middle", "back")[z]
        column = ("left", "center", "right")[x]
        is_controller = (x,y,z) == (1,0,0)
        name = controller if is_controller else f"{machine}_{layer}_{depth}_{column}"
        origin = (x * 16, y * 16, z * 16)
        elements = [part for e in source["elements"] if (part := clip(e, origin))]
        model = {key: source[key] for key in ("parent", "textures", "render_type")}
        model["elements"] = elements
        write(ASSETS / "models/block" / (name + ".json"), model)
        if not is_controller:
            for angle, facing in enumerate(("north", "east", "south", "west")):
                condition = dict(facing=facing, x_part=column, depth_part=depth, layer_part=layer)
                apply = {"model": "jdte:block/" + name}
                if angle:
                    apply["y"] = angle * 90
                multipart.append({"when": condition, "apply": apply})
    if legacy_roof:
        multipart.append({"when": {"layer_part": "roof"}, "apply": {"model": "jdte:block/" + legacy_roof}})
    write(ASSETS / "blockstates" / (part_block + ".json"), {"multipart": multipart})


if __name__ == "__main__":
    for machine in MACHINES:
        generate(machine)
