#!/usr/bin/env python3
"""Exercise one classic-SynBioHub search topology and write an evidence report.

The required mode attempts every XML file in the pinned SBOLTestSuite SBOL2
corpus.  ``--smoke`` is intentionally diagnostic: it never satisfies the full
coverage gate, even when all of its checks pass.
"""

from __future__ import annotations

import argparse
import dataclasses
import datetime as dt
import hashlib
import json
import mimetypes
import os
from pathlib import Path
import re
import subprocess
import sys
import time
from typing import Iterable
import urllib.error
import urllib.parse
import urllib.request
import uuid
import xml.etree.ElementTree as ET


HERE = Path(__file__).resolve().parent
MANIFEST_PATH = HERE / "backend-manifest.json"
TOP_LEVELS = {
    "Activity",
    "Agent",
    "Attachment",
    "Collection",
    "CombinatorialDerivation",
    "ComponentDefinition",
    "Experiment",
    "ExperimentalData",
    "GenericTopLevel",
    "Implementation",
    "Model",
    "ModuleDefinition",
    "Plan",
    "Sequence",
}
RDF_ABOUT = "{http://www.w3.org/1999/02/22-rdf-syntax-ns#}about"
RDF_RESOURCE = "{http://www.w3.org/1999/02/22-rdf-syntax-ns#}resource"
SMOKE_PATHS = (
    "SBOL2/BBa_I0462.xml",
    "SBOL2_bp/BBa_T9002.xml",
    "SBOL2_ic/CollectionOutput.xml",
    "SBOL2_nc/BBa_I0462_orig.xml",
)


@dataclasses.dataclass
class Response:
    status: int
    headers: dict[str, str]
    body: bytes

    @property
    def text(self) -> str:
        return self.body.decode("utf-8", errors="replace")


class Client:
    def __init__(self, base_url: str, timeout: float):
        self.base_url = base_url.rstrip("/") + "/"
        self.timeout = timeout

    def request(
        self,
        method: str,
        path: str,
        *,
        headers: dict[str, str] | None = None,
        fields: dict[str, str] | None = None,
        files: dict[str, Path] | None = None,
    ) -> Response:
        request_headers = dict(headers or {})
        data: bytes | None = None
        if files:
            data, content_type = encode_multipart(fields or {}, files)
            request_headers["Content-Type"] = content_type
        elif fields is not None:
            data = urllib.parse.urlencode(fields).encode()
            request_headers["Content-Type"] = "application/x-www-form-urlencoded"
        url = urllib.parse.urljoin(self.base_url, path.lstrip("/"))
        req = urllib.request.Request(
            url, data=data, headers=request_headers, method=method.upper()
        )
        try:
            with urllib.request.urlopen(req, timeout=self.timeout) as result:
                return Response(
                    result.status, dict(result.headers.items()), result.read()
                )
        except urllib.error.HTTPError as error:
            return Response(error.code, dict(error.headers.items()), error.read())


def encode_multipart(
    fields: dict[str, str], files: dict[str, Path]
) -> tuple[bytes, str]:
    boundary = "----sbh-search-" + uuid.uuid4().hex
    chunks: list[bytes] = []
    for name, value in fields.items():
        chunks.extend(
            [
                f"--{boundary}\r\n".encode(),
                f'Content-Disposition: form-data; name="{name}"\r\n\r\n'.encode(),
                str(value).encode(),
                b"\r\n",
            ]
        )
    for name, path in files.items():
        content_type = mimetypes.guess_type(path.name)[0] or "application/octet-stream"
        chunks.extend(
            [
                f"--{boundary}\r\n".encode(),
                (
                    f'Content-Disposition: form-data; name="{name}"; '
                    f'filename="{path.name}"\r\n'
                ).encode(),
                f"Content-Type: {content_type}\r\n\r\n".encode(),
                path.read_bytes(),
                b"\r\n",
            ]
        )
    chunks.append(f"--{boundary}--\r\n".encode())
    return b"".join(chunks), f"multipart/form-data; boundary={boundary}"


