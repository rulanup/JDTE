import struct, zlib

def write_png(path, pixels):
    def chunk(tag, data):
        c = struct.pack('>I', len(data)) + tag + data
        return c + struct.pack('>I', zlib.crc32(tag + data) & 0xFFFFFFFF)
    w = h = 16
    ihdr = struct.pack('>IIBBBBB', w, h, 8, 2, 0, 0, 0)
    raw = b''
    for row in pixels:
        raw += b'\x00' + bytes([c for px in row for c in px])
    idat = zlib.compress(raw)
    with open(path, 'wb') as f:
        f.write(b'\x89PNG\r\n\x1a\n')
        f.write(chunk(b'IHDR', ihdr))
        f.write(chunk(b'IDAT', idat))
        f.write(chunk(b'IEND', b''))

BG  = (30, 30, 35)
ITM = (220, 130, 30)   # orange  – item
FLD = (50, 120, 200)   # blue    – fluid
NRG = (220, 50, 50)    # red     – energy
OFF = (0, 0, 0)

pixels = [[OFF]*16 for _ in range(16)]

# Panel background rows 6–15, cols 3–12
for r in range(6, 16):
    for c in range(3, 13):
        pixels[r][c] = BG

# Item port (orange 3×3 square) cols 4–6, rows 8–10
for r in range(8, 11):
    for c in range(4, 7):
        pixels[r][c] = ITM

# Fluid port (blue 3×3 filled circle) cols 7–9, rows 8–10
for r, c in [(8,7),(8,8),(8,9),(9,7),(9,8),(9,9),(10,7),(10,8),(10,9)]:
    pixels[r][c] = FLD

# Energy port (red zigzag) cols 10–11, rows 8–11
for r, c in [(8,11),(9,10),(9,11),(10,10),(11,10)]:
    pixels[r][c] = NRG

write_png('D:/Code/java/JDTE/src/main/resources/assets/jdte/textures/block/greenhouse_io_panel.png', pixels)
print("ok")
