#!/usr/bin/env python3
"""Compare two PDF files: structure, images, fonts."""
import os
import re
import sys
from pathlib import Path


def analyze_pdf(path: Path) -> dict:
    data = path.read_bytes()
    images = []
    for m in re.finditer(br"/Subtype\s*/Image", data):
        chunk = data[m.start() : m.start() + 2000]
        w = re.search(br"/Width\s+(\d+)", chunk)
        h = re.search(br"/Height\s+(\d+)", chunk)
        filt = re.search(br"/Filter\s*/(\w+)", chunk)
        cs = re.search(br"/ColorSpace\s*/(\w+)", chunk)
        bpc = re.search(br"/BitsPerComponent\s+(\d+)", chunk)
        length = re.search(br"/Length\s+(\d+)", chunk)
        ww = int(w.group(1)) if w else 0
        hh = int(h.group(1)) if h else 0
        bpp = int(bpc.group(1)) if bpc else 8
        stream_len = int(length.group(1)) if length else 0
        images.append(
            {
                "w": ww,
                "h": hh,
                "filter": filt.group(1).decode() if filt else "?",
                "colorspace": cs.group(1).decode() if cs else "?",
                "bpc": bpp,
                "stream_kb": stream_len // 1024,
                "raw_est_kb": (ww * hh * bpp) // 8 // 1024,
            }
        )

    # stream sizes after 'stream\n'
    stream_sizes = []
    for m in re.finditer(br"stream\r?\n", data):
        end = data.find(b"endstream", m.end())
        if end > 0:
            stream_sizes.append(end - m.end())

    stream_sizes.sort(reverse=True)
    top_streams = stream_sizes[:15]

    return {
        "path": path,
        "size": len(data),
        "objects": len(re.findall(br"\d+ \d+ obj", data)),
        "font_refs": len(re.findall(br"/Type\s*/Font", data)),
        "dct": len(re.findall(br"/Filter\s*/DCTDecode", data)),
        "flate": len(re.findall(br"/Filter\s*/FlateDecode", data)),
        "images": images,
        "top_stream_kb": [s // 1024 for s in top_streams],
        "total_stream_mb": sum(stream_sizes) / 1024 / 1024,
    }


def print_report(label: str, a: dict) -> None:
    p = a["path"]
    print(f"\n{'='*60}")
    print(f"{label}: {p.name}")
    print(f"File size: {a['size']/1024/1024:.2f} MB ({a['size']:,} bytes)")
    print(f"PDF objects: {a['objects']}")
    print(f"Font refs: {a['font_refs']}  |  JPEG(DCT): {a['dct']}  |  Flate: {a['flate']}")
    print(f"Image XObjects: {len(a['images'])}")
    print(f"Sum of all stream payloads: {a['total_stream_mb']:.2f} MB")
    if a["top_stream_kb"]:
        print(f"Top 10 stream sizes (KB): {a['top_stream_kb'][:10]}")
    if a["images"]:
        print("Images (first 12):")
        for i, img in enumerate(a["images"][:12], 1):
            print(
                f"  {i:2d}. {img['w']}x{img['h']} {img['filter']} "
                f"cs={img['colorspace']} stream~{img['stream_kb']}KB "
                f"raw~{img['raw_est_kb']}KB"
            )
        if len(a["images"]) > 12:
            print(f"  ... +{len(a['images'])-12} more")
        big = sorted(a["images"], key=lambda x: x["stream_kb"], reverse=True)[:5]
        print("Largest images by /Length:")
        for img in big:
            print(f"    {img['w']}x{img['h']} {img['filter']} ~{img['stream_kb']} KB")


def find_files(dl: Path, test_ids: list[str]) -> list[Path]:
    out = []
    for tid in test_ids:
        matches = [f for f in dl.glob("SB26*.pdf") if tid in f.name]
        if matches:
            out.append(max(matches, key=lambda f: f.stat().st_size))
    return out


def main() -> None:
    dl = Path(os.environ["USERPROFILE"]) / "Downloads"
    args = sys.argv[1:]
    if len(args) >= 2:
        files = [Path(a) for a in args]
    else:
        files = find_files(dl, ["G2650", "G1184"])
        if len(files) < 2:
            all_pdf = sorted(dl.glob("SB26*.pdf"), key=lambda f: f.stat().st_size)
            if len(all_pdf) >= 2:
                files = [all_pdf[0], all_pdf[-1]]
    for i, f in enumerate(files):
        if not f.is_file():
            print(f"NOT FOUND: {f}")
            sys.exit(1)
        a = analyze_pdf(f)
        print_report("SMALL/OLD" if i == 0 else "LARGE/NEW", a)

    if len(files) == 2:
        a0, a1 = analyze_pdf(files[0]), analyze_pdf(files[1])
        ratio = a1["size"] / max(a0["size"], 1)
        print(f"\n{'='*60}")
        print(f"Size ratio: {ratio:.1f}x ({a1['size']/1024/1024:.2f} / {a0['size']/1024/1024:.2f} MB)")
        print(f"Image count: {len(a0['images'])} -> {len(a1['images'])}")
        print(f"JPEG count: {a0['dct']} -> {a1['dct']}")
        print(f"Stream payload: {a0['total_stream_mb']:.2f} -> {a1['total_stream_mb']:.2f} MB")


if __name__ == "__main__":
    main()
