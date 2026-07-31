#!/usr/bin/env python3
"""Compare diagnostic Explorer reports with the required native sbol-db row."""

from __future__ import annotations

import argparse
import json
from pathlib import Path
import sys


MIN_EXACT_FIRST_PAGE_RATIO = 0.90
MAX_COUNT_DIFFERENCE_PROBES = 2
MAX_ABSOLUTE_COUNT_DELTA = 1


def status_class(status: int | None) -> int | None:
    return status // 100 if status is not None else None


def result_signature(result: dict[str, object]) -> tuple[object, ...]:
    return (
        result.get("uri_path"),
        result.get("displayId"),
        result.get("name"),
        result.get("type"),
        result.get("version"),
    )


def signature_sort_key(signature: tuple[object, ...]) -> tuple[str, ...]:
    return tuple("" if value is None else str(value) for value in signature)


def keyed(report: dict[str, object], field: str) -> dict[str, dict[str, object]]:
    return {str(entry["path"]): entry for entry in report[field]}


def evaluate_parity(
    corpus_equal: bool,
    baseline_gate_passed: bool,
    candidate_gate_passed: bool,
    summary: dict[str, int | float],
    probe_differences: list[dict[str, object]],
) -> dict[str, object]:
    """Apply a pinned-corpus compatibility policy without requiring ES order.

    sbol-db and SBOLExplorer use different ranking implementations, so exact
    top-ten order is diagnostic evidence rather than a compatibility gate. The
    policy does require almost all complete first pages to agree, tightly
    bounds count drift, and independently requires both lifecycle suites.
    """
    compared = int(summary["compared_probe_count"])
    exact = int(summary["exact_first_page_parity_count"])
    exact_ratio = exact / compared if compared else 0.0
    count_deltas = [
        abs(int(difference["baseline_count"]) - int(difference["candidate_count"]))
        for difference in probe_differences
        if difference.get("baseline_count") is not None
        and difference.get("candidate_count") is not None
        and difference["baseline_count"] != difference["candidate_count"]
    ]
    max_count_delta = max(count_deltas, default=0)
    checks = {
        "same_pinned_corpus": corpus_equal,
        "baseline_conformance_passed": baseline_gate_passed,
        "candidate_conformance_passed": candidate_gate_passed,
        "submission_outcomes_identical": summary["submission_difference_count"] == 0,
        "exact_first_page_ratio_at_least_90_percent": (
            exact_ratio >= MIN_EXACT_FIRST_PAGE_RATIO
        ),
        "count_difference_probes_at_most_2": (
            summary["count_difference_count"] <= MAX_COUNT_DIFFERENCE_PROBES
        ),
        "maximum_count_delta_at_most_1": max_count_delta <= MAX_ABSOLUTE_COUNT_DELTA,
    }
    return {
        "checks": checks,
        "passed": all(checks.values()),
        "observed": {
            "exact_first_page_ratio": exact_ratio,
            "maximum_count_delta": max_count_delta,
        },
        "policy": {
            "minimum_exact_first_page_ratio": MIN_EXACT_FIRST_PAGE_RATIO,
            "maximum_count_difference_probes": MAX_COUNT_DIFFERENCE_PROBES,
            "maximum_absolute_count_delta": MAX_ABSOLUTE_COUNT_DELTA,
            "top_10_order_is_diagnostic_only": True,
        },
    }


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--baseline", type=Path, required=True)
    parser.add_argument("--candidate", type=Path, required=True)
    parser.add_argument("--output", type=Path, required=True)
    args = parser.parse_args()

    baseline = json.loads(args.baseline.read_text())
    candidate = json.loads(args.candidate.read_text())
    corpus_equal = all(
        baseline["corpus"].get(key) == candidate["corpus"].get(key)
        for key in (
            "revision",
            "discovered_xml_documents",
            "selected_xml_documents",
            "manifest_sha256",
        )
    )

    baseline_submissions = keyed(baseline, "submissions")
    candidate_submissions = keyed(candidate, "submissions")
    submission_differences = []
    for path in sorted(set(baseline_submissions) | set(candidate_submissions)):
        left = baseline_submissions.get(path, {})
        right = candidate_submissions.get(path, {})
        if status_class(left.get("status")) != status_class(right.get("status")):
            submission_differences.append(
                {
                    "path": path,
                    "baseline_status": left.get("status"),
                    "candidate_status": right.get("status"),
                }
            )

    baseline_probes = keyed(baseline, "probes")
    candidate_probes = keyed(candidate, "probes")
    compared_probe_paths = set(baseline_probes) | set(candidate_probes)
    probe_differences = []
    for path in sorted(compared_probe_paths):
        left = baseline_probes.get(path, {})
        right = candidate_probes.get(path, {})
        left_set = {result_signature(row) for row in left.get("results", [])}
        right_set = {result_signature(row) for row in right.get("results", [])}
        if left.get("count") != right.get("count") or left_set != right_set:
            probe_differences.append(
                {
                    "path": path,
                    "baseline_count": left.get("count"),
                    "candidate_count": right.get("count"),
                    "only_baseline": [
                        list(item)
                        for item in sorted(
                            left_set - right_set, key=signature_sort_key
                        )
                    ],
                    "only_candidate": [
                        list(item)
                        for item in sorted(
                            right_set - left_set, key=signature_sort_key
                        )
                    ],
                    "top_10_order_equal": [
                        result_signature(row) for row in left.get("results", [])[:10]
                    ]
                    == [result_signature(row) for row in right.get("results", [])[:10]],
                }
            )

    summary = {
        "submission_difference_count": len(submission_differences),
        "compared_probe_count": len(compared_probe_paths),
        "exact_first_page_parity_count": len(compared_probe_paths)
        - len(probe_differences),
        "probe_difference_count": len(probe_differences),
        "count_difference_count": sum(
            difference["baseline_count"] != difference["candidate_count"]
            for difference in probe_differences
        ),
        "top_10_order_difference_count": sum(
            difference["top_10_order_equal"] is False
            for difference in probe_differences
        ),
    }
    parity_gate = evaluate_parity(
        corpus_equal,
        baseline["gate"]["passed"],
        candidate["gate"]["passed"],
        summary,
        probe_differences,
    )
    output = {
        "schema_version": 3,
        "baseline": baseline["topology"],
        "candidate": candidate["topology"],
        "corpus_equal": corpus_equal,
        "submission_outcome_differences": submission_differences,
        "probe_differences": probe_differences,
        "summary": summary,
        "baseline_gate": baseline["gate"],
        "candidate_gate": candidate["gate"],
        "parity_gate": parity_gate,
        "passed": parity_gate["passed"],
        "note": (
            "Exact top-ten order remains diagnostic because the implementations "
            "use different rankers; lifecycle, result coverage, first-page set "
            "agreement, and tightly bounded count drift are required."
        ),
    }
    args.output.parent.mkdir(parents=True, exist_ok=True)
    args.output.write_text(json.dumps(output, indent=2, sort_keys=True) + "\n")
    print(
        json.dumps(
            {
                "passed": output["passed"],
                "corpus_equal": corpus_equal,
                "baseline_gate_passed": baseline["gate"]["passed"],
                "candidate_gate_passed": candidate["gate"]["passed"],
                "parity_gate": parity_gate,
                "summary": summary,
            },
            indent=2,
            sort_keys=True,
        )
    )
    return 0 if output["passed"] else 1


if __name__ == "__main__":
    sys.exit(main())
