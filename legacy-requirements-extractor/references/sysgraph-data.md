# SysGraph Data Mapping Reference

Use this reference when generating SysGraph analysis payloads from recovered requirements and code artifacts.

## Canonical sources

- Schema: `packages/shared/src/schemas/batch.schema.json`
- Sample payload: `scripts/sample-batch.json`

## Id format

- Format: `Type:namespace:name`
- `Type` must be one of the schema labels (case-sensitive).
- `namespace` is a short scope such as `platform`, `billing`, `web`.
- `name` is a stable slug; prefer lowercase with dashes; avoid spaces.

## Node labels (enum)

BusinessDomain, BusinessCapability, BusinessProcess, BusinessEntity, System, Microservice, Module, CodeClass,
Function, APIEndpoint, FrontendApp, Page, UIComponent, Form, FormField, Database, Table, Column,
InfrastructureResource, Middleware

## Relationship types (enum)

BELONGS_TO, CONTAINS, HAS_CAPABILITY, DEPENDS_ON, IMPORTS, CALLS, IMPLEMENTS, REALIZES, READS_FROM, WRITES_TO,
EXPOSES, CONSUMES, MAPS_TO, TRIGGERS, RENDERS, SUBMITS_TO, FOREIGN_KEY_TO, DEPLOYED_ON, USES_MIDDLEWARE

## Mapping guidance

- Model recovered requirements as `BusinessProcess` or `BusinessCapability` nodes with `description` in properties.
- Link `BusinessDomain -> BusinessCapability` with `HAS_CAPABILITY`.
- Link `BusinessCapability -> BusinessProcess` with `BELONGS_TO` or `REALIZES` (pick one and stay consistent).
- Link `Function`/`CodeClass`/`Microservice -> BusinessProcess` with `REALIZES` or `IMPLEMENTS`.
- Link `APIEndpoint -> BusinessProcess` with `EXPOSES` (provider) and `CONSUMES` (client).
- Link `Table`/`Column -> BusinessEntity` with `MAPS_TO` or `FOREIGN_KEY_TO` (relational constraints).
- Use `CONTAINS` for structural ownership (System -> Microservice -> Module -> CodeClass -> Function).
- Use `DEPENDS_ON` or `IMPORTS` for technical dependencies across layers.

## Source metadata

- Set `properties.source` to `static_analysis`, `runtime_telemetry`, or `manual`.
- For code artifacts, add `properties.source_file` and `properties.source_line` when known.
- For relationships, set `properties.source` based on the evidence.

## Minimal payload checklist

- `batch_id`, `timestamp` (ISO 8601 with Z), `source.analyzer`, `data.nodes`, `data.relationships`
- Ensure every relationship endpoint exists in `nodes` unless `create_missing_refs` is true.

## Example snippet

```json
{
  "nodes": [
    {
      "id": "BusinessProcess:billing:invoice-generation",
      "label": "BusinessProcess",
      "properties": {
        "name": "Generate invoice",
        "source": "static_analysis"
      }
    },
    {
      "id": "Function:billing:buildInvoice",
      "label": "Function",
      "properties": {
        "name": "buildInvoice",
        "source": "static_analysis",
        "source_file": "src/billing/invoice.ts"
      }
    }
  ],
  "relationships": [
    {
      "source_id": "Function:billing:buildInvoice",
      "target_id": "BusinessProcess:billing:invoice-generation",
      "type": "REALIZES",
      "properties": { "source": "static_analysis" }
    }
  ]
}
```
