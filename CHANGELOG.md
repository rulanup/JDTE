# Changelog

### English

#### v0.5.5 (Current)

- **Performance/New**: Reworked all three Time Accelerators to use one managed scheduler. Overlapping accelerators fully add their multipliers, loaded block entities are discovered by chunk, paid virtual ticks remain queued while contributors stay active, execution uses configurable MSPT headroom, and AE2 `IGridTickable` devices are supported.
- **Fixed/Compatibility**: AE2 grid ticking now takes priority over maintenance-only block entity tickers, allowing AE2 Lightning Tech processing machines and similarly structured AE2 add-on machines to be accelerated correctly.
- **Compatibility/Fixed**: Added Just Dyna Things to the runtime test environment and fully integrated its Goo behavior. Energy-powered Goo consumes the add-on's configured FE upkeep per active tick without food, while Creative Goo consumes neither food nor machine FE. External item pipes can now extract only from Gel Generator output slots instead of removing gel, fuel, or input materials.
- **New/Compatibility**: Advanced Potion Brewers now have a Blaze Powder Input toggle, disabled by default. Enabling it allows external containers and pipes to insert Blaze Powder from any side. A configurable safeguard, enabled by default, still rejects fuel-slot insertion from adjacent AE2 `ICraftingProvider` pattern providers while leaving manual insertion and other recipe inputs available.
- **New/Performance/Compatibility**: Added the Crystal Incubator, an eight-upgrade-slot area machine with an adjustable 1-512x Time Fluid growth rate or 1024x with Overclock/Creative, automatic mature-cluster harvesting into nine output slots, and up to eight Fortune Upgrades. Batched range caching and bounded round-robin work avoid full-volume scans every tick; common budding/cluster tags, datapack extension tags, and a public-API Just Dyna Things adapter cover vanilla, JDT, AE2, Data Energistics, AE2 add-ons, and other conventional budding blocks.
- **Crystal Incubator**: Changed its block appearance to the Extended Glue Activator textures, added configurable FE capacity and Time Wand-equivalent runtime energy cost, enabled automatic Time Fluid input and nine-slot item output, and made accelerated Just Dyna Things budding blocks preserve their own configured FE/Time Fluid activation costs.
- **Fixed/Compatibility**: Crystal Incubators now automatically transfer the exact missing activation FE and Time Fluid into Just Dyna Things budding blocks before acceleration, while reserving the Incubator's own current-tick operating cost and avoiding partial two-resource transfers.
- **Fixed/Performance**: Resource-consuming and ordinary budding blocks now use separate cached lists, round-robin cursors, and bounded per-tick budgets, preventing large groups of Dyna budding blocks from starving vanilla/JDT/AE2 random-tick budding growth in mixed areas.
- **Balance/Performance**: Ordinary Crystal Incubator targets now use AE2 Growth Accelerator-style forced random ticks. The default 8x setting equals six AE2 Growth Accelerators, other rates scale proportionally, and bounded accumulated work prevents high-rate area operation from creating an unbounded catch-up queue.
- **New Upgrade**: Added the Crystal Incubator-only Precision Upgrade using JDT's Ore Miner upgrade icon. It applies vanilla Silk Touch to the simulated harvesting tool so mod loot tables retain control of precise drops, and it cannot be installed together with Fortune Upgrades.
- **GuideME**: Added missing machine item associations, block/item images, recipes, structured resource and upgrade tables, and the omitted Fortune/Precision sections to the bilingual in-game guide.
- **Balance**: Basic now runs at 16x or 32x with Overclock/Creative; Advanced is adjustable to 64x or runs at 128x with Overclock/Creative; Extended remains adjustable to 512x or runs at 1024x. Basic, Advanced, and Extended Time Fluid costs use fixed 1x, 2x, and 5x tier rates respectively.
- **Development**: Added AE2 Crystal Science file `8112039`, AE2 Lightning Tech, and Data Energistics to the local runtime test environment.

#### v0.5.4

