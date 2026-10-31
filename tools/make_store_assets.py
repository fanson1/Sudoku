#!/usr/bin/env python3
"""Generate Google Play store assets for the Sudoku app.

Outputs into play-store-assets/:
  icon-512.png
  feature-graphic-en.png
  feature-graphic-zh.png
  phone-1..6.png  (1080x2400 concept mockups)

Requires: pillow
"""

from __future__ import annotations

import os
from PIL import Image, ImageDraw, ImageFont

OUT_DIR = os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))), "play-store-assets")

PRIMARY = (79, 70, 229)        # #4F46E5
PRIMARY_DARK = (55, 48, 190)
SECONDARY = (14, 116, 144)     # #0E7490
BG_LIGHT = (248, 250, 252)     # #F8FAFC
BG_DARK = (15, 23, 42)         # #0F172A
SURFACE = (255, 255, 255)
TEXT_MUTED = (100, 116, 139)
HINT_GOLD = (245, 158, 11)
GREEN = (34, 197, 94)

FONT_PATHS = [
    "/System/Library/Fonts/PingFang.ttc",
    "/System/Library/Fonts/Helvetica.ttc",
    "/System/Library/Fonts/Supplemental/Arial.ttf",
]


def font(size: int, index: int = 0) -> ImageFont.FreeTypeFont:
    for path in FONT_PATHS:
        if os.path.exists(path):
            try:
                return ImageFont.truetype(path, size, index=index)
            except OSError:
                continue
    return ImageFont.load_default()


def vertical_gradient(size, top, bottom):
    w, h = size
    grad = Image.new("RGB", (1, h))
    for y in range(h):
        t = y / max(h - 1, 1)
        grad.putpixel((0, y), tuple(int(top[i] + (bottom[i] - top[i]) * t) for i in range(3)))
    return grad.resize((w, h))


def rounded_mask(size, radius):
    mask = Image.new("L", size, 0)
    ImageDraw.Draw(mask).rounded_rectangle([0, 0, size[0] - 1, size[1] - 1], radius=radius, fill=255)
    return mask


def make_icon(path, size=512):
    img = vertical_gradient((size, size), PRIMARY, PRIMARY_DARK).convert("RGBA")
    img.putalpha(rounded_mask((size, size), int(size * 0.28)))

    draw = ImageDraw.Draw(img)

    # Inner translucent tile
    inner = int(size * 0.58)
    off = (size - inner) // 2
    tile = Image.new("RGBA", (inner, inner), (255, 255, 255, 40))
    tile.putalpha(rounded_mask((inner, inner), int(inner * 0.30)).point(lambda v: int(v * 0.16)))
    img.alpha_composite(tile, (off, off))

    # "9"
    f = font(int(size * 0.42))
    text = "9"
    box = draw.textbbox((0, 0), text, font=f)
    tw, th = box[2] - box[0], box[3] - box[1]
    draw.text(
        ((size - tw) / 2 - box[0], (size - th) / 2 - box[1]),
        text,
        font=f,
        fill=(255, 255, 255, 255),
    )
    img.save(path)


