#!/usr/bin/env python3
import argparse
import json
import sys
from datetime import datetime, timezone


def iso_timestamp() -> str:
    return datetime.now(timezone.utc).replace(microsecond=0).isoformat().replace("+00:00", "Z")


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Scaffold a SysGraph payload (import or batch) for legacy requirements extraction."
    )
    parser.add_argument("--out", help="Write output to a file instead of stdout.")
    parser.add_argument(
        "--format",
        choices=["import", "batch"],
        default="import",
        help="Payload format: import for /data/import, batch for /ingestion/batch.",
    )
    parser.add_argument("--batch-id", dest="batch_id", help="Override batch_id value.")
    parser.add_argument("--analyzer", default="legacy-requirements-extractor")
    parser.add_argument("--version", help="Analyzer version string.")
    parser.add_argument("--repository", help="Repository name or URL.")
    parser.add_argument("--merge-strategy", default="upsert", choices=["insert", "upsert", "replace"])
    parser.add_argument("--no-validate-schema", dest="validate_schema", action="store_false")
    parser.add_argument("--no-create-missing-refs", dest="create_missing_refs", action="store_false")
    parser.add_argument("--dry-run", action="store_true")
    parser.set_defaults(validate_schema=True, create_missing_refs=True)
    return parser.parse_args()


def main() -> int:
    args = parse_args()
    timestamp = iso_timestamp()
    batch_id = args.batch_id or f"legacy-{timestamp.replace(':', '').replace('-', '')}"

    source = {"analyzer": args.analyzer}
    if args.version:
        source["version"] = args.version
    if args.repository:
        source["repository"] = args.repository

    if args.format == "batch":
        payload = {
            "batch_id": batch_id,
            "timestamp": timestamp,
            "source": source,
            "data": {"nodes": [], "relationships": []},
            "options": {
                "merge_strategy": args.merge_strategy,
                "validate_schema": args.validate_schema,
                "create_missing_refs": args.create_missing_refs,
                "dry_run": args.dry_run,
            },
        }
    else:
        payload = {"nodes": [], "relationships": []}

    output = json.dumps(payload, indent=2)
    if args.out:
        with open(args.out, "w", encoding="utf-8") as handle:
            handle.write(output + "\n")
    else:
        sys.stdout.write(output + "\n")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
