# Project: Turn-Based Dungeon Crawler (libGDX)

## Tech Stack

- **Framework**: libGDX 1.14.0
- **Language**: Java 17
- **Build**: Gradle multi-module (core, desktop — HTML/GWT planned)
- **Desktop backend**: LWJGL3
- **Package**: `com.mygdx.game`

## Project Structure

```
core/src/com/mygdx/game/   — All shared game logic (platform-agnostic)
desktop/src/com/mygdx/game/ — Desktop launcher (LWJGL3)
assets/                     — Shared assets (textures, sounds, fonts)
```

Source directories use `src/` (not `src/main/java/`). Assets live in root `assets/` and are referenced via `Gdx.files.internal()`.

## Architecture

The game follows a turn-based board game pattern:

- `MyGdxGame` extends `ApplicationAdapter` — main game loop (create/render/dispose)
- `GameBoard` holds `Square[][]` grid, manages turns, monsters, items, exploration
- `Creature` is the base class for `Hero` and `Monster`
- `Weapon` is abstract — subclasses: `Sword`, `Axe`, `Mace`, `BigClub`, `Bite`
- `Item` is abstract base for collectibles
- `Position` is an immutable 2D coordinate value object
- `MyInputAdapter` extends `InputAdapter` for keyboard controls
- `RoomMazeGenerator` handles procedural dungeon generation
- `RandomMonsterFactory` creates monsters via factory pattern
- `Toast` handles in-game notification popups

## Code Conventions

- All game logic goes in `core/` module — NEVER put game logic in platform modules
- Platform launchers (desktop, html) are thin wrappers that only configure and start the game
- Use libGDX APIs, not raw Java AWT/Swing/JavaFX — everything must be cross-platform
- Textures are loaded via `new Texture("filename.png")` from the assets folder
- Sound via `Gdx.audio.newSound(Gdx.files.internal("file.mp3"))`
- Dispose all disposable resources (Texture, SpriteBatch, Sound, BitmapFont, ShapeRenderer) in `dispose()`
- Use `Gdx.files.internal()` for all file access — never use `java.io.File` or `java.nio.file`
- Prefer `com.badlogic.gdx.utils` collections (Array, ObjectMap) over java.util when performance matters
- Coordinate system: origin (0,0) is bottom-left, Y-axis points up
- Board uses `SQUARE_SIZE = 64` pixels per tile, grid is `BOARD_SQUARE_WIDTH x BOARD_SQUARE_HEIGHT`

## Rendering Rules

- All rendering happens between `batch.begin()` and `batch.end()` — never nest or overlap
- ShapeRenderer has its own begin/end cycle — do not mix with SpriteBatch
- Use `OrthographicCamera` for world view and a separate `hudCamera` for UI overlay
- Call `camera.update()` before setting projection matrix
- Clear screen with `ScreenUtils.clear()` at the start of render
- Font rendering uses `BitmapFont` — for styled text use libGDX's `Label` with `Scene2D`

## HTML/GWT Compatibility

This project is designed to support HTML export via GWT (or TeaVM). Follow these rules strictly:

### Allowed
- All `com.badlogic.gdx.*` APIs
- `java.lang.*`, `java.util.*` (most of it), `java.io.Serializable`
- Simple data classes, enums, interfaces
- `Math`, `Random`, `StringBuilder`, `String` operations

### Forbidden in core/ (breaks GWT)
- `java.util.function.*` (Consumer, Supplier, Function) — use libGDX or custom interfaces instead
- `java.util.stream.*` — use explicit loops
- `java.nio.file.*`, `java.io.File` — use `Gdx.files` API
- `java.lang.Thread`, `synchronized`, `java.util.concurrent.*` — GWT is single-threaded
- `java.time.*` — use `TimeUtils.millis()` or `TimeUtils.nanoTime()`
- `java.util.Optional` — use null checks
- Reflection (`Class.forName`, `Method.invoke`) — GWT does not support runtime reflection
- `String.format()` — not supported in GWT, use concatenation
- Lambda expressions on functional interfaces from `java.util.function` — define custom single-method interfaces
- `try-with-resources` on non-GWT types
- Any JDK API added after Java 8 that isn't in the GWT JRE emulation