def sha256(path: Path) -> str:
    digest = hashlib.sha256()
    with path.open("rb") as stream:
        for block in iter(lambda: stream.read(1024 * 1024), b""):
            digest.update(block)
    return digest.hexdigest()


def git_revision(path: Path) -> str:
    return subprocess.check_output(
        ["git", "-C", str(path), "rev-parse", "HEAD"], text=True
    ).strip()


def local_name(tag: str) -> str:
    return tag.rsplit("}", 1)[-1]


def probe_display_id(path: Path) -> str | None:
    try:
        root = ET.parse(path).getroot()
    except ET.ParseError:
        return None
    for element in root.iter():
        if local_name(element.tag) not in TOP_LEVELS:
            continue
        for child in element:
            if local_name(child.tag) == "displayId" and child.text:
                value = child.text.strip()
                if value:
                    return value
    return None


def corpus_files(root: Path, directories: Iterable[str]) -> list[Path]:
    return sorted(
        path
        for directory in directories
        for path in (root / directory).rglob("*.xml")
        if path.is_file()
    )


def token_headers(token: str | None, accept: str = "text/plain") -> dict[str, str]:
    headers = {"Accept": accept}
    if token:
        headers["X-authorization"] = token
    return headers


def login(client: Client, username: str, password: str) -> str | None:
    response = client.request(
        "POST",
        "/login",
        headers={"Accept": "text/plain"},
        fields={"email": username, "password": password},
    )
    token = response.text.strip()
    if response.status == 200 and re.fullmatch(
        r"[0-9a-fA-F]{8}(?:-[0-9a-fA-F]{4}){3}-[0-9a-fA-F]{12}", token
    ):
        return token
    return None


def ensure_setup(client: Client, args: argparse.Namespace) -> str:
    token = login(client, args.username, args.password)
    if token:
        return token
    response = client.request(
        "POST",
        "/setup",
        headers={"Accept": "text/plain"},
        fields={
            "userName": args.username,
            "userFullName": "Search Backend Test User",
            "userEmail": args.email,
            "userPassword": args.password,
            "userPasswordConfirm": args.password,
            "instanceName": f"Search backend evaluation: {args.topology}",
            "instanceUrl": client.base_url,
            "uriPrefix": client.base_url,
            "color": "#D25627",
            "frontPageText": "search-backend-evaluation",
            "virtuosoINI": "/etc/virtuoso-opensource-7/virtuoso.ini",
            "virtuosoDB": "/var/lib/virtuoso-opensource-7/db",
            "allowPublicSignup": "true",
        },
    )
    if response.status != 200 or "Setup Successful" not in response.text:
        raise RuntimeError(
            f"unable to initialize instance: HTTP {response.status}: {response.text[:500]}"
        )
    token = login(client, args.username, args.password)
    if not token:
        raise RuntimeError("setup succeeded but login did not return an API token")
    return token


def submit(
    client: Client,
    token: str,
    source: Path,
    collection_id: str,
    overwrite: str = "0",
) -> Response:
    return client.request(
        "POST",
        "/submit",
        headers=token_headers(token),
        fields={
            "id": collection_id,
            "version": "1",
            "name": collection_id,
            "description": f"search backend fixture from {source.name}",
            "citations": "",
            "overwrite_merge": overwrite,
        },
        files={"file": source},
    )


def normalized_results(response: Response) -> tuple[list[dict[str, object]], str | None]:
    if response.status != 200:
        return [], f"HTTP {response.status}: {response.text[:500]}"
    try:
        decoded = json.loads(response.text)
    except json.JSONDecodeError as error:
        return [], f"invalid JSON: {error}: {response.text[:500]}"
    if not isinstance(decoded, list):
        return [], f"expected result list, got {type(decoded).__name__}"
    normalized = []
    for row in decoded:
        uri = str(row.get("uri", ""))
        parsed = urllib.parse.urlsplit(uri)
        normalized.append(
            {
                "uri_path": parsed.path or uri,
                "displayId": row.get("displayId", ""),
                "name": row.get("name", ""),
                "type": row.get("type", ""),
                "version": row.get("version", ""),
            }
        )
    return normalized, None