- **Recipe**: Added a crafting recipe for the Eclipse Alloy Wrench.
- **New/Balance**: Added the Fortune Upgrade for Gel Generators.
- **New**: Added the Entity Suppressor with five modes: suppress entity updates, prevent entity spawning/world insertion, disable entity rendering, disable block entity rendering, and disable client particles. Entity modes support hostile mobs, passive mobs, all living entities, selected entity types, non-living entities, and all entity types.
- **New**: Added the Range Blocker with Containment and Demagnetization modes. Containment keeps mobs inside its configured area; Demagnetization blocks player magnets while allowing machine collection.
- **New**: Added the Advanced Item Collector with eight standard upgrade slots. It intercepts drops before they enter the world and inserts them into the adjacent inventory on its facing side. It supports oversized Sophisticated Storage drops and direct writes to ME Interfaces, ExtendedAE Extended Interfaces, and Oversize Interfaces.
- **New**: Sneak-right-clicking a JDT or JDTE machine with an Upgrade Card now fills as many valid standard or dedicated upgrade slots as the held stack permits. With FTB Ultimine enabled, eligible machines in the current selection are filled in order.
- **New**: The Eclipse Alloy Wrench can define an area with two left-clicked corners, preview it with JDT's area effect and live X/Y/Z dimensions, then write it to an adjustable-area machine. Shift-left-click clears the selection. Completed selections remain locked after being applied so the same area can be copied to multiple machines.
- **Jade**: Machine tooltips now display installed standard and dedicated upgrades with item icons, localized names, and aggregated counts.
- **Compatibility**: The Eclipse Alloy Wrench can rotate or safely quick-dismantle blocks that expose compatible wrench behavior.
- **Compatibility**: Item Senders and Receivers query sided item capabilities before falling back to unsided capabilities, allowing interaction with configured Mekanism machine input/output faces. Available-mode tooltip hints are now gray.
- **Auto I/O**: Direction buttons now cycle through Disabled, Auto Input/Output (default color), Auto Input (orange), and Auto Output (blue), skipping modes unsupported by the machine's actual routes.
- **Performance**: Auto I/O now transfers up to 10,000 items or 1,000,000 mB per tick. Item and Fluid Senders/Receivers default to 64 items or 20,000 mB per tick and become faster with an Overclock or Creative Upgrade.
- **Naming**: Renamed the Basic, Advanced, and Extended Fluid Placers to Fluid Senders.
- **Fixed**: Advanced Item Collector upgrade slots now accept and advertise only Range and Filter Upgrades.
- **Performance**: Reduced the rendering cost of machine area previews.
- **Fixed**: Fixed incompatibility with newer FTB Ultimine versions that prevented Eclipse Alloy Wrench range adjustment.

#### v0.5.3