def make_feature_graphic(path, title, tagline, accent_digits, pill="100% OFFLINE · NO ADS"):
    w, h = 1024, 500
    base = vertical_gradient((w, h), PRIMARY, SECONDARY)  # RGB: alphas blend properly

    # Faint sudoku grid on the right, drawn on an overlay then blended
    overlay = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    od = ImageDraw.Draw(overlay)
    gx, gy, cell = 690, 90, 36
    for i in range(10):
        lw = 3 if i % 3 == 0 else 1
        od.line([(gx + i * cell, gy), (gx + i * cell, gy + 9 * cell)], fill=(255, 255, 255, 70), width=lw)
        od.line([(gx, gy + i * cell), (gx + 9 * cell, gy + i * cell)], fill=(255, 255, 255, 70), width=lw)
    f_digit = font(20)
    for (r, c, d) in accent_digits:
        bb = od.textbbox((0, 0), str(d), font=f_digit)
        od.text(
            (gx + c * cell + (cell - (bb[2] - bb[0])) / 2,
             gy + r * cell + (cell - (bb[3] - bb[1])) / 2 - bb[1]),
            str(d), font=f_digit, fill=(255, 255, 255, 225),
        )
    img = Image.alpha_composite(base.convert("RGBA"), overlay)
    draw = ImageDraw.Draw(img)

    # Left copy
    draw.text((72, 150), title, font=font(64), fill=(255, 255, 255, 255))
    draw.text((72, 240), tagline, font=font(30), fill=(255, 255, 255, 220))

    pf = font(22)
    pb = draw.textbbox((0, 0), pill, font=pf)
    pw, ph = pb[2] - pb[0], pb[3] - pb[1]
    pill_layer = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    pd = ImageDraw.Draw(pill_layer)
    pd.rounded_rectangle([72, 316, 72 + pw + 44, 316 + ph + 30], radius=22, fill=(255, 255, 255, 55))
    pd.text((94, 328), pill, font=pf, fill=(255, 255, 255, 255))
    img = Image.alpha_composite(img, pill_layer)

    img.convert("RGB").save(path)  # no alpha channel allowed


def draw_board(draw, x, y, size, values, given):
    cell = size / 9
    draw.rounded_rectangle([x - 6, y - 6, x + size + 6, y + size + 6], radius=18, fill=SURFACE)
    for i in range(10):
        lw = 3 if i % 3 == 0 else 1
        col = (15, 23, 42) if i % 3 == 0 else (203, 213, 225)
        draw.line([(x + i * cell, y), (x + i * cell, y + size)], fill=col, width=lw)
        draw.line([(x, y + i * cell), (x + size, y + i * cell)], fill=col, width=lw)
    f = font(int(cell * 0.55))
    for r in range(9):
        for c in range(9):
            v = values[r][c]
            if v == 0:
                continue
            color = (15, 23, 42) if given[r][c] else PRIMARY
            bb = draw.textbbox((0, 0), str(v), font=f)
            draw.text(
                (x + c * cell + (cell - (bb[2] - bb[0])) / 2,
                 y + r * cell + (cell - (bb[3] - bb[1])) / 2 - bb[1]),
                str(v), font=f, fill=color,
            )


def draw_keypad(draw, x, y, width, notes_active=False):
    gap = 8
    btn = (width - 2 * gap) / 3
    for r in range(3):
        for c in range(3):
            n = r * 3 + c + 1
            bx = x + c * (btn + gap)
            by = y + r * (btn * 0.95 + gap)
            draw.rounded_rectangle([bx, by, bx + btn, by + btn * 0.95], radius=16, fill=(241, 245, 249))
            f = font(int(btn * 0.34))
            bb = draw.textbbox((0, 0), str(n), font=f)
            draw.text(
                (bx + (btn - (bb[2] - bb[0])) / 2, by + (btn * 0.95 - (bb[3] - bb[1])) / 2 - bb[1]),
                str(n), font=f, fill=PRIMARY,
            )
    return y + 3 * (btn * 0.95 + gap)


PUZZLE = [
    [5, 3, 0, 0, 7, 0, 0, 0, 0],
    [6, 0, 0, 1, 9, 5, 0, 0, 0],
    [0, 9, 8, 0, 0, 0, 0, 6, 0],
    [8, 0, 0, 0, 6, 0, 0, 0, 3],
    [4, 0, 0, 8, 0, 3, 0, 0, 1],
    [7, 0, 0, 0, 2, 0, 0, 0, 6],
    [0, 6, 0, 0, 0, 0, 2, 8, 0],
    [0, 0, 0, 4, 1, 9, 0, 0, 5],
    [0, 0, 0, 0, 8, 0, 0, 7, 9],
]
GIVEN = [[v != 0 for v in row] for row in PUZZLE]


def phone_canvas(title, subtitle):
    img = Image.new("RGB", (1080, 2400), BG_LIGHT)
    draw = ImageDraw.Draw(img)
    draw.text((72, 96), title, font=font(58), fill=(15, 23, 42))
    draw.text((72, 176), subtitle, font=font(34), fill=TEXT_MUTED)
    return img, draw


