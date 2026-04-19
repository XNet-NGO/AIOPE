# AIOPE2 Agent Harness Improvement Proposals

This document outlines architectural and functional improvements for the client-side agent harness, based on the analysis of `StreamingOrchestrator.kt`.

## 1. Optimization of Parallel Execution (`PARALLEL_SAFE`)
Currently, the harness uses a static set of tools that can be executed concurrently. To reduce latency during complex agent workflows, we should expand this list.

### Proposed Additions:
- `read_calendar`: Read-only operation.
- `read_contacts`: Read-only operation.
- `device_info`: Static system query.
- `memory_recall`: Read-only database query.

### Benefit:
Allows the agent to gather a complete "environmental snapshot" (location, schedule, contacts, device status) in a single parallel burst, reducing the number of orchestration rounds.

---

## 2. Semantic Context Trimming
The current trimming strategy is a "sliding window" that drops or truncates all but the last 3 tool results. This can lead to "amnesia" where the agent forgets a critical piece of data found in round 1 while performing round 5.

### Proposed Change:
Implement a **Priority-Based Trimming** system:
- **Pinned Results**: Allow the agent to mark a tool result as `pinned` (via a special tag in the response), preventing it from being truncated.
- **Semantic Scoring**: Use a small local embedding or keyword-matching system to keep tool results that are most relevant to the current `goal` string, regardless of their position in the history.

### Benefit:
Maintains long-term coherence in multi-step reasoning chains without blowing out the context window.

---

## 3. Dynamic Tool Provisioning (Just-In-Time Tools)
The agent currently operates with a fixed toolset defined at compile-time. This limits extensibility.

### Proposed Change:
Implement a **Tool Manifest** system where the client can register new tools at runtime (e.g., via a plugin APK or a JSON manifest).
- **Tool Request**: The agent can emit a special `REQUEST_TOOL` signal.
- **Dynamic Injection**: The harness searches available plugins and injects the tool definition into the system prompt for the next round.

### Benefit:
Allows the agent to evolve its capabilities without requiring a full app update.

---

## 4. Rich UI Feedback Loops
The transition between tool calls is currently "silent" until the final response is rendered.

### Proposed Change:
Integrate **Interim State Indicators** into `aiope-ui`:
- **Status Badges**: While a tool is running, the harness pushes a temporary `badge` or `progress` component to the UI (e.g., `Searching Web... 🔄`).
- **Tool-Specific UI**: Instead of just text results, allow tools to return `aiope-ui` fragments directly (e.g., a `table` of search results) that the user can interact with before the agent even finishes its final thought.

### Benefit:
Eliminates the "black box" feeling during long tool-use chains and improves perceived performance.

---

## 5. User-Interruptible Loops
Currently, the `while` loop in `StreamingOrchestrator` runs until completion or max rounds.

### Proposed Change:
Introduce a **Pause/Confirm** mechanism:
- **Confirmation Gates**: For "unsafe" tools (e.g., `delete_sms`, `send_sms`), the harness should automatically inject a `confirm` button via `aiope-ui` and pause the loop until the user approves.

### Benefit:
Prevents catastrophic agent errors and increases user trust.