- **GUI**: Machine GUIs now use fixed embedded upgrade panels instead of draggable popups. Four-slot machines use a right-side 1x4 panel, while eight-slot machines use left/right 1x4 panels. Empty upgrade slots now show valid upgrade types, current counts, limits, machine incompatibilities, and overclock/underclock conflicts. Most machine layouts were adjusted.
- **New**: Added an absolute-direction auto input/output side panel for machines with real item or fluid I/O.
- **New**: Added the Loot Fabricator. It uses reusable spawn eggs as entity templates, consumes Life Fluid, Time Fluid, and FE, and produces matching mob loot. It supports redstone control, automatic I/O, four spawn-egg inputs, paged outputs, eight standard upgrade slots, Capacity Upgrades, and up to three Looting Upgrades.
- Loot Fabricator can process all inserted spawn eggs in parallel during one progress cycle. Costs scale with participating eggs, and completion consumes Life Fluid, Time Fluid, and FE only for successful outputs.
- Looting Upgrades now apply a configurable extra-copy roll after loot-table rolls and increase Life/Time Fluid costs by a configurable percentage, defaulting to +50% per installed Looting Upgrade.
- Boss and compatibility handling: Wither and Elder Guardian eggs cost 10x fluid, Ender Dragon eggs cost 100x, Wither recipes include the Nether Star, and Draconic Evolution Ender Dragon fabrication includes Dragon Heart plus configured Draconium Dust.
- **New**: Advanced Bio Crusher now emits captured loot through the vanilla item-drop path at the killed entity's position. Extended Bio Crusher stores generated loot in its paged output inventory when space is available and can expand effective output capacity with Capacity Upgrades.
- Bio Crusher experience fluid is now based on the entity's final adjusted XP reward, including Apothic Spawners Echoing effects, and does not spawn XP orbs.
- Bio Crusher spawner integration now takes over the spawn cycle before entity creation, preserves vanilla/Apothic spawn data and delay logic, captures complete virtual deaths, and lets the original spawner continue when the machine cannot process the cycle.
- Bio Crusher and Life Extractor hostile/friendly modes now classify targets by hostile entity type instead of the current AI aggression state.
- **New**: Added optional Bio Crusher compatibility for Draconic Evolution Chaos Guardian fights, with server config toggles for crystal destruction and FakePlayer-attributed lethal attacks.
- **New**: Infusion Machines can automatically craft matching spawn eggs from a full stack of a uniquely identifying mob drop, 64 B of Life Fluid, and 100,000 FE.
- Added dedicated JDTE Wither, Ender Dragon, and Elder Guardian Spawn Egg infusion recipes using the matching Boss Essence and 64 B of Life Fluid. Generic scanning excludes Wither to avoid creating a duplicate Nether Star recipe.
- Spawn egg infusion recipes now register through one JEI path, no longer duplicate, and are shown before other Infusion Machine recipes. Gel Generator, Infusion Machine, and Potion Brewer recipes expose stable IDs so JEI recipe bookmarking works.
- Infusion Machines can fill compatible fluid containers through NeoForge item-fluid capabilities, including buckets, modded containers, water bottles from glass bottles, and honey bottles from honey-like fluids.
- Life Apples now permanently and cumulatively increase maximum health, armor, and armor toughness without a cap. Tooltip, JEI information, and GuideME documentation were updated; the tooltip text is red and the GuideME growth explanation is table-based.
- **New**: Added the Advanced Potion Brewer with three bottle inputs, six ordered ingredient steps, blaze-powder fuel, three product outputs, water tank, Time Fluid tank, energy bar, redstone control, speed control, and slot tooltips.
- Brewing advances every server tick like a vanilla brewing stand, supports ordered multi-step chains, server-confirmed ingredient-slot locking, one-stack bottle/product GUI slots, and restricted automation semantics for bottle/material/fuel inputs and product outputs.
- Advanced Potion Brewer Time Fluid cost is based on ticks saved versus the vanilla 400-tick brewing step, and vanilla-speed operation consumes no Time Fluid.
- Advanced Potion Brewer JEI pages prefer complete brewing chains starting from glass bottles and water, merge equivalent ingredients for the same stage, support water-filling recipes, register the catalyst, and open from the progress arrow.
- **New**: Added the Eclipse Alloy Wrench. Normal right-click rotates machines like JDT's Ferricore Wrench, while sneak right-click picks up supported machines with saved machine data. Optional FTB Ultimine support can rotate or pick up selected machines in bulk.
- Eclipse Alloy Wrench scroll-wheel range adjustment now requires holding the wrench and the Wrench Area Modifier key, can adjust area-machine X/Y/Z radius together, shows a green current/max range overlay, and is documented in GuideME.
- **Fixed**: Added right-click fluid container interaction to JDTE fluid machines.
- **Fixed**: Fixed filter pagination logic, including dynamic slot indices, automatic reset when available pages shrink, page label styling, and page-button size/position.
- **Fixed**: Bio Crusher kills now use a cached FakePlayer, real weapon/looting context, and final NeoForge drop events, preserving mod-adjusted drops while preventing normal Sharpness kills from spilling duplicate items into the world. Unknown modded entities no longer receive fabricated fallback drops.
- **Fixed**: Life Extractor now directly removes targets and produces only Life Fluid, with no normal drops or experience. Output is based on target current health through configurable conversion, and larger batches improve throughput without extra range scans.
- Advanced/Extended Life Extractors and Bio Crushers keep normal mining hardness but are Wither-proof against explosions.
- Wither Essence has a 5% base drop chance, and Ender Dragon Essence has a 10% base drop chance. Looting affects quantity only after a successful essence roll. JEI information pages explain Boss Essences and their current infusion uses.
- **Fixed**: Fixed Extended Dropper and Extended Clicker settings display initialization.
- **Fixed**: Fixed Extended Sensor crash when opening its GUI.
- **Fixed**: Added Gel Generator and Infusion Machine JEI categories and progress-arrow navigation. Gel Generator reads JDT goo spread recipes, marks gel as a non-consumed catalyst, and displays compact item/fluid recipe pages with an energy bar.
- **New**: Looting and Sharpness upgrades now appear in the JDTE creative tab. Bio Crusher dedicated Sharpness/Looting upgrade slots and target-mode controls were redesigned.
- **Balance**: Time Accelerator Time Fluid consumption now matches JDT Time Wand efficiency by default and is tunable through `timeAcceleratorFluidCostMultiplier` and `/jdte timeaccelerator fluidCostMultiplier <value>`.

