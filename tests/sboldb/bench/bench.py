#!/usr/bin/env python3
"""Benchmark Virtuoso against sbol-db on the triplestore workloads SynBioHub
issues.

The runner loads an identical SBOL corpus into each backend over the Graph
Store Protocol, then replays realized versions of SynBioHub's own SPARQL
queries directly against each backend's HTTP endpoint. SynBioHub is not in the
loop, so the numbers reflect the triplestore itself.

For every read query the runner records the client-observed latency
distribution (mean, p50, p95) over a fixed number of timed iterations after a
warmup, plus the row count each backend returned (a parity check that both
stores hold the same data and answer the same query the same way). Ingest is
measured as the wall time to load the whole corpus into a fresh graph,
averaged over a few clean runs.

Results are written as JSON for gen_report.py to turn into the deck's table and
chart.
"""

import argparse
import json
import os
import platform
import statistics
import subprocess
import time
from pathlib import Path

import requests
from requests.auth import HTTPDigestAuth

GRAPH = "http://synbiohub.org/public"

# The corpus subjects live under this host; getCollectionMembers filters
# members to the submission's own namespace, matching SynBioHub's query.
GRAPH_PREFIX = "http://localhost:7777/"

PREFIXES = """\
PREFIX sbol2: <http://sbols.org/v2#>
PREFIX dcterms: <http://purl.org/dc/terms/>
PREFIX sbh: <http://wiki.synbiohub.org/wiki/Terms/synbiohub#>
PREFIX biopax: <http://www.biopax.org/release/biopax-level3.owl#>
PREFIX so: <http://identifiers.org/so/>
"""


class Backend:
    """One triplestore behind SynBioHub's three-endpoint HTTP surface."""

    def __init__(self, name, base, auth=None):
        self.name = name
        self.sparql = base.rstrip("/") + "/sparql"
        self.crud = base.rstrip("/") + "/sparql-graph-crud-auth/"
        self.auth = auth
        self.session = requests.Session()

    def ask_ready(self):
        try:
            r = self.session.post(
                self.sparql,
                data={"query": "ASK {}"},
                headers={"Accept": "application/sparql-results+json"},
                timeout=10,
            )
            return r.status_code == 200
        except requests.RequestException:
            return False

    def query_json(self, query, timeout=600):
        r = self.session.post(
            self.sparql,
            data={"query": query},
            headers={"Accept": "application/sparql-results+json"},
            timeout=timeout,
        )
        r.raise_for_status()
        return r.json()

    def query_raw(self, query, accept="application/n-triples", timeout=600):
        r = self.session.post(
            self.sparql,
            data={"query": query},
            headers={"Accept": accept},
            timeout=timeout,
        )
        r.raise_for_status()
        return r.content

    def clear_graph(self, graph):
        self.session.delete(
            self.crud, params={"graph-uri": graph}, auth=self.auth, timeout=600
        )

    def load_rdfxml(self, graph, data):
        r = self.session.post(
            self.crud,
            params={"graph-uri": graph},
            data=data,
            headers={"Content-Type": "application/rdf+xml"},
            auth=self.auth,
            timeout=600,
        )
        r.raise_for_status()

    def count_triples(self, graph):
        rows = self.query_json(
            "SELECT (COUNT(*) AS ?c) FROM <%s> WHERE { ?s ?p ?o }" % graph
        )
        return int(rows["results"]["bindings"][0]["c"]["value"])


def rows_of(result):
    return len(result["results"]["bindings"])


def percentile(values, pct):
    if not values:
        return 0.0
    s = sorted(values)
    k = (len(s) - 1) * (pct / 100.0)
    lo = int(k)
    hi = min(lo + 1, len(s) - 1)
    if lo == hi:
        return s[lo]
    return s[lo] + (s[hi] - s[lo]) * (k - lo)