def parse_collection_oracle(
    body: bytes,
    collection_id: str,
    source_display_id: str | None,
) -> dict[str, object]:
    """Select a direct post-import member without consulting search.

    SynBioHub may discard external top levels, prefix generic display IDs, or
    normalize invalid URNs while importing a submission. The collection SBOL
    download is generated from store membership, so it is an independent
    oracle for the object that the search backend should return.
    """
    root = ET.fromstring(body)
    root_display_id = f"{collection_id}_collection"
    descriptions: dict[str, dict[str, str]] = {}
    collection = None
    for element in list(root):
        uri = element.get(RDF_ABOUT)
        if not uri:
            continue
        display_id = next(
            (
                child.text.strip()
                for child in element
                if local_name(child.tag) == "displayId"
                and child.text
                and child.text.strip()
            ),
            "",
        )
        descriptions[uri] = {
            "display_id": display_id,
            "type": local_name(element.tag),
            "uri_path": urllib.parse.urlsplit(uri).path or uri,
        }
        if local_name(element.tag) == "Collection" and display_id == root_display_id:
            collection = element

    if collection is None:
        raise ValueError(f"collection {root_display_id!r} is absent from SBOL download")

    member_uris = [
        child.get(RDF_RESOURCE)
        for child in collection
        if local_name(child.tag) == "member" and child.get(RDF_RESOURCE)
    ]
    collection_segment = f"/{collection_id}/"
    candidates = sorted(
        (
            descriptions[uri]
            for uri in member_uris
            if uri in descriptions
            and descriptions[uri]["display_id"]
            and collection_segment in descriptions[uri]["uri_path"]
            and descriptions[uri]["display_id"] != root_display_id
        ),
        key=lambda member: (
            member["display_id"],
            member["uri_path"],
            member["type"],
        ),
    )
    preferred = None
    if source_display_id:
        preferred = next(
            (
                member
                for member in candidates
                if member["display_id"] == source_display_id
                or member["display_id"].endswith(f"_{source_display_id}")
            ),
            None,
        )
    selected = preferred or (candidates[0] if candidates else None)
    return {
        "member_reference_count": len(member_uris),
        "probeable_member_count": len(candidates),
        "source_display_id": source_display_id,
        "selection": (
            "source-display-id"
            if preferred is not None
            else "deterministic-direct-member" if selected is not None else None
        ),
        "selected": selected,
    }


def discover_imported_member(
    client: Client,
    token: str,
    username: str,
    collection_id: str,
    source_display_id: str | None,
) -> dict[str, object]:
    path = (
        f"/user/{urllib.parse.quote(username, safe='')}/{collection_id}/"
        f"{collection_id}_collection/1/sbol"
    )
    response = client.request(
        "GET", path, headers=token_headers(token, "application/rdf+xml")
    )
    evidence: dict[str, object] = {
        "path": path,
        "status": response.status,
        "bytes": len(response.body),
        "sha256": hashlib.sha256(response.body).hexdigest(),
        "passed": False,
    }
    if response.status != 200:
        evidence["error"] = f"HTTP {response.status}: {response.text[:500]}"
        return evidence
    try:
        evidence.update(
            parse_collection_oracle(response.body, collection_id, source_display_id)
        )
    except (ET.ParseError, ValueError) as error:
        evidence["error"] = str(error)
        return evidence
    evidence["passed"] = True
    return evidence


def search(
    client: Client,
    term: str,
    token: str | None,
    *,
    limit: int = 50,
    offset: int = 0,
) -> dict[str, object]:
    encoded = urllib.parse.quote(term, safe="")
    suffix = "?" + urllib.parse.urlencode({"limit": limit, "offset": offset})
    response = client.request(
        "GET",
        f"/search/{encoded}{suffix}",
        headers=token_headers(token, "application/json"),
    )
    results, error = normalized_results(response)
    count_response = client.request(
        "GET", f"/searchCount/{encoded}{suffix}", headers=token_headers(token)
    )
    count: int | None = None
    if count_response.status == 200:
        try:
            count = int(count_response.text.strip())
        except ValueError:
            pass
    return {
        "term": term,
        "offset": offset,
        "limit": limit,
        "status": response.status,
        "count_status": count_response.status,
        "count": count,
        "results": results,
        "error": error,
    }


