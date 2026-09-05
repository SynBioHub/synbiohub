#!/usr/bin/env python3
"""Compare diagnostic Explorer reports with the required native sbol-db row."""

from __future__ import annotations

import argparse
import json
from pathlib import Path
import sys


MIN_FIRST_PAGE_IDENTITY_OVERLAP = 0.50
MIN_SUBSTANTIAL_OVERLAP_PROBE_RATIO = 0.90


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


def result_identities(results: list[dict[str, object]]) -> set[str]:
    """Return stable result identities without comparing rendered metadata."""
    return {
        str(result["uri_path"])
        for result in results
        if result.get("uri_path") is not None
    }


def jaccard_overlap(left: set[str], right: set[str]) -> float:
    """Measure shared identities while penalizing unrelated extra results."""
    if not left and not right:
        return 1.0
    return len(left & right) / len(left | right)


def keyed(report: dict[str, object], field: str) -> dict[str, dict[str, object]]:
    return {str(entry["path"]): entry for entry in report[field]}


def evaluate_parity(
    corpus_equal: bool,
    baseline_gate_passed: bool,
    candidate_gate_passed: bool,
    summary: dict[str, int | float],
) -> dict[str, object]:
    """Apply a pinned-corpus compatibility policy across distinct rankers.

    sbol-db and SBOLExplorer use different ranking implementations, so exact
    result sets, counts, metadata, and top-ten order are diagnostic evidence.
    The compatibility gate instead requires substantial first-page identity
    overlap for almost all probes and independently requires both lifecycle
    suites.
    """
    compared = int(summary["compared_probe_count"])
    exact = int(summary["exact_first_page_parity_count"])
    exact_ratio = exact / compared if compared else 0.0
    substantial = int(summary["substantial_first_page_overlap_count"])
    substantial_ratio = substantial / compared if compared else 0.0
    checks = {
        "same_pinned_corpus": corpus_equal,
        "baseline_conformance_passed": baseline_gate_passed,
        "candidate_conformance_passed": candidate_gate_passed,
        "submission_outcomes_identical": summary["submission_difference_count"] == 0,
        "substantial_first_page_overlap_ratio_at_least_90_percent": (
            substantial_ratio >= MIN_SUBSTANTIAL_OVERLAP_PROBE_RATIO
        ),
    }
    return {
        "checks": checks,
        "passed": all(checks.values()),
        "observed": {
            "exact_first_page_ratio": exact_ratio,
            "substantial_first_page_overlap_ratio": substantial_ratio,
            "mean_first_page_identity_overlap": summary[
                "mean_first_page_identity_overlap"
            ],
            "minimum_first_page_identity_overlap": summary[
                "minimum_first_page_identity_overlap"
            ],
            "maximum_count_delta": summary["maximum_count_delta"],
        },
        "policy": {
            "minimum_first_page_identity_overlap": MIN_FIRST_PAGE_IDENTITY_OVERLAP,
            "minimum_substantial_overlap_probe_ratio": (
                MIN_SUBSTANTIAL_OVERLAP_PROBE_RATIO
            ),
            "exact_result_sets_are_diagnostic_only": True,
            "result_counts_are_diagnostic_only": True,
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
    first_page_overlaps = []
    count_deltas = []
    for path in sorted(compared_probe_paths):
        left = baseline_probes.get(path, {})
        right = candidate_probes.get(path, {})
        left_set = {result_signature(row) for row in left.get("results", [])}
        right_set = {result_signature(row) for row in right.get("results", [])}
        left_identities = result_identities(left.get("results", []))
        right_identities = result_identities(right.get("results", []))
        identity_overlap = jaccard_overlap(left_identities, right_identities)
        first_page_overlaps.append(identity_overlap)
        if left.get("count") is not None and right.get("count") is not None:
            count_deltas.append(abs(int(left["count"]) - int(right["count"])))
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
                    "first_page_identity_overlap": identity_overlap,
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
        "substantial_first_page_overlap_count": sum(
            overlap >= MIN_FIRST_PAGE_IDENTITY_OVERLAP
            for overlap in first_page_overlaps
        ),
        "mean_first_page_identity_overlap": (
            sum(first_page_overlaps) / len(first_page_overlaps)
            if first_page_overlaps
            else 0.0
        ),
        "minimum_first_page_identity_overlap": min(first_page_overlaps, default=0.0),
        "probe_difference_count": len(probe_differences),
        "count_difference_count": sum(
            difference["baseline_count"] != difference["candidate_count"]
            for difference in probe_differences
        ),
        "maximum_count_delta": max(count_deltas, default=0),
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
    )
    output = {
        "schema_version": 4,
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
            "Exact result sets, counts, metadata, and top-ten order remain "
            "diagnostic because the implementations use different rankers; "
            "lifecycle, result coverage, and substantial first-page identity "
            "overlap are required."
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
