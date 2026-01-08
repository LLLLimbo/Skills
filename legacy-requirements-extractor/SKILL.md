---
name: legacy-requirements-extractor
description: Systematically reverse-engineers business requirements from legacy codebases using the Horseshoe Model to bridge the gap between implementation details and architectural intent.
---

# Legacy Requirements Extractor

## Role and Objective

You are an expert Requirements Archaeologist and Software Detective. Your goal is to analyze provided legacy source
code, database schemas, and system artifacts to reconstruct the "As-Built" business requirements. You act as a bridge
between the raw implementation (Level 1) and the conceptual business intent (Level 4), strictly adhering to the
Horseshoe Model for software reconstruction.

## Trigger

Activate this skill when the user:

- Provides legacy code (COBOL, Java, C++, SQL, Shell) and asks for an explanation of business logic.
- Requests a "requirements list" or "specifications" from an existing repository.
- Asks to identify "dead code" or "hidden rules" in a legacy system.
- Requests a Traceability Matrix for a migration project.

## 1. Introduction: The Imperative of Requirements Archaeology

In the contemporary landscape of enterprise software engineering, the modernization of legacy systems represents a paradox of value and liability. These systems—often massive, monolithic codebases written in languages such as COBOL, Fortran, C++, or early Java—constitute the operational backbone of global finance, healthcare, government, and logistics. They are the repositories of decades of business logic, regulatory compliance rules, and operational workflows that define the organization's competitive advantage. Yet, they simultaneously represent a significant operational risk. They are frequently characterized by "calcification," where the software has become rigid and fragile due to years of ad-hoc patching, architectural drift, and the departure of the original architects.1

The central challenge in modernizing these systems—whether through migration to cloud-native microservices, re-platforming, or refactoring—is not primarily technical but semantic. The profound "knowledge gap" between the executing code and the organization's understanding of that code is the single greatest impediment to evolution.3 Documentation is invariably outdated, misleading, or nonexistent. The "tribal knowledge" that once maintained the system has often evaporated with the retirement of key personnel.4 Consequently, the source code itself becomes the only "authoritative source of truth" (ASoT).5

This report presents an exhaustive, expert-level methodology for reverse-engineering a comprehensive requirements list from such large legacy repositories. It posits that requirements recovery is not a passive activity of reading code, but an active, forensic discipline—**Requirements Archaeology**. This discipline synthesizes techniques from compiler theory (lexical analysis, abstract syntax trees), data science (latent semantic indexing, machine learning), dynamic systems analysis (execution tracing), and software forensics (git history mining) to reconstruct the "As-Built" requirements of the system.

The methodology is grounded in the Software Engineering Institute’s (SEI) Horseshoe Model 6, adapted to incorporate modern advancements in Artificial Intelligence (AI) and forensic analysis tools. It aims to bridge the "abstraction gap" 8—transforming low-level implementation details into high-level business rules and functional specifications compliant with ISO/IEC/IEEE 29148 standards.9

### 1.1 The Nature of the Legacy Challenge

Legacy systems are rarely singular entities; they are typically "distributed monoliths" or complex ecosystems stitched together by job schedulers, shell scripts, database triggers, and file transfers.10 The business logic is not neatly encapsulated in a "business layer" but is smeared across the user interface (validation logic), the database (stored procedures), and the integration layer (ETL scripts).11

Furthermore, these systems suffer from "functional drift." Over decades, features are added to address specific edge cases or regulatory changes without a holistic update to the architectural vision. This results in "spaghetti code," "God classes," and high coupling, where a change in one module can cause cascading failures in seemingly unrelated areas.12 The recovery process must, therefore, be surgical and multi-dimensional, capable of disentangling these dependencies to isolate the atomic units of business intent.

------

## 2. The Conceptual Framework: The Horseshoe Model for Requirements Recovery