#### v0.5.2
- **Fixed**: Extended Dropper GUI crash — `MACHINE_SLOTS` set to 9 in constructor, `getMachineHandler()` auto-expands old 1-slot handler to 9-slot.
- **Fixed**: Registered `Capabilities.ItemHandler.BLOCK` for all JDTE machines, enabling external pipe interaction (Mekanism, etc.).

#### v0.5.1
- **GuideME**: Fixed bilingual page display — default language pages moved to root, English pages moved to `_en_us` directory.
- **GuideME**: Added documentation for Life Extractor and Infusion Machine (EN/CN).
- **Fixed**: Missing blockstate/model warnings for `life_fluid_block`.
- **Fixed**: `basic_glue_activator` recipe pattern — undefined `L` symbol.
- **Fixed**: GuideME `item_ids` conflicts for 5 entries.
- **Fluid bar**: Unified all machine fluid bars to right side (offset=204).
- **Filter system**: Reverted filter slot position to JDT default (8,54); added pagination (prev/next buttons + page number) for FILTER upgrade.
- **Filter state**: Sync via `FilterPagePayload` network packet.
- **Fixed**: Time accelerator filter logic — empty filter slots no longer match all blocks in whitelist mode.
- **Mode button**: Replaced vanilla Button with custom texture-rendered button for hostile/passive/all modes.

#### v0.5.0
- **Fixed**: Global filter slot bug — one machine's config no longer causes all machines to lose filter slots.
- **Fixed**: Missing rotation transforms in blockstate JSON for 12 machines — wrench rotation now works visually.
- **GUI**: Added vanilla `container/slot` slot border rendering to upgrade and filter popups.
- **GuideME**: Added English translation (14 pages), Chinese pages moved to `zh_cn/` directory.
- **Fixed**: `fluid-stabilizer.md` navigation position conflict.

#### v0.4.0
- **New**: Bio Crusher (Advanced & Extended) — kills mobs for drops and XP fluid, supports Looting and Sharpness upgrades.
- **New**: Bio Crusher can be placed above mob spawners to intercept spawn logic.
- **New**: Boss essence items — Wither Essence, Ender Dragon Essence, Elder Guardian Essence.
- **New**: Looting upgrade and Sharpness upgrade cards.
- **New**: SpawnerMixin — intercepts spawner logic for Bio Crusher.
- **New**: Config system — game-accessible via Mods > Config menu.
- **New**: Life Extractor (Advanced & Extended) — extracts life fluid from entities in range.
- **New**: Infusion Machine (Advanced & Extended) — infusion processing using gel and items.

#### v0.3.0
- *(Details not recorded)*

#### v0.2.0
- **New**: 9 upgrade card types — extended to include Filter and Creative upgrades.
- **New**: 3 time accelerator tiers — Basic, Advanced, Extended Advanced.
- **New**: Extended Advanced Time Accelerator with 8 upgrade slots.
- **New**: 6 new automation machine families — Glue Activator, Gel Generator, Fluid Stabilizer, Item/Fluid Sender, Item/Fluid Receiver.
- **New**: Upgrade and filter popup windows — independently displayed, draggable, position-persistent.
- **New**: Dynamic filter slots — expandable based on FILTER upgrade count.
- **Docs**: README and AGENTS.md synced to current code.

#### v0.1.0
- **Initial release**: Core upgrade system.
- **New**: Basic/Advanced Time Accelerator.
- **New**: 8 Extended JDT T2 machines.
- **New**: GuideME in-game documentation pages.

---

### 中文