def measure(call, warmup, iters, max_seconds):
    """Time `call` over warmup + timed iterations. Returns a stats dict, or
    ``{"failed": True, ...}`` if the call errors (e.g. a 504 timeout on a
    pathological query). Adapts to slow queries: if the first probe call takes
    over a second, it drops warmup and caps iterations so a misbehaving backend
    can't run for hours, and it stops early once a wall-clock budget is spent."""
    try:
        t0 = time.perf_counter()
        call()
        probe = (time.perf_counter() - t0) * 1000.0
    except Exception as e:
        return {"failed": True, "error": "%s: %s" % (type(e).__name__, str(e)[:160])}

    if probe > 1000.0:
        warmup = 0
        iters = min(iters, 5)

    for _ in range(warmup):
        try:
            call()
        except Exception:
            break

    latencies = []
    start = time.perf_counter()
    for _ in range(iters):
        try:
            t0 = time.perf_counter()
            call()
            latencies.append((time.perf_counter() - t0) * 1000.0)
        except Exception as e:
            return {
                "failed": True,
                "error": "%s: %s" % (type(e).__name__, str(e)[:160]),
                "samples": len(latencies),
            }
        if len(latencies) >= 3 and (time.perf_counter() - start) > max_seconds:
            break

    if not latencies:
        latencies = [probe]
    mean = statistics.mean(latencies)
    return {
        "n": len(latencies),
        "mean_ms": mean,
        "p50_ms": percentile(latencies, 50),
        "p95_ms": percentile(latencies, 95),
        "min_ms": min(latencies),
        "max_ms": max(latencies),
        "qps": 1000.0 / mean if mean else 0.0,
    }


def discover_uris(backend, graph):
    """Find real URIs in the loaded corpus so the read queries hit data that
    actually exists, identically on both backends."""
    coll = backend.query_json(
        PREFIXES
        + "SELECT ?c FROM <%s> WHERE { ?c a sbol2:Collection } LIMIT 1" % graph
    )
    toplevel = backend.query_json(
        PREFIXES
        + """SELECT ?s FROM <%s> WHERE {
        ?s sbh:topLevel ?s ; dcterms:title ?t
    } LIMIT 1"""
        % graph
    )
    cd = backend.query_json(
        PREFIXES
        + """SELECT ?s FROM <%s> WHERE {
        ?s a sbol2:ComponentDefinition ; sbh:topLevel ?s
    } LIMIT 1"""
        % graph
    )
    return {
        "collection": coll["results"]["bindings"][0]["c"]["value"],
        "toplevel": toplevel["results"]["bindings"][0]["s"]["value"],
        "componentdefinition": cd["results"]["bindings"][0]["s"]["value"],
    }


def build_read_queries(graph, uris):
    """Realized versions of SynBioHub's templated SPARQL, targeting one graph
    and the discovered URIs. Each is valid SPARQL 1.1 (FROM stays at the top
    level), so it runs unchanged on Virtuoso and sbol-db."""
    g = graph
    coll = uris["collection"]
    top = uris["toplevel"]
    cd = uris["componentdefinition"]
    queries = {}

    queries["getCollections"] = (
        PREFIXES
        + """SELECT DISTINCT ?subject ?displayId ?name FROM <%s> WHERE {
    ?subject a sbol2:Collection .
    OPTIONAL { ?subject sbol2:displayId ?displayId . }
    OPTIONAL { ?subject dcterms:title ?name . }
}"""
        % g
    )

    queries["countComponentDefinition"] = (
        PREFIXES
        + "SELECT (COUNT(DISTINCT ?cd) AS ?count) FROM <%s> WHERE { ?cd a sbol2:ComponentDefinition }"
        % g
    )

    queries["search"] = (
        PREFIXES
        + """SELECT DISTINCT ?subject ?displayId ?version ?name ?description ?type ?sbolType ?role
FROM <%s>
WHERE {
    ?subject a ?type .
    ?subject sbh:topLevel ?subject .
    OPTIONAL { ?subject sbol2:displayId ?displayId . }
    OPTIONAL { ?subject sbol2:version ?version . }
    OPTIONAL { ?subject dcterms:title ?name . }
    OPTIONAL { ?subject dcterms:description ?description . }
    OPTIONAL { ?subject sbol2:type ?sbolType . FILTER(STRSTARTS(str(?sbolType),'http://www.biopax.org/release/biopax-level3.owl')) }
    OPTIONAL { ?subject sbol2:role ?role . FILTER(STRSTARTS(str(?role),'http://identifiers.org/so/')) }
}
LIMIT 50 OFFSET 0"""
        % g
    )

    queries["searchCount"] = (
        PREFIXES
        + """SELECT (sum(?tempcount) as ?count)
FROM <%s>
WHERE {
{
    SELECT (count(distinct ?subject) as ?tempcount)
    WHERE {
        ?subject a ?type .
        ?subject sbh:topLevel ?subject .
        OPTIONAL { ?subject sbol2:displayId ?displayId . }
        OPTIONAL { ?subject sbol2:version ?version . }
        OPTIONAL { ?subject dcterms:title ?name . }
        OPTIONAL { ?subject dcterms:description ?description . }
    }
}
}"""
        % g
    )

    queries["getCollectionMembers"] = (
        PREFIXES
        + """SELECT ?uri ?displayId ?name ?description ?type ?sbolType ?role
FROM <%s>
WHERE { {
SELECT DISTINCT ?uri ?displayId ?name ?description ?type ?sbolType ?role
WHERE {
<%s> a sbol2:Collection .
<%s> sbol2:member ?uri .
OPTIONAL { ?uri a ?type . }
OPTIONAL { ?uri sbol2:displayId ?displayId . }
OPTIONAL { ?uri dcterms:title ?name . }
OPTIONAL { ?uri dcterms:description ?description . }
OPTIONAL { ?uri sbol2:type ?sbolType . FILTER(STRSTARTS(str(?sbolType),'http://www.biopax.org/release/biopax-level3.owl')) }
OPTIONAL { ?uri sbol2:role ?role . FILTER(STRSTARTS(str(?role),'http://identifiers.org/so/')) }
FILTER(STRSTARTS(str(?uri), '%s'))
FILTER NOT EXISTS {
<%s> sbol2:member ?otherMember .
{ ?otherMember ?ref ?uri . }
UNION
{ ?otherMember ?ref ?child . ?child ?childRef ?uri . }
FILTER(?otherMember != ?uri)
}
}
}}
LIMIT 50 OFFSET 0"""
        % (g, coll, coll, GRAPH_PREFIX, coll)
    )

    queries["getMetadata"] = (
        PREFIXES
        + """SELECT ?name ?description FROM <%s> WHERE {
    <%s> dcterms:title ?name .
    OPTIONAL { <%s> dcterms:description ?description }
}"""
        % (g, top, top)
    )

    # DESCRIBE is the object/download path (RetrieveSBOL.sparql); the response
    # is RDF, not a result set, so it is measured separately as raw bytes.
    queries["describe"] = "DESCRIBE <%s> FROM <%s>" % (cd, g)

    return queries