def make_phone_1(path):
    img, d = phone_canvas("Choose a level", "50 levels · 5 difficulties")
    # progress card
    d.rounded_rectangle([72, 270, 1008, 420], radius=24, fill=SURFACE)
    d.text((104, 296), "Level progress", font=font(34), fill=(15, 23, 42))
    d.text((104, 344), "Unlocked 13 / 50", font=font(28), fill=TEXT_MUTED)
    d.rounded_rectangle([104, 388, 976, 404], radius=8, fill=(226, 232, 240))
    d.rounded_rectangle([104, 388, 330, 404], radius=8, fill=PRIMARY)

    # pro banner
    d.rounded_rectangle([72, 450, 1008, 590], radius=24, fill=PRIMARY)
    d.text((112, 484), "Unlock Pro", font=font(36), fill=(255, 255, 255))
    d.text((112, 532), "One purchase. Yours forever.", font=font(26), fill=(224, 231, 255))

    # challenge cards
    d.rounded_rectangle([72, 620, 528, 800], radius=22, fill=(224, 231, 255))
    d.text((104, 656), "Daily challenge", font=font(30), fill=(46, 42, 153))
    d.text((104, 706), "One puzzle every day", font=font(24), fill=(71, 85, 105))
    d.rounded_rectangle([552, 620, 1008, 800], radius=22, fill=(224, 231, 255))
    d.text((584, 656), "Timed challenge", font=font(30), fill=(46, 42, 153))
    d.text((584, 706), "10 minutes", font=font(24), fill=(71, 85, 105))

    # level grid
    start_y = 840
    for row in range(4):
        for col in range(4):
            x = 72 + col * 238
            y = start_y + row * 238
            n = row * 4 + col + 1
            fill = PRIMARY if n == 13 else SURFACE
            d.rounded_rectangle([x, y, x + 214, y + 214], radius=22, fill=fill)
            f = font(64)
            bb = d.textbbox((0, 0), str(n), font=f)
            color = (255, 255, 255) if n == 13 else (15, 23, 42)
            d.text((x + (214 - (bb[2] - bb[0])) / 2, y + (214 - (bb[3] - bb[1])) / 2 - bb[1]), str(n), font=f, fill=color)
    img.save(path)


def make_phone_2(path):
    img, d = phone_canvas("Level 7", "Medium")
    draw_board(d, 90, 300, 900, PUZZLE, GIVEN)
    y = draw_keypad(d, 90, 1330, 900)
    d.rounded_rectangle([90, y + 30, 440, y + 130], radius=18, fill=(226, 232, 240))
    d.text((150, y + 58), "Undo", font=font(34), fill=(71, 85, 105))
    d.rounded_rectangle([460, y + 30, 810, y + 130], radius=18, fill=(226, 232, 240))
    d.text((520, y + 58), "Redo", font=font(34), fill=(71, 85, 105))
    d.rounded_rectangle([830, y + 30, 990, y + 130], radius=18, fill=HINT_GOLD)
    d.text((866, y + 58), "3", font=font(34), fill=(255, 255, 255))
    img.save(path)