#### v0.5.5（当前）

- **性能/新增**：三档时间加速器全部改用同一统一调度器。重叠加速器倍率完整累加，按区块发现已加载方块实体，在贡献加速器保持启用时保留已付费虚拟 tick，并根据可配置 MSPT 余量统一执行，同时支持 AE2 `IGridTickable` 设备。
- **修复/兼容**：AE2 网格 tick 现在优先于仅执行维护工作的方块实体 ticker，使 AE2 Lightning Tech 加工机器及采用相同结构的 AE2 附属机器能够被正确加速。
- **兼容/修复**：将 Just Dyna Things 加入运行测试依赖并完整适配其凝胶行为。能量驱动凝胶工作时按该模组配置逐 tick 消耗 FE，不再需要食物；创造凝胶既不消耗食物，也不消耗机器 FE。外部物品管道现在只能从凝胶发生器输出槽抽取，不再抽走凝胶、燃料或输入材料。
- **新增/兼容**：高级炼药机新增“烈焰粉输入”开关，默认关闭；开启后外部容器和管道可从任意方向输入烈焰粉。默认启用的可配置保护仍会拒绝相邻 AE2 `ICraftingProvider` 样板供应器向燃料槽插入物品，同时保留手动放入和其他配方输入。
- **新增/性能/兼容**：新增水晶培育机，拥有 8 个升级槽，可调 1-512x 并消耗时间流体催生范围内母岩，安装超频或创造升级后达到 1024x，自动将成熟晶簇回收到 9 个输出槽，并支持最多 8 个时运升级。机器使用分批范围缓存和有上限的轮询任务，不会每 Tick 全量扫描；通过通用母岩/晶簇标签、可由数据包扩展的 JDTE 标签和 Just Dyna Things 公开 API 兼容原版、JDT、AE2、Data Energistics、AE2 附属及其他常规母岩。
- **水晶培育机**：方块外观改用扩展粘胶激活器贴图，新增可配置能量容量和按时间手杖等效换算的运行 FE 消耗，自动 I/O 支持时间流体输入与 9 槽产物输出；催生 Just Dyna Things 母岩时继续尊重目标自身配置的 FE/时间流体激活成本。
- **修复/兼容**：水晶培育机现在会在催生前把 Just Dyna Things 母岩缺少的激活 FE 和时间流体精确补入目标，同时预留培育机当前 Tick 的自身运行成本，并避免只转移其中一种资源。
- **修复/性能**：耗资源母岩与普通母岩改用独立缓存、轮询游标和有上限的每 Tick 预算，避免混合范围内大量 Dyna 母岩占满批次后饿死原版/JDT/AE2 随机刻母岩。
- **平衡/性能**：水晶培育机现在按 AE2 晶体催生器的强制随机刻方式催生普通母岩；默认 8x 档位等效 6 个 AE2 晶体催生器，其他倍率按比例换算，并通过有上限的累计任务避免高倍率范围催生形成无限补算队列。
- **新增升级**：新增水晶培育机专用精准升级，使用 JDT 矿石采掘升级图标。自动采收工具应用原版精准采集附魔，由目标模组战利品表决定精准掉落，且不能与时运升级同时安装。
- **GuideME**：补充缺失的机器物品关联、方块/物品图片、配方、资源与升级表格，并在中英文总升级指南中补回时运和精准升级章节。
- **平衡**：初级时间加速器改为 16x，安装超频或创造升级后为 32x；高级版可调至 64x，安装超频或创造升级后为 128x；扩展版保持可调至 512x，安装超频或创造升级后为 1024x。初级、高级和扩展版时间流体成本分别使用固定的 1 倍、2 倍和 5 倍档位倍率。
- **开发环境**：将 AE2 Crystal Science 文件 `8112039`、AE2 Lightning Tech 和 Data Energistics 加入本地运行测试依赖。

#### v0.5.4