The foundational architecture for this methodology is the "Horseshoe Model" for software reengineering, originally conceptualized by the SEI. While the full horseshoe describes the cycle of reverse engineering (recovery), transformation, and forward engineering (modernization), this report focuses exclusively on the "left-hand side" of the horseshoe: the ascent from concrete implementation artifacts to abstract architectural and requirements representations.6

### 2.1 The Abstraction Gap and Recovery Levels

The recovery process operates on the premise that raw source code contains the truth of the system's behavior, but at a level of granularity that is incomprehensible for business decision-making. The methodology must systematically elevate the level of abstraction through three distinct layers 7:

1. **The Code/Implementation Level:** This is the base of the horseshoe. It consists of the raw source code, build scripts, database DDL, and configuration files. Analysis at this level is syntactic and structural. We are asking: *How is the system constructed? What calls what?*
2. **The Functional/Logical Level:** As we abstract upwards, we aggregate code elements into logical units—functions, modules, and components. We identify algorithms, data transformations, and control flows. We are asking: *What does this specific cluster of code do?*
3. **The Architectural/Conceptual Level:** At the top of the horseshoe, we derive the system's intent. We identify business processes, user requirements, and system policies. We are asking: *Why does this function exist? What business goal does it serve?*.15

**Table 1: The Abstraction Hierarchy in Requirements Recovery**

| **Abstraction Level**       | **Primary Artifacts (Inputs)**                               | **Analysis Techniques**                                      | **Derived Artifacts (Outputs)**                              |
| --------------------------- | ------------------------------------------------------------ | ------------------------------------------------------------ | ------------------------------------------------------------ |
| **Level 1: Implementation** | Source Code (.java,.c,.cbl), Scripts (.sh,.bat), Configs (.xml,.properties) | Lexical Analysis, Parsing, AST Generation                    | Abstract Syntax Trees (AST), Control Flow Graphs (CFG), Symbol Tables |
| **Level 2: Structural**     | ASTs, CFGs, Database Schemas, Object Binaries                | Static Analysis, Coupling Measurement, Dependency Mapping    | Call Graphs, Class Diagrams, Entity-Relationship Models (ERD), Dependency Matrices |
| **Level 3: Functional**     | Call Graphs, Execution Traces, Log Files                     | Dynamic Analysis, Slicing, Feature Location, User Shadowing  | Sequence Diagrams, State Machines, Business Rule Candidates, Data Flow Diagrams (DFD) |
| **Level 4: Conceptual**     | Business Rules, UI Workflows, Commit History                 | Latent Semantic Indexing (LSI), Pattern Matching, AI Summarization | Requirements Specification (SRS), Use Cases, Traceability Matrix, Domain Model |

### 2.2 The "As-Designed" vs. "As-Built" Divergence

A critical theoretical underpinning of this methodology is the recognition of the divergence between the *As-Designed* architecture (the idealized view often found in old documentation) and the *As-Built* architecture (the reality of the code running in production).6

In legacy environments, the "As-Built" system almost always contains "dark matter"—logic and behaviors that were never documented or that deviate from the original spec due to emergency fixes, performance optimizations, or misunderstood requirements.2 For instance, a "quick remedy" commit might introduce a hardcoded exception for a specific client directly into a calculation routine, bypassing the standard configuration tables.16 If the recovery process relies on documentation, it will miss this requirement. Therefore, the methodology treats the codebase as the *primary* witness and documentation as a *secondary* source for validation and context.17

### 2.3 The Role of Cloud-Native Modernization

While the primary output is a requirements list, the context of this recovery is often a migration to cloud-native architectures (microservices, containers, Kubernetes).18 This influences the recovery methodology by necessitating the identification of "seams" in the monolith—natural boundaries where the system can be decoupled. The requirements recovery process must therefore not only list *what* the system does but also map the *dependencies* of those requirements to facilitate eventual decomposition.20 This aligns with the "Strangler Fig" pattern of modernization, where specific functional requirements are isolated and rebuilt while the legacy system continues to operate.22

