#!/usr/bin/env python3
"""
Finds licence-checked photo CANDIDATES on Wikimedia Commons for every entry in catalog.py.
It downloads nothing but metadata: it writes tools/photos/candidates.json and candidates.html (a contact
sheet that shows Commons thumbnails, for picking). Approved picks go into photos.json; process_photos.py
then downloads and processes them.

  python tools/photos/find_photos.py                 all entries
  python tools/photos/find_photos.py biz_ prop_      only image names starting with these prefixes
  python tools/photos/find_photos.py --html-only biz_  rebuild the contact sheet without searching

Licence filter: Public Domain, CC0, CC BY, CC BY-SA only (from each file's own metadata).
Files tagged "trademarked" are skipped for items; files tagged "personality" are flagged for people.
"""
import html
import json
import os
import re
import sys
import time
import urllib.parse
import urllib.request

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import catalog  # noqa: E402

COMMONS = "https://commons.wikimedia.org/w/api.php"
WIKIPEDIA = "https://en.wikipedia.org/w/api.php"
USER_AGENT = "BusinessEmpirePhotoTool/1.0 (offline game asset script)"
OUT_JSON = os.path.join(catalog.HERE, "candidates.json")
OUT_HTML = os.path.join(catalog.HERE, "candidates.html")
OK_LICENCE = re.compile(r"^(public domain|pd\b|cc0|cc[ -]by(-sa)?( \d\.\d)?)", re.I)


def api(url, params):
    params = dict(params, format="json", formatversion="2")
    req = urllib.request.Request(url + "?" + urllib.parse.urlencode(params), headers={"User-Agent": USER_AGENT})
    for attempt in range(3):
        try:
            with urllib.request.urlopen(req, timeout=30) as resp:
                return json.load(resp)
        except Exception:  # noqa: BLE001 - retry politely, then give up on this query
            time.sleep(2 + attempt * 3)
    return {}


def clean(text):
    text = re.sub(r"<[^>]+>", "", text or "")
    return html.unescape(re.sub(r"\s+", " ", text)).strip()


def describe(page):
    info = (page.get("imageinfo") or [{}])[0]
    meta = info.get("extmetadata", {})
    licence = clean(meta.get("LicenseShortName", {}).get("value", ""))
    restrictions = clean(meta.get("Restrictions", {}).get("value", ""))
    return dict(
        file=page.get("title", ""),
        sourceUrl=info.get("descriptionurl", ""),
        fileUrl=info.get("url", ""),
        thumb=info.get("thumburl", ""),
        width=info.get("width", 0),
        height=info.get("height", 0),
        mime=info.get("mime", ""),
        licence=licence,
        licenceUrl=clean(meta.get("LicenseUrl", {}).get("value", "")),
        author=clean(meta.get("Artist", {}).get("value", ""))[:120] or "Unknown",
        title=clean(meta.get("ObjectName", {}).get("value", "")) or page.get("title", "").replace("File:", ""),
        restrictions=restrictions,
    )


IMAGEINFO = dict(prop="imageinfo", iiprop="url|size|mime|extmetadata", iiurlwidth="360")


def search_items(query, limit=6):
    """Prefer Commons "Quality images" (reviewed, well composed); fall back to a plain search."""
    out = _search(query + " incategory:Quality_images", limit)
    if len(out) < limit:
        seen = {c["file"] for c in out}
        out += [c for c in _search(query, limit) if c["file"] not in seen][: limit - len(out)]
    return out


def _search(query, limit):
    data = api(COMMONS, dict(action="query", generator="search", gsrnamespace="6",
                             gsrsearch=query + " filetype:bitmap", gsrlimit=str(limit * 4), **IMAGEINFO))
    out = []
    for page in sorted((data.get("query") or {}).get("pages", []), key=lambda p: p.get("index", 0)):
        c = describe(page)
        if c["mime"] not in ("image/jpeg", "image/png", "image/webp"):
            continue
        if not OK_LICENCE.match(c["licence"]) or "trademark" in c["restrictions"].lower():
            continue
        if c["width"] < 1000 or c["width"] < c["height"] * 0.9:  # want big, landscape-ish photos
            continue
        out.append(c)
        if len(out) >= limit:
            break
    return out


def person_photo(name):
    """The Wikipedia lead image, if it lives on Commons with an allowed licence."""
    data = api(WIKIPEDIA, dict(action="query", titles=name, prop="pageimages", piprop="name", redirects="1"))
    pages = (data.get("query") or {}).get("pages", [])
    if not pages or "pageimage" not in pages[0]:
        return []
    file = "File:" + pages[0]["pageimage"]
    info = api(COMMONS, dict(action="query", titles=file, **IMAGEINFO))
    cpages = (info.get("query") or {}).get("pages", [])
    if not cpages or cpages[0].get("missing"):
        return []  # e.g. a local non-free file on Wikipedia: not allowed, use initials
    c = describe(cpages[0])
    if not OK_LICENCE.match(c["licence"]):
        return []
    c["personalityRights"] = "personality" in c["restrictions"].lower()
    return [c]


def main():
    html_only = "--html-only" in sys.argv  # rebuild the contact sheet from candidates.json, no searching
    prefixes = tuple(a for a in sys.argv[1:] if not a.startswith("--"))
    entries = [e for e in catalog.all_entries() if not prefixes or e["imageName"].startswith(prefixes)]
    result = {}
    if os.path.exists(OUT_JSON):
        with open(OUT_JSON, encoding="utf-8") as fh:
            result = json.load(fh)
    for i, e in enumerate([] if html_only else entries):
        cands = person_photo(e["person"]) if e["kind"] == "person" else search_items(e["query"])
        result[e["imageName"]] = dict(shows=e["shows"], kind=e["kind"], candidates=cands)
        print("%3d/%d %-36s %d candidate(s)" % (i + 1, len(entries), e["imageName"], len(cands)))
        time.sleep(3)
    with open(OUT_JSON, "w", encoding="utf-8", newline="\n") as fh:
        json.dump(result, fh, indent=1, ensure_ascii=False)
    rows = []
    for name, r in result.items():
        if prefixes and not name.startswith(prefixes):
            continue
        cells = "".join(
            '<figure><img src="%s" loading="lazy"><figcaption>%d. %s %s</figcaption></figure>' % (
                html.escape(c["thumb"]), k + 1, html.escape(c["licence"]), html.escape(c["author"][:40]))
            for k, c in enumerate(r["candidates"][:3]))
        rows.append('<section><h3>%s</h3><p>%s</p><br>%s</section>' % (html.escape(name), html.escape(r["shows"]), cells or "<i>none</i>"))
    with open(OUT_HTML, "w", encoding="utf-8", newline="\n") as fh:
        fh.write("<!doctype html><meta charset='utf-8'><style>body{font:8px sans-serif;margin:1px}"
                 "section{border-top:1px solid #bbb;padding:2px 0}h3{margin:0;font-size:11px;display:inline}"
                 "p{display:inline;margin-left:6px}figure{display:inline-block;margin:1px;width:106px;vertical-align:top}"
                 "img{width:106px;height:80px;object-fit:cover}figcaption{height:12px;overflow:hidden}</style>"
                 + "".join(rows))
    print("Wrote", OUT_JSON, "and", OUT_HTML)


if __name__ == "__main__":
    main()