- **配方**：新增蚀空合金扳手合成配方。
- **新增/平衡**：新增凝胶发生器专用的时运升级。
- **新增**：加入实体抑制器。提供抑制实体更新、禁止实体生成/加入世界、禁用实体渲染、禁用方块实体渲染和禁用客户端粒子五种模式。实体模式支持敌对生物、被动生物、所有生物、指定实体类型、非生物实体和所有类型。
- **新增**：新增范围屏蔽器，提供围困与退磁两种模式。围困模式下生物仅可在范围内活动，退磁模式可屏蔽玩家磁铁但允许机器收集。
- **新增**：新增高级物品拾取器，提供 8 个标准升级槽。机器在掉落物加入世界前将其拦截，直接写入朝向一侧的相邻容器。支持拾取精妙存储超大掉落物，以及ME 接口、ExtendedAE 扩展接口和超大接口直接写入ME 网络。
- **新增**：手持升级卡蹲下右键 JDT 或 JDTE 机器，可在手中数量允许时尽量填充所有有效普通或专用升级槽。启用 FTB Ultimine 时，会按顺序为当前选区中的合格机器尽量填充。
- **新增**：蚀空合金扳手可通过两次左键设置角点框选区域，使用 JDT 范围效果预览并实时显示 X/Y/Z 尺寸，随后左键可调范围机器即可精确写入。Shift+左键取消框选，服务端会校验机器半径与偏移上限。两个角点选定后框选会保持锁定，成功应用后仍保留预览，可将同一范围复制到多台机器；只有 Shift+左键才能清空。
- **Jade**：机器信息栏现在会显示已安装的普通和专用升级，包括物品图标、本地化名称及合并后的数量。
- **兼容性**：蚀空合金扳手可旋转或安全快捷拆除其支持的方块。
- **兼容**：物品发送器和接收器现在会优先查询带方向的物品能力，再回退到无方向能力，可按 Mekanism 机器配置的输入/输出面正常交互；tooltip 中的可用模式提示已改为灰色。
- **自动 I/O**：方向按钮现在按“关闭、自动输入输出（默认颜色）、自动输入（橙色）、自动输出（蓝色）”循环，机器实际路由不支持的模式会被跳过。
- **性能**：自动 I/O 单次传输量更改为 10,000 件物品或 1,000,000 mB/tick；物品/流体发送器和接收器默认更改为64 件或 20,000 mB/tick，安装超频或创造升级后传输速度加快。
- **命名**：初级、高级和扩展“流体放置器”改为“流体发送器”。
- **修复**：高级物品拾取器升级槽现在只接受并提示范围升级和过滤升级。
- **性能**：降低机器范围预览性能损耗。
- **修复**：修复蚀空合金扳手新版 FTB Ultimine 不兼容，导致范围调整失效。

#### v0.5.3

