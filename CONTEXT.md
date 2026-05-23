# Turn-Based Dungeon Crawler

A single-player, turn-based dungeon crawl on a procedurally generated grid. The player controls one **Hero** exploring fog-covered **Rooms**, fighting **Monsters**, and using **Weapons** and **Items**. One **Run** is one **Dungeon**; the player **wins** by fully exploring the dungeon or **loses** on **Game over**.

## Language

### Space and layout

**Dungeon**:
The entire playable grid for one run (32×32 **Squares** arranged as 8×8 **Rooms**).
_Avoid_: Level, map, board (except when referring to the `GameBoard` class in code)

**Square**:
One cell on the dungeon grid. May hold a wall texture, a **Creature**, or nothing (open floor).
_Avoid_: Tile (overloaded — see Flagged ambiguities)

**Room**:
A fixed 4×4 block of adjacent Squares (64 Squares total across the dungeon). Rooms are connected by carved passages; the dungeon layout is generated at the Room level then expanded to Squares.
_Avoid_: Chamber, zone, tile

**Wall**:
A Square whose texture is solid (non-walkable). All walls share one visual.
_Avoid_: Block, obstacle

**Position**:
An immutable (x, y) coordinate on the Square grid. Origin is bottom-left; Y increases upward.
_Avoid_: Point, cell, tile coords

### Visibility and discovery

**Explored**:
A flag on a Square meaning the player has revealed it. Unexplored Squares render as fog regardless of contents.
_Avoid_: Visible, discovered, lit

**Exploration**:
The act of marking Squares as Explored. When the **Hero** enters a Room, all 16 Squares in that Room become Explored.
_Avoid_: Scouting, fog clearing

**Spawn on explore**:
The rule that the first time any Square in a Room is Explored, that Room gets exactly one spawn attempt (Monster or Item). Each Room spawns at most once per run. When the spawn is a **Monster**, type is chosen uniformly from the full roster (**equal weight** per type).
_Avoid_: Room activation, encounter trigger

**Fully explored dungeon**:
Every Room in the dungeon has been Explored (all 64 Rooms visited by the Hero at least once). Because entering a Room Explores all 16 of its Squares, this equals full fog clearance across the grid. When the Hero explores the last remaining Room, a **toast** and **combat log** entry announce that all rooms are explored and the Hero must survive the turn to win.
_Avoid_: 100% map, cleared dungeon

**Rooms explored** (progress):
Run stat shown in the HUD as `Rooms X / 64` (center panel, below **Round**). Increments when the Hero first **Explores** a Room—i.e. when that Room's Squares become Explored. Re-entering an already-explored Room does not increment again.
_Avoid_: Map percentage, exploration score

### Actors

**Creature**:
Anything with HP, a sprite, and life/death state. Base type for **Hero** and **Monster** (and, in code, **Item**).
_Avoid_: Entity, actor, unit

**Hero**:
The player-controlled Creature. Has movement budget (**MOV**), one attack per **Turn**, and a **Weapon** inventory.
_Avoid_: Player, character, avatar

**Monster**:
An enemy Creature placed by spawn-on-explore. Inactive until **Activated**; once active, takes turns moving toward and attacking the Hero. Sprites load from PNG files in `assets/` (Hero, Troll, Orc, Werewolf, etc.). **Ogre** and **Skeleton** have no art yet—use colored **placeholders** until `ogre.png` / `skeleton.png` are added; stats and AI stay the same after swap.
_Avoid_: Enemy, mob, foe

**Active** (monster):
A Monster that has been Activated (typically because the Hero came **Near** it). Only Active Monsters act during the monster phase.
_Avoid_: Aggroed, awakened, engaged

**Activated**:
The transition when a previously inactive Monster becomes Active. Happens when the Hero is Near that Monster's Position.
_Avoid_: Aggro, wake up

**Item**:
A pickup on the dungeon floor (currently **Heal Potion** only). Auto-collected when the Hero is Near it; consumed via `use(Hero)`. Additional Item types are **deferred**—second content pass after **Victory** and the expanded **Monster** roster ship.
_Avoid_: Loot, pickup, consumable (use Item for the domain concept; consumable describes behavior)

**Heal Potion**:
An Item that restores 1 HP if the Hero is below max HP; otherwise found but not consumed.
_Avoid_: Health potion, red potion

### Combat and equipment

**Monster archetype**:
A design role for a **Monster** type defined by its HP, MOV, and **Weapon** spread—not unique AI. Roster (5 types, **equal weight** spawn):

| Monster | Archetype | HP | MOV | Weapon |
|---------|-----------|----|-----|--------|
| Skeleton | swarm/fast | 1 | 7 | Rusty Blade (65% hit, 1 dmg) |
| Werewolf | fast/fragile | 1 | 6 | Bite (75% hit, 1 dmg) |
| Orc | balanced | 2 | 5 | Mace (80% hit, 1 dmg) |
| Troll | tank/bruiser | 2 | 4 | Big Club (50% hit, 2 dmg) |
| Ogre | heavy tank | 3 | 3 | Hammer (40% hit, 3 dmg) |

_Avoid_: Class, mob type, enemy tier

**Ogre**:
**Monster** archetype heavy tank (HP 3, MOV 3, **Hammer**). Until `ogre.png` exists, rendered as a dark-green placeholder solid `(0.15, 0.35, 0.12)`.
_Avoid_: Giant, brute

**Skeleton**:
**Monster** archetype swarm/fast (HP 1, MOV 7, **Rusty Blade**). Until `skeleton.png` exists, rendered as an off-white placeholder solid `(0.85, 0.82, 0.75)`.
_Avoid_: Undead, ghoul

**Rusty Blade**:
A **Weapon** wielded by **Skeleton**—low accuracy, light damage; fits the swarm/fast archetype.
_Avoid_: Short sword, dagger

