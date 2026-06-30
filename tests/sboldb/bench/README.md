# Benchmarking Virtuoso against the sbol-db storage backends

This harness measures the triplestore workloads SynBioHub issues, run directly
against Virtuoso and all three [sbol-db](https://github.com/marpaia/sbol-db)
storage backends (Postgres, SQLite, RocksDB), with no SynBioHub in the loop, so
the numbers reflect the database itself.

`docker-compose.yml` brings the backends up side by side, each on its own host
port:

| Service | Backend | Port |
| --- | --- | --- |
| `virtuoso` | Virtuoso | 18901 |
| `sboldb-postgres` | sbol-db on Postgres | 18902 |
| `sboldb-sqlite` | sbol-db on SQLite | 18903 |
| `sboldb-rocksdb` | sbol-db on RocksDB | 18904 |

`bench.py` loads the same SBOL corpus into each over the Graph Store Protocol,
then replays realized versions of SynBioHub's own SPARQL queries, recording the
client-observed latency distribution per query and the wall time to ingest the
corpus.

## Prerequisites

- The Python test venv at `tests/venv` (see `tests/README.md`); `bench.py`
  uses `requests`, already in `tests/test_requirements.txt`.
- Docker. Virtuoso is pulled automatically; the sbol-db image defaults to the
  published `ghcr.io/marpaia/sbol-db:v0.1.1` (override with `SBOLDB_IMAGE`, e.g.
  a locally built `sbol-db:bench` when iterating on the backend, built from the
  sbol-db repo with `docker build -t sbol-db:bench .`). The image links glibc so
  the RocksDB backend's C++ library builds (a static musl target cannot).
- The SBOL corpus at `tests/Emulated/` (189 round-tripped SBOL2 files),
  produced by `tests/sboldb/run-sboltestrunner.sh`.

## Usage

```sh
tests/sboldb/bench/run-bench.sh                             # full run (v0.1.1), leaves the stack up
tests/sboldb/bench/run-bench.sh --iterations 100
tests/sboldb/bench/run-bench.sh --down                       # tear the stack down afterward
```

Results are written to `results/bench-<host>.json`, and LaTeX fragments
(`reads-table.tex`, `reads-chart.tex`, `ingest.tex`) to `out/`. The script also
prints a Markdown summary. To benchmark a subset of backends, pass e.g.
`--sboldb-sqlite skip` to `bench.py` (or via `run-bench.sh`).

## Method

- **Identical data.** Every `.xml` file in the corpus is POSTed as
  `application/rdf+xml` into the graph `http://synbiohub.org/public` on each
  backend. The run prints the loaded triple count per backend: all four agree
  (a graph is a set of triples on every backend), which confirms parity.
- **Identical queries.** The read queries are realized from SynBioHub's
  templates (`sparql/*.sparql`) with `FROM` at the top level (valid SPARQL 1.1),
  so the same query string runs unchanged everywhere. Sample URIs are discovered
  from the loaded data so queries hit real objects. After timing, the run checks
  that every backend returned the same row count for each SELECT (DESCRIBE is
  excluded: its result scope is implementation-defined).
- **Timing.** Client-side wall time over `--iterations` timed calls after
  `--warmup` warmups; the JSON keeps mean, p50, p95, min, max, and throughput. A
  query that errors or exceeds `--query-timeout` is recorded as failed rather
  than aborting the run; a slow query (first probe over a second) runs fewer
  iterations under a `--max-seconds-per-query` budget. Ingest is the wall time to
  load the whole corpus into a fresh graph, averaged over `--ingest-runs` runs.

## Architecture note

Virtuoso ships only as an x86-64 image and runs under emulation on Apple
Silicon, while the sbol-db image is native. That makes Virtuoso's numbers
conservative, so the cross-engine comparison is indicative; the comparison
*among* the three sbol-db backends is like-for-like.

The ports are off the defaults so this stack coexists with the test stacks.
Tear down manually with:

```sh
docker compose -p sboldbbench -f tests/sboldb/bench/docker-compose.yml down -v
```