def completed_rebuilds(metrics_url: str | None, timeout: float) -> float | None:
    if not metrics_url:
        return None
    try:
        with urllib.request.urlopen(metrics_url, timeout=timeout) as response:
            metrics = response.read().decode("utf-8", errors="replace")
    except (urllib.error.URLError, TimeoutError):
        return None
    total = 0.0
    found = False
    for line in metrics.splitlines():
        if not line.startswith("sbol_db_jobs_completed_total{"):
            continue
        if 'kind="rebuild_search_index"' not in line or 'status="succeeded"' not in line:
            continue
        try:
            total += float(line.rsplit(" ", 1)[1])
            found = True
        except (IndexError, ValueError):
            continue
    return total if found else 0.0


def trigger_and_wait_for_index(
    client: Client,
    token: str,
    metrics_url: str | None,
    timeout: float,
    poll_interval: float,
) -> dict[str, object]:
    baseline_metrics = completed_rebuilds(metrics_url, client.timeout)
    before = client.request(
        "GET", "/admin/explorerIndexingLog", headers=token_headers(token)
    )
    baseline_completions = before.text.count("INDEXING COMPLETED")
    trigger = client.request(
        "POST", "/admin/explorerUpdateIndex", headers=token_headers(token), fields={}
    )
    started = time.monotonic()
    observation = None
    while time.monotonic() - started < timeout:
        current_metrics = completed_rebuilds(metrics_url, client.timeout)
        if (
            baseline_metrics is not None
            and current_metrics is not None
            and current_metrics > baseline_metrics
        ):
            observation = "sbol-db completed-job metric advanced"
            break
        log = client.request(
            "GET", "/admin/explorerIndexingLog", headers=token_headers(token)
        )
        if log.status == 200 and log.text.count("INDEXING COMPLETED") > baseline_completions:
            observation = "SBOLExplorer indexing log completed"
            break
        time.sleep(poll_interval)
    return {
        "trigger_status": trigger.status,
        "trigger_body": trigger.text[:500],
        "baseline_completed_rebuilds": baseline_metrics,
        "baseline_log_completions": baseline_completions,
        "observation": observation,
        "elapsed_seconds": round(time.monotonic() - started, 3),
        "passed": trigger.status == 200 and observation is not None,
    }


def result_has_display_id(probe: dict[str, object], display_id: str) -> bool:
    return any(row.get("displayId") == display_id for row in probe["results"])


def result_has_imported_member(
    probe: dict[str, object], expected_member: dict[str, str]
) -> bool:
    return any(
        row.get("uri_path") == expected_member["uri_path"]
        and row.get("displayId") == expected_member["display_id"]
        for row in probe["results"]
    )


def find_imported_member(
    client: Client,
    token: str,
    expected_member: dict[str, str],
    *,
    window: int,
    page_size: int = 50,
) -> dict[str, object]:
    """Page through a bounded result window without bloating the report.

    ``results`` remains the first page used for backend parity comparison. A
    later exact submission-member match is recorded separately in
    ``matched_result`` with the amount of the ranked window examined.
    """
    term = expected_member["display_id"]
    first_limit = min(page_size, window)
    probe = search(client, term, token, limit=first_limit)
    pages_scanned = 1
    scanned_results = len(probe["results"])
    matching = next(
        (
            row
            for row in probe["results"]
            if result_has_imported_member({"results": [row]}, expected_member)
        ),
        None,
    )

    count = probe.get("count")
    ceiling = min(window, count) if isinstance(count, int) else window
    offset = first_limit
    encoded = urllib.parse.quote(term, safe="")
    while matching is None and offset < ceiling:
        limit = min(page_size, ceiling - offset)
        suffix = "?" + urllib.parse.urlencode({"limit": limit, "offset": offset})
        response = client.request(
            "GET",
            f"/search/{encoded}{suffix}",
            headers=token_headers(token, "application/json"),
        )
        page, error = normalized_results(response)
        pages_scanned += 1
        scanned_results += len(page)
        if error is not None:
            probe["error"] = f"paged search at offset {offset}: {error}"
            break
        matching = next(
            (
                row
                for row in page
                if result_has_imported_member({"results": [row]}, expected_member)
            ),
            None,
        )
        if len(page) < limit:
            break
        offset += limit

    probe["pages_scanned"] = pages_scanned
    probe["scanned_results"] = scanned_results
    probe["matched_result"] = matching
    probe["expected_found"] = matching is not None
    return probe


