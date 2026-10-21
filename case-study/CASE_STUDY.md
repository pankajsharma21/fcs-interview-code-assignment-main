# Case Study: Strategic Cost Control & Architecture in Fulfillment Operations

This document provides a comprehensive senior-architect and business-consulting analysis for the 5 fulfillment cost control scenarios outlined in the case study. It bridges domain modeling, modern cloud and event-driven architecture, and financial governance to deliver actionable business value.

---

## Scenario 1: Cost Allocation and Tracking

### Situation
The company needs to track and allocate costs accurately across different Warehouses and Stores. The costs include labor, inventory, transportation, and overhead expenses.

### Task Analysis & Architectural Discussion

#### 1. Core Challenges in Cost Tracking in Fulfillment
- **Direct vs. Indirect Overhead Allocation:**
  - *Direct costs* (e.g., picking piece-rate labor, packaging materials, point-to-point freight from Warehouse to Store) can be traced directly to specific product units or store fulfillment runs.
  - *Indirect costs* (e.g., warehouse lease/rent, refrigeration utilities, equipment maintenance, facility supervisory overhead) cannot be directly assigned to a single order and require mathematically sound allocation drivers (such as cubic-meter-days for storage, machine-hour utilization for automation).
- **Split Fulfillment & Multi-Echelon Routing:**
  - When an order for a Store is split across multiple Warehouses (e.g., Warehouse A ships Product 1, Warehouse B ships Product 2), transportation overhead and dock receiving costs multiply. Without granular allocation, one warehouse may artificially appear inefficient simply because it handles fragmented or slower-moving SKUs.
- **Time-Lag & Invoicing Cadence (Accruals vs. Real-Time Telemetry):**
  - Carrier freight bills, monthly electrical bills, and third-party logistics (3PL) invoices typically arrive 30–60 days in arrears. Operational systems need real-time cost estimations for decision-making, requiring an automated reconciliation process between estimated cost accruals and actual audited invoices.

#### 2. Recommended Methodology: Activity-Based Costing (ABC) with Event-Driven Cost Telemetry
Rather than broad, arbitrary percentage-based allocations, we implement **Activity-Based Costing (ABC)** powered by domain events emitted across the fulfillment lifecycle:
- `InboundReceivedEvent`: Allocates dock unloading labor and inspection overhead based on pallet count and weight.
- `InventoryStoredDailySnapshot`: Allocates storage footprint and facility overhead (rent, insurance, HVAC) based on SKU volume ($m^3$) and days in storage.
- `OrderPickedAndPackedEvent`: Allocates direct warehouse labor and packaging materials based on pick complexity (single-item vs. multi-line, fragile, refrigerated).
- `ShipmentDispatchedEvent`: Allocates transit freight based on dimensional weight and distance to destination Store.

```
+------------------+     +--------------------+     +------------------------+
|  Domain Events   | --> |  Cost Allocation   | --> | Granular Cost Ledger   |
| (Pick/Pack/Ship) |     |  Engine (ABC Model)|     | (SKU / Store / WH / BU)|
+------------------+     +--------------------+     +------------------------+
```

#### 3. Stakeholder Discovery Questions & Considerations:
1. **Reporting Granularity:** At what atomic level does the CFO and Head of Operations need cost visibility (per SKU, per order line, per store replenishment wave, or monthly aggregated per Business Unit)?
2. **Shared Transportation Allocation:** For "milk runs" where a single truck delivers freight to three stores from one warehouse, what allocation driver is agreed upon (drop-off sequence distance, cubic volume share, or gross weight share)?
3. **Inventory Valuation Standard:** Does financial accounting mandate FIFO (First-In, First-Out), Moving Average Cost, or Standard Costing for inventory valuation across warehouse nodes?

---

## Scenario 2: Cost Optimization Strategies

### Situation
The company wants to identify and implement cost optimization strategies for its fulfillment operations. The goal is to reduce overall costs without compromising service quality or store SLAs.

### Task Analysis & Architectural Discussion

#### 1. High-Impact Cost Optimization Strategies

