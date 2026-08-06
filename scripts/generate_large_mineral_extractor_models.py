import json
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
MODEL_DIR = ROOT / "src/main/resources/assets/jdte/models/block"
ITEM_MODEL_DIR = ROOT / "src/main/resources/assets/jdte/models/item"
BLOCKSTATE_DIR = ROOT / "src/main/resources/assets/jdte/blockstates"

X_PARTS = (("left", -16, 0), ("center", 0, 16), ("right", 16, 32))
DEPTH_PARTS = (("front", 0, 16), ("middle", 16, 32), ("back", 32, 48))
LAYER_PARTS = (("base", 0, 16), ("top", 16, 32))
FACING_ROTATIONS = (("north", 0), ("east", 90), ("south", 180), ("west", 270))

TEXTURES = {
    "casing": "justdirethings:block/eclipsealloy_block",
    "frame": "justdirethings:block/blazegold_block",
    "foundation": "minecraft:block/polished_deepslate",
    "rail": "minecraft:block/smooth_basalt",
    "metal": "minecraft:block/iron_block",
    "pipe": "minecraft:block/cut_copper",
    "glass": "minecraft:block/tinted_glass",
    "fluid_side": "jdte:block/advanced_fluid_receiver_side",
    "fluid_top": "jdte:block/advanced_fluid_receiver_top",
    "output": "jdte:block/advanced_item_receiver_top",
    "particle": "minecraft:block/polished_deepslate",
}

DIRECTIONS = {
    "down": (1, 1),
    "up": (4, 4),
    "north": (2, 2),
    "south": (5, 5),
    "west": (0, 0),
    "east": (3, 3),
}


def box(name, bounds, texture, **face_textures):
    """Declare one machine-space cuboid; bounds may cross structure blocks."""
    return {
        "name": name,
        "bounds": bounds,
        "textures": {direction: face_textures.get(direction, texture) for direction in DIRECTIONS},
    }