def run_lifecycle(
    client: Client,
    token: str,
    corpus_root: Path,
    args: argparse.Namespace,
) -> dict[str, object]:
    source = corpus_root / "SBOL2/BBa_I0462.xml"
    private_id = "search_acl_private"
    submitted = submit(client, token, source, private_id)
    first_index = trigger_and_wait_for_index(
        client, token, args.metrics_url, args.index_timeout, args.poll_interval
    )
    private_auth = search(client, "BBa_I0462", token)
    private_anon = search(client, "BBa_I0462", None)

    made_public = client.request(
        "POST",
        f"/user/{args.username}/{private_id}/{private_id}_collection/1/makePublic",
        headers=token_headers(token),
        fields={
            "tabState": "new",
            "id": "search_acl_public",
            "version": "1",
            "name": "search ACL public",
            "description": "public visibility probe",
            "citations": "",
        },
    )
    second_index = trigger_and_wait_for_index(
        client, token, args.metrics_url, args.index_timeout, args.poll_interval
    )
    public_anon = search(client, "BBa_I0462", None)

    remove_source = corpus_root / "SBOL2/Measure.xml"
    remove_probe_id = probe_display_id(remove_source)
    remove_id = "search_remove_private"
    remove_submit = submit(client, token, remove_source, remove_id)
    third_index = trigger_and_wait_for_index(
        client, token, args.metrics_url, args.index_timeout, args.poll_interval
    )
    before_remove = search(client, remove_probe_id or "Measure", token)
    removed = client.request(
        "GET",
        f"/user/{args.username}/{remove_id}/{remove_id}_collection/1/removeCollection",
        headers=token_headers(token),
    )
    fourth_index = trigger_and_wait_for_index(
        client, token, args.metrics_url, args.index_timeout, args.poll_interval
    )
    after_remove = search(client, remove_probe_id or "Measure", token)
    remove_prefix = f"/user/{args.username}/{remove_id}/"
    before_paths = {str(row["uri_path"]) for row in before_remove["results"]}
    after_paths = {str(row["uri_path"]) for row in after_remove["results"]}
    removed_paths = {path for path in before_paths if remove_prefix in path}

    checks = {
        "private_visible_to_owner": result_has_display_id(private_auth, "BBa_I0462"),
        "private_hidden_from_anonymous": not result_has_display_id(
            private_anon, "BBa_I0462"
        ),
        "public_visible_to_anonymous": result_has_display_id(public_anon, "BBa_I0462"),
        "removed_collection_was_indexed": bool(removed_paths),
        "removed_collection_disappeared": removed_paths.isdisjoint(after_paths),
        "all_index_transitions_observed": all(
            step["passed"]
            for step in (first_index, second_index, third_index, fourth_index)
        ),
    }
    return {
        "submit_private_status": submitted.status,
        "make_public_status": made_public.status,
        "submit_remove_status": remove_submit.status,
        "remove_status": removed.status,
        "index_transitions": [first_index, second_index, third_index, fourth_index],
        "private_authenticated": private_auth,
        "private_anonymous": private_anon,
        "public_anonymous": public_anon,
        "before_remove": before_remove,
        "after_remove": after_remove,
        "checks": checks,
        "passed": all(checks.values())
        and submitted.status == 200
        and made_public.status == 200
        and remove_submit.status == 200
        and removed.status == 200,
    }


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser()
    parser.add_argument("--topology", required=True)
    parser.add_argument("--base-url", required=True)
    parser.add_argument("--metrics-url")
    parser.add_argument(
        "--corpus-root",
        type=Path,
        default=Path.home() / "git/SynBioDex/SBOLTestSuite",
    )
    parser.add_argument("--report", type=Path, required=True)
    parser.add_argument(
        "--reprobe-from",
        type=Path,
        help="reuse submissions, lifecycle, and index evidence from a prior report",
    )
    parser.add_argument("--username", default="testuser")
    parser.add_argument("--email", default="test@user.synbiohub")
    parser.add_argument("--password", default="test")
    parser.add_argument("--request-timeout", type=float, default=1800)
    parser.add_argument("--index-timeout", type=float, default=3600)
    parser.add_argument("--poll-interval", type=float, default=5)
    parser.add_argument("--probe-window", type=int, default=10_000)
    parser.add_argument("--smoke", action="store_true")
    parser.add_argument("--skip-lifecycle", action="store_true")
    return parser.parse_args()