| Strategy | Description | Expected Business Outcome |
| :--- | :--- | :--- |
| **Dynamic Inventory Placement (ABC Velocity Profiling)** | Stratify inventory into Fast-moving (Class A, top 20% SKUs generating 80% volume), Medium (Class B), and Slow-moving (Class C). Distribute Class A SKUs to regional warehouses closest to high-density stores; centralize Class C long-tail SKUs. | • 20–35% reduction in last-mile/store transit freight costs.<br>• Faster replenishment turnaround times and reduced stockout rates in stores. |
| **Constraint-Enforced Fulfillment Routing** *(Aligns with Bonus Task)* | Enforce strict fulfillment relationship constraints: max 2 warehouses per product per store, max 3 warehouses per store, and max 5 product types per warehouse. | • Drastically reduces parcel fragmentation and small-batch shipment surcharges.<br>• Consolidates store dock receiving labor by minimizing inbound delivery vehicles. |
| **Consolidated Wave & Batch Replenishment** | Instead of ad-hoc on-demand store dispatches, pool replenishment requirements into scheduled fulfillment waves and optimize vehicle cube utilization (targeting >92% truck fill rate). | • 15–20% decrease in total transport runs.<br>• Reduced carbon footprint and driver detention fees. |
| **Dynamic Carrier & Mode Selection** | Integrate real-time rate card APIs into the dispatch use case to compare dedicated fleet cost vs. contracted common carrier vs. LTL (Less-Than-Truckload) based on volumetric weight and delivery SLA windows. | • Immediate 8–12% freight spend reduction through automated lowest-cost routing compliance. |

#### 2. Identification, Prioritization, and Implementation Framework
- **Step 1: Identify via Cost Pareto Analysis:** Extract the top 20% cost contributors from historical operational data. In retail fulfillment, transportation (45–55%) and labor (25–35%) consistently represent the largest levers.
- **Step 2: Prioritize using an Impact vs. Effort Matrix:**
  - *Quick Wins (High Impact, Low Effort):* Implement fulfillment association rules (Bonus task), enforce minimum order quantities (MOQ) for store transfers, optimize packaging box sizes to reduce dimensional weight charges.
  - *Strategic Initiatives (High Impact, Higher Effort):* Automated warehouse slotting optimization, algorithmic multi-echelon demand sensing, cross-docking facility automation.
- **Step 3: Phased Rollout with Guardrail KPIs:**
  - Run optimization models in **Shadow Mode** (simulating decisions against real order traffic) to validate cost reductions.
  - Roll out to a pilot region (e.g., 5 warehouses, 20 stores).
  - Monitor core guardrail KPIs: **Cost Per Unit Delivered (CPUD)**, **On-Time In-Full (OTIF) Delivery %**, and **Store Stockout Rate**. Cost reductions must never degrade OTIF below 98%.

#### 3. Stakeholder Discovery Questions & Considerations:
1. **SLA Tolerance:** What is the maximum acceptable lead time for store replenishment? If a store can tolerate next-day morning delivery instead of same-day, freight costs drop by over 40%.
2. **Physical Facility Capabilities:** Do regional warehouses have the material handling equipment and staging lanes to support cross-docking and high-density pallet racking?
3. **Supplier Freight Terms:** Are inbound freight costs to warehouses paid by suppliers (FOB Destination) or absorbed by our company (FOB Origin)?

---

## Scenario 3: Integration with Financial Systems

### Situation
The Cost Control Tool needs to integrate with existing financial systems (e.g., SAP ERP, Oracle NetSuite, Microsoft Dynamics 365) to ensure accurate and timely cost data. The integration should support real-time data synchronization and financial reporting.

### Task Analysis & Architectural Discussion

#### 1. Business Importance & Strategic Value
- **Eliminating Shadow Spreadsheets & Data Silos:** Operational teams frequently manage fulfillment budgets in disconnected spreadsheets, while finance relies on delayed General Ledger (GL) entries. Direct integration provides unified cost metrics across executive dashboards.
- **Continuous Margin Visibility (Intraday P&L):** Modern retail requires instantaneous visibility into unit economics. Instead of waiting for monthly books to close (Day +15 post-month), integrated systems deliver near-real-time gross margin and fulfillment cost tracking.
- **Audit Compliance & Governance (SOX, IFRS 15/16, GAAP):** Financial integrations provide immutable audit logs tying operational events (inventory movement, warehouse depreciation, replacement asset transfers) directly to GL journal vouchers.

