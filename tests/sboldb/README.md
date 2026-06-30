# Running the test suite against sbol-db

This harness runs SynBioHub's test suite against
[sbol-db](https://github.com/marpaia/sbol-db) in place of Virtuoso, so the
same fixtures that define the Virtuoso baseline also gate the sbol-db
integration.

## Prerequisites

- The Python test venv at `tests/venv` (see `tests/README.md`).
- The `synbiohub/synbiohub:snapshot-standalone` image, built from this repo:
  `docker build -t synbiohub/synbiohub:snapshot-standalone -f docker/Dockerfile .`
- Docker. The sbol-db image is pulled from
  `ghcr.io/marpaia/sbol-db:v0.1.1` (published by the sbol-db repo's `container`
  workflow), pinned in `docker-compose.yml`; there is no local sbol-db build step.

## Layout

- `docker-compose.yml` runs SynBioHub, sbol-db, and Postgres. sbol-db
  answers at the `virtuoso` network alias on port 8890, so SynBioHub's
  configuration is identical to the Virtuoso baseline.
- `config.local.json` is seeded into the SynBioHub container as its initial
  config and points the `triplestore` block at sbol-db.
- `test-sboldb.sh` brings the stack up, waits for SynBioHub to report
  healthy, and runs `test_suite.py`.
- `run-sboltestrunner.sh` runs the Java SBOLTestRunner round-trip
  conformance suite against the running stack.

## Usage

```sh
tests/sboldb/test-sboldb.sh             # run the Python suite against sbol-db
tests/sboldb/test-sboldb.sh --persist   # suite, then restart and re-check persistence
tests/sboldb/test-sboldb.sh --no-test   # just bring the stack up
tests/sboldb/test-sboldb.sh --down      # tear down (and drop volumes) after
tests/sboldb/run-sboltestrunner.sh      # SBOL2 round-trip conformance (stack must be up)
```

The stack runs under the Compose project `sboldbproject`, separate from the
Virtuoso suite's `testsuiteproject`, so both can coexist. Tear down manually
with:

```sh
docker compose -p sboldbproject -f tests/sboldb/docker-compose.yml down -v
```

To pin a different sbol-db build, change the `image:` tag in
`docker-compose.yml` to another `ghcr.io/marpaia/sbol-db:<short-sha>` or a
release tag such as `:v0.2.0`.

## How the backend swap works

SynBioHub talks to its triplestore entirely over HTTP. The `triplestore`
block in `config.local.json` sets `sparqlEndpoint` and `graphStoreEndpoint`;
SynBioHub derives the authenticated update endpoint as `sparqlEndpoint` +
`-auth`. sbol-db serves all three:

| SynBioHub call | sbol-db endpoint |
| --- | --- |
| read query | `GET`/`POST` `/sparql` |
| update (`sparqlEndpoint` + `-auth`) | `GET`/`POST` `/sparql-auth` |
| graph store CRUD | `/sparql-graph-crud-auth/` |

Credentials default to `dba`/`dba`, matching a stock Virtuoso. The harness
sets `SBOL_DB_SPARQL_AUTH_DISABLED=true` so the write endpoints skip the
401 challenge, which interacts badly with the large chunked uploads
SynBioHub streams.
