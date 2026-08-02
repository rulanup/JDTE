#!/usr/bin/env python3
"""Generate 16x16 seamless texture tiles for the Life Synthesis Vat.

Deterministic pixel art with a dependency-free PNG encoder, matching the
project's existing texture generation style. The controller frame uses a
dark crimson alloy (distinct from the bluish Eclipse Alloy frames of other
machines) with red-copper energy trim and life-green accents; the glass is a
translucent deep-red pane with soft noise.
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


def frame_pixel(x, y):
    """Dark crimson alloy frame: beveled edges, copper trim, life-green studs."""
    # Outer bevel
    if x == 0 or y == 0:
        return (96, 104, 118, 255)
    if x == 15 or y == 15:
        return (34, 38, 46, 255)
    # Inner rim shading
    if x == 1 or y == 1:
        return (64, 70, 82, 255)
    if x == 14 or y == 14:
        return (44, 48, 58, 255)

    # Metal body with deterministic noise
    noise = ((x * 11 + y * 7 + (x * y) % 5) * 13) % 9
    r = clamp(66 - 5 + noise)
    g = clamp(58 - 5 + noise)
    b = clamp(72 - 4 + noise)

    # Red-copper energy trim (horizontal bands)
    if y == 4 and 3 <= x <= 12:
        return (178, 82, 62, 255) if x in (3, 12) else (150, 66, 50, 255)
    if y == 11 and 3 <= x <= 12:
        return (178, 82, 62, 255) if x in (3, 12) else (150, 66, 50, 255)

    # Life-green studs in the center band
    if x in (6, 9) and y == 7:
        return (110, 208, 140, 255)
    if x in (6, 9) and y == 8:
        return (84, 166, 112, 255)

    # Center rivet
    if x in (7, 8) and y in (7, 8):
        return (30, 34, 42, 255) if x == 7 and y == 7 else (52, 58, 70, 255)

    return (r, g, b, 255)


def glass_pixel(x, y):
    """Seamless translucent deep-red glass: soft noise and diagonal streaks."""
    noise = ((x * 7 + y * 13 + (x * y) % 5) * 31) % 11
    r = clamp(204 - 6 + noise)
    g = clamp(78 - 4 + noise)
    b = clamp(88 - 3 + noise)
    if (x + y) % 4 == 0:
        r = clamp(r + 9)
        g = clamp(g + 5)
        b = clamp(b + 5)
    elif (x + y) % 4 == 3:
        r = clamp(r - 8)
        g = clamp(g - 4)
        b = clamp(b - 4)
    alpha = 150 + (noise % 3) * 6
    return (r, g, b, alpha)


def main():
    TEXTURE_DIR.mkdir(parents=True, exist_ok=True)
    write_png(
        TEXTURE_DIR / "life_synthesis_frame.png",
        [[frame_pixel(x, y) for x in range(16)] for y in range(16)],
    )
    write_png(
        TEXTURE_DIR / "life_synthesis_glass.png",
        [[glass_pixel(x, y) for x in range(16)] for y in range(16)],
    )
    print("generated life synthesis textures")


if __name__ == "__main__":
    main()