#### 2. Resilient Integration Architecture for Real-Time Synchronization

```
+-----------------------------------------------------------------------------------+
|                            FULFILLMENT COST ENGINE                                |
|                                                                                   |
|  [Store / Warehouse Resource]                                                     |
|             │                                                                     |
|             ▼ (Atomic Local DB Transaction)                                      |
|  ┌───────────────────────┐       ┌────────────────────────┐                      |
|  │  Business Tables      │  ───> │  Transactional Outbox  │                      |
|  │ (Warehouse/Store/Cost)│       │  (Event Journal Table) │                      |
|  └───────────────────────┘       └───────────┬────────────┘                      |
+──────────────────────────────────────────────┼────────────────────────────────────+
                                               │ (Change Data Capture / Debezium)
                                               ▼
                                    ┌─────────────────────┐
                                    │ Kafka / Cloud Event │
                                    │ Event Streaming Bus │
                                    └──────────┬──────────┘
                                               │
                           ┌───────────────────┴───────────────────┐
                           ▼                                       ▼
               ┌───────────────────────┐               ┌───────────────────────┐
               │ Financial Sync Worker │               │ Dead-Letter Queue     │
               │ (Idempotent ERP API)  │               │ (Retry & Reconciliation│
               └───────────┬───────────┘               └───────────────────────┘
                           │
                           ▼
               ┌───────────────────────┐
               │ SAP / Oracle NetSuite │
               │ General Ledger & AP   │
               └───────────────────────┘
```

#### 3. Key Technical Pillars:
1. **Transactional Outbox Pattern (Dual-Write Prevention):**
   - As demonstrated in **Task 2** with Quarkus transaction-bound events, operational writes and integration events must never fail independently. In a distributed setting, domain changes and outbox records are written in the same local ACID transaction. A CDC connector (e.g., Debezium) streams outbox events to Apache Kafka.
2. **Idempotent Financial Journal Ingestion:**
   - Every financial transaction payload carries a deterministic `idempotencyKey` (e.g., `WH-ACT-${warehouseId}-${date}-${activityCode}`). If network blips cause message retries, the financial system rejects duplicate journal entries.
3. **Automated End-of-Day Reconciliation Engine:**
   - A scheduled batch job (running at midnight) calculates operational debit/credit balances across all warehouses and stores, compares them with General Ledger journal balances in SAP, and automatically flags variances greater than 0.01% for manual exception review.

#### 4. Stakeholder Discovery Questions & Considerations:
1. **System of Record (SoR):** Is the ERP the authoritative master for location master data, chart of accounts, and cost centers, with the Cost Control Tool acting as the operational subsystem?
2. **Posting Cadence:** Does finance require individual transaction-level journal entries in the GL (which can overwhelm ERP capacity), or batched hourly/daily sub-ledger summaries with drill-down audit capabilities?
3. **Handling Offline Periods & Maintenance Windows:** How should the system handle planned ERP maintenance windows without halting operational warehouse shipments? (Buffered event queues provide complete decoupling).

---

## Scenario 4: Budgeting and Forecasting

### Situation
The company needs to develop budgeting and forecasting capabilities for its fulfillment operations. The goal is to predict future costs and allocate resources effectively across locations and capacity tiers.

### Task Analysis & Architectural Discussion

#### 1. Strategic Importance of Fulfillment Forecasting
- **Peak Season Surge Preparedness:** Fulfillment demand is non-linear. Black Friday, Cyber Monday, seasonal promotions, and weather disruptions create severe demand spikes (often 3x–5x baseline volume). Accurate forecasting allows managers to secure temporary warehouse flex space, reserve carrier freight capacity at discounted negotiated rates, and schedule labor shifts without paying exorbitant last-minute overtime or spot-market freight rates.
- **Working Capital & Inventory Carrying Cost Control:** Over-budgeting leads to bloated inventory, excessive storage fees, and working capital lockup. Under-budgeting leads to stockouts, emergency expedited air freight, and missed customer SLAs.