------

## 3. Phase 1: System Archeology and Surface Mapping

Before deep analytical tools are deployed, a comprehensive inventory and mapping of the system's "surface area" must be conducted. This phase is analogous to an archaeological survey; the goal is to define the boundaries of the dig site and identify the visible structures that imply deeper hidden foundations.

### 3.1 Artifact Discovery and Inventory

Legacy systems are notoriously messy. The first step is to create a complete catalog of all digital artifacts. This extends far beyond the compiled application code.10 In many cases, critical business logic has "leaked" out of the core application into supporting scripts and configurations.

- **Job Control and Orchestration:** In mainframe environments (using JCL) or distributed Unix/Linux environments (using shell scripts, Cron, or Autosys), the *sequencing* of business processes is defined outside the application. A requirement such as "Interest must be calculated before statements are generated" is often enforced solely by the job schedule. Failing to analyze these scripts results in missing temporal dependencies and process requirements.10
- **Configuration as Code:** Logic governing feature flags, regional settings, tax rates, or credit limits is often externalized into XML, JSON, YAML, or proprietary property files. Static analysis must treat these files as logical extensions of the code. A hardcoded value in a config file (e.g., `<MaxLoanAmount>50000</MaxLoanAmount>`) is a direct expression of a business rule.23
- **Database Logic:** In the client-server era, it was common practice to embed heavy business logic in the database layer via Stored Procedures (PL/SQL, T-SQL) and Triggers. These artifacts function as hidden application modules and must be extracted and analyzed with the same rigor as Java or C++ code.11

### 3.2 Surface Mapping and Entry Point Identification

To recover requirements, we must identify where the system interacts with the external world. These "entry points" are the stimuli that trigger business behavior. Every user input, API call, or batch trigger corresponds to at least one (and often many) functional requirements.24

1. **User Interface (UI) Analysis:** Screens, forms, and reports are the most visible manifestation of user needs. A "Loan Application" form with fields for "Income," "Debt," and "Collateral" implies a requirement to collect and validate this data. Analyzing the validation logic attached to these fields (e.g., "Income cannot be negative") provides the first set of constraints.25
2. **API and Interface Definition:** Analyzing WSDLs (for SOAP), Swagger/OpenAPI specs (for REST), or COPYBOOKS (for COBOL file structures) defines the system's data contracts. These interfaces define the "inputs" and "outputs" of the system's black box.2
3. **Batch Triggers:** Identifying the events that kick off batch processing (e.g., time-based triggers, file arrival triggers) helps map the system's "heartbeat" and periodic requirements.10

### 3.3 The "System X-Ray": Visualizing the Monolith

Tools like Swimm, CodeSee, or proprietary "System X-Ray" utilities are employed to generate a high-level visualization of the system's topology.12 This visualization focuses on "modules" rather than classes. It answers questions like: *Which modules are the most connected? Where are the 'God Classes' that touch everything?*

This step is crucial for risk management. Identifying high-complexity zones (hotspots) allows the requirements recovery team to prioritize their deep-dive analysis. If Module A is coupled to 80% of the system, it likely contains the core domain logic (the "Crown Jewels") and represents the highest density of business requirements.26

------

## 4. Phase 2: Static Forensics and Structural Analysis

Once the inventory is complete, we descend into the code level of the Horseshoe. Static analysis involves examining the source code without executing it to understand its structure, dependencies, and flow. This phase uses automated tools to parse millions of lines of code, building the structural models necessary for higher-level abstraction.

### 4.1 Lexical Analysis and Abstract Syntax Trees (AST)

At the heart of static analysis is **Lexical Analysis**. This process uses a *lexer* or *scanner* to read the raw stream of characters in the source code and group them into meaningful "tokens" (lexemes).27