def cpu_brand():
    try:
        return subprocess.check_output(
            ["sysctl", "-n", "machdep.cpu.brand_string"], text=True
        ).strip()
    except Exception:
        return platform.processor() or platform.machine()


def mem_bytes():
    try:
        return int(subprocess.check_output(["sysctl", "-n", "hw.memsize"], text=True).strip())
    except Exception:
        return 0


def wait_ready(backends, timeout=300):
    deadline = time.time() + timeout
    pending = list(backends)
    while pending and time.time() < deadline:
        pending = [b for b in pending if not b.ask_ready()]
        if pending:
            time.sleep(3)
    if pending:
        raise SystemExit(
            "backends not ready: " + ", ".join(b.name for b in pending)
        )


def run_ingest(backend, payloads, graph, runs):
    durations = []
    for _ in range(runs):
        backend.clear_graph(graph)
        t0 = time.perf_counter()
        for data in payloads:
            backend.load_rdfxml(graph, data)
        durations.append(time.perf_counter() - t0)
    return {
        "runs_s": durations,
        "mean_s": statistics.mean(durations),
        "min_s": min(durations),
        "files": len(payloads),
        "files_per_s": len(payloads) / statistics.mean(durations),
    }


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--virtuoso", default="http://localhost:18901")
    ap.add_argument("--sboldb-postgres", default="http://localhost:18902")
    ap.add_argument("--sboldb-sqlite", default="http://localhost:18903")
    ap.add_argument("--sboldb-rocksdb", default="http://localhost:18904")
    ap.add_argument("--corpus", required=True, help="dir of SBOL RDF/XML files")
    ap.add_argument("--graph", default=GRAPH)
    ap.add_argument("--iterations", type=int, default=40)
    ap.add_argument("--warmup", type=int, default=8)
    ap.add_argument("--ingest-runs", type=int, default=3)
    ap.add_argument("--query-timeout", type=int, default=60,
                    help="per-call HTTP timeout (s); a hung query fails here")
    ap.add_argument("--max-seconds-per-query", type=int, default=20,
                    help="wall-clock budget per query/backend before stopping early")
    ap.add_argument("--out", required=True)
    args = ap.parse_args()

    corpus = sorted(Path(args.corpus).glob("*.xml"))
    if not corpus:
        raise SystemExit("no .xml files in %s" % args.corpus)
    payloads = [p.read_bytes() for p in corpus]
    corpus_bytes = sum(len(p) for p in payloads)

    # Each backend is one HTTP endpoint. Virtuoso needs digest auth for the
    # graph-store writes; the sbol-db backends run with auth disabled. A target
    # URL of "skip" (or empty) drops it, so a subset can be benchmarked.
    specs = [
        ("virtuoso", args.virtuoso, HTTPDigestAuth("dba", "dba")),
        ("postgres", args.sboldb_postgres, None),
        ("sqlite", args.sboldb_sqlite, None),
        ("rocksdb", args.sboldb_rocksdb, None),
    ]
    backends = [
        Backend(name, url, auth=auth)
        for name, url, auth in specs
        if url and url.lower() != "skip"
    ]
    endpoints = {b.name: b.sparql for b in backends}

    print("[bench] waiting for %d backends to be ready" % len(backends))
    wait_ready(backends)

    print("[bench] ingest: loading %d files into each backend (%d runs)"
          % (len(corpus), args.ingest_runs))
    ingest = {}
    for b in backends:
        ingest[b.name] = run_ingest(b, payloads, args.graph, args.ingest_runs)
        print("[bench]   %-9s %.2fs mean (%.1f files/s)"
              % (b.name, ingest[b.name]["mean_s"], ingest[b.name]["files_per_s"]))

    triples = {b.name: b.count_triples(args.graph) for b in backends}
    print("[bench] triples loaded: "
          + "  ".join("%s=%d" % (n, triples[n]) for n in triples))

    uris = discover_uris(backends[0], args.graph)
    print("[bench] sample collection: %s" % uris["collection"])
    queries = build_read_queries(args.graph, uris)

    def p50(x):
        return "FAILED" if x.get("failed") else "%.1fms" % x["p50_ms"]

    reads = []
    qt = args.query_timeout
    for name, query in queries.items():
        raw = name == "describe"
        entry = {"name": name, "query": query, "backends": {}}
        for b in backends:
            if raw:
                call = lambda b=b, q=query: b.query_raw(q, timeout=qt)
            else:
                call = lambda b=b, q=query: b.query_json(q, timeout=qt)
            # Result shape (row count / byte size) from one call: a cross-backend
            # parity check, and harmless if the query is failing on this backend.
            shape = {}
            try:
                out = call()
                shape["result_bytes" if raw else "result_rows"] = (
                    len(out) if raw else rows_of(out)
                )
            except Exception:
                pass
            stats = measure(call, args.warmup, args.iterations, args.max_seconds_per_query)
            stats.update(shape)
            entry["backends"][b.name] = stats

        print("[bench]   %-22s %s" % (name,
              "  ".join("%s %s" % (b.name, p50(entry["backends"][b.name])) for b in backends)))
        reads.append(entry)

    # Cross-backend row-count parity: every SELECT should return the same number
    # of rows on every backend that answered it. DESCRIBE is excluded (its result
    # scope is implementation-defined).
    print("[bench] row-count parity check:")
    for entry in reads:
        if entry["name"] == "describe":
            continue
        rows = {n: s.get("result_rows") for n, s in entry["backends"].items()
                if not s.get("failed") and "result_rows" in s}
        distinct = set(rows.values())
        status = "ok" if len(distinct) <= 1 else "MISMATCH %s" % rows
        print("[bench]   %-22s %s" % (entry["name"], status))

    result = {
        "meta": {
            "host": platform.node(),
            "os": platform.platform(),
            "cpu": cpu_brand(),
            "cores": os.cpu_count(),
            "mem_bytes": mem_bytes(),
            "python": platform.python_version(),
            "corpus_dir": str(Path(args.corpus).resolve()),
            "corpus_files": len(corpus),
            "corpus_bytes": corpus_bytes,
            "graph": args.graph,
            "iterations": args.iterations,
            "warmup": args.warmup,
            "ingest_runs": args.ingest_runs,
            "backends": [b.name for b in backends],
            "endpoints": endpoints,
            "virtuoso_image": "tenforce/virtuoso:virtuoso7.2.5",
            "sboldb_image": os.environ.get("SBOLDB_IMAGE", "sbol-db:bench"),
            "triples": triples,
            "uris": uris,
        },
        "ingest": ingest,
        "reads": reads,
    }

    Path(args.out).write_text(json.dumps(result, indent=2))
    print("[bench] wrote %s" % args.out)


if __name__ == "__main__":
    main()