#### 2. Architecture of a Fulfillment Budgeting & Forecasting System

```
+───────────────────────────────────────────────────────────────────────────+
|                       PREDICTIVE FORECASTING ENGINE                       |
+───────────────────────────────────────────────────────────────────────────+
  INPUTS:                                            MODELS:
  • Historical Volume by BU Code                     • Seasonal Decomposition
  • Store Expansion / New Store Openings             • Multi-Variable Regression
  • Promotional Calendar & Marketing Plans           • Capacity Constraint Engine
  • Macro Indices (Fuel Surcharge, Labor Inflation)    (Max Warehouse / Capacity)
                            │                                  │
                            └─────────────────┬────────────────┘
                                              ▼
                                   [ Rolling Forecast ]
                                              │
                      ┌───────────────────────┴───────────────────────┐
                      ▼                                               ▼
           [ "What-If" Simulation ]                       [ Variance Monitoring ]
      • Warehouse Replacement Impact                • Actual vs. Budget vs. Forecast
      • Fuel Price Shock (+20%)                     • Rate / Volume / Efficiency Split
```

#### 3. Key Design Pillars:
1. **Driver-Based Cost Modeling (Rather than Fixed Budgets):**
   - Traditional static annual budgets become obsolete within weeks. Modern fulfillment systems use **Driver-Based Rolling Forecasts** where costs are dynamically calculated as:
     $$\text{Forecasted Cost} = (\text{Volume} \times \text{Labor Rate/Unit}) + (\text{Pallet Days} \times \text{Storage Rate}) + (\text{Shipments} \times \text{Freight Rate})$$
2. **Location & Warehouse Capacity Constraint Integration:**
   - Directly linking the forecast to our application domain: every `Location` has `maxCapacity` and `maxWarehouses`, and each `Warehouse` has a `capacity` limit. If forecasted replenishment volume exceeds a location's cumulative capacity, the system triggers proactive alerts to either license overflow 3PL capacity or initiate a **Warehouse Replacement / Expansion** use case before capacity breaches occur.
3. **Automated Three-Way Variance Analysis:**
   - System decomposes variances between Actual Spend and Budget into three distinct root causes:
     - *Volume Variance:* Did costs rise because store demand was 20% higher than anticipated? (Acceptable operational surge).
     - *Rate Variance:* Did costs rise because carriers introduced unexpected peak fuel surcharges? (Market external factor).
     - *Efficiency Variance:* Did picking labor hours per 1,000 units increase? (Operational inefficiency requiring management intervention).

#### 4. Stakeholder Discovery Questions & Considerations:
1. **Planning Horizon & Refresh Frequency:** What planning cadence is required (12-week rolling operational forecast vs. 3-year strategic capital expenditure plan)?
2. **Promotional Collaboration:** How do merchandising and marketing teams communicate store promotional calendars to fulfillment operations, and how quickly does the forecasting engine ingest these signals?
3. **Labor Flexibility:** What percentage of warehouse labor is fixed/contracted versus flex/temporary agency staff that can be scaled dynamically week-by-week?

---

## Scenario 5: Cost Control in Warehouse Replacement

### Situation
The company is planning to replace an existing Warehouse with a new one. The new Warehouse will reuse the Business Unit Code (`businessUnitCode`) of the old Warehouse. The old Warehouse will be archived (`archivedAt`), but its cost history must be preserved.

### Task Analysis & Architectural Discussion

#### 1. Why Preserving Cost History is Critical
- **Benchmark for ROI & Capital Justification:**
  - When replacing a warehouse (e.g., transitioning from an older, manual facility in `ZWOLLE-001` to a modern, high-capacity automated facility), management justifies the investment based on projected reductions in Cost Per Unit (CPU) and improved energy efficiency.
  - If historical cost data is lost or overwritten, there is zero baseline against which to evaluate whether the replacement delivered its projected payback or whether operational expenses actually increased.
- **Continuous Financial Reporting & Cost Center Continuity:**
  - In enterprise ERP and financial accounting systems, the **Business Unit Code (BU Code)** represents a continuous reporting entity/cost center. Reusing the BU Code ensures uninterrupted financial statements, year-over-year (YoY) comparative reporting, and multi-year trend analysis without requiring accountants to consolidate disjointed reporting entities.