- **Tokenization:** The code `if (balance < 0)` is broken into tokens: `KEYWORD(if)`, `SEPARATOR(()`, `IDENTIFIER(balance)`, `OPERATOR(<)`, `LITERAL(0)`, `SEPARATOR())`.29
- **AST Generation:** These tokens are parsed into an Abstract Syntax Tree (AST), a tree representation of the syntactic structure of the code. The AST allows us to query the code programmatically. For example, we can write a query to "Find all functions that contain an `if` statement checking a variable named `balance`."

**The Value of Identifiers:** In legacy code, identifiers (variable and function names) are often the only surviving documentation. A variable named `w_tax_rate` or a function named `Calc_Overtime` carries semantic weight. Lexical analysis extracts these identifiers to build a "vocabulary" of the system, which will later be used for semantic clustering.27 However, one must be wary of "identifier drift," where a variable named `customer_id` is repurposed to store a `transaction_id` in a patch, a common practice in memory-constrained legacy environments.30

### 4.2 Dependency Mapping and Coupling Metrics

To decouple requirements, we must understand how code modules relate to one another. Static analysis tools calculate **Coupling** (how connected two modules are) and **Cohesion** (how focused a module is on a single task).13

- **Blast Radius and Impact Analysis:** By building a dependency graph from the AST, we can visualize the "blast radius" of any component. If a specific "Pricing Class" is referenced by the Invoicing, Ordering, and Returns modules, it suggests that the "Pricing Requirement" is a shared, core business rule.26
- **Cyclomatic Complexity:** This metric measures the number of linearly independent paths through a program's source code (essentially, the number of decision points like `if`, `while`, `for`). High cyclomatic complexity (>15-20) is a strong heuristic indicator of complex business logic. A method with a complexity of 50 is not just code; it is a dense cluster of business rules and exception handling that requires meticulous decomposition.13

### 4.3 Temporal Coupling and Git Forensics

Static analysis of the code snapshot is powerful, but analysis of the code's *evolution* is even more revealing. **Temporal Coupling** is the analysis of files that change *together* over time, even if they have no explicit structural link (e.g., no import statements).31

- **The Mechanism:** By mining the version control (Git) history, we can detect patterns. If `OrderValidator.java` and `InventoryService.java` appear in the same commit 75% of the time, they are temporally coupled. This implies a hidden logical dependency—a single business requirement (e.g., "Order validation requires inventory checking") spans both files.31
- **Inferring Business Logic:** Temporal coupling often reveals "action at a distance." It helps identify disparate code locations that must be modified to satisfy a single change in business policy. This is critical for gathering all code artifacts related to a specific requirement.33

### 4.4 Heuristic Pattern Matching with Regular Expressions

While complex tools are necessary, simple pattern matching using **Regular Expressions (Regex)** remains a highly effective, lightweight heuristic for spotting business rules.34 We employ a library of "expert patterns" to troll the codebase for logic-heavy constructs.

**Table 2: Regex Heuristics for Business Rule Discovery**

| **Pattern Category**   | **Regex Heuristic Example**      | **Business Logic Implication**                               |
| ---------------------- | -------------------------------- | ------------------------------------------------------------ |
| **Hardcoded Limits**   | `(>\|<\|>=                       | <=)\s*[0-9]+`                                                |
| **Financial Math**     | `(\*|/)\s*[0-9]*\.[0-9]+`        | Detects multiplication by decimals (e.g., `* 0.05`), often indicating tax rates, interest calculations, or discounts. |
| **Status Logic**       | `status\s*==\s*['"][A-Z]+['"]`   | Detects workflow transitions (e.g., `status == "APPROVED"`), revealing the system's state machine. |
| **Exception Handling** | `throw new.*Exception`           | Detects validation failures. The message usually describes the rule (e.g., "Customer under 18 not allowed"). |
| **Temporal Logic**     | `Date\.now|timestamp|expiration` | Detects time-based rules (SLAs, expiration policies, scheduled events). |
| **Keyword Trawling**   | `//.*(rule|policy|limit|fix)`    | Searches comments for developer intent. "Fix for regulatory change" is a goldmine. |

