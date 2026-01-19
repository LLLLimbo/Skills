#!/usr/bin/env python3
import argparse
import json
import re
import sys
from typing import Any, Dict, List, Set


LABELS: Set[str] = {
    "BusinessDomain",
    "BusinessCapability",
    "BusinessProcess",
    "BusinessEntity",
    "System",
    "Microservice",
    "Module",
    "CodeClass",
    "Function",
    "APIEndpoint",
    "FrontendApp",
    "Page",
    "UIComponent",
    "Form",
    "FormField",
    "Database",
    "Table",
    "Column",
    "InfrastructureResource",
    "Middleware",
}

REL_TYPES: Set[str] = {
    "BELONGS_TO",
    "CONTAINS",
    "HAS_CAPABILITY",
    "DEPENDS_ON",
    "IMPORTS",
    "CALLS",
    "IMPLEMENTS",
    "REALIZES",
    "READS_FROM",
    "WRITES_TO",
    "EXPOSES",
    "CONSUMES",
    "MAPS_TO",
    "TRIGGERS",
    "RENDERS",
    "SUBMITS_TO",
    "FOREIGN_KEY_TO",
    "DEPLOYED_ON",
    "USES_MIDDLEWARE",
}

SOURCES: Set[str] = {"static_analysis", "runtime_telemetry", "manual"}
ID_RE = re.compile(r"^[A-Za-z]+:[A-Za-z0-9_./-]+:[A-Za-z0-9_.-]+$")


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Validate a SysGraph import or batch payload for basic schema alignment."
    )
    parser.add_argument("path", help="Path to batch JSON file.")
    parser.add_argument(
        "--format",
        choices=["auto", "batch", "import"],
        default="auto",
        help="Payload format: auto-detect, batch, or import.",
    )
    parser.add_argument(
        "--strict",
        action="store_true",
        help="Treat warnings (like missing node references) as errors.",
    )
    return parser.parse_args()


def load_json(path: str) -> Dict[str, Any]:
    with open(path, "r", encoding="utf-8") as handle:
        return json.load(handle)


def ensure_dict(value: Any, name: str, errors: List[str]) -> Dict[str, Any]:
    if not isinstance(value, dict):
        errors.append(f"{name} must be an object")
        return {}
    return value


def ensure_list(value: Any, name: str, errors: List[str]) -> List[Any]:
    if not isinstance(value, list):
        errors.append(f"{name} must be an array")
        return []
    return value


def main() -> int:
    args = parse_args()
    errors: List[str] = []
    warnings: List[str] = []

    payload = load_json(args.path)
    root = ensure_dict(payload, "payload", errors)

    format_hint = args.format
    if format_hint == "auto":
        if isinstance(root.get("data"), dict):
            if any(key in root for key in ("batch_id", "timestamp", "source")):
                format_hint = "batch"
            else:
                format_hint = "import"
        elif "nodes" in root or "relationships" in root:
            format_hint = "import"
        else:
            errors.append("Unable to detect format: expected batch or import structure.")
            format_hint = "import"

    if format_hint == "batch":
        for key in ["batch_id", "timestamp", "source", "data"]:
            if key not in root:
                errors.append(f"Missing required field: {key}")

        source = ensure_dict(root.get("source", {}), "source", errors)
        if "analyzer" not in source or not isinstance(source.get("analyzer"), str):
            errors.append("source.analyzer must be a string")

        data = ensure_dict(root.get("data", {}), "data", errors)
        nodes = ensure_list(data.get("nodes", []), "data.nodes", errors)
        relationships = ensure_list(data.get("relationships", []), "data.relationships", errors)
    else:
        if isinstance(root.get("data"), dict):
            data = ensure_dict(root.get("data", {}), "data", errors)
            nodes = ensure_list(data.get("nodes", []), "data.nodes", errors)
            relationships = ensure_list(data.get("relationships", []), "data.relationships", errors)
        else:
            nodes = ensure_list(root.get("nodes", []), "nodes", errors)
            relationships = ensure_list(root.get("relationships", []), "relationships", errors)

    node_ids: Set[str] = set()
    for idx, node in enumerate(nodes):
        if not isinstance(node, dict):
            errors.append(f"nodes[{idx}] must be an object")
            continue
        node_id = node.get("id")
        label = node.get("label")
        props = node.get("properties")
        if not isinstance(node_id, str) or not ID_RE.match(node_id):
            errors.append(f"nodes[{idx}].id invalid format: {node_id}")
        else:
            node_ids.add(node_id)
        if label not in LABELS:
            errors.append(f"nodes[{idx}].label invalid: {label}")
        if not isinstance(props, dict):
            errors.append(f"nodes[{idx}].properties must be an object")
        elif "name" not in props or not isinstance(props.get("name"), str):
            errors.append(f"nodes[{idx}].properties.name must be a string")
        source_value = props.get("source") if isinstance(props, dict) else None
        if source_value is not None and source_value not in SOURCES:
            errors.append(f"nodes[{idx}].properties.source invalid: {source_value}")

    for idx, rel in enumerate(relationships):
        if not isinstance(rel, dict):
            errors.append(f"relationships[{idx}] must be an object")
            continue
        source_id = rel.get("source_id")
        target_id = rel.get("target_id")
        rel_type = rel.get("type")
        props = rel.get("properties", {})

        if not isinstance(source_id, str):
            errors.append(f"relationships[{idx}].source_id must be a string")
        if not isinstance(target_id, str):
            errors.append(f"relationships[{idx}].target_id must be a string")
        if rel_type not in REL_TYPES:
            errors.append(f"relationships[{idx}].type invalid: {rel_type}")

        if isinstance(source_id, str) and source_id not in node_ids:
            warnings.append(f"relationships[{idx}].source_id not found in nodes: {source_id}")
        if isinstance(target_id, str) and target_id not in node_ids:
            warnings.append(f"relationships[{idx}].target_id not found in nodes: {target_id}")

        if props is not None and not isinstance(props, dict):
            errors.append(f"relationships[{idx}].properties must be an object")
        else:
            rel_source = props.get("source") if isinstance(props, dict) else None
            if rel_source is not None and rel_source not in SOURCES:
                errors.append(f"relationships[{idx}].properties.source invalid: {rel_source}")

    if errors:
        sys.stderr.write("Errors:\n")
        for entry in errors:
            sys.stderr.write(f"- {entry}\n")

    if warnings:
        sys.stderr.write("Warnings:\n")
        for entry in warnings:
            sys.stderr.write(f"- {entry}\n")

    if args.strict and warnings:
        errors.append("Strict mode enabled and warnings present.")

    if errors:
        return 1

    sys.stdout.write(
        f"OK: {len(nodes)} nodes, {len(relationships)} relationships validated (format: {format_hint}).\n"
    )
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