**Hammer**:
A **Weapon** wielded by **Ogre**—slow, high damage, low hit chance; fits the heavy tank archetype.
_Avoid_: Maul, warhammer

**Weapon**:
An attack profile: display name, hit chance (0–1), and damage on hit. Hero and Monster each wield one Weapon when attacking.
_Avoid_: Attack type, gear, equipment slot

**Hit**:
An attack roll that succeeds; deals the Weapon's damage. A failed roll is a **Miss** (0 damage).
_Avoid_: Strike, connect

**Miss**:
An attack roll that fails; no damage dealt.
_Avoid_: Dodge, block (those imply different mechanics)

**Near**:
Chebyshev distance ≤ 1 between two Positions (orthogonal or diagonal neighbors). Required for melee attack and item pickup.
_Avoid_: Adjacent (ambiguous — see Flagged ambiguities), in range

**Very near**:
Orthogonally adjacent only (same row or column, distance 1). Used as the pathfinding goal for Monsters closing on the Hero.
_Avoid_: Adjacent, melee range

### Turn structure

**Turn** (hero turn / monster turn):
One side's action phase before the other side acts. Controlled by a hero-turn vs monster-turn alternation—not simultaneous.
_Avoid_: Phase (reserved for sub-steps within a turn if added later)

**Round**:
A numbered cycle that increments when the monster phase ends and a new hero turn begins. Round 1 starts at dungeon creation.
_Avoid_: Turn (overloaded), wave, day

**MOV** (movement points):
How many Square moves the Hero (or Monster) may still make this turn. Hero starts at 8 per hero turn; each move costs 1.
_Avoid_: Speed (used in code/HUD but means MOV in player-facing terms), AP, steps

**Attack action**:
One melee attempt per hero turn (separate from MOV). Ending MOV does not grant extra attacks.
_Avoid_: Swing, strike count

**End turn**:
Hero voluntarily closes their turn; inactive Monsters are skipped, Active Monsters each execute their monster turn (move + attack if Near).
_Avoid_: Pass, wait, skip

### Feedback and run lifecycle

**Combat log**:
A short rolling text strip (last few messages) for hit/miss/damage and round announcements. Persists across the HUD.
_Avoid_: Chat, feed, history

**Toast**:
A transient popup for player-facing events (especially item pickup). Also duplicated into the combat log when triggered via `showToast`.
_Avoid_: Notification, alert, popup

**Victory**:
The Hero achieves a **fully explored dungeon**. After the Hero **ends turn** on the turn the last Room became Explored, the **monster phase runs normally**—**Active** Monsters move and attack. If the Hero is still alive when that monster phase finishes and every Room is Explored, **Victory** triggers. If the Hero dies during that monster phase, **Game over** takes precedence. The **Victory screen** uses the same layout as **Game over** but with the HUD **gold** accent: title “Victory!”, subtitle “The dungeon is fully explored.”, **Round** and **Monsters killed**, gold **Restart** button.
_Avoid_: Win screen, completion, success state

**Monsters killed**:
Run stat: count of **Monsters** the Hero reduced to 0 HP. Incremented once per Monster death from Hero damage (not from other causes—there are none today).
_Avoid_: Kill count, score, KOs

**Game over**:
The Hero's HP reached 0. Input stops except **Restart**. Shows the same run summary as **Victory**: **Round** and **Monsters killed**. A new run creates a fresh Dungeon.
_Avoid_: Death screen, fail state

**Run**:
One play session from dungeon generation until **Victory** or **Game over**.
_Avoid_: Session, game, match

## Flagged ambiguities

| Term | Conflict | Resolution (proposed) |
|------|----------|------------------------|
| **Tile** | Code/UI uses "tile" for Square (`heroClickOnTile`) and readme uses "tile" for Square, while **Room** is the 4×4 unit | Use **Square** for grid cells and **Room** for 4×4 groups in domain language |
| **Adjacent** | Could mean Near (8-direction) or very near (4-direction) | Use **Near** or **very near** explicitly |
| **Speed** | HUD label "MOV" but code field `speed` | Player-facing: **MOV**; code may keep `speed` |
| **Board** | `GameBoard` is the state owner, not a physical object in the dungeon | **Dungeon** for the world; "board" only when discussing code |
| **Item extends Creature** | Items have HP=1 and sit on Squares like Creatures | Domain: Item is a floor pickup, not a combatant—code model is legacy |
| **Assets in repo** | Git tracks only a subset of `assets/`; full PNG/audio set exists locally for Hero and original Monsters | New Monsters follow same filenames (`ogre.png`, `skeleton.png`); placeholders only until those files exist |

## Example dialogue

**Dev**: When the player walks into a new area, what happens?

**Designer**: They **Explore** a **Room**—all 16 **Squares** in that **Room** flip to **Explored**. If it's the first time that **Room** was touched, **spawn on explore** fires once: usually a **Monster**, sometimes a **Heal Potion**.

**Dev**: Does every Monster fight immediately?

**Designer**: No. New Monsters start inactive. When the **Hero** gets **Near** one, it becomes **Active**. On **end turn**, only **Active** Monsters take a **turn**—they path until **very near** the Hero, then attack if **Near**.

**Dev**: What ends a run?

**Designer**: Two outcomes. **Victory** when every Room has been Explored—the Hero must **end turn**, survive the **monster phase**, and still be alive. Win and loss screens both show **Round** and **Monsters killed**; the HUD tracks **Rooms explored** during the run. **Restart** starts a new **Run** with a fresh **Dungeon**.

**Dev**: What's next after the victory loop?

**Designer**: **Content**—two new **Monsters** (Ogre, Skeleton) after the **Victory** loop ships. **Items** come later.