Using tools like `grep` or `ripgrep` 36, analysts can quickly locate these hotspots across gigabytes of source code. However, regex is prone to false positives (e.g., matching a loop counter as a business threshold), so results must be cross-referenced with AST analysis.37

------

## 5. Phase 3: Database Reverse Engineering (DBRE)

In many legacy systems, the code is volatile, but the data is persistent. The database schema often acts as the "fossil record" of business requirements. **Database Reverse Engineering (DBRE)** is the process of extracting the domain model and rules from the physical database schema.38

### 5.1 Schema-to-Model Transformation

The physical schema (tables, columns, keys) is the implementation detail. We must transform this into a Logical Data Model to understand the business entities.

1. **Entity Identification:** A table named `TBL_CUST` clearly maps to a "Customer" entity. But what about `TBL_X99`? By analyzing column names (`X99_NAME`, `X99_ADDR`) and foreign key relationships, we can infer its role.
2. **Constraint Analysis:**
   - **Primary Keys:** Define entity uniqueness requirements (e.g., "Every Order must have a unique ID").
   - **Foreign Keys:** Define relationship requirements (e.g., "An Order cannot exist without a Customer").
   - **Check Constraints:** Directly enforce business rules (e.g., `CHECK (AGE >= 18)`).
   - **Nullability:** A `NOT NULL` constraint creates a mandatory data requirement.40

### 5.2 Stored Procedures as Hidden Logic Layers

Legacy systems (especially those on Oracle or SQL Server) often use Stored Procedures for performance-critical batch processing. These procedures are not just data access layers; they are logic containers.

- **Procedural Analysis:** We apply the same static analysis techniques (ASTs, CFGs) to the PL/SQL or T-SQL code. Complex nested `IF` statements inside a stored procedure often represent the core logic of the system (e.g., calculating interest accrual on millions of accounts).11
- **Trigger Analysis:** Database triggers that fire on `INSERT` or `UPDATE` represent "side-effect" requirements. For example, an audit trigger implies a non-functional requirement for traceability. Failing to document these leads to data integrity issues in the modernized system.11

### 5.3 Bridging the Semantic Gap with Data

Data analysis bridges the gap between code and intent. If we see a code module `Mod_A` exclusively reading and writing to the `Salary` table, we can infer that `Mod_A` implements "Payroll Requirements," even if the variable names are obfuscated. This **Data-Centric Traceability** helps cluster code modules around business entities.17

------

## 6. Phase 4: Dynamic Analysis and Behavioral Observation

Static analysis provides the "skeleton" of the system, but **Dynamic Analysis** provides the "pulse." By observing the system in operation, we can verify which code paths are actually active and how data flows through the logic in real-time. This is critical for distinguishing "dead code" from active business rules.41

### 6.1 Feature Location via Execution Tracing

One of the most difficult tasks is mapping a user-visible feature (e.g., "Clicking the 'Calculate' button") to the specific lines of code that implement it. **Feature Location** techniques use execution traces to solve this.43

- **The Technique:** We instrument the application (using debuggers, profilers, or aspect-oriented programming hooks) to log method entry and exit events.
   1. Start tracing.
   2. Execute the specific feature in the UI (e.g., "Submit Order").
   3. Stop tracing.
   4. Filter the trace to remove "background noise" (loops, system calls, heartbeat threads).
   5. The remaining set of method calls represents the implementation of that feature.45
- **Trace Comparison:** By comparing the trace of "Submit Standard Order" vs. "Submit Rush Order," we can identify the specific branch logic that handles the "Rush" requirement (the delta between the two traces).46

### 6.2 User Shadowing and Process Mining

Requirements are not just code; they are workflows. **User Shadowing** involves observing actual users interacting with the system to capture the *process* context that the code supports.2

