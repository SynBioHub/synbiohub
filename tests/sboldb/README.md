# Running the test suite against sbol-db

This harness runs SynBioHub's Python test suite against
[sbol-db](https://github.com/marpaia/sbol-db) in place of Virtuoso, so the
same fixtures that define the Virtuoso baseline also gate the sbol-db
integration.

## Prerequisites

- The Python test venv at `tests/venv` (see `tests/README.md`).
- The `synbiohub/synbiohub:snapshot-standalone` image (built from this repo:
  `docker build -t synbiohub/synbiohub:snapshot-standalone -f docker/Dockerfile .`).
- The `sbol-db:harness` image, built from the sbol-db repo:
  `docker build -t sbol-db:harness .`

## Layout

- `docker-compose.yml` — SynBioHub + sbol-db + Postgres. SynBioHub reaches
  the triplestore at `http://sboldb:8890`.
- `config.local.json` — seeded into the SynBioHub container as its initial
  config; points the `triplestore` block at sbol-db.
- `test-sboldb.sh` — brings the stack up, waits for SynBioHub to report
  healthy, and runs `test_suite.py`.

## Usage

```sh
tests/sboldb/test-sboldb.sh            # run the full suite against sbol-db
tests/sboldb/test-sboldb.sh --no-test  # just bring the stack up
tests/sboldb/test-sboldb.sh --down     # tear down (and drop volumes) after
```

The stack runs under the Compose project `sboldbproject`, separate from the
Virtuoso suite's `testsuiteproject`, so both can coexist. Tear down manually
with:

```sh
docker compose -p sboldbproject -f tests/sboldb/docker-compose.yml down -v
```

## How the backend swap works

SynBioHub talks to its triplestore entirely over HTTP. The `triplestore`
block in `config.local.json` sets `sparqlEndpoint` and `graphStoreEndpoint`;
SynBioHub derives the authenticated update endpoint as `sparqlEndpoint` +
`-auth`. sbol-db serves all three:

| SynBioHub call            | sbol-db endpoint                       |
| ------------------------- | -------------------------------------- |
| read query                | `GET/POST /sparql`                     |
| update (`sparqlEndpoint`+`-auth`) | `GET/POST /sparql-auth` (Basic) |
| graph store CRUD          | `/sparql-graph-crud-auth/` (Basic)     |

Credentials are `dba`/`dba`, matching a stock Virtuoso.
