# Template B: Bi-Directional Traceability Matrix (RTM)

| Req ID | Requirement Summary     | Implemented In (File/Module)         | Discovery Method    |
|--------|-------------------------|--------------------------------------|---------------------|
| R-01   | Customer Age Validation | CustomerCheck.java, sp_validate_user | Static Regex (< 18) |
| R-02   | Overtime Calculation    | Payroll.cpp (lines 400-450)          | Logic Slicing       |
| R-03   | Audit Logging           | DB_Trigger_Audit_Update              | DBRE                |