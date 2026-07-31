# Search-backend conformance matrix

This harness evaluates the Explorer role behind classic SynBioHub without
allowing SynBioHub to silently retry failed Explorer queries against its store.
The topology itself must set `SBH_EXPLORER_FALLBACK=false`.

The full run is the principal gate. It pins SBOLTestSuite revision
`0044284331b2f915a6e4b9d50e1cbf3ea2f62dcd` and attempts all 291 XML documents
currently found under `SBOL2`, `SBOL2_bp`, `SBOL2_ic`, and `SBOL2_nc`. Invalid
or incomplete inputs may be rejected by SynBioHub; the report preserves every
outcome so the same acceptance boundary can be compared across topologies.

Each accepted document with at least one imported member contributes its own
search probe. Before querying search, the harness downloads that document's
collection SBOL and selects a direct member from store-backed collection
membership. The report records the download hash, membership counts, and exact
post-import member. This matters because SynBioHub can discard external top
levels, prefix generic display IDs, and normalize invalid URNs during import.
Accepted empty documents remain recorded but are correctly non-probeable. The
run also verifies private search visibility, anonymous isolation,
private-to-public publication, removal, and an observable index completion.

## Automation

The `Integration testing` workflow runs the focused indexed-HTML contract on
every push for Virtuoso with SBOLExplorer, sbol-db with SBOLExplorer, and
sbol-db with its compatibility listener. Each row explicitly rebuilds the
configured index and compares the complete `/search/I0462` HTML snapshot.

The `Search backend conformance` workflow runs the full pinned corpus every
Sunday and on manual dispatch. It runs the SBOLExplorer baseline and sbol-db
candidate on separate runners, uploads both JSON reports and their Compose
logs, and then uploads the comparison report. Each workflow's
`SYNBIOHUB_DOCKER_REF` selects its companion topology revision.

## Run one topology

For the native sbol-db row, pass the main listener's metrics endpoint so job
completion is observable:

```sh
python3 tests/search-backends/run-conformance.py \
  --topology sboldb-native \
  --base-url http://127.0.0.1:27777/ \
  --metrics-url http://127.0.0.1:18890/metrics \
  --corpus-root /Users/marpaia/git/SynBioDex/SBOLTestSuite \
  --report artifacts/search-backends/sboldb-native.json
```

The fixed SBOLExplorer row obtains completion evidence through Explorer's
indexing log:

```sh
python3 tests/search-backends/run-conformance.py \
  --topology sboldb-explorer-fixed \
  --base-url http://127.0.0.1:37777/ \
  --corpus-root /Users/marpaia/git/SynBioDex/SBOLTestSuite \
  --report artifacts/search-backends/sboldb-explorer-fixed.json
```

`--smoke` selects four documents, one from each corpus category. Smoke mode is
useful for debugging but deliberately fails `full_corpus_coverage` and can
never satisfy the required native gate.

Corpus probes retain the first 50 ranked rows for parity comparison, then page
through a bounded 10,000-row window only until they find the exact URI and
display ID selected by the independent collection oracle. This accommodates
large result sets without mistaking a duplicate ID from another submitted
document for success. If only probe logic changes after an expensive full
import, reuse the immutable submission, lifecycle, and index evidence while
re-running every collection download and live search query:

```sh
python3 tests/search-backends/run-conformance.py \
  --topology sboldb-native \
  --base-url http://127.0.0.1:27777/ \
  --metrics-url http://127.0.0.1:18890/metrics \
  --corpus-root /Users/marpaia/git/SynBioDex/SBOLTestSuite \
  --reprobe-from artifacts/search-backends/sboldb-native.json \
  --report artifacts/search-backends/sboldb-native-reprobed.json
```

## Compare reports

```sh
python3 tests/search-backends/compare-reports.py \
  --baseline artifacts/search-backends/sboldb-explorer-fixed.json \
  --candidate artifacts/search-backends/sboldb-native.json \
  --output artifacts/search-backends/comparison.json
```

The comparison records submission and search-result differences and applies a
pinned-corpus compatibility policy. Both lifecycle gates and identical
submission outcomes are required; at least 90% of complete first-page result
sets must agree, no more than two probes may have count drift, and either drift
may be at most one result. Exact top-ten order remains diagnostic because
SBOLExplorer and sbol-db intentionally use different ranking implementations.
This boundary catches tokenizer explosions and missing ontology enrichment
without pretending the two rankers are byte-identical.
