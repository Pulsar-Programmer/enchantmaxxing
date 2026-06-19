#!/usr/bin/env python3
"""Stamp the current gradle.properties versions into README + CHANGELOG.

Single source of truth is gradle.properties. Run this after bumping any
version there. It will:

  * Make sure the Dependency Guide table in README.md has a row for the
    current mod_version (loader / config lib / owo / mod menu columns),
    inserting one at the top if missing or updating it in place if present.
  * Make sure CHANGELOG.md has a section header for the current mod_version,
    inserting an empty one at the top if missing.

The CHANGELOG *bullets* are written by hand — this only stamps the header so
you never have to retype the version string. Fill in the bullets, commit,
tag `v<version>`, and CI publishes the rest.

Usage:  python3 scripts/bump.py
"""
from pathlib import Path
import re
import sys

ROOT = Path(__file__).resolve().parent.parent
PROPS = ROOT / "gradle.properties"
README = ROOT / "README.md"
CHANGELOG = ROOT / "CHANGELOG.md"


def read_props() -> dict:
    props = {}
    for line in PROPS.read_text().splitlines():
        line = line.strip()
        if not line or line.startswith("#") or "=" not in line:
            continue
        key, _, value = line.partition("=")
        props[key.strip()] = value.strip()
    return props


def update_changelog(mod_version: str) -> bool:
    text = CHANGELOG.read_text()
    first = text.lstrip().splitlines()[0] if text.strip() else ""
    if first.strip() == mod_version:
        print(f"CHANGELOG: section for {mod_version} already present — skipping")
        return False
    section = f"{mod_version}\n- _Describe changes here._\n\n"
    CHANGELOG.write_text(section + text)
    print(f"CHANGELOG: inserted empty section for {mod_version}")
    return True


def update_readme(props: dict) -> bool:
    mod_version = props["mod_version"]
    row_cells = [
        f"**{mod_version}**",
        props["loader_version"],
        props["walksylib_version"],
        props["owo_version"],
        props["mod_menu"],
    ]
    new_row = "| " + " | ".join(row_cells) + " |"

    lines = README.read_text().splitlines()

    # Find the dependency-guide header row ("| FTT ...") and its separator.
    header_idx = next(
        (i for i, l in enumerate(lines) if re.match(r"\|\s*FTT\s*\|", l)), None
    )
    if header_idx is None:
        print("README: could not find the dependency table — skipping", file=sys.stderr)
        return False
    sep_idx = header_idx + 1  # the | --- | --- | row
    first_data_idx = sep_idx + 1

    # Normalized cell values of a table row (drops padding + ** emphasis), so
    # an already-correct but nicely-aligned row isn't rewritten just for spacing.
    def cells(row: str) -> list:
        return [c.strip().strip("*").strip() for c in row.strip().strip("|").split("|")]

    want = [c.strip().strip("*").strip() for c in row_cells]
    top = lines[first_data_idx] if first_data_idx < len(lines) else ""
    if top.startswith("|") and cells(top) and cells(top)[0] == mod_version:
        if cells(top) == want:
            print(f"README: row for {mod_version} already up to date — skipping")
            return False
        lines[first_data_idx] = new_row
        print(f"README: updated existing row for {mod_version}")
    else:
        lines.insert(first_data_idx, new_row)
        print(f"README: inserted new row for {mod_version}")

    README.write_text("\n".join(lines) + "\n")
    return True


def main() -> None:
    props = read_props()
    missing = [
        k
        for k in ("mod_version", "loader_version", "walksylib_version", "owo_version", "mod_menu")
        if k not in props
    ]
    if missing:
        sys.exit(f"gradle.properties is missing: {', '.join(missing)}")
    update_readme(props)
    update_changelog(props["mod_version"])


if __name__ == "__main__":
    main()
