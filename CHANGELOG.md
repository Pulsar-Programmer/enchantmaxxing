2.1.0+26.1.x
- Switched the config library from Cloth Config to WalksyLib (1.0.11+26.1, pulled from the Modrinth maven)
  - All existing config options are unchanged: Max Out Items by Default, Enchantment Tooltip Hover Color,
    Static Enchantmax Notes, Do Afterfuse in Menu, Anvil Apply Sound, Fancy Menu, Force Combinable Enchantments, Curse Order
  - WalksyLib now owns persistence too — config is read/written by the library at `config/enchantify.json`
    (the old Jankson-based `config/enchantify.json5` and `Filesystem` helper were removed). Settings are not
    auto-migrated from the old file; re-set them once in the new screen if upgrading from 2.0.0
  - The config screen is now provided by WalksyLib's own ModMenu integration via a `walksylib` entrypoint,
    so the mod's own ModMenu entrypoint (`EnchantifyModMenu`) is gone
  - `hoverColor` is now a `WalksyLibColor` (supports rainbow/pulse), serialized with the rest of the config
- Replaces dependency `cloth-config` with `walksylib` in fabric.mod.json and swaps the shedaniel maven for the Modrinth maven

2.0.0+26.1.x
- Updated to Minecraft 26.1 — the first unobfuscated Minecraft version (new year-based version scheme)
- Single jar covers the whole 26.1 patch line (26.1, 26.1.1, 26.1.2): fabric.mod.json declares `minecraft ">=26.1 <26.2"`,
  and because 26.1 is unobfuscated the official names compiled against are stable across the patch releases
  (verified by diffing the referenced classes/mixin targets across all three jars). We dev/compile against 26.1.2.
- Dropped Yarn entirely: 26.1 ships with Mojang's official names + parameters, so there are no mappings to download
  - Switched the Loom plugin id from `fabric-loom` to the non-remapping `net.fabricmc.fabric-loom` (1.17.11); removed the `mappings` line
  - Mod dependencies are now plain `implementation`/`api` deps (no `modImplementation`/`modApi`) since nothing is remapped
  - Access widener header is now `official` instead of `named`
- Now requires Java 25 (Minecraft 26.1's minimum); bumped `release`/source/target compatibility 21 -> 25, fabric.mod.json `java` -> >=25, CI JDK -> 25
- Bumped all dependencies to the latest that support 26.1.x: Fabric API 0.152.1+26.1.2, Cloth Config 26.1.154, owo-lib 0.13.0+26.1, Mod Menu 18.0.0-beta.1; Fabric Loader 0.19.3
- Ported the whole source tree from Yarn to Mojang names. Notable renames:
  - Text -> Component, Identifier moved to net.minecraft.resources (still named `Identifier`, not ResourceLocation),
    net.minecraft.util.Pair -> net.minecraft.util.Tuple (getLeft/getRight/setLeft/setRight -> getA/getB/setA/setB)
  - RegistryEntry -> Holder, RegistryWrapper -> HolderLookup, RegistryKeys -> Registries, Registries -> BuiltInRegistries,
    RegistryKey -> ResourceKey, EnchantmentLevelEntry -> EnchantmentInstance, ItemEnchantmentsComponent -> ItemEnchantments
  - *ScreenHandler -> *Menu (AnvilMenu/EnchantmentMenu/GrindstoneMenu/AbstractContainerMenu), SlotActionType -> ContainerInput,
    MinecraftClient -> Minecraft, HandledScreen -> AbstractContainerScreen, KeyBinding -> KeyMapping, InputUtil -> InputConstants
  - Mixin targets/method names: onSlotClick -> clicked, onTakeOutput/onTakeItem -> onTake, focusedSlot -> hoveredSlot,
    GrindstoneScreenHandler$4 -> GrindstoneMenu$4
- Adapted to other 26.1 API moves: the GUI graphics class is now GuiGraphicsExtractor (getMatrices() -> pose(), drawTexture(...) -> blit(...)),
  RenderPipeline blend moved into ColorTargetState (withBlend -> withColorTargetState), the glint snippet is GUI_TEXTURED_SNIPPET,
  the glint texture is ItemFeatureRenderer.ENCHANTED_GLINT_ITEM, the action-bar message is Gui#setOverlayMessage,
  and the Fabric keybinding helper moved to fabric-key-mapping-api-v1 (KeyMappingHelper#registerKeyMapping)

2.0.0+1.21.11
- Updated to Minecraft 1.21.11
- Bumped all dependencies to latest: Fabric API 0.141.4, Cloth Config 21.11.153, owo-lib 0.13.0, Mod Menu 17.0.0
- Bumped build tooling: Fabric Loom 1.17, Gradle 9.6 (required by the newer dependencies)
- Added JitPack repository (owo-lib 0.13.0 pulls in kdl4j transitively)
- Migrated to the owo-lib 0.13.0 UI API rename: Component->UIComponent, ParentComponent->ParentUIComponent,
  Components->UIComponents, Containers->UIContainers, OwoUIDrawContext->OwoUIGraphics
- Fully-qualified net.minecraft.text.Text in EnchantmentButton (1.21.11 ButtonWidget added a nested Text type that shadowed it)
- Fixed graph menu connector lines rendering above the item boxes (drawn as fill() quads instead of owo drawLine, which the new GUI renderer layered on top)

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