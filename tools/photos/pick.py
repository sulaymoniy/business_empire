#!/usr/bin/env python3
"""
Records picked candidates into photos.json as NOT approved (approved=false). Nothing is downloaded.

  python tools/photos/pick.py biz_food_truck_1=1 biz_bakery_chain_2=biz_bakery_chain_1:3 ...

<imageName>=<n> picks candidate n (1-based) found for that image name; <imageName>=<other>:<n> borrows
candidate n from another entry's search. After the user approves a batch, run
  python tools/photos/pick.py --approve biz_lemonade_stand_ biz_food_truck_ ...
to set approved=true for those prefixes, then process_photos.py downloads and processes them.
"""
import json
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import catalog  # noqa: E402

CANDIDATES = os.path.join(catalog.HERE, "candidates.json")
PHOTOS = os.path.join(catalog.HERE, "photos.json")


def load(path, default):
    if not os.path.exists(path):
        return default
    with open(path, encoding="utf-8") as fh:
        return json.load(fh)


def main():
    photos = load(PHOTOS, {"photos": []})
    by_name = {p["imageName"]: p for p in photos["photos"]}
    if sys.argv[1:2] == ["--approve"]:
        prefixes = tuple(sys.argv[2:])
        n = 0
        for p in photos["photos"]:
            if p["imageName"].startswith(prefixes):
                p["approved"] = True
                n += 1
        print("Approved", n)
    else:
        cands = load(CANDIDATES, {})
        for arg in sys.argv[1:]:
            name, choice = arg.split("=")
            src, _, idx = choice.rpartition(":") if ":" in choice else (name, "", choice)
            c = cands[src or name]["candidates"][int(idx) - 1]
            by_name[name] = dict(imageName=name, approved=False, fileUrl=c["fileUrl"], sourceUrl=c["sourceUrl"],
                                 title=c["title"], author=c["author"], licence=c["licence"], licenceUrl=c["licenceUrl"],
                                 personalityRights=c.get("personalityRights", False))
        photos["photos"] = sorted(by_name.values(), key=lambda p: p["imageName"])
    with open(PHOTOS, "w", encoding="utf-8", newline="\n") as fh:
        json.dump(photos, fh, indent=2, ensure_ascii=False)
        fh.write("\n")
    print("photos.json:", len(photos["photos"]), "entries,", sum(p["approved"] for p in photos["photos"]), "approved")


if __name__ == "__main__":
    main()