- **Predictive Baseline for Operational Budgeting:**
  - The new warehouse operates in the same geographical territory, serving identical store networks. Historical throughput curves, seasonal peak volume ratios, and localized freight rate histories from the archived warehouse serve as the training baseline for the new facility's operating budget.

#### 2. Domain Modeling & Architectural Implementation

In our domain architecture implemented in **Task 3**, we strictly modeled this separation of concerns:

```java
// Clean separation between Physical Entity Instance (id) and Logical Business Unit (businessUnitCode)
public class Warehouse {
    public Long id;                  // Unique surrogate primary key (differentiates physical sites)
    public String businessUnitCode;  // Shared business unit code (continuous financial reporting)
    public String location;          // Geographical area
    public Integer capacity;         // Site capacity
    public Integer stock;            // Current stock
    public LocalDateTime createdAt;  // Commission date
    public LocalDateTime archivedAt; // Decommission date (null if currently active)
}
```

- When `ReplaceWarehouseUseCase` executes:
  1. The legacy warehouse record is **not deleted**; it is updated with `archivedAt = LocalDateTime.now()`.
  2. A new `Warehouse` record is persisted with a new surrogate `id`, the same `businessUnitCode`, updated capacity, and stock migrated from the old warehouse.
  3. All historical cost ledger entries, pick/pack events, and invoices remain permanently linked to the legacy entity's unique `id`, while reporting queries grouped by `businessUnitCode` provide seamless multi-year cost rollups.

```
+───────────────────────────────────────────────────────────────────────────────+
|                         BUSINESS UNIT CODE: "MWH.001"                         |
+───────────────────────────────────────────────────────────────────────────────+
         │                                                      │
         ▼                                                      ▼
  [ARCHIVED WAREHOUSE]                                   [ACTIVE REPLACEMENT]
  id: 1                                                  id: 4
  location: "ZWOLLE-001"                                 location: "ZWOLLE-001"
  capacity: 100, stock: 10                               capacity: 150, stock: 10
  createdAt: 2024-07-01                                  createdAt: 2026-10-21
  archivedAt: 2026-10-21                                 archivedAt: null
         │                                                      │
         ▼                                                      ▼
  HISTORICAL COST LEDGER                                 CURRENT OPERATIONAL
  (2024-2026 Invoices, Labor, Utilities)                 COST LEDGER (2026+)
```

#### 3. Managing the Cost Aspects of Facility Replacement:
- **Tracking Transition & Decommissioning Costs (Stranded Costs):**
  - Replacing a warehouse involves transitional overlap expenses: dual lease payments during fit-out, freight transfer costs to relocate stock, and site restoration/decommissioning fees.
  - *Best Practice:* Isolate these transition costs into a dedicated project CAPEX/transition cost bucket rather than diluting the operational OPEX budget of the new warehouse.
- **Capacity & Stock Integrity Verification:**
  - In our implementation of `ReplaceWarehouseUseCase`, we rigorously validated that:
    1. The replacement warehouse capacity is strictly greater than or equal to the previous warehouse's stock ($\text{capacity}_{\text{new}} \ge \text{stock}_{\text{old}}$).
    2. The replacement stock precisely matches the previous stock ($\text{stock}_{\text{new}} == \text{stock}_{\text{old}}$) to guarantee zero unexplained inventory shrinkage or ghost inventory during site transition.
    3. The location's cumulative capacity limit is not breached when factoring out the archived facility.

#### 4. Stakeholder Discovery Questions & Considerations:
1. **Physical Asset Depreciation:** How are residual unamortized leasehold improvements or decommissioned capital equipment from the archived warehouse accounted for (accelerated depreciation write-down vs. transfer of capital assets)?
2. **Inventory Cutover Strategy:** Will the replacement occur as a hard "flash cutover" over a weekend, or a phased parallel migration where both facilities run concurrently for several weeks?
3. **Vendor & Carrier Contract Portability:** Are existing logistics carrier contracts, utility service agreements, and municipal permits transferable under the existing Business Unit Code to the new physical site?
