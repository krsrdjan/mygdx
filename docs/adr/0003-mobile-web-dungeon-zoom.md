# Mobile web: fixed dungeon zoom and landscape lock

On phone browsers the standard play-area minimum (1280×538 logical) made **Squares** too small for touch play. Players also need a consistent **landscape** layout; portrait showed an unusably narrow **Play area**.

**Decision:** For **Mobile web play** only (touch + screen short side ≤ 768 CSS px, detected in `teavm/webapp/index.html`):

1. **Fixed dungeon zoom ×2** — halve the world `ExtendViewport` minimum to 640×269 logical. The HUD viewport stays at 1280×720 minimum (no HUD zoom). Pass `mobileWebPlay` from JS (`window.__MYGDX_MOBILE_WEB__`) into `MyGdxGame` via `TeaVMLauncher`.
2. **Landscape lock** — full-screen rotate overlay and hidden canvas until `innerWidth > innerHeight`. Delay TeaVM `main()` until landscape on first load. Re-show overlay if the player rotates to portrait mid-run. Call `screen.orientation.lock('landscape-primary')` where supported; do not rely on it (especially iOS Safari).

Desktop JAR and desktop web (non-mobile detection) are unchanged.

**Why not the alternatives:**
- **`camera.zoom` on world camera** — fights `ExtendViewport` scaling; harder to keep touch unprojection aligned with the **HUD strip** split.
- **Zoom the HUD too** — weapon cards and **End turn** would shrink; rejected explicitly.
- **Portrait mobile layout** — **HUD strip** consumes too much of a narrow portrait width; **landscape lock** matches the dungeon’s wide **Play area** design.
- **User-agent sniffing** — brittle for tablets and desktop-mode browsers; touch + short side is sufficient.

**Consequences:** World and HUD minimum sizes diverge on mobile web; `updateViewportsIfSized` and **Camera follow** behave correctly because only world mins change. See `CONTEXT.md` (**Mobile web play**, **Fixed dungeon zoom**, **Landscape lock**, **Mobile device**).