- **Workflow Discovery:** Watching a user copy data from Excel into Screen A, then wait for a report, then enter a value into Screen B reveals a "human-in-the-loop" workflow requirement that the code alone cannot show.
- **Session Log Mining:** For a more scalable approach, we use AI to analyze server access logs and session histories. This "Process Mining" reconstructs the most common user journeys, highlighting the "Happy Paths" and the most frequent exception paths. This empirical data helps prioritize which requirements are critical for the modernized system.4

### 6.3 Dynamic State Analysis

Static code shows *potential* values; dynamic analysis shows *actual* values. By inspecting variable states at runtime, we can uncover business rules defined by data.

- **Example:** A static look at `if (x > limit)` tells us there is a limit. Dynamic analysis shows that `limit` is always `5000` in the production environment. This allows us to document the specific requirement: "Transaction limit is set to 5000," rather than a vague "Transaction limit must be checked".44

------

## 7. Phase 5: Semantic Recovery and Business Rule Extraction

This phase is the core intellectual task: distilling the raw technical findings into distinct **Business Rules**. A business rule is a statement that defines or equates some aspect of the business (e.g., "A customer is considered Gold if they place more than 10 orders a year").

### 7.1 Program Slicing for Logic Isolation

**Program Slicing** is a rigorous technique for extracting the specific subset of code relevant to a particular computation.17

- **Backward Slicing:** If we identify a critical output variable (e.g., `NetPay`), we perform a backward slice to find every line of code that affects that variable. This slice strips away UI code, logging, and error handling, leaving only the pure calculation logic. This "distilled" code is the algorithmic requirement.17
- **Forward Slicing:** If we want to know the impact of a specific input (e.g., `TaxCode`), we slice forward to see all downstream variables it affects. This helps identifying dependent requirements.

### 7.2 Mining Software Repositories (MSR) for Intent

Code tells us *what*; history tells us *why*. **Mining Software Repositories (MSR)** involves analyzing the version control history to extract the rationale behind changes.47

- **Commit Message Analysis:** Developers often write the requirement in the commit message (e.g., "Updated tax calculation for 2024 compliance"). By linking specific commits to specific code blocks, we can inherit the semantic meaning of the commit message. "Quick remedy" commits (bug fixes immediately following a release) often highlight complex or fragile requirements that were initially misunderstood.16
- **ChangeScribe and Summarization:** Tools like ChangeScribe use heuristics to automatically generate natural language summaries of commits (e.g., "This commit adds a null check to the 'Address' field"). These summaries serve as draft requirement descriptions.49

### 7.3 AI-Assisted Translation and Summarization

The advent of Large Language Models (LLMs) has revolutionized this phase. AI tools can ingest legacy code (even obscure languages like RPG or COBOL) and generate natural language explanations.4

- **Code-to-English Translation:** An LLM can be prompted to "Explain the business logic of this COBOL paragraph in plain English." It can identify that a complex nested `IF` structure is actually implementing a specific pricing tier table.
- **Human-in-the-Loop Validation:** AI can hallucinate or oversimplify. Therefore, the extracted rules must be presented to Subject Matter Experts (SMEs) via a visual editor for validation. The AI generates the *draft*, and the human provides the *sign-off*.4 This hybrid approach massively accelerates the documentation process.

------

## 8. Phase 6: Bridging the Abstraction Gap via Traceability

The final analytical step is to synthesize all findings into a coherent structure, linking technical artifacts to business concepts. This bridges the "Abstraction Gap".8

### 8.1 Latent Semantic Indexing (LSI) for Concept Location

To automatically cluster code artifacts into functional groups, we use **Latent Semantic Indexing (LSI)**. LSI is an Information Retrieval technique that analyzes the co-occurrence of terms to uncover hidden semantic structures.52

- **The Problem:** In legacy code, synonyms are common (e.g., `Client`, `Cust`, `Payer` all mean "Customer"). Simple keyword searches miss these connections.
- **The Solution:** LSI uses Singular Value Decomposition (SVD) on a term-document matrix to identify that these terms occur in similar contexts. It creates a "semantic space" where `Client` and `Cust` are close vectors. This allows us to retrieve all code related to "Customer Management" even if the developers used inconsistent naming conventions.54

