#!/usr/bin/env python3
"""Gulim.ttf → Gulim-subset.ttf (성적서 PDF용 한글·ASCII 서브셋).

사용: python scripts/subset_gulim_font.py
필요: pip install fonttools
"""
from pathlib import Path

FONT_DIR = Path(__file__).resolve().parent.parent / "src/main/webapp/report/common/css/font"
SRC = FONT_DIR / "Gulim.ttf"
DST = FONT_DIR / "Gulim-subset.ttf"

# 성적서에 쓰이는 ASCII·한글·기호 (≤ ≥ · 등)
UNICODES = (
    "U+0020-007E,"  # ASCII
    "U+00A0-00FF,"  # Latin-1
    "U+2010-2015,"  # dash
    "U+2026,"       # …
    "U+2264,U+2265,"  # ≤ ≥
    "U+3000-303F,"  # CJK 부호
    "U+3131-318E,"  # 한글 자모
    "U+AC00-D7A3,"  # 한글 음절
    "U+FF01-FF5E"   # 전각 ASCII
)


def main() -> None:
    try:
        from fontTools.subset import main as pyftsubset_main
    except ImportError as e:
        raise SystemExit("fonttools 필요: pip install fonttools") from e

    if not SRC.is_file():
        raise SystemExit(f"원본 없음: {SRC}")

    args = [
        str(SRC),
        f"--output-file={DST}",
        f"--unicodes={UNICODES}",
        "--layout-features=*",
        "--glyph-names",
        "--symbol-cmap",
        "--legacy-cmap",
        "--notdef-glyph",
        "--notdef-outline",
        "--recommended-glyphs",
    ]
    pyftsubset_main(args)
    print(f"OK {SRC.name} ({SRC.stat().st_size // 1024} KB) -> {DST.name} ({DST.stat().st_size // 1024} KB)")


if __name__ == "__main__":
    main()