- **GUI**：机器 GUI 改为固定嵌入式升级面板，不再使用可拖动弹窗。4 槽机器使用右侧 1x4 面板，8 槽机器使用左右两个 1x4 面板；空升级槽现在显示可用升级类型、当前数量、上限、机器不兼容状态，以及超频/降频互斥提示。调整大部分机器布局。
- **新增**：为具备真实物品或流体 I/O 的机器加入绝对方向自动输入输出面板。
- **新增**：新增战利品制造机。它使用可复用刷怪蛋作为生物模板，消耗生命流体、时间流体和 FE，生产对应生物战利品。支持红石控制、自动输入输出、4 个刷怪蛋输入、分页输出、8 个标准升级槽、容量升级和最多 3 张抢夺升级。
- 可在同一轮进度中并行处理所有已放入的刷怪蛋。消耗按参与刷怪蛋计算，完成后只按成功产出的项目扣除对应生命流体、时间流体和 FE。
- 抢夺升级现在会在战利品表随机后应用可配置额外复制掉落，并按可配置百分比提高生命/时间流体消耗，默认每张抢夺升级 +50%。
- Boss 与兼容处理：凋灵和远古守卫者刷怪蛋流体消耗为 10 倍，末影龙为 100 倍；凋灵配方包含下界之星；龙之研究末影龙制造结果包含龙心和按配置计算的龙尘。
- **新增**：高级生物粉碎机通过原版物品掉落路径在被击杀实体位置生成捕获的战利品；扩展生物粉碎机会在有空间时写入分页输出库存，并可通过容量升级扩展有效输出容量。
- 生物粉碎机经验流体改为按实体最终修正后的经验奖励计算，包含神化刷怪笼“回响”等效果，且不会生成经验球。
- 生物粉碎机刷怪笼集成会在实体创建前接管刷怪周期，保留原版/神化刷怪数据和延迟逻辑，捕获完整虚拟死亡；机器无法处理时仍由原刷怪笼逻辑继续执行。
- 生物粉碎机和生命提取器敌对/友好模式现在按敌对实体类型判断，不再依赖当前 AI 攻击状态。
- **新增**：新增生物粉碎机可选龙之研究混沌守卫兼容，通过服务端配置分别控制破坏水晶和假玩家归因致命攻击。
- **新增**：灌注机可使用能唯一标识生物的一整组掉落物、64 B 生命流体和 100,000 FE 自动合成对应刷怪蛋。
- 新增 JDTE 凋灵、末影龙、远古守卫者刷怪蛋灌注配方，使用对应 Boss 精华和 64 B 生命流体。通用扫描排除凋灵，避免重复生成下界之星配方。
- 刷怪蛋灌注配方只通过单一路径注册到 JEI，不再重复，并优先显示在灌注机配方列表中。凝胶发生器、灌注机和炼药机配方提供稳定 ID，JEI 书签按钮可正常使用。
- 灌注机现在可通过 NeoForge 物品流体能力填充兼容流体容器，包括桶、其它模组容器、玻璃瓶装水，以及蜂蜜类流体制作蜂蜜瓶。
- 生命苹果现在可无上限永久累计提升最大生命值、护甲和护甲韧性；tooltip、JEI 信息页和 GuideME 已更新，tooltip 为红色，GuideME 使用表格说明成长计算。
- **新增**：新增高级炼药机，包含 3 个瓶子输入、6 个有序材料步骤、烈焰粉燃料、3 个产物输出、水槽、时间流体槽、能量条、红石控制、速度控制和槽位 tooltip。
- 酿造流程像原版炼药台一样每个服务端 tick 推进，支持有序多步链路、服务端确认的材料槽锁定、瓶子/产物槽 GUI 单堆叠限制，以及面向瓶子/材料/燃料输入和产物输出的受限自动化语义。
- 高级炼药机时间流体费用按相对原版 400 tick 酿造步骤节省的 tick 计算，原版速度不消耗时间流体。
- 高级炼药机 JEI 配方页尽量显示从玻璃瓶和水开始的完整酿造链路，合并同一阶段的等价材料，支持装水配方、催化剂注册和点击进度箭头跳转。
- **新增**：新增蚀空合金扳手。普通右键像 JDT 核源铁扳手一样旋转机器，蹲下右键可搬运支持的机器并保留机器数据；可选 FTB Ultimine 支持批量旋转或搬运选区机器。
- 蚀空合金扳手滚轮调节范围现在要求手持扳手并按住“扳手范围修饰”键，可同时调节范围机器 X/Y/Z 半径，显示绿色当前/最大范围提示，并已写入 GuideME。
- **修复**：为 JDTE 流体机器加入右键流体容器交互。
- **修复**：修复过滤翻页逻辑，包括动态槽位索引、可用页数减少时自动回到有效页、页码样式和翻页按钮尺寸/位置。
- **修复**：生物粉碎机击杀现在使用缓存假玩家、真实武器/抢夺上下文和最终 NeoForge 掉落事件，保留其它模组调整后的掉落，同时避免锋利直接击杀把重复掉落物生成到世界中。未知模组实体不再生成伪造兜底掉落。
- **修复**：生命提取器现在直接移除目标，只产生生命流体，不产生普通掉落物或经验；产量依据目标当前生命并走可配置换算，通过扩大单次批处理提高速度，不额外增加范围扫描次数。
- 高级/扩展生命提取器和生物粉碎机保留正常挖掘硬度，但设为凋灵级不可爆破。
- 凋灵精华基础掉落概率为 5%，末影龙精华基础掉落概率为 10%；抢夺只在成功触发精华后影响数量。JEI 信息页说明了 Boss 精华和当前灌注用途。
- **修复**：修复扩展高级投掷器、扩展高级点击器设置显示初始化。
- **修复**：修复扩展传感器打开时游戏崩溃。
- **修复**：新增凝胶发生器和灌注机 JEI 分类及进度箭头跳转。凝胶发生器读取 JDT 凝胶扩散配方，凝胶槽标记为不消耗，并显示紧凑物品/流体配方页和能耗条。
- **新增**：抢夺升级和锋利升级现在会显示在 JDTE 创造标签页；生物粉碎机专用锋利/抢夺升级槽和目标模式控件重新布局。
- **平衡**：时间加速器时间流体消耗默认按 JDT 时间手杖效率折算，并可通过 `timeAcceleratorFluidCostMultiplier` 和 `/jdte timeaccelerator fluidCostMultiplier <value>` 调整。