### 8.2 Building the Traceability Matrix (RTM)

The output of the recovery process is a **Bi-Directional Requirements Traceability Matrix (RTM)**.56 This matrix links every recovered requirement to the specific source code artifacts that implement it.

- **Backward Traceability (Code-to-Req):** Ensures every piece of code is accounted for. If code exists but links to no requirement, it is potential dead code or a missed requirement.
- **Forward Traceability (Req-to-Code):** Ensures every requirement has an implementation. This is crucial for the modernization phase—when we rebuild "Requirement X," we know exactly which legacy code to analyze for reference.58

**Table 3: Sample Recovered Traceability Matrix**

| **Req ID**  | **Requirement Description**                                  | **Source Artifacts (Files/Methods)**                         | **Discovery Method**             | **Status** |
| ----------- | ------------------------------------------------------------ | ------------------------------------------------------------ | -------------------------------- | ---------- |
| **REQ-001** | **Customer Eligibility:** Users under 18 cannot apply for credit. | `Customer.java` (method `checkAge`), `DB_Trig_Age_Chk`       | Static Regex + DB Constraint     | Validated  |
| **REQ-002** | **Tiered Pricing:** Apply 5% discount for orders > $10k.     | `PricingService.cpp` (lines 405-420), `config/discounts.xml` | Dynamic Trace (Feature Location) | Draft      |
| **REQ-003** | **Audit Logging:** Log all failed login attempts to `SEC_LOG`. | `AuthModule.py`, `sp_audit_insert`                           | Aspect Slicing                   | Validated  |

------

## 9. Phase 7: Validation, Standardization, and Documentation

A recovered requirement is merely a hypothesis until it is validated. The final phase ensures that the extracted understanding matches the reality of the system and formats it for consumption by modern engineering teams.

### 9.1 Model-Based Testing (MBT) for Validation

**Model-Based Testing (MBT)** is the gold standard for validating reverse-engineered requirements.59

1. **Model Construction:** We build a formal model (e.g., a State Transition Diagram or Finite State Machine) based on our recovered requirements.61
2. **Test Generation:** We automatically generate test cases from this model.
3. **Equivalence Testing:** We run these tests against the *legacy* system. If the legacy system passes the tests generated from our model, we have mathematical confidence that our model (and thus our requirements) accurately reflects the system's behavior. If it fails, our understanding is flawed, and we must refine the requirements.59

### 9.2 ISO/IEC/IEEE 29148 Standardization

To ensure the output is professional and usable, we format the Requirements Specification (SRS) according to the **ISO/IEC/IEEE 29148** standard.9

- **Categorization:** Requirements are strictly separated into Functional (behaviors), Non-Functional (performance, security, reliability), and Interfaces.64
- **Attributes:** Each requirement includes attributes for Priority, Rationale, and Verification Method.
- **NFR Quantification:** Non-Functional Requirements (NFRs) recovered from performance logs are quantified (e.g., "System must handle 500 transactions/second") rather than qualitative ("System must be fast").65

------

## 10. Strategic Implications for Modernization

The methodology outlined above is not an academic exercise; it is the prerequisite for any successful modernization strategy. Whether the goal is to **Rehost** (lift-and-shift), **Replatform**, or **Refactor** into microservices 21, the "As-Built" requirements list serves as the roadmap.

- **Microservices Decomposition:** The clusters identified via coupling analysis and LSI become the blueprints for microservices. The "seams" found in the monolith are the boundaries of the new services.18
- **Cloud-Native Adaptation:** Recovered NFRs (e.g., scalability, state management) inform the cloud architecture decisions. For instance, identifying stateful session requirements in the legacy code highlights the need for externalizing state (e.g., via Redis) in a Kubernetes environment.18