def machine_parts():
    parts = []

    # A recessed foundation and separate feet keep the silhouette from reading
    # as one solid 3x3 cube.
    parts += [
        box("foundation", (-14, 2, 2, 30, 6, 46), "foundation"),
        box("front_rail", (-14, 6, 2, 30, 8, 6), "rail"),
        box("back_rail", (-14, 6, 42, 30, 8, 46), "rail"),
        box("left_rail", (-14, 6, 6, -10, 8, 42), "rail"),
        box("right_rail", (26, 6, 6, 30, 8, 42), "rail"),
    ]
    for x1, x2 in ((-14, -8), (24, 30)):
        for z1, z2 in ((2, 8), (40, 46)):
            parts.append(box("support_foot", (x1, 0, z1, x2, 3, z2), "foundation"))

    # Four compact columns and open sides form a drilling gantry rather than a
    # glass tank. Gold is restricted to load-bearing joints and energy accents.
    for x1, x2 in ((-12, -8), (24, 28)):
        for z1, z2 in ((5, 9), (39, 43)):
            parts += [
                box("lower_column", (x1, 8, z1, x2, 19, z2), "casing"),
                box("column_joint", (x1 - 1, 18, z1 - 1, x2 + 1, 21, z2 + 1), "frame"),
                box("upper_column", (x1, 21, z1, x2, 27, z2), "casing"),
            ]

    # Narrow inspection windows leave most of the mechanism exposed.
    parts += [
        box("left_window", (-11, 11, 13, -10, 18, 35), "glass"),
        box("right_window", (26, 11, 13, 27, 18, 35), "glass"),
        box("left_window_lower", (-12, 10, 11, -9, 12, 37), "casing"),
        box("left_window_upper", (-12, 17, 11, -9, 19, 37), "casing"),
        box("right_window_lower", (25, 10, 11, 28, 12, 37), "casing"),
        box("right_window_upper", (25, 17, 11, 28, 19, 37), "casing"),
    ]

    # Perimeter beams are separated instead of using a full roof slab.
    parts += [
        box("front_top_beam", (-13, 27, 3, 29, 31, 8), "casing"),
        box("back_top_beam", (-13, 27, 40, 29, 31, 45), "casing"),
        box("left_top_beam", (-13, 27, 8, -8, 31, 40), "casing"),
        box("right_top_beam", (24, 27, 8, 29, 31, 40), "casing"),
        box("front_energy_strip", (-8, 26, 7, 24, 28, 9), "frame"),
        box("back_energy_strip", (-8, 26, 39, 24, 28, 41), "frame"),
    ]

    # The central power head, segmented shaft, collar and stepped bit provide a
    # strong visual focus visible through the open gantry.
    parts += [
        box("power_head", (-1, 20, 17, 17, 29, 31), "casing"),
        box("power_head_band", (1, 19, 19, 15, 22, 29), "frame"),
        box("upper_shaft", (5, 17, 21, 11, 20, 27), "metal"),
        box("shaft_coupler", (3, 15, 19, 13, 18, 29), "frame"),
        box("lower_shaft", (6, 9, 22, 10, 15, 26), "metal"),
        box("drill_collar", (2, 7, 18, 14, 10, 30), "casing"),
        box("drill_bit_upper", (5, 5, 21, 11, 8, 27), "metal"),
        box("drill_bit_middle", (6, 3, 22, 10, 5, 26), "metal"),
        box("drill_bit_tip", (7, 1, 23, 9, 3, 25), "metal"),
    ]

    # Existing machine textures identify interaction and automation faces.
    parts += [
        box("controller_console", (2, 8, 0, 14, 15, 3), "casing", north="output", up="frame"),
        box("left_fluid_port", (-16, 7, 18, -12, 13, 26), "fluid_side", west="fluid_top"),
        box("right_output_port", (28, 7, 18, 32, 13, 30), "casing", east="output"),
        box("rear_power_bus", (3, 8, 44, 13, 15, 48), "pipe", south="frame"),
        box("left_pipe_vertical", (-9, 8, 32, -6, 24, 35), "pipe"),
        box("left_pipe_horizontal", (-9, 22, 24, 4, 25, 27), "pipe"),
    ]
    return parts


def is_original_surface(direction, original, clipped):
    original_axis, clipped_axis = DIRECTIONS[direction]
    return original[original_axis] == clipped[clipped_axis]


def default_uv(direction, bounds):
    x1, y1, z1, x2, y2, z2 = bounds
    if direction in ("north", "south"):
        return [x1, 16 - y2, x2, 16 - y1]
    if direction in ("west", "east"):
        return [z1, 16 - y2, z2, 16 - y1]
    return [x1, z1, x2, z2]


def clipped_element(part, cell):
    original = part["bounds"]
    x1, y1, z1, x2, y2, z2 = original
    cx1, cy1, cz1, cx2, cy2, cz2 = cell
    clipped = (
        max(x1, cx1), max(y1, cy1), max(z1, cz1),
        min(x2, cx2), min(y2, cy2), min(z2, cz2),
    )
    if clipped[0] >= clipped[3] or clipped[1] >= clipped[4] or clipped[2] >= clipped[5]:
        return None

    local = (
        clipped[0] - cx1, clipped[1] - cy1, clipped[2] - cz1,
        clipped[3] - cx1, clipped[4] - cy1, clipped[5] - cz1,
    )
    faces = {}
    for direction in DIRECTIONS:
        if not is_original_surface(direction, original, clipped):
            continue
        faces[direction] = {
            "texture": f"#{part['textures'][direction]}",
            "uv": default_uv(direction, local),
            "shade": False,
        }

    if not faces:
        return None
    return {
        "name": part["name"],
        "from": list(local[:3]),
        "to": list(local[3:]),
        "faces": faces,
    }


def model_for(cell):
    elements = [clipped_element(part, cell) for part in machine_parts()]
    return {
        "parent": "minecraft:block/block",
        "ambientocclusion": False,
        "render_type": "minecraft:cutout",
        "textures": TEXTURES,
        "elements": [element for element in elements if element is not None],
    }


