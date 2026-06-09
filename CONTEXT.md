# Turn-Based Dungeon Crawler

A single-player, turn-based dungeon crawl on procedurally generated grids. The player controls one **Hero** exploring fog-covered **Rooms**, fighting **Monsters**, and using **Weapons** and **Items**. One **Run** spans multiple **Floors**; the **Hero** keeps stats and inventory across **Descent**. The player **wins** by reaching **Exit stairs** on Floor 3 or **loses** on **Game over**.

## Language

### Space and layout

**Dungeon**:
The playable grid for one **Floor** (32×32 **Squares** arranged as 8×8 **Rooms**). Each **Floor** gets a freshly generated **Dungeon** layout.
_Avoid_: Level, map, board (except when referring to the `GameBoard` class in code)

**Floor**:
One **Dungeon** layout in a **Run**. A **Run** has exactly **3 Floors**. The **Hero** starts on Floor 1 and may **Descend** to Floors 2 and 3. Stats, **Weapon inventory**, and run counters persist across floors.
_Avoid_: Level, depth, stage

**Entry Room**:
The fixed center **Room** where the **Hero** spawns on Floor 1 and reappears after each **Descent** on deeper **Floors** (same room coordinates every floor). **Safe hub**: exempt from **spawn on explore**; always auto-**Explored** when the **Hero** arrives (Floor 1 start and every **Descent**). Uses the open **Room layout** regardless of exit count.
_Avoid_: Start room, spawn room

**Descent**:
Moving from the current **Floor** to the next deeper **Floor** via **Exit stairs**. Triggers **instantly** when the **Hero** steps onto a stairs **Square**—current hero turn ends, remaining **MOV** is discarded, and the previous **Dungeon** (including all **Monsters**) is discarded. A new **Dungeon** is generated; the **Hero** appears in the **Entry Room** with HP, **Weapon inventory**, **Round** count, and **Monsters killed** unchanged. **Toast** and **Combat log** announce the new floor (e.g. “Descending to Floor 2…”). On Floor 3, **Exit stairs** trigger **Victory** instead of **Descent**.
_Avoid_: Level transition, floor change

**Exit Room**:
The **Room** chosen at **Dungeon** generation as the floor goal. Selected by **Room path distance** from the **Entry Room** (maximum hop count through carved **Room** passages—not grid distance). Ties among farthest **Rooms** are broken at random. Never the **Entry Room**. Changes every **Floor**. Uses the open **Room layout** (no **Rocks**), same as the **Entry Room**. Holds the **Exit stairs**. **Spawn on explore** uses normal rules (not exempt). No in-game hint points toward the **Exit Room**—the player finds it by exploration.
_Avoid_: Goal room, stair room, boss room

**Room path distance**:
Hop count between two **Rooms** along the **Room** connectivity graph (BFS on the 8×8 maze the generator builds). One hop = one carved passage to an adjacent **Room**.
_Avoid_: Manhattan distance, tile distance, grid distance

**Exit stairs**:
A 2×2 visual on the open center of the **Exit Room** (walkable **Squares** at local positions (1,1)–(2,2) within the **Room**). Rendered as one sprite (`stairs-down.png`, 128×128) covering the footprint; until that asset exists, a procedural solid placeholder via `TextureCache.getOrCreateSolid`. **Spawn on explore** never places a **Monster** or **Item** on these four **Squares**. Stepping onto any of them triggers **Descent** on Floors 1–2 or **Victory** on Floor 3—instantly, with no **End turn** or monster-phase gate.
_Avoid_: Ladder, portal, exit tile

**Square**:
One cell on the dungeon grid. May hold a **Wall**, a **Rock**, a **Creature**, or open floor.
_Avoid_: Tile (overloaded — see Flagged ambiguities)

**Room**:
A fixed 4×4 block of adjacent Squares (64 Squares total across the dungeon). Rooms are connected by carved passages; the dungeon layout is generated at the Room level then expanded to Squares. Walkable **Squares** inside a **Room** may contain **Rocks** placed by **Room layout** (see **Rock**).
_Avoid_: Chamber, zone, tile

**Wall**:
A Square whose texture is solid (non-walkable). All perimeter **Walls** share one visual (black solid). Carved passages connect **Rooms**.
_Avoid_: Block, obstacle

**Rock**:
An interior obstacle **Square** inside a **Room**—distinct from perimeter **Wall** (`rock.png`). Non-walkable; blocks **Hero** and **Monster** movement. **Monsters** and **Items** never spawn on **Rocks**. Placed by **Room layout** patterns.
_Avoid_: Pillar, boulder, prop