def make_phone_3(path):
    img, d = phone_canvas("Notes mode", "Pencil marks, auto-cleaned")
    # board with candidates
    x, y, size = 90, 300, 900
    cell = size / 9
    d.rounded_rectangle([x - 6, y - 6, x + size + 6, y + size + 6], radius=18, fill=SURFACE)
    for i in range(10):
        lw = 3 if i % 3 == 0 else 1
        col = (15, 23, 42) if i % 3 == 0 else (203, 213, 225)
        d.line([(x + i * cell, y), (x + i * cell, y + size)], fill=col, width=lw)
        d.line([(x, y + i * cell), (x + size, y + i * cell)], fill=col, width=lw)
    f_big = font(int(cell * 0.55))
    f_small = font(int(cell * 0.2))
    import random
    random.seed(7)
    for r in range(9):
        for c in range(9):
            v = PUZZLE[r][c]
            if v:
                bb = d.textbbox((0, 0), str(v), font=f_big)
                d.text((x + c * cell + (cell - (bb[2] - bb[0])) / 2,
                        y + r * cell + (cell - (bb[3] - bb[1])) / 2 - bb[1]),
                       str(v), font=f_big, fill=(15, 23, 42))
            else:
                cands = sorted(random.sample(range(1, 10), random.randint(2, 5)))
                for cand in cands:
                    idx = cand - 1
                    cr, cc = divmod(idx, 3)
                    tx = x + c * cell + cc * cell / 3 + cell / 12
                    ty = y + r * cell + cr * cell / 3 + cell / 16
                    d.text((tx, ty), str(cand), font=f_small, fill=TEXT_MUTED)
    y2 = draw_keypad(d, 90, 1330, 900)
    d.rounded_rectangle([90, y2 + 30, 990, y2 + 130], radius=18, fill=PRIMARY)
    d.text((470, y2 + 58), "Notes ON", font=font(34), fill=(255, 255, 255))
    img.save(path)


def make_phone_4(path):
    img = Image.new("RGB", (1080, 2400), BG_DARK)
    d = ImageDraw.Draw(img)
    # confetti
    import random
    random.seed(3)
    palette = [PRIMARY, SECONDARY, HINT_GOLD, GREEN, (236, 72, 153)]
    for _ in range(220):
        cx, cy = random.randint(0, 1080), random.randint(0, 1900)
        s = random.randint(8, 20)
        d.rectangle([cx, cy, cx + s, cy + s], fill=random.choice(palette))

    d.rounded_rectangle([90, 420, 990, 1880], radius=36, fill=SURFACE)
    d.ellipse([450, 500, 630, 680], fill=(224, 231, 255))
    d.text((492, 540), "★", font=font(80), fill=PRIMARY)
    d.text((250, 730), "Puzzle solved!", font=font(62), fill=(15, 23, 42))
    d.text((330, 820), "Level 7 completed", font=font(34), fill=TEXT_MUTED)

    rows = [("Level", "7"), ("Time", "04:12"), ("Mistakes", "1"), ("Best combo", "x6")]
    y = 910
    for label, value in rows:
        d.text((170, y), label, font=font(34), fill=TEXT_MUTED)
        bb = d.textbbox((0, 0), value, font=font(34))
        d.text((910 - (bb[2] - bb[0]), y), value, font=font(34), fill=(15, 23, 42))
        y += 78
    d.line([(170, y + 10), (910, y + 10)], fill=(226, 232, 240), width=2)
    d.text((170, y + 46), "Score", font=font(42), fill=(15, 23, 42))
    d.text((760, y + 36), "2480", font=font(56), fill=PRIMARY)

    d.rounded_rectangle([170, y + 180, 910, y + 300], radius=28, fill=PRIMARY)
    d.text((430, y + 216), "Next level", font=font(38), fill=(255, 255, 255))
    img.save(path)


def make_phone_5(path):
    img, d = phone_canvas("Settings", "8 languages · 3 themes")
    y = 280
    d.text((72, y), "LANGUAGE", font=font(28), fill=TEXT_MUTED)
    y += 60
    chips = ["Follow system", "English", "简体中文", "繁體中文"]
    x = 72
    for chip in chips:
        f = font(28)
        bb = d.textbbox((0, 0), chip, font=f)
        w = bb[2] - bb[0] + 56
        if x + w > 1010:
            x = 72
            y += 92
        selected = chip == "English"
        d.rounded_rectangle([x, y, x + w, y + 72], radius=36,
                            fill=PRIMARY if selected else (226, 232, 240))
        d.text((x + 28, y + 20), chip, font=f, fill=(255, 255, 255) if selected else (15, 23, 42))
        x += w + 16
    y += 150

    d.text((72, y), "THEME", font=font(28), fill=TEXT_MUTED)
    y += 60
    for name, sub, on in [("Follow system", "Switches automatically", False),
                          ("Light", "Bright and crisp", True),
                          ("Dark", "Easy on the eyes at night", False)]:
        d.rounded_rectangle([72, y, 1008, y + 132], radius=22,
                            fill=(224, 231, 255) if on else SURFACE)
        d.text((112, y + 30), name, font=font(34), fill=(15, 23, 42))
        d.text((112, y + 78), sub, font=font(26), fill=TEXT_MUTED)
        y += 148

    y += 20
    d.text((72, y), "PRO VERSION", font=font(28), fill=TEXT_MUTED)
    y += 60
    d.rounded_rectangle([72, y, 1008, y + 150], radius=22, fill=PRIMARY)
    d.text((112, y + 34), "Pro unlocked", font=font(36), fill=(255, 255, 255))
    d.text((112, y + 86), "Thank you! Every feature is unlocked.", font=font(26), fill=(224, 231, 255))
    img.save(path)


