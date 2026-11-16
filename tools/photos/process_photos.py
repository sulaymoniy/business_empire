#!/usr/bin/env python3
"""
Business Empire photo pipeline.

  python tools/photos/process_photos.py            download + process every APPROVED photo in photos.json
  python tools/photos/process_photos.py --list     only rewrite ART_LIST.md (no downloads)

photos.json holds one entry per approved photo (filled in after the user OKs each batch):
  {"imageName": "lux_day_cruiser", "approved": true,
   "fileUrl": "<direct image URL>", "sourceUrl": "<file page on Commons/Pexels/Unsplash/Pixabay>",
   "title": "...", "author": "...", "licence": "CC BY-SA 4.0", "licenceUrl": "...",
   "crop": [left, top, right, bottom]   optional, fractions 0..1 of the original, to frame the subject}

For each approved entry it:
  1. downloads the original into tools/photos/originals/ (git-ignored),
  2. crops to 4:3 (cards) or 1:1 (people), centred unless "crop" is given,
  3. resizes to 720 px wide (cards) or 400 x 400 (people),
  4. saves WebP quality 80 to app/src/main/res/drawable-nodpi/<imageName>.webp,
  5. rewrites app/src/main/assets/image_credits.json and ART_LIST.md.

Only crop and resize: no filters or other edits. CC BY-SA photos stay CC BY-SA after cropping (noted in credits).
Needs Pillow:  python -m pip install --user pillow
"""
import json
import os
import sys
import time
import urllib.parse
import urllib.request

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import catalog  # noqa: E402

HERE = catalog.HERE
ROOT = catalog.ROOT
PHOTOS_JSON = os.path.join(HERE, "photos.json")
ORIGINALS = os.path.join(HERE, "originals")
OUT_DIR = os.path.join(ROOT, "app", "src", "main", "res", "drawable-nodpi")
CREDITS = os.path.join(ROOT, "app", "src", "main", "assets", "image_credits.json")
ART_LIST = os.path.join(ROOT, "ART_LIST.md")
USER_AGENT = "BusinessEmpirePhotoTool/1.0 (offline game asset script)"
ALLOWED = ("public domain", "pd", "cc0", "cc by", "cc-by", "pexels", "unsplash", "pixabay")


def load_photos():
    if not os.path.exists(PHOTOS_JSON):
        return []
    with open(PHOTOS_JSON, encoding="utf-8") as fh:
        return json.load(fh).get("photos", [])


def licence_ok(licence):
    low = licence.lower()
    return any(a in low for a in ALLOWED) and "nc" not in low.replace("cc by", "").split() and "-nd" not in low and " nd" not in low