def main() -> int:
    args = parse_args()
    if args.probe_window <= 0:
        raise SystemExit("--probe-window must be greater than zero")
    manifest = json.loads(MANIFEST_PATH.read_text())
    if args.topology not in manifest["topologies"]:
        raise SystemExit(f"unknown topology {args.topology!r}")
    expected_revision = manifest["corpus"]["revision"]
    actual_revision = git_revision(args.corpus_root)
    if actual_revision != expected_revision:
        raise SystemExit(
            f"SBOLTestSuite revision drift: expected {expected_revision}, got {actual_revision}"
        )

    discovered = corpus_files(args.corpus_root, manifest["corpus"]["directories"])
    selected = (
        [args.corpus_root / relative for relative in SMOKE_PATHS]
        if args.smoke
        else discovered
    )
    client = Client(args.base_url, args.request_timeout)
    token = ensure_setup(client, args)

    reprobed_from_run_id = None
    if args.reprobe_from:
        prior = json.loads(args.reprobe_from.read_text())
        expected_mode = "smoke" if args.smoke else "full"
        if prior.get("topology") != args.topology or prior.get("mode") != expected_mode:
            raise SystemExit("reprobe report topology or mode does not match this run")
        prior_corpus = prior.get("corpus", {})
        if (
            prior_corpus.get("revision") != actual_revision
            or prior_corpus.get("selected_xml_documents") != len(selected)
        ):
            raise SystemExit("reprobe report corpus does not match the pinned selection")
        lifecycle = prior["lifecycle"]
        submissions = prior["submissions"]
        final_index = prior["final_index"]
        transport_errors = list(prior.get("transport_errors", []))
        reprobed_from_run_id = prior.get("run_id")
    else:
        lifecycle = (
            {"skipped": True, "passed": False}
            if args.skip_lifecycle
            else run_lifecycle(client, token, args.corpus_root, args)
        )
        submissions = []
        transport_errors = []
        for index, path in enumerate(selected):
            relative = path.relative_to(args.corpus_root).as_posix()
            digest = sha256(path)
            entry: dict[str, object] = {
                "path": relative,
                "sha256": digest,
                "bytes": path.stat().st_size,
                "source_probe_display_id": probe_display_id(path),
                "collection_id": f"corpus_{index:04d}_{digest[:8]}",
            }
            try:
                response = submit(client, token, path, str(entry["collection_id"]))
                entry.update(
                    {
                        "status": response.status,
                        "accepted": response.status == 200,
                        "response": response.text[:1000],
                    }
                )
            except (urllib.error.URLError, TimeoutError, OSError) as error:
                entry.update(
                    {"status": None, "accepted": False, "transport_error": str(error)}
                )
                transport_errors.append({"path": relative, "error": str(error)})
            submissions.append(entry)
            print(
                f"[{index + 1}/{len(selected)}] {relative}: {entry.get('status')}",
                flush=True,
            )

        final_index = trigger_and_wait_for_index(
            client, token, args.metrics_url, args.index_timeout, args.poll_interval
        )
    probes = []
    for entry in submissions:
        if not entry.get("accepted"):
            continue
        source_display_id = entry.get(
            "source_probe_display_id", entry.get("probe_display_id")
        )
        try:
            oracle = discover_imported_member(
                client,
                token,
                args.username,
                str(entry["collection_id"]),
                str(source_display_id) if source_display_id else None,
            )
            entry["collection_oracle"] = oracle
            expected_member = oracle.get("selected")
            if not isinstance(expected_member, dict):
                continue
            probe = find_imported_member(
                client,
                token,
                expected_member,
                window=args.probe_window,
            )
            probe["path"] = entry["path"]
            probe["source_display_id"] = source_display_id
            probe["expected_member"] = expected_member
            probe["collection_id"] = entry["collection_id"]
            probes.append(probe)
        except (urllib.error.URLError, TimeoutError, OSError) as error:
            transport_errors.append({"path": entry["path"], "error": str(error)})

    accepted_with_probe = [
        entry
        for entry in submissions
        if entry.get("accepted")
        and isinstance(entry.get("collection_oracle"), dict)
        and isinstance(entry["collection_oracle"].get("selected"), dict)
    ]
    accepted = [entry for entry in submissions if entry.get("accepted")]
    full_coverage = not args.smoke and len(selected) == len(discovered)
    checks = {
        "full_corpus_coverage": full_coverage,
        "pinned_corpus_revision": actual_revision == expected_revision,
        "no_transport_errors": not transport_errors,
        "lifecycle_acl_and_removal": lifecycle.get("passed") is True,
        "final_index_completion_observed": final_index["passed"],
        "all_collection_oracles_succeeded": all(
            isinstance(entry.get("collection_oracle"), dict)
            and entry["collection_oracle"].get("passed") is True
            for entry in accepted
        ),
        "one_probe_per_accepted_probeable_document": len(probes)
        == len(accepted_with_probe),
        "all_expected_imported_members_found": all(
            probe["expected_found"] for probe in probes
        ),
        "all_probe_requests_succeeded": all(
            probe["status"] == 200
            and probe["count_status"] == 200
            and probe["error"] is None
            for probe in probes
        ),
    }
    required = bool(manifest["topologies"][args.topology]["required"])
    report = {
        "schema_version": 2,
        "run_id": str(uuid.uuid4()),
        "created_at": dt.datetime.now(dt.timezone.utc).isoformat(),
        "topology": args.topology,
        "topology_contract": manifest["topologies"][args.topology],
        "required": required,
        "mode": "smoke" if args.smoke else "full",
        "base_url": client.base_url,
        "metrics_url": args.metrics_url,
        "probe_window": args.probe_window,
        "reprobed_from_run_id": reprobed_from_run_id,
        "corpus": {
            "revision": actual_revision,
            "expected_revision": expected_revision,
            "directories": manifest["corpus"]["directories"],
            "discovered_xml_documents": len(discovered),
            "selected_xml_documents": len(selected),
            "manifest_sha256": hashlib.sha256(
                "".join(sha256(path) for path in discovered).encode()
            ).hexdigest(),
        },
        "lifecycle": lifecycle,
        "submissions": submissions,
        "final_index": final_index,
        "probes": probes,
        "transport_errors": transport_errors,
        "gate": {"checks": checks, "passed": all(checks.values())},
    }
    args.report.parent.mkdir(parents=True, exist_ok=True)
    args.report.write_text(json.dumps(report, indent=2, sort_keys=True) + "\n")
    print(json.dumps(report["gate"], indent=2, sort_keys=True))
    # Diagnostic baselines always report their evidence without blocking CI.
    return 1 if required and not report["gate"]["passed"] else 0


if __name__ == "__main__":
    sys.exit(main())