### GWT-Safe Patterns
```java
// BAD — breaks GWT
private java.util.function.Consumer<String> callback;
monsters.stream().filter(m -> m.isAlive()).forEach(m -> m.takeTurn());
synchronized(lock) { ... }

// GOOD — GWT compatible
public interface StringCallback { void call(String value); }
private StringCallback callback;
for (Monster m : monsters) { if (m.isAlive()) m.takeTurn(); }
// No synchronization needed — single-threaded on web
```

### HTML Module Setup (when adding)
- HTML module uses `gwt` plugin with `com.badlogicgames.gdx:gdx-backend-gwt`
- GWT entry point extends `GwtApplication` and returns the `MyGdxGame` instance
- A `.gwt.xml` module descriptor must list all source packages
- Assets are served from `webapp/` via the GWT war directory
- Use `GwtApplicationConfiguration` to set canvas size

## Game Development Best Practices

### Resource Management
- Load textures/sounds once (in `create()` or via `AssetManager`), reuse everywhere
- Always call `.dispose()` on Texture, Sound, Music, SpriteBatch, ShapeRenderer, BitmapFont
- For many assets, use `AssetManager` for async loading and centralized disposal
- Use `TextureAtlas` (packed spritesheets) instead of individual textures when asset count grows

### Performance
- Avoid allocating objects in `render()` — preallocate and reuse (Vector2, Rectangle, etc.)
- Use object pools (`com.badlogic.gdx.utils.Pool` / `DefaultPool`) for frequently created/destroyed objects
- SpriteBatch draw calls are batched automatically — avoid switching textures mid-batch
- `ShapeRenderer` is expensive — prefer textured sprites for production UI
- Delta time: always use `Gdx.graphics.getDeltaTime()` for time-based updates, never frame counting

### Game State
- Keep game state in `core/` data classes, separate from rendering
- The render loop should read state and draw — avoid mixing mutation and drawing
- Turn-based logic: clearly separate player turn, AI turn, and animation phases
- Use state machine or enum for game phases (PLAYER_TURN, MONSTER_TURN, ANIMATING, GAME_OVER)

### Input
- Use `InputAdapter` or `InputMultiplexer` for input — never poll in render unless needed
- For UI + game input, use `InputMultiplexer`: Stage first, then game input adapter
- Map controls to actions, not raw keys — makes rebinding easy

### Scaling & Cameras
- Use `Viewport` (FitViewport, ExtendViewport) to handle window resizing properly
- Override `resize(int width, int height)` and call `viewport.update(width, height)`
- Separate world camera from UI camera — world uses game units, UI uses screen pixels

### Audio
- Use `Sound` for short effects (< 5 seconds), `Music` for background tracks
- `Music` streams from disk — only one active instance needed
- Set volume via a config object (like `AudioConfig`) so it's adjustable globally

## Gradle Commands

```bash
./gradlew :desktop:run          # Run the game
./gradlew :desktop:dist         # Build distributable JAR
./gradlew :core:compileJava     # Compile core module only
./gradlew clean                 # Clean build artifacts
```

## Adding New Content

### New Monster
1. Create weapon class extending `Weapon` in core/
2. Register monster type in `RandomMonsterFactory`
3. Add texture to `assets/`
4. No changes needed in platform modules

### New Weapon
1. Extend `Weapon` with `chanceToHit` and `damage` values
2. Assign to a monster or make available to the hero

### New Item
1. Extend `Item` abstract class in core/
2. Add pickup/use logic in `Hero` or `GameBoard`
3. Add texture to `assets/`
