# Exit-stairs victory across three floors

ADR 0001 made **Victory** depend on fully exploring all 64 **Rooms**, which often turned into mop-up rather than a directed goal. We wanted descent-style progression without new monster AI.

**Decision:** A **Run** spans **3 Floors**. Each **Floor** generates a new **Dungeon**. The **Entry Room** (center, fixed) is a safe hub; the **Exit Room** is the **Room** at maximum **Room path distance** from the **Entry Room** (BFS on the room graph, random tie-break). The **Exit Room** uses an open layout and a 2×2 **Exit stairs** footprint. Stepping onto stairs instantly triggers **Descent** (Floors 1–2) or **Victory** (Floor 3). HP, **Weapon inventory**, **Round**, and **Monsters killed** persist across **Descent**. HUD shows `Floor N / 3` instead of rooms explored. Stairs use a procedural placeholder until `stairs-down.png` exists.

**Why not the alternatives:**
- **Keep explore-all victory** — conflicts with multi-floor scope; encourages full clearance every floor.
- **Square-level farthest exit** — rock layouts distort distance; room graph matches player mental model.
- **End-turn / monster-phase gate on stairs** — old explore-all tension; instant transition fits leaving the floor.
- **Exit hints** — scope; pure exploration for v1.

**Consequences:** Supersedes ADR 0001. `RoomMazeGenerator` picks **Exit Room** per floor. `GameBoard` regenerates the grid on **Descent** while keeping the **Hero**. Victory no longer runs through `endMonsterTurn`.