def process(entry, kinds):
    from PIL import Image  # imported here so --list works without Pillow

    name = entry["imageName"]
    kind = kinds.get(name)
    if kind is None:
        raise SystemExit("Unknown imageName %s (not in catalog.py)" % name)
    if not licence_ok(entry["licence"]):
        raise SystemExit("Licence not allowed for %s: %s" % (name, entry["licence"]))
    if kind == "person" and entry.get("personalityRights"):
        # Commons warns that this person's likeness may not be used commercially without their consent.
        raise SystemExit("Skipping %s: personality-rights warning on Commons" % name)
    os.makedirs(ORIGINALS, exist_ok=True)
    os.makedirs(OUT_DIR, exist_ok=True)
    ext = os.path.splitext(entry["fileUrl"].split("?")[0])[1].lower() or ".jpg"
    original = os.path.join(ORIGINALS, name + ext)
    if not os.path.exists(original):
        url = entry["fileUrl"]
        if "upload.wikimedia.org" in url:
            # Ask Commons for a scaled copy: plenty for 720 px output and far smaller than the original.
            fname = urllib.parse.unquote(url.split("?")[0].rsplit("/", 1)[1])
            width = 800 if kind == "person" else 1600
            url = "https://commons.wikimedia.org/wiki/Special:FilePath/%s?width=%d" % (urllib.parse.quote(fname), width)
        req = urllib.request.Request(url, headers={"User-Agent": USER_AGENT})
        time.sleep(1)
        with urllib.request.urlopen(req, timeout=60) as resp, open(original, "wb") as fh:
            fh.write(resp.read())
    img = Image.open(original).convert("RGB")
    w, h = img.size
    if entry.get("crop"):
        l, t, r, b = entry["crop"]
        img = img.crop((int(l * w), int(t * h), int(r * w), int(b * h)))
        w, h = img.size
    target = 1.0 if kind == "person" else 4 / 3
    if w / h > target:  # too wide: trim the sides
        nw = int(h * target)
        img = img.crop(((w - nw) // 2, 0, (w - nw) // 2 + nw, h))
    else:  # too tall: trim top/bottom, keep a bit more of the top (faces, skylines)
        nh = int(w / target)
        top = int((h - nh) * (0.35 if kind == "person" else 0.5))
        img = img.crop((0, top, w, top + nh))
    size = (400, 400) if kind == "person" else (720, 540)
    img = img.resize(size, Image.LANCZOS)
    out = os.path.join(OUT_DIR, name + ".webp")
    img.save(out, "WEBP", quality=80, method=6)
    return os.path.getsize(out)


def write_credits(photos):
    credits = []
    for e in photos:
        if not e.get("approved") or not os.path.exists(os.path.join(OUT_DIR, e["imageName"] + ".webp")):
            continue
        modified = "cropped, resized, converted to WebP"
        if "sa" in e["licence"].lower().replace("cc by-sa", "sa").split():
            modified += "; this version is also shared under " + e["licence"]
        credits.append(dict(imageName=e["imageName"], title=e["title"], author=e["author"], sourceUrl=e["sourceUrl"],
                            licence=e["licence"], licenceUrl=e.get("licenceUrl", ""), modified=modified))
    with open(CREDITS, "w", encoding="utf-8", newline="\n") as fh:
        json.dump(dict(note="One entry per bundled photo. Written by tools/photos/process_photos.py. "
                            "Shown on Settings > About > Photo credits.", photos=credits), fh, indent=2, ensure_ascii=False)
        fh.write("\n")
    return len(credits)


def write_art_list():
    entries = catalog.all_entries()
    present = {os.path.splitext(f)[0] for f in os.listdir(OUT_DIR)} if os.path.isdir(OUT_DIR) else set()
    lines = [ART_LIST_HEADER % (len(entries), sum(1 for e in entries if e["imageName"] in present))]
    group = None
    for e in entries:
        if e["group"] != group:
            group = e["group"]
            lines += ["", "### " + group, "", "| Image name | Size | Should show | Status |", "|---|---|---|---|"]
        size = "400 x 400 (1:1)" if e["kind"] == "person" else "720 x 540 (4:3)"
        status = "added" if e["imageName"] in present else "placeholder"
        lines.append("| `%s` | %s | %s | %s |" % (e["imageName"], size, e["shows"], status))
    with open(ART_LIST, "w", encoding="utf-8", newline="\n") as fh:
        fh.write("\n".join(lines) + "\n")


ART_LIST_HEADER = """# Art list

Every picture the game can show: %d image names, %d of them have a photo so far. Anything without a photo
shows a coloured placeholder card with an icon, so the game always builds and runs.

## Rules for photos
- Sources: Pexels, Unsplash, Pixabay, or Wikimedia Commons files marked Public Domain, CC0, CC BY or CC BY-SA.
  Check the licence on each file page. Never Google Images, news sites, social media, agencies or makers' sites.
- No visible brand logos, badges, licence plates or recognisable faces on item photos.
- Rich List people: Wikimedia Commons only, neutral and respectful, crop/resize only, used only inside the Rich
  List screens (never the icon, splash, store screenshots, promo graphics or notifications). No licensed photo:
  the initials avatar is used.
- Every photo must have an entry in `app/src/main/assets/image_credits.json` (shown on Settings > About > Photo
  credits).

## Adding or replacing a photo
- Easiest: add an approved entry to `tools/photos/photos.json` and run `python tools/photos/process_photos.py`.
  It downloads, crops, resizes, saves WebP and updates the credits and this list.
- By hand: save a WebP/PNG named exactly like the image name (e.g. `lux_day_cruiser.webp`) at the size below into
  `app/src/main/res/drawable-nodpi/`, and add its credit to `image_credits.json`. No code changes needed.
- Cards are 4:3 at 720 px wide; people are square at 400 px. Keep the total under 15 MB.
"""


def main():
    if "--list" not in sys.argv:
        kinds = {e["imageName"]: e["kind"] for e in catalog.all_entries()}
        photos = load_photos()
        total = 0
        done = 0
        failed = []
        for e in photos:
            if e.get("approved"):
                try:
                    total += process(e, kinds)
                    done += 1
                except Exception as ex:  # noqa: BLE001 - report and keep going
                    failed.append((e["imageName"], str(ex)[:80]))
        for name, err in failed:
            print("FAILED", name, err)
        n = write_credits(photos)
        print("Processed %d photos, %d credits, %.1f MB of WebP this run" % (done, n, total / 1e6))
    write_art_list()
    print("ART_LIST.md updated")


if __name__ == "__main__":
    main()
