import importlib.util
import json
from pathlib import Path
import subprocess
import sys
import tempfile
import unittest


MODULE_PATH = Path(__file__).with_name("compare-reports.py")
SPEC = importlib.util.spec_from_file_location("compare_reports", MODULE_PATH)
compare_reports = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(compare_reports)


class ParityPolicyTest(unittest.TestCase):
    def test_accepts_substantial_overlap_despite_count_drift(self):
        summary = {
            "compared_probe_count": 284,
            "exact_first_page_parity_count": 261,
            "substantial_first_page_overlap_count": 263,
            "mean_first_page_identity_overlap": 0.94,
            "minimum_first_page_identity_overlap": 0.41,
            "submission_difference_count": 0,
            "count_difference_count": 3,
            "maximum_count_delta": 4,
        }

        gate = compare_reports.evaluate_parity(True, True, True, summary)

        self.assertTrue(gate["passed"])
        self.assertEqual(gate["observed"]["maximum_count_delta"], 4)
        self.assertTrue(gate["policy"]["result_counts_are_diagnostic_only"])

    def test_rejects_widespread_low_result_overlap(self):
        summary = {
            "compared_probe_count": 284,
            "exact_first_page_parity_count": 109,
            "substantial_first_page_overlap_count": 200,
            "mean_first_page_identity_overlap": 0.42,
            "minimum_first_page_identity_overlap": 0.0,
            "submission_difference_count": 0,
            "count_difference_count": 170,
            "maximum_count_delta": 9999,
        }

        gate = compare_reports.evaluate_parity(True, True, True, summary)

        self.assertFalse(gate["passed"])
        self.assertFalse(
            gate["checks"][
                "substantial_first_page_overlap_ratio_at_least_90_percent"
            ]
        )

    def test_jaccard_overlap_uses_result_identity(self):
        left = {"/one", "/two", "/three"}
        right = {"/two", "/three", "/four"}

        self.assertAlmostEqual(compare_reports.jaccard_overlap(left, right), 0.5)
        self.assertEqual(compare_reports.jaccard_overlap(set(), set()), 1.0)

    def test_result_identities_ignore_rendered_metadata(self):
        results = [
            {"uri_path": "/shared", "name": "Explorer name"},
            {"uri_path": "/shared", "name": "sbol-db name"},
            {"uri_path": None, "name": "not an addressable result"},
        ]

        self.assertEqual(compare_reports.result_identities(results), {"/shared"})

    def test_report_comparison_gates_on_overlap_not_count_equality(self):
        corpus = {
            "revision": "pinned",
            "discovered_xml_documents": 1,
            "selected_xml_documents": 1,
            "manifest_sha256": "same",
        }
        baseline = {
            "topology": "explorer",
            "corpus": corpus,
            "submissions": [{"path": "probe.xml", "status": 200}],
            "probes": [
                {
                    "path": "probe.xml",
                    "count": 88,
                    "results": [
                        {"uri_path": "/one"},
                        {"uri_path": "/two"},
                        {"uri_path": "/three"},
                    ],
                }
            ],
            "gate": {"passed": True},
        }
        candidate = {
            **baseline,
            "topology": "sbol-db",
            "probes": [
                {
                    "path": "probe.xml",
                    "count": 84,
                    "results": [
                        {"uri_path": "/two", "name": "different metadata"},
                        {"uri_path": "/three"},
                        {"uri_path": "/four"},
                    ],
                }
            ],
        }

        with tempfile.TemporaryDirectory() as directory:
            baseline_path = Path(directory) / "baseline.json"
            candidate_path = Path(directory) / "candidate.json"
            output_path = Path(directory) / "comparison.json"
            baseline_path.write_text(json.dumps(baseline))
            candidate_path.write_text(json.dumps(candidate))

            completed = subprocess.run(
                [
                    sys.executable,
                    str(MODULE_PATH),
                    "--baseline",
                    str(baseline_path),
                    "--candidate",
                    str(candidate_path),
                    "--output",
                    str(output_path),
                ],
                check=False,
                capture_output=True,
                text=True,
            )
            comparison = json.loads(output_path.read_text())

        self.assertEqual(completed.returncode, 0, completed.stdout + completed.stderr)
        self.assertTrue(comparison["passed"])
        self.assertEqual(comparison["schema_version"], 4)
        self.assertEqual(comparison["summary"]["count_difference_count"], 1)
        self.assertEqual(
            comparison["probe_differences"][0]["first_page_identity_overlap"],
            0.5,
        )


if __name__ == "__main__":
    unittest.main()