**Room layout**:
The pattern of **Rocks** (or open floor) inside a **Room**, chosen from a small library based on how many exits the **Room** has. Dead-end **Rooms** (one exit) use chokepoint/ambush patterns; two-exit **Rooms** use medium patterns; junction **Rooms** (three or four exits) stay open. The **Hero** spawn **Room** always uses the open layout regardless of exit count. A pattern is rejected if it would split the **Room**'s floor into disconnected areas (which would trap **Monsters** or the **Hero**).
_Avoid_: Room template, room type, biome

**Position**:
An immutable (x, y) coordinate on the Square grid. Origin is bottom-left; Y increases upward.
_Avoid_: Point, cell, tile coords

**HUD strip**:
The fixed bottom band of the screen showing stats, controls, and the **Combat log**. **Dungeon** art never renders here; the band keeps a consistent layout height across screen sizes. Touches on the **HUD strip** never pass through to the **Play area**—only mapped controls (weapon cards, **End turn**, etc.) do anything.
_Avoid_: UI panel, overlay, bottom bar

**Hero panel**:
The left third of the **HUD strip**. Shows the **Hero** portrait, HP/MOV, and **Weapon card**s. Uses the same internal layout template as the **Adjacent monster panel**: **Panel inset**, stat rows with flexible HP/MOV bars, and right-aligned numeric values.
_Avoid_: Player box, left HUD, status panel

**Adjacent monster panel**:
The right third of the **HUD strip**. Shown only when a **Monster** is **Near** the **Hero**; displays that **Monster**'s portrait, HP/MOV, and a left-aligned **Weapon** summary line (same **Panel inset** as the stat rows). Mirrors the **Hero panel** layout.
_Avoid_: Enemy box, right HUD, target panel

**Panel inset**:
The uniform horizontal padding inside the **Hero panel** and **Adjacent monster panel** between content and the panel edges (12px at minimum HUD width). Long names and **Weapon** summary lines truncate with an ellipsis rather than overflow.

**Play area**:
The screen region above the **HUD strip** where the **Dungeon** is rendered. The camera frames the **Hero** within this region so a full **Room** stays readable.
_Avoid_: World view, game canvas, viewport

**Camera follow**:
The view tracks the **Hero**, centering them in the **Play area** when not at a map edge. At the **Dungeon** boundary the camera clamps to the grid; on the south edge the Hero sits toward the bottom of the **Play area** (still above the **HUD strip**), with no empty space shown below the grid.
_Avoid_: Scroll, pan, viewport center

**Mobile web play**:
Playing the game by opening a shared web link in a phone browser—load the page and play with touch. Does not include the desktop downloadable build. On mobile, the game is played in **landscape** with a **fixed dungeon zoom** (larger **Squares** in the **Play area**); the **HUD strip** is not zoomed.
_Avoid_: Mobile app, native client, desktop web

**Fixed dungeon zoom**:
On **Mobile web play**, the **Play area** shows fewer **Squares** at once so each **Square** renders larger (fixed ×2 scale vs desktop web). The **HUD strip** keeps the same layout and scaling as desktop; only dungeon rendering is zoomed.
_Avoid_: Pinch zoom, camera zoom slider, HUD scaling

**Mobile device** (web):
A client that qualifies for **Mobile web play** layout: primary input is touch **and** the screen’s shorter side is at most 768 CSS pixels. Desktop browsers and large tablets in non-touch mode use the standard layout.
_Avoid_: Phone (vague), user-agent match

**Landscape lock**:
On **Mobile web play**, the game requires landscape orientation. If the device is held in portrait, a full-screen overlay blocks the **Dungeon** and **HUD strip** and all input until the player rotates; the canvas stays hidden until landscape.
_Avoid_: Portrait mode, auto-rotate layout

### Visibility and discovery

**Explored**:
A flag on a Square meaning the player has revealed it. Unexplored Squares render as fog regardless of contents.
_Avoid_: Visible, discovered, lit

**Exploration**:
The act of marking Squares as Explored. When the **Hero** enters a Room, all 16 Squares in that Room become Explored.
_Avoid_: Scouting, fog clearing

**Spawn on explore**:
The rule that the first time any Square in a Room is Explored on the current **Floor**, that Room gets exactly one spawn attempt (**80% Monster**, **20% Item**). Each Room spawns at most once per **Floor**. When the spawn is a **Monster**, type is chosen uniformly from the full roster (**equal weight** per type). When the spawn is an **Item**, it is **50%** **Greater Heal Potion** or **50%** **Weapon pickup** (**Heal Potion** does not spawn from explore).
_Avoid_: Room activation, encounter trigger

**Floor progress** (HUD):
Run stat shown in the HUD center panel (below **Round**) as `Floor N / 3`. Increments on **Descent**; does not reset on **Game over** display. Replaces the former **Rooms explored** counter.
_Avoid_: Depth indicator, level number

### Actors

**Creature**:
Anything with HP, **Armour Class (AC)**, a sprite, and life/death state. Base type for **Hero** and **Monster** (and, in code, **Item**).
_Avoid_: Entity, actor, unit

**Hero**:
The player-controlled Creature. **8 max HP**, **AC 14**, movement budget (**MOV**), one attack per **Turn**, and a **Weapon** inventory. Starts at **Hero level** 1 with 0 **Experience (EXP)**; **Hero level** and EXP persist across **Descent** and reset only on **Restart**.
_Avoid_: Player, character, avatar

**Hero level**:
The **Hero**'s progression rank within a **Run**, starting at 1. Distinct from **Floor** (dungeon depth). Each level-up requires **10 EXP**; reward is **+1 max HP** and **heal 1 HP** (capped at the new max).
_Avoid_: Floor, depth, character level (ambiguous)

**Experience (EXP)**:
Points earned when the **Hero** defeats a **Monster**. Amount equals that **Monster**'s **level**. Shown in the **Hero panel** as progress toward the next **Hero level** (threshold **10 EXP** per level; remainder carries over on level-up).
_Avoid_: Score, XP bar (use EXP in domain language)

**Monster level**:
A **Monster** stat representing how much **Experience (EXP)** the **Hero** earns on defeat. Initially set equal to HP for each roster type; may diverge later.
_Avoid_: CR, challenge rating, tier

**Monster**:
An enemy Creature placed by spawn-on-explore. Inactive until **Activated**; once active, takes turns moving toward and attacking the Hero. Sprites load from PNG files in `assets/` (Hero, Troll, Orc, Werewolf, Ogre, Skeleton, etc.).
_Avoid_: Enemy, mob, foe

**Active** (monster):
A Monster that has been Activated (typically because the Hero came **Near** it). Only Active Monsters act during the monster phase. Active Monsters path toward the Hero through passable **Squares** regardless of fog (**Explored** or not).
_Avoid_: Aggroed, awakened, engaged

**Activated**:
The transition when a previously inactive Monster becomes Active. Happens when the Hero is Near that Monster's Position.
_Avoid_: Aggro, wake up

**Initiative strike**:
When a **Monster** with **1 max HP** is first **Activated**, it immediately attempts one melee attack (normal **Weapon** **Hit** / **Miss**) if **Near** the **Hero**. Fires once per **Monster** per **Run**; does not replace its **Turn** on **End turn** if still alive.
_Avoid_: First strike, ambush, pre-emptive attack

**Item**:
A pickup on the dungeon floor (**Heal Potion**, **Greater Heal Potion**). Auto-collected when the Hero enters its **Square**; consumed via `use(Hero)`. Items occupy a **Square** but do not block movement—**Hero** and **Monster** pathfinding may traverse Item **Squares**; an unconsumed Item is restored when the occupant steps off.
_Avoid_: Loot, pickup, consumable (use Item for the domain concept; consumable describes behavior)

**Heal Potion**:
An Item that restores 1 HP if the Hero is below max HP; otherwise **found but not consumed**—the potion stays on the floor and does not block movement. Sprite: `potion-red.png`. Does not spawn from **spawn on explore**; **Monster** kill drops only: **33%** chance.
_Avoid_: Health potion, red potion

**Greater Heal Potion**:
An Item that restores 2 HP if the Hero is below max HP (capped at max); otherwise **found but not consumed**. Sprite: `potion-blue.png`. Room **spawn on explore** only: **50%** of **Item** spawns (~**10%** of **Rooms**). Does not drop from **Monster** kills.
_Avoid_: Large potion, blue potion

**Weapon pickup**:
A floor **Item** that grants the **Hero** a **Weapon** when collected. Auto-collected on entering its **Square** (same as potions). Sprite: generic `weapon.png` for all types; the **Weapon** name and stats are shown when the player hovers that **Square** (same pattern as **Monster** inspect). Pool: **Mace**, **Big Club**, **Hammer** (equal weight among **Weapon pickup** spawns). Room **spawn on explore** only: **50%** of **Item** spawns (~**10%** of **Rooms**). If **Weapon inventory** has an empty slot (fewer than 3 **Weapons**), the **Weapon** is added without auto-equip—the player selects via **Weapon card** or keyboard switch. If inventory is full, collecting triggers **Weapon swap**. Removed from the floor on successful pickup (or becomes the swapped-out **Weapon**).
_Avoid_: Weapon drop, gear loot, equipment chest

