# Explore-all victory with survive-the-turn resolution

The game had no win condition—only death and restart. We needed a goal that fits the existing fog-of-war, room-by-room exploration loop without new art or AI systems.

**Decision:** The player wins by fully exploring the dungeon (all 64 Rooms visited). Victory is checked after the Hero ends turn on the turn the last Room was explored, the monster phase runs normally, and the Hero is still alive. Game over takes precedence if the Hero dies during that monster phase. The Victory and Game over screens both show Round count and Monsters killed; the HUD shows Rooms explored (X / 64) during the run. When the final Room is explored, a toast and combat log message warn the player to survive the turn.

**Why not the alternatives:**
- **Boss or exit tile** — requires new generation rules, placement logic, or art; explore-all reuses room exploration already central to every run.
- **Victory on immediate explore** — anticlimactic; a Monster can spawn in the last Room the same turn it is revealed.
- **Victory only after all Monsters dead** — shifts the goal from exploration to cleanup and encourages tedious backtracking.
- **Skip monster phase on victory turn** — removes tension; the player chose to keep Active Monsters acting on the final turn.

**Consequences:** `GameBoard` must track rooms explored, monsters killed, and a post-monster-phase victory check. `exploredAll` (or equivalent) aligns with full fog clearance. A future boss or exit win condition would supersede or complement this ADR, not replace the exploration stat tracking.
