import struct, zlib

# Generates large_greenhouse_glass_edge.png: vanilla glass texture with a
# 2px vertical metal frame on the left edge (u=0). This is the "connected"
# glass variant used by corner parts: the framed side always faces the
# structure edge (the corner post), the unframed side meets the neighbour
# wall glass seamlessly. Wall/roof parts use plain vanilla glass instead.

def read_png(path):
    with open(path, 'rb') as f:
        data = f.read()
    assert data[:8] == b'\x89PNG\r\n\x1a\n'
    pos = 8
    w = h = bd = ct = 0
    idat = b''
    while pos < len(data):
        ln = struct.unpack('>I', data[pos:pos + 4])[0]
        tag = data[pos + 4:pos + 8]
        if tag == b'IHDR':
            w, h, bd, ct = struct.unpack('>IIBB', data[pos + 8:pos + 18])
        elif tag == b'IDAT':
            idat += data[pos + 8:pos + 8 + ln]
        pos += 12 + ln
    raw = zlib.decompress(idat)
    stride = w * 4 + 1
    rows = [raw[i * stride + 1:(i + 1) * stride] for i in range(h)]
    return w, h, rows

def write_png(path, w, h, rows):
    def chunk(tag, data):
        c = struct.pack('>I', len(data)) + tag + data
        return c + struct.pack('>I', zlib.crc32(tag + data) & 0xFFFFFFFF)
    ihdr = struct.pack('>IIBBBBB', w, h, 8, 2, 0, 0, 0)
    raw = b''
    for row in rows:
        raw += b'\x00' + bytes([c for px in row for c in px])
    idat = zlib.compress(raw)
    with open(path, 'wb') as f:
        f.write(b'\x89PNG\r\n\x1a\n')
        f.write(chunk(b'IHDR', ihdr))
        f.write(chunk(b'IDAT', idat))
        f.write(chunk(b'IEND', b''))

# Frame colours sampled to match justdirethings eclipsealloy_block (dark blue-grey metal).
FRAME_LIGHT = (150, 170, 195, 255)  # u=0 column: bright metal edge
FRAME_DARK = (64, 76, 96, 255)      # u=1 column: shaded inner edge

w, h, rows = read_png('build/tmp_glass.png')
assert (w, h) == (16, 16)

out = []
for r in range(h):
    row = []
    for c in range(w):
        base = tuple(rows[r][c * 4:(c + 1) * 4])
        if c == 0:
            px = FRAME_LIGHT
        elif c == 1:
            px = FRAME_DARK
        else:
            px = base
        row.append(px)
    out.append(row)

write_png('src/main/resources/assets/jdte/textures/block/large_greenhouse_glass_edge.png', w, h, out)
print('ok')