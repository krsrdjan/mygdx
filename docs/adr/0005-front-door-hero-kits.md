# Front door and Hero kits

The game opened straight into a **Run**. **Restart** after **Victory** or **Game over** immediately started Floor 1, so there was nowhere to choose how a **Run** begins.

**Decision:** A **Cold launch** shows the **Front door**: **Splash** (two seconds, skippable; `startup-logo.png` and the title **Turn-Based Adventure**, also the desktop window title), then the **Main menu** (**Play**, and **Quit** on desktop only), then **Hero selection**. Two **Hero kits** share `hero.png`. **Squire** is Sword, 8 HP, AC 14, MOV 8, and starts highlighted. **Brute** is Axe, 8 HP, AC 14, MOV 6. **Begin** starts the **Run** at once with that **Weapon** equipped. **Restart** returns to **Hero selection**; it does not start a dungeon. **Back** returns to the **Main menu** and leaves a finished **Run** finished. Nothing during a **Run** opens the menu. **Atmosphere** starts on **Cold launch** and is not restarted by **Begin** or **Restart**. The **Play area** / **HUD strip** split applies only during a **Run**.

**Why not the alternatives:**
- **Cosmetic-only selection** — a screen with no mechanical choice.
- **Classes with unique actions** — reopens combat; out of scope for the front door.
- **Restart starts Floor 1 immediately** — **Hero selection** would happen once per app launch.
- **Restart returns to the Main menu** — an extra step after every death; the kit is the decision that matters.
- **A third kit on Mace** — Mace strictly outclasses Sword, and there is only one hero portrait.
- **Splash and menu as one screen** — the **Splash** is a brand beat; choices live on the **Main menu**.

**Consequences:** The **Hero** is created from a **Hero kit**, with one starting **Weapon**. See `CONTEXT.md` (**Front door**, **Hero kit**, **Squire**, **Brute**, **Atmosphere**). Weapon-inventory cap (glossary 3, code 2) and EXP per level (glossary 10, code 5) stay parked.
