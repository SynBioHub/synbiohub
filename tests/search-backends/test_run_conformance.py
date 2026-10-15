import importlib.util
import io
from pathlib import Path
import sys
from types import SimpleNamespace
import unittest
from unittest.mock import patch
import urllib.error


MODULE_PATH = Path(__file__).with_name("run-conformance.py")
SPEC = importlib.util.spec_from_file_location("search_backend_conformance", MODULE_PATH)
assert SPEC is not None and SPEC.loader is not None
CONFORMANCE = importlib.util.module_from_spec(SPEC)
sys.modules[SPEC.name] = CONFORMANCE
SPEC.loader.exec_module(CONFORMANCE)


class CollectionOracleTest(unittest.TestCase):
    def test_selects_rewritten_direct_member_and_ignores_external_reference(self):
        body = b"""<?xml version="1.0"?>
<rdf:RDF xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
         xmlns:sbol="http://sbols.org/v2#"
         xmlns:grn="http://example.org/grn#">
  <sbol:Collection rdf:about="http://example.test/user/test/corpus_0001/corpus_0001_collection/1">
    <sbol:displayId>corpus_0001_collection</sbol:displayId>
    <sbol:member rdf:resource="http://example.test/user/test/corpus_0001/ComponentDefinition_design/1"/>
    <sbol:member rdf:resource="https://external.example/part/1"/>
  </sbol:Collection>
  <sbol:ComponentDefinition rdf:about="http://example.test/user/test/corpus_0001/ComponentDefinition_design/1">
    <sbol:displayId>ComponentDefinition_design</sbol:displayId>
  </sbol:ComponentDefinition>
  <grn:RegulatoryReaction rdf:about="https://external.example/part/1">
    <sbol:displayId>external_part</sbol:displayId>
  </grn:RegulatoryReaction>
</rdf:RDF>
"""

        oracle = CONFORMANCE.parse_collection_oracle(body, "corpus_0001", "design")

        self.assertEqual(oracle["member_reference_count"], 2)
        self.assertEqual(oracle["probeable_member_count"], 1)
        self.assertEqual(oracle["selection"], "source-display-id")
        self.assertEqual(
            oracle["selected"],
            {
                "display_id": "ComponentDefinition_design",
                "type": "ComponentDefinition",
                "uri_path": "/user/test/corpus_0001/ComponentDefinition_design/1",
            },
        )

    def test_falls_back_deterministically_after_import_normalization(self):
        body = b"""<rdf:RDF xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
         xmlns:sbol="http://sbols.org/v2#">
  <sbol:Collection rdf:about="http://example.test/user/test/corpus_0002/corpus_0002_collection/1">
    <sbol:displayId>corpus_0002_collection</sbol:displayId>
    <sbol:member rdf:resource="http://example.test/user/test/corpus_0002/urn_z/1"/>
    <sbol:member rdf:resource="http://example.test/user/test/corpus_0002/urn_a/1"/>
  </sbol:Collection>
  <sbol:ComponentDefinition rdf:about="http://example.test/user/test/corpus_0002/urn_z/1">
    <sbol:displayId>urn_z</sbol:displayId>
  </sbol:ComponentDefinition>
  <sbol:ComponentDefinition rdf:about="http://example.test/user/test/corpus_0002/urn_a/1">
    <sbol:displayId>urn_a</sbol:displayId>
  </sbol:ComponentDefinition>
</rdf:RDF>"""

        oracle = CONFORMANCE.parse_collection_oracle(body, "corpus_0002", "rbsB")

        self.assertEqual(oracle["selection"], "deterministic-direct-member")
        self.assertEqual(oracle["selected"]["display_id"], "urn_a")


class MetricsEndpointTest(unittest.TestCase):
    def test_fresh_worker_can_have_no_completed_jobs(self):
        with patch.object(
            CONFORMANCE.urllib.request,
            "urlopen",
            return_value=io.BytesIO(b"# TYPE sbol_db_jobs_completed_total counter\n"),
        ):
            self.assertEqual(CONFORMANCE.completed_rebuilds("http://metrics", 10), 0)

    def test_counts_only_successful_search_rebuilds(self):
        metrics = b'''sbol_db_jobs_completed_total{kind="rebuild_search_index",status="succeeded"} 3
sbol_db_jobs_completed_total{kind="rebuild_search_index",status="failed"} 2
sbol_db_jobs_completed_total{kind="rebuild_vector_index",status="succeeded"} 7
'''
        with patch.object(
            CONFORMANCE.urllib.request, "urlopen", return_value=io.BytesIO(metrics)
        ):
            self.assertEqual(CONFORMANCE.completed_rebuilds("http://metrics", 10), 3)

    def test_wrong_metrics_port_fails_before_setup_or_imports(self):
        url = "http://127.0.0.1:28890/metrics"
        args = SimpleNamespace(probe_window=10000, metrics_url=url, request_timeout=1800)
        with patch.object(CONFORMANCE, "parse_args", return_value=args), patch.object(
            CONFORMANCE.urllib.request,
            "urlopen",
            side_effect=urllib.error.HTTPError(url, 404, "Not Found", {}, None),
        ) as request, patch.object(CONFORMANCE, "ensure_setup") as setup:
            with self.assertRaisesRegex(SystemExit, "separate operations listener"):
                CONFORMANCE.main()
            request.assert_called_once_with(url, timeout=10)
            setup.assert_not_called()


if __name__ == "__main__":
    unittest.main()
