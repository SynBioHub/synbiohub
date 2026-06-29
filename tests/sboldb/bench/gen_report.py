#!/usr/bin/env python3
"""Turn a multi-backend bench.py results JSON into the LaTeX fragments the
status-update deck embeds: a booktabs latency table, a pgfplots grouped-bar
chart, and an ingest/footprint summary. Also prints a Markdown summary to
stdout. Handles any number of backends (Virtuoso plus the sbol-db backends)."""

import argparse
import json
from pathlib import Path

# Read workloads, in display order. DESCRIBE is measured by the harness but
# excluded here: its result scope is implementation-defined, so the engines
# return different triples and a latency comparison is not apples-to-apples. The
# SELECT workloads below return identical row counts on every backend.
LABELS = [
    ("getCollections", "List collections"),
    ("countComponentDefinition", "Count parts"),
    ("getMetadata", "Object metadata"),
    ("search", "Browse / search"),
    ("searchCount", "Search count"),
    ("getCollectionMembers", "Collection members"),
]

# Backend key -> (table header, pgfplots fill color). Virtuoso is the sand
# accent; the three sbol-db backends are increasingly saturated teal.
BACKENDS = {
    "virtuoso": ("Virtuoso", "biosand"),
    "postgres": ("Postgres", "bioteal!35"),
    "sqlite": ("SQLite", "bioteal!65"),
    "rocksdb": ("RocksDB", "bioteal"),
}


def order(data):
    return data["meta"].get("backends", list(data["ingest"].keys()))


def by_name(reads):
    return {r["name"]: r for r in reads}


def fmt_ms(x):
    return "%.1f" % x if x < 100 else "%.0f" % x


def cell(stats):
    if stats.get("failed"):
        return "timeout"
    return fmt_ms(stats["p50_ms"])


def gen_table(data):
    names = order(data)
    reads = by_name(data["reads"])
    headers = " & ".join(BACKENDS.get(n, (n, ""))[0] for n in names)
    cols = "l" + "r" * len(names)
    lines = [
        r"\begin{tabular}{@{}%s@{}}" % cols,
        r"\toprule",
        "Workload & %s \\\\" % headers,
        r"\midrule",
    ]
    for name, label in LABELS:
        if name not in reads:
            continue
        cells = " & ".join(cell(reads[name]["backends"][n]) for n in names)
        lines.append("%s & %s \\\\" % (label, cells))
    lines += [r"\bottomrule", r"\end{tabular}"]
    return "\n".join(lines)


def gen_chart(data):
    names = order(data)
    reads = by_name(data["reads"])
    rows = [
        (label, reads[name])
        for name, label in LABELS
        if name in reads
        and all(not reads[name]["backends"][n].get("failed") for n in names)
    ]
    coords = ", ".join(label for label, _ in rows)
    plots = []
    for n in names:
        header, color = BACKENDS.get(n, (n, "gray"))
        pts = " ".join(
            "(%s, %.3f)" % (label, r["backends"][n]["p50_ms"]) for label, r in rows
        )
        plots.append(
            r"\addplot[fill=%s, draw=black!55] coordinates {%s};" % (color, pts)
        )
        plots.append(r"\addlegendentry{%s}" % header)
    return "\n".join([
        r"\begin{tikzpicture}",
        r"\begin{axis}[",
        r"    ybar=1pt,",
        r"    width=0.95\linewidth, height=5.2cm,",
        r"    ymode=log, log origin=infty,",
        r"    ylabel={median latency (ms)},",
        r"    ymajorgrids, grid style={gray!25},",
        r"    bar width=4.5pt,",
        r"    enlarge x limits=0.10,",
        r"    symbolic x coords={%s}," % coords,
        r"    xtick=data, x tick label style={rotate=30, anchor=east, font=\tiny},",
        r"    y tick label style={font=\tiny}, ylabel style={font=\scriptsize},",
        r"    legend style={font=\tiny, at={(0.5,1.02)}, anchor=south, legend columns=-1, draw=none, fill=none, /tikz/every even column/.append style={column sep=6pt}},",
        r"    legend cell align=left,",
        r"]",
        *plots,
        r"\end{axis}",
        r"\end{tikzpicture}",
    ])


def gen_ingest(data):
    names = order(data)
    meta = data["meta"]
    mb = meta["corpus_bytes"] / 1e6
    parts = []
    for n in names:
        header = BACKENDS.get(n, (n, ""))[0]
        parts.append("%s %.1f\\,s" % (header, data["ingest"][n]["mean_s"]))
    triples = meta["triples"]
    same = len(set(triples.values())) == 1
    triples_note = (
        "All backends store the same %s triples." % "{:,}".format(next(iter(triples.values())))
        if same else
        "Triples stored: " + ", ".join("%s %s" % (BACKENDS.get(n, (n, ""))[0],
                                                   "{:,}".format(triples[n])) for n in names) + "."
    )
    return (
        "Loading the %d-file corpus (%.0f\\,MB) took: %s. %s"
        % (meta["corpus_files"], mb, ", ".join(parts), triples_note)
    )


def print_summary(data):
    meta = data["meta"]
    names = order(data)
    reads = by_name(data["reads"])
    print("# Benchmark summary")
    print("host: %s  cpu: %s  cores: %s" % (meta["host"], meta["cpu"], meta["cores"]))
    print("corpus: %d files, %.1f MB" % (meta["corpus_files"], meta["corpus_bytes"] / 1e6))
    print("triples: " + "  ".join("%s=%d" % (n, meta["triples"][n]) for n in names))
    print("ingest:  " + "  ".join("%s=%.1fs" % (n, data["ingest"][n]["mean_s"]) for n in names))
    print()
    hdr = "%-22s" % "workload" + "".join("%12s" % n for n in names)
    print(hdr)
    for name, label in LABELS + [("describe", "Fetch object")]:
        if name not in reads:
            continue
        cells = "".join("%12s" % cell(reads[name]["backends"][n]) for n in names)
        print("%-22s%s" % (label, cells))
    print()


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("results")
    ap.add_argument("--outdir", required=True)
    args = ap.parse_args()

    data = json.loads(Path(args.results).read_text())
    out = Path(args.outdir)
    out.mkdir(parents=True, exist_ok=True)

    (out / "reads-table.tex").write_text(gen_table(data) + "\n")
    (out / "reads-chart.tex").write_text(gen_chart(data) + "\n")
    (out / "ingest.tex").write_text(gen_ingest(data) + "\n")

    print_summary(data)
    print("wrote fragments to %s" % out)


if __name__ == "__main__":
    main()