#### v0.5.2
- **修复**：扩展投掷器 GUI 崩溃 — 构造函数设置 `MACHINE_SLOTS=9`，`getMachineHandler()` 自动扩展旧存档的 1 槽 handler 到 9 槽。
- **修复**：为所有 JDTE 机器注册 `Capabilities.ItemHandler.BLOCK`，使外部管道（如 Mekanism）可以交互。

#### v0.5.1
- **GuideME**：修复中英文双语页面显示 — 默认语言页面移至根目录，英文页面移至 `_en_us` 目录。
- **GuideME**：新增生命提取器和灌注机的中英文文档。
- **修复**：`life_fluid_block` 缺少 blockstate 和 model 导致的警告。
- **修复**：`basic_glue_activator` 配方中未定义的 `L` 符号。
- **修复**：GuideME 中 5 个 `item_ids` 冲突警告。
- **流体条**：统一所有机器的流体条到右侧（偏移量 204）。
- **过滤系统**：过滤槽位置恢复为 JDT 默认（8,54）；安装过滤升级后增加翻页功能（上一页/下一页按钮和页码显示）。
- **过滤状态**：通过 `FilterPagePayload` 网络包同步到服务端。
- **修复**：时间加速器过滤逻辑 — 空过滤槽时白名单模式不再加速所有方块。
- **模式按钮**：用自绘贴图按钮替换原版 Button，支持敌对/友好/全部三种模式。

#### v0.5.0
- **修复**：全局过滤槽 bug — 一台机器的配置不再导致所有机器的过滤槽消失。
- **修复**：12 台机器的 blockstate JSON 缺少旋转变换 — 扳手旋转现在视觉生效。
- **GUI**：升级弹窗和过滤弹窗增加原版 `container/slot` 槽位边框渲染。
- **GuideME**：新增英文翻译（14 页），中文页面移至 `zh_cn/` 目录。
- **修复**：`fluid-stabilizer.md` 导航位置冲突。

#### v0.4.0
- **新增**：生物粉碎机（高级和扩展） — 杀死生物产生掉落物和经验流体，支持抢夺和锋利升级。
- **新增**：生物粉碎机可放置在刷怪笼上方拦截刷怪逻辑。
- **新增**：BOSS 精华物品 — 凋灵精华、末影龙精华、远古守卫者精华。
- **新增**：抢夺升级和锋利升级卡。
- **新增**：SpawnerMixin — 拦截刷怪笼逻辑。
- **新增**：可配置系统 — 支持游戏内 Mods > Config 编辑。
- **新增**：生命提取器（高级和扩展） — 从范围内生物提取生命流体。
- **新增**：灌注机（高级和扩展） — 使用凝胶和物品进行灌注加工。

#### v0.3.0
- *（未记录详细内容）*

#### v0.2.0
- **新增**：9 种升级卡 — 扩展到包含过滤升级和创造升级。
- **新增**：3 种时间加速器等级 — 初级、高级、扩展高级。
- **新增**：扩展高级时间加速器支持 8 个升级槽。
- **新增**：6 种自动化机器族 — 粘胶激活器、凝胶发生器、流体稳定器、物品/流体发送器、物品/流体接收器。
- **新增**：升级和过滤悬浮弹窗 — 可独立显示、拖动、位置持久化。
- **新增**：动态过滤槽 — 根据过滤升级数量动态增加。
- **文档**：同步更新 README 和 AGENTS.md。

#### v0.1.0
- **初始发布**：核心升级系统。
- **新增**：初级/高级时间加速器。
- **新增**：8 种扩展 JDT T2 机器。
- **新增**：GuideME 游戏内文档页面。