def make_phone_6(path):
    img = Image.new("RGB", (1080, 2400), BG_DARK)
    d = ImageDraw.Draw(img)
    d.rounded_rectangle([60, 300, 1020, 2100], radius=40, fill=SURFACE)
    d.ellipse([470, 360, 610, 500], fill=PRIMARY)
    d.text((516, 396), "★", font=font(64), fill=(255, 255, 255))
    d.text((300, 550), "Unlock Pro", font=font(64), fill=(15, 23, 42))
    d.text((180, 650), "One purchase, every feature, forever.", font=font(30), fill=TEXT_MUTED)

    features = [
        "Levels 11-50 with 5 difficulty tiers",
        "Daily challenge with a shared puzzle",
        "10-minute timed challenge",
        "Unlimited hints on every level",
        "Detailed statistics and per-level records",
        "All future updates included — no subscription",
    ]
    y = 760
    for text in features:
        d.ellipse([150, y + 6, 186, y + 42], fill=(224, 231, 255))
        d.text((158, y + 10), "✓", font=font(24), fill=PRIMARY)
        d.text((210, y), text, font=font(30), fill=(15, 23, 42))
        y += 82

    d.rounded_rectangle([140, y + 80, 940, y + 230], radius=32, fill=PRIMARY)
    d.text((330, y + 126), "Unlock Pro · $2.99", font=font(38), fill=(255, 255, 255))
    d.text((300, y + 260), "Restore purchase", font=font(30), fill=PRIMARY)
    d.text((180, y + 340), "One-time payment · No subscription · No ads",
           font=font(26), fill=TEXT_MUTED)
    img.save(path)


def main():
    os.makedirs(OUT_DIR, exist_ok=True)

    make_icon(os.path.join(OUT_DIR, "icon-512.png"))

    accents = [(0, 0, 5), (1, 4, 3), (2, 8, 7), (4, 0, 4), (4, 4, 9),
               (4, 8, 1), (6, 0, 8), (7, 4, 6), (8, 8, 2)]
    make_feature_graphic(
        os.path.join(OUT_DIR, "feature-graphic-en.png"),
        "Sudoku",
        "100% offline · No ads · One-time Pro",
        accents,
    )
    make_feature_graphic(
        os.path.join(OUT_DIR, "feature-graphic-zh.png"),
        "数独 Sudoku",
        "完全离线 · 无广告 · 一次买断",
        accents,
        pill="100% 离线 · 无广告",
    )

    make_phone_1(os.path.join(OUT_DIR, "phone-1.png"))
    make_phone_2(os.path.join(OUT_DIR, "phone-2.png"))
    make_phone_3(os.path.join(OUT_DIR, "phone-3.png"))
    make_phone_4(os.path.join(OUT_DIR, "phone-4.png"))
    make_phone_5(os.path.join(OUT_DIR, "phone-5.png"))
    make_phone_6(os.path.join(OUT_DIR, "phone-6.png"))

    for name in sorted(os.listdir(OUT_DIR)):
        path = os.path.join(OUT_DIR, name)
        with Image.open(path) as im:
            print(f"{name}: {im.size[0]}x{im.size[1]} {im.mode}")


if __name__ == "__main__":
    main()
