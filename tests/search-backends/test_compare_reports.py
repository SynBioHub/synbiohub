import importlib.util
from pathlib import Path
import unittest


MODULE_PATH = Path(__file__).with_name("compare-reports.py")
SPEC = importlib.util.spec_from_file_location("compare_reports", MODULE_PATH)
compare_reports = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(compare_reports)


class ParityPolicyTest(unittest.TestCase):
    def test_accepts_current_compatibility_boundary(self):
        summary = {
            "compared_probe_count": 284,
            "exact_first_page_parity_count": 261,
            "submission_difference_count": 0,
            "count_difference_count": 2,
        }
        differences = [
            {"baseline_count": 3, "candidate_count": 4},
            {"baseline_count": 3, "candidate_count": 4},
        ]

        gate = compare_reports.evaluate_parity(True, True, True, summary, differences)

        self.assertTrue(gate["passed"])
        self.assertEqual(gate["observed"]["maximum_count_delta"], 1)

    def test_rejects_tokenizer_style_count_explosion(self):
        summary = {
            "compared_probe_count": 284,
            "exact_first_page_parity_count": 109,
            "submission_difference_count": 0,
            "count_difference_count": 170,
        }
        differences = [{"baseline_count": 1, "candidate_count": 10000}]

        gate = compare_reports.evaluate_parity(True, True, True, summary, differences)

        self.assertFalse(gate["passed"])
        self.assertFalse(gate["checks"]["maximum_count_delta_at_most_1"])


if __name__ == "__main__":
    unittest.main()
