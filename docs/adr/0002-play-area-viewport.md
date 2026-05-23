# Split play-area viewport above HUD strip

The dungeon was rendered on the full screen while the HUD overlay occupied the bottom ~182 logical pixels. The camera centered the Hero on the full frame, so bottom-row Rooms were hidden under the HUD strip and tiles bled through the panel.

**Decision:** Use two viewports. The world viewport fills only the **Play area** (screen above the **HUD strip**) with minimum logical size 1280×538 (720 minus HUD height). The HUD viewport still uses the full screen at 1280×720 minimum. On resize, compute HUD screen height from `HUD_PANEL_Y_TOP`, update the world viewport with the remaining play-area height, then shift `screenY` above the HUD strip (because `ExtendViewport.update()` resets bounds to the bottom of its passed height). **Camera follow** centers the Hero in the play-area viewport when not at a map edge; at the south boundary, clamp to the grid bottom so no empty space appears below the **Dungeon** (the Hero may sit lower in the frame instead of staying centered). Scissor the world draw to the play-area bounds; HUD backgrounds are fully opaque.

**Why not the alternatives:**
- **Camera offset only on full-screen viewport** — fragile; easy to double-count with viewport height; does not stop tiles drawing under the HUD.
- **Opaque HUD without viewport split** — hides bleed-through but wastes GPU work and breaks touch mapping (clicks in the HUD strip still unproject to world coords).
- **Scissor only, no viewport split** — helps clipping but keeps wrong aspect/scaling for the play area and confuses input unless scissor bounds are duplicated everywhere.
- **South-edge padding (empty space below y=0)** — keeps the Hero centered at the map bottom but shows a grey band above the HUD strip; rejected in favor of grid clamp.

**Consequences:** `MyGdxGame.updateViewportsIfSized` must update HUD first, then world with play-area dimensions and Y offset. HUD clicks use `hudScreenPx` to ignore the play area. Monster hover popups skip when the cursor is in the HUD strip. See `CONTEXT.md` (**Play area**, **HUD strip**, **Camera follow**). Temp bottom-row Hero spawn in `GameBoard` remains for QA until verified, then revert to `(16, 16)`.
