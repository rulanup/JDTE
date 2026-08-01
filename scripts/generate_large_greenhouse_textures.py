#!/usr/bin/env python3
"""Generate 16x16 seamless texture tiles for the Large Greenhouse.

The output is deterministic pixel art, written with a dependency-free PNG
encoder so the tiles tile seamlessly and stay visually consistent with the
JDT-style metallic frames used by the rest of the mod.
"""

import struct
import zlib
from pathlib import Path


TEXTURE_DIR = Path(__file__).resolve().parent.parent / "src/main/resources/assets/jdte/textures/block"


def write_png(path, pixels):
    height = len(pixels)
    width = len(pixels[0])
    rgba = any(len(px) == 4 for row in pixels for px in row)

    def chunk(tag, data):
        return (
            struct.pack(">I", len(data))
            + tag
            + data
            + struct.pack(">I", zlib.crc32(tag + data) & 0xFFFFFFFF)
        )

    ihdr = struct.pack(">IIBBBBB", width, height, 8, 6 if rgba else 2, 0, 0, 0)
    raw = b"".join(b"\x00" + bytes(v for px in row for v in px) for row in pixels)
    png = (
        b"\x89PNG\r\n\x1a\n"
        + chunk(b"IHDR", ihdr)
        + chunk(b"IDAT", zlib.compress(raw, 9))
        + chunk(b"IEND", b"")
    )
    path.write_bytes(png)


def clamp(value, low=0, high=255):
    return max(low, min(high, value))


def glass_pixel(x, y):
    """Seamless frosted glass: soft noise and diagonal streaks, no border."""
    noise = ((x * 7 + y * 13 + (x * y) % 5) * 31) % 11
    r = clamp(184 - 4 + noise)
    g = clamp(207 - 4 + noise)
    b = clamp(219 - 3 + noise)
    if (x + y) % 4 == 0:
        r = clamp(r + 7)
        g = clamp(g + 8)
        b = clamp(b + 9)
    elif (x + y) % 4 == 3:
        r = clamp(r - 6)
        g = clamp(g - 7)
        b = clamp(b - 7)
    alpha = 148 + (noise % 3) * 6
    return (r, g, b, alpha)


def glass_edge_pixel(x, y):
    """1px frosted border with a transparent center, for panel reveals."""
    if x == 0 or x == 15 or y == 0 or y == 15:
        return (198, 222, 232, 190)
    if x == 1 or x == 14 or y == 1 or y == 14:
        return (118, 154, 174, 130)
    return (0, 0, 0, 0)


def io_panel_pixel(x, y):
    """Dark terminal panel with item, fluid, and energy ports."""
    if x == 0 or x == 15 or y == 0 or y == 15:
        return (86, 96, 110, 255)
    background = (34, 40, 48, 255)
    if x == 1 or x == 14 or y == 1 or y == 14:
        background = (46, 54, 64, 255)

    # Item port: orange 3x3
    if 3 <= x <= 5 and 8 <= y <= 10:
        if x == 3 and y == 8:
            return (255, 204, 128, 255)
        return (214, 118, 34, 255)
    # Fluid port: blue 3x3
    if 6 <= x <= 8 and 8 <= y <= 10:
        if x == 6 and y == 8:
            return (148, 214, 255, 255)
        return (48, 118, 196, 255)
    # Energy port: red 3x3
    if 10 <= x <= 12 and 8 <= y <= 10:
        if x == 10 and y == 8:
            return (255, 176, 156, 255)
        return (196, 58, 44, 255)
    # Status indicator
    if x == 13 and y == 5:
        return (122, 232, 152, 255)
    return background


def main():
    TEXTURE_DIR.mkdir(parents=True, exist_ok=True)
    write_png(
        TEXTURE_DIR / "large_greenhouse_glass.png",
        [[glass_pixel(x, y) for x in range(16)] for y in range(16)],
    )
    write_png(
        TEXTURE_DIR / "large_greenhouse_glass_edge.png",
        [[glass_edge_pixel(x, y) for x in range(16)] for y in range(16)],
    )
    write_png(
        TEXTURE_DIR / "greenhouse_io_panel.png",
        [[io_panel_pixel(x, y) for x in range(16)] for y in range(16)],
    )
    print("generated large greenhouse textures")


if __name__ == "__main__":
    main()