**Weapon swap**:
Exchanging the equipped **Weapon** with a **Weapon pickup** on the floor when **Weapon inventory** is full. The picked-up **Weapon** is equipped immediately; the old **Weapon** remains on the **Square** as a **Weapon pickup**. Duplicate **Weapon** types may exist simultaneously in **Weapon inventory** and on the floor. Lets the **Hero** rearrange loadout during a **Run** without losing **Weapons**.
_Avoid_: Drop weapon, discard, trash slot

**Weapon inventory**:
The set of **Weapons** the **Hero** carries. Starts with **Axe** and **Sword**; **Weapon pickup** items add more. Maximum **3 Weapons** at once. The player equips one **Weapon** at a time via **Weapon card** taps or `I` / switch-weapon input. When at the cap, collecting a **Weapon pickup** triggers **Weapon swap** (equipped **Weapon** goes to the floor; floor **Weapon** is equipped).
_Avoid_: Loadout, arsenal, equipment slots

**Weapon card**:
A **Hero panel** control showing one **Weapon** from the **Weapon inventory** (name, attack bonus, damage). Up to **3** cards when inventory is full. Tapping a card equips that **Weapon**. Gold underline marks the active **Weapon**.
_Avoid_: Weapon slot, gear tab

**Weapon pickup feedback**:
On first pickup (empty inventory slot): **Toast** and **Combat log** entry with **Weapon** name and stats (e.g. "Found Mace! (+7 atk, 1 dmg)"). On **Weapon swap** (full inventory): **Toast** and **Combat log** with both names (e.g. "Swapped Sword for Hammer"). Every swap shows a **Toast** (no throttling).
_Avoid_: Loot notification, pickup popup

### Combat and equipment

**Monster archetype**:
A design role for a **Monster** type defined by its HP, AC, MOV, and **Weapon** spread—not unique AI. Roster (5 types, **equal weight** spawn):

| Monster | Archetype | HP | AC | MOV | Weapon |
|---------|-----------|----|----|-----|--------|
| Skeleton | swarm/fast | 1 | 11 | 7 | Rusty Blade (+4 atk, 1 dmg) |
| Werewolf | fast/fragile | 1 | 12 | 6 | Bite (+6 atk, 1 dmg) |
| Orc | balanced | 2 | 13 | 5 | Mace (+7 atk, 1 dmg) |
| Troll | tank/bruiser | 2 | 14 | 4 | Big Club (+1 atk, 2 dmg) |
| Ogre | heavy tank | 3 | 15 | 3 | Hammer (−1 atk, 3 dmg) |

_Avoid_: Class, mob type, enemy tier

**Ogre**:
**Monster** archetype heavy tank (HP 3, MOV 3, **Hammer**). Sprite: `ogre.png`.
_Avoid_: Giant, brute

**Skeleton**:
**Monster** archetype swarm/fast (HP 1, MOV 7, **Rusty Blade**). Sprite: `skeleton.png`.
_Avoid_: Undead, ghoul

**Rusty Blade**:
A **Weapon** wielded by **Skeleton**—moderate **Attack bonus**, light damage; fits the swarm/fast archetype.
_Avoid_: Short sword, dagger

**Hammer**:
A **Weapon** wielded by **Ogre**—high damage, negative **Attack bonus**; fits the heavy tank archetype.
_Avoid_: Maul, warhammer

**Weapon**:
An attack profile: display name, **attack bonus** (integer added to the d20 **Attack roll**), and damage on **Hit**. Hero and Monster each wield one Weapon when attacking.
_Avoid_: Attack type, gear, equipment slot

**Armour Class (AC)**:
How difficult a **Creature** is to hit. Each **Hero** and **Monster** has an AC; an **Attack roll** total must meet or exceed the target's AC to **Hit**. Represents armour, agility, and toughness combined.
_Avoid_: Defense stat, dodge rating

**Attack bonus**:
The integer added to a d20 **Attack roll**, usually from the attacker's **Weapon**. Shown as "+N" (or "−N" when negative) in the HUD and pickup text.
_Avoid_: Hit chance, to-hit modifier

**Attack roll**:
One d20 roll plus the attacker's **Attack bonus**. Compared against the target's **Armour Class (AC)**; total ≥ AC is a **Hit**, otherwise a **Miss**.
_Avoid_: Hit check, accuracy roll