def model_name(layer, depth, x_part):
    return f"large_mineral_extractor_{layer}_{depth}_{x_part}"


def write_json(path, data):
    path.write_text(json.dumps(data, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


def generate_models():
    for layer, y1, y2 in LAYER_PARTS:
        for depth, z1, z2 in DEPTH_PARTS:
            for x_part, x1, x2 in X_PARTS:
                model = model_for((x1, y1, z1, x2, y2, z2))
                if (layer, depth, x_part) == ("base", "front", "center"):
                    path = MODEL_DIR / "large_mineral_extractor.json"
                else:
                    path = MODEL_DIR / f"{model_name(layer, depth, x_part)}.json"
                write_json(path, model)


def item_element(part):
    x1, y1, z1, x2, y2, z2 = part["bounds"]
    # Fit the complete 48x32x48 machine into one item-model cube while keeping
    # a small margin so GuideME and inventory previews do not clip the frame.
    scale = 14.0 / 48.0
    bounds = (
        1 + (x1 + 16) * scale,
        1 + y1 * scale,
        1 + z1 * scale,
        1 + (x2 + 16) * scale,
        1 + y2 * scale,
        1 + z2 * scale,
    )
    faces = {
        direction: {
            "texture": f"#{part['textures'][direction]}",
            "uv": default_uv(direction, bounds),
        }
        for direction in DIRECTIONS
    }
    return {
        "name": part["name"],
        "from": [round(value, 5) for value in bounds[:3]],
        "to": [round(value, 5) for value in bounds[3:]],
        "faces": faces,
    }


def generate_item_model():
    write_json(ITEM_MODEL_DIR / "large_mineral_extractor.json", {
        "parent": "minecraft:block/block",
        "gui_light": "front",
        "ambientocclusion": False,
        "render_type": "minecraft:cutout",
        "textures": TEXTURES,
        "elements": [item_element(part) for part in machine_parts()],
        "display": {
            "gui": {"rotation": [30, 225, 0], "translation": [0, 0, 0], "scale": [1, 1, 1]},
            "ground": {"translation": [0, 3, 0], "scale": [0.25, 0.25, 0.25]},
            "fixed": {"rotation": [0, 180, 0], "scale": [0.5, 0.5, 0.5]},
        },
    })


def generate_controller_blockstate():
    variants = {
        f"facing={facing}": {
            "model": "jdte:block/large_mineral_extractor",
            **({"y": rotation} if rotation else {}),
        }
        for facing, rotation in FACING_ROTATIONS
    }
    variants["facing=up"] = {"model": "jdte:block/large_mineral_extractor"}
    variants["facing=down"] = {"model": "jdte:block/large_mineral_extractor", "y": 180}
    write_json(BLOCKSTATE_DIR / "large_mineral_extractor.json", {"variants": variants})


def generate_part_blockstate():
    multipart = []
    for layer, _, _ in LAYER_PARTS:
        for depth, _, _ in DEPTH_PARTS:
            for x_part, _, _ in X_PARTS:
                if (layer, depth, x_part) == ("base", "front", "center"):
                    continue
                for facing, rotation in FACING_ROTATIONS:
                    apply = {"model": f"jdte:block/{model_name(layer, depth, x_part)}"}
                    if rotation:
                        apply["y"] = rotation
                    multipart.append({
                        "when": {
                            "facing": facing,
                            "x_part": x_part,
                            "depth_part": depth,
                            "layer_part": layer,
                        },
                        "apply": apply,
                    })
    write_json(BLOCKSTATE_DIR / "large_mineral_extractor_part.json", {"multipart": multipart})


def main():
    generate_models()
    generate_item_model()
    generate_controller_blockstate()
    generate_part_blockstate()
    print("Generated open-frame industrial drilling platform models")


if __name__ == "__main__":
    main()