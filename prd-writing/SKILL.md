---
name: prd-writing
description: Write structured, implementation-ready Product Requirements Documents (PRDs) with a reusable, module-based template. Use when asked to draft, refine, review, or standardize PRD documents, feature requirement docs, functional design specs, or product research summaries across any business domain and any tech stack.
---

# PRD Writing

## Outcome

Produce a complete PRD that is clear, testable, and traceable from goals to requirements to acceptance criteria.
Keep the document domain-neutral unless the user provides domain specifics.

## Workflow

1. Clarify scope and constraints.
Collect: feature scope, target users, goals, success metrics, constraints, dependencies, rollout expectations.
If input is incomplete, state assumptions explicitly in the document.

2. Select output depth.
Use full template by default.
Switch to a lightweight PRD only if the user asks for a short version or discovery-phase draft.

3. Build structure from template.
Use `references/prd-template.md` as the baseline.
Keep chapter numbering and section naming stable for readability and review consistency.

4. Fill module details feature by feature.
For each functional module, define:
- User stories and role-based value.
- UX and interaction expectations.
- Functional rules, states, and business logic.
- Edge cases and exception handling.
- Measurable acceptance criteria and performance targets.

5. Run quality gate before finalizing.
Use `references/prd-quality-checklist.md`.
Fix gaps before delivering final output.

## Writing Rules

- Use concrete, testable statements; avoid ambiguous language like "optimize", "friendly", or "fast enough".
- Attach priority to requirements (`P0`/`P1`/`P2`).
- Define acceptance criteria as observable outcomes.
- Separate "what/why" (PRD) from "how" (implementation details), unless user explicitly asks for technical design depth.
- Use exact calendar dates when mentioning milestones or versions.

## Output Requirements

Include these minimum outputs:
1. Document metadata (version, date, owner, status).
2. Problem and objective definition.
3. Requirement source and priority mapping.
4. Module-based functional design sections.
5. Unified non-functional and governance standards.
6. References and change log.

## Resource Usage

- Use `references/prd-template.md` to generate the PRD skeleton.
- Use `references/prd-quality-checklist.md` to validate completeness and quality.
- Reuse the template headings directly unless the user requests a custom structure.

## Response Pattern

When responding, follow this order:
1. State assumptions (if any).
2. Provide the PRD draft in Markdown with stable heading hierarchy.
3. End with a short "Open Questions" section if key inputs are missing.

## Constraints

- Keep the skill independent from any specific business context.
- Keep the skill independent from any specific framework, language, or architecture stack.
- Use placeholders when domain facts are unknown; never fabricate business facts.
