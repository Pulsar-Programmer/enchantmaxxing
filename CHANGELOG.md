2.0.0+1.21.10
- Updated to Minecraft 1.21.10
- Bumped Fabric API to 0.138.4 (owo-lib 0.12.24, Cloth Config 20.0.149, Mod Menu 16.0.1 unchanged — all 1.21.10 compatible)
- No source changes needed

2.0.0+1.21.9
- Updated to Minecraft 1.21.9
- Bumped dependencies: Fabric API 0.134.1, Cloth Config 20.0.149, owo-lib 0.12.24, Mod Menu 16.0.1
- Bumped build tooling for 1.21.9 mappings: Fabric Loom 1.11, Gradle 8.14.3
- Adapted to 1.21.9 API changes:
  - PlayerEntity.getWorld() -> getEntityWorld()
  - Screen.hasShiftDown() removed; added EnchantifyClient.hasShiftDown() helper (queries GLFW via InputUtil)
  - KeyBinding now takes a KeyBinding.Category instead of a String category (added enchantify:main category + lang key)
  - ScreenKeyboardEvents.afterKeyPress and KeyBinding.matchesKey now take a KeyInput
  - owo MouseDown callbacks now receive (Click, boolean) instead of (double, double, int)
  - ButtonWidget.onPress() now requires the triggering AbstractInput

2.0.0+1.21.8
- Updated to Minecraft 1.21.8
- Bumped Fabric API to 0.136.1 (owo-lib 0.12.23, Cloth Config 19.0.147, Mod Menu 15.0.2 unchanged — all native to 1.21.8)
- No source changes needed
- Fixed task-menu labels: "Cancel Task" tooltip now on the ✕ button, and the level-cost number no longer renders transparent

2.0.0+1.21.7
- Updated to Minecraft 1.21.7
- Bumped dependencies: Fabric API 0.129.0, owo-lib 0.12.23 (Cloth Config 19.0.147 and Mod Menu 15.0.2 unchanged, both 1.21.6-1.21.8 compatible)
- No source changes needed; Fabric API pinned to the 1.21.7 build to override owo's 1.21.8 transitive

2.0.0+1.21.6
- Updated to Minecraft 1.21.6
- Bumped dependencies: Fabric API 0.128.2, Cloth Config 19.0.147, owo-lib 0.12.21, Mod Menu 15.0.2
- Adapted to 1.21.6 render pipeline overhaul (DrawContext#drawTexture now takes a RenderPipeline; RenderSystem.setShaderColor removed)

2.0.0+1.21.5
- Updated to Minecraft 1.21.5
- Bumped dependencies: Fabric API 0.128.2, Cloth Config 18.0.145, owo-lib 0.12.21, Mod Menu 14.0.2
- Adapted to 1.21.5 API changes (EnchantmentLevelEntry record accessors, RenderSystem blend removal)

1.0.0
- Added all base features of the mod

0.35.0