**Hit**:
An **Attack roll** whose total (d20 + **Attack bonus**) is equal to or greater than the target's **Armour Class (AC)**; deals the **Weapon**'s damage. A failed roll is a **Miss** (0 damage).
_Avoid_: Strike, connect

**Miss**:
An **Attack roll** whose total is less than the target's **Armour Class (AC)**; no damage dealt.
_Avoid_: Dodge, block (those imply different mechanics)

**Near**:
Chebyshev distance ≤ 1 between two Positions (orthogonal or diagonal neighbors). Required for melee **Attack action** and for **Activated** checks. **Items** are picked up by entering their **Square**, not by being **Near** alone.
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
The **Hero** reaches **Exit stairs** on Floor 3 and steps onto them while alive. Triggers instantly (same transition as **Descent**—no **End turn** / monster-phase gate). The **Victory screen** uses the same layout as **Game over** but with the HUD **gold** accent: title “Victory!”, subtitle “You reached the deepest level.”, run summary with **Round** and **Monsters killed**, gold **Restart** button.
_Avoid_: Win screen, completion, success state

**Monsters killed**:
Run stat: count of **Monsters** the Hero reduced to 0 HP. Incremented once per Monster death from Hero damage (not from other causes—there are none today). On death, each **Monster** has a **33%** chance to leave a **Heal Potion** on its **Square** (separate from room spawn potions).
_Avoid_: Kill count, score, KOs

**Game over**:
The Hero's HP reached 0. Input stops except **Restart**. Shows the same run summary as **Victory**: **Round** and **Monsters killed**. **Restart** starts a new **Run** at Floor 1.
_Avoid_: Death screen, fail state

**Run**:
One play session from Floor 1 until **Victory** or **Game over**. Spans multiple **Floors**; **Descent** does not end the **Run**.
_Avoid_: Session, game, match

## Flagged ambiguities

| Term | Conflict | Resolution (proposed) |
|------|----------|------------------------|
| **Tile** | Code/UI uses "tile" for Square (`heroClickOnTile`) and readme uses "tile" for Square, while **Room** is the 4×4 unit | Use **Square** for grid cells and **Room** for 4×4 groups in domain language |
| **Adjacent** | Could mean Near (8-direction) or very near (4-direction) | Use **Near** or **very near** explicitly |
| **Speed** | HUD label "MOV" but code field `speed` | Player-facing: **MOV**; code may keep `speed` |
| **Board** | `GameBoard` is the state owner, not a physical object in the dungeon | **Dungeon** for the world; "board" only when discussing code |
| **Item extends Creature** | Items have HP=1 and sit on Squares like Creatures | Domain: Item is a floor pickup, not a combatant—code model is legacy |
| **Assets in repo** | Git tracks only a subset of `assets/`; full PNG/audio set exists locally for Hero and Monsters | Monster sprites use lowercase filenames (`ogre.png`, `skeleton.png`, etc.) |
| **Victory** | ADR 0001 superseded by ADR 0004 (**Exit stairs** on Floor 3) | Implemented |
| **Level** | Colloquial for **Floor**, whole **Run**, or **Hero level** | Use **Floor** for dungeon depth; **Hero level** for progression; **Run** for the full descent |

## Example dialogue

**Dev**: When the player walks into a new area, what happens?

**Designer**: They **Explore** a **Room**—all 16 **Squares** in that **Room** flip to **Explored**. If it's the first time that **Room** was touched, **spawn on explore** fires once: usually a **Monster**, sometimes a **Greater Heal Potion** or **Weapon pickup**.

**Dev**: Does every Monster fight immediately?

**Designer**: No. New Monsters start inactive. When the **Hero** gets **Near** one, it becomes **Active**. On **end turn**, only **Active** Monsters take a **turn**—they path until **very near** the Hero, then attack if **Near**.

**Dev**: What ends a run?

**Designer**: Two outcomes. **Victory** when the **Hero** reaches **Exit stairs** on Floor 3. **Game over** when HP hits 0. Between floors, **Descent** via **Exit stairs** on Floors 1–2 keeps stats and inventory. The HUD shows **Floor progress** (`Floor N / 3`), **Round**, and **Monsters killed**. **Restart** starts a new **Run** at Floor 1.

**Dev**: What's the goal each floor?

**Designer**: Find the **Exit Room**—the **Room** farthest by **Room path distance** from the **Entry Room**—and reach the **Exit stairs**. Exploration and **spawn on explore** still drive encounters, but clearing every **Room** is not required.
