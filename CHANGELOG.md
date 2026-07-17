# Changelog

### English

#### v0.5.5 (Current)

- **New**: Added the Crystal Incubator with adjustable 1-512x growth acceleration or 1024x with Overclock/Creative, automatic nine-slot harvesting, eight upgrade slots, Fortune VIII, and broad budding-block support for vanilla, JDT, AE2, Data Energistics, AE2 add-ons, and Just Dyna Things.
- **New**: Added the Crystal Incubator-only Precision Upgrade, which applies vanilla Silk Touch behavior and conflicts with Fortune Upgrades.
- **New**: Added the Greenhouse with four reusable stackable plant templates, four rendered plants, 16-64 paged output slots, generic crop/flower/sapling support, Fortune III, JEI recipes, and Mystical Agriculture/Agradditions integration.
- **New**: Added the Bio Factory with reusable animal or Productive Bees specimens, three material inputs, four isolated fluid routes, 8-32 item outputs, adjustable 1-32x or 64x operation, data-driven animal products, JEI recipes, and cached client-only creature rendering.
- **New**: Added the Life Breeder with three breeding/growth modes, four feed inputs, eight collected-product outputs, adjustable 1-32x biological acceleration, instant Overclock/Creative age settlement, range/redstone/auto-I/O support, standard `Animal` compatibility, and bounded entity/drop processing.
- **Life Breeder**: Added Villager breeding and growth support, spawn-egg entity allowlist/denylist filters, JDT-native energy rendering, a compact 2x2 feed and aligned 4x2 output layout, and time-derived Life Fluid costs. Fixed the Shift-transfer crash caused by JDT's private container helper, blocked automation from extracting feed, corrected Capacity-upgraded energy-bar rendering, set the fluid multiplier to 10x, and skipped unnecessary entity/drop scans, filter walks, dirty saves, and client syncs.
- **Life Extractor**: Health above 100 now uses configurable marginal yield decay for each additional 100-health band, defaulting to a 10% reduction per band while preserving continuous total output at boundaries.
- **New**: Added the Factory Packer and portable Factory Packages for transactional area relocation. UUID-backed compressed world records preserve blocks, populated block entities, non-player entity trees, and scheduled block/fluid ticks. Packages provide a cached actual-block model preview, right-click anchoring, Alt-scroll horizontal rotation, internal absolute-coordinate remapping, public AE2 move strategies, bounded rollback/restart recovery, and asynchronous I/O.
- **Factory Packer**: Increased the configurable base X/Y/Z radius from 5 to 10; Range Upgrades now extend it from that larger baseline. New defaults allow 128 blocks per axis and a 1,000,000-block selected volume.
- **Time Accelerators**: Reworked all three tiers around a shared managed scheduler with additive overlap, chunk-based target discovery, retained virtual ticks, configurable MSPT headroom, and public AE2 `IGridTickable` acceleration. Basic runs at 16x/32x, Advanced at up to 64x/128x, and Extended at up to 512x/1024x, with fixed 1x/2x/5x Time Fluid cost tiers.
- **Crystal Incubator**: Ordinary budding blocks use bounded AE2 Growth Accelerator-style forced random ticks, while resource-consuming Just Dyna Things targets receive their exact required FE and Time Fluid. Separate caches and round-robin budgets keep mixed target areas fair and low-overhead.
- **Greenhouse**: Supports 1-32x or 64x operation, mature-block loot tables with primary products and byproducts, stack-density scaling, direct generation into adjacent inventories, automatic I/O, native four-direction connected models, and bounded batch processing.
- **Bio Factory**: Productive Bees compatibility follows Advanced Beehive outputs, productivity and operation genes, all four Productivity Upgrade tiers, Omega comb-block output, and exact item/block/fluid/entity flowering rules. Entity-type bees such as Ribbeet accept component-correct Amber specimens, including inverse-tag semantics.
- **Machine Updates**: Added the Advanced Potion Brewer Blaze Powder Input toggle with an optional AE2 pattern-provider safeguard. Gel Generators now fully support Just Dyna Things energy-powered and Creative Goo behavior while exposing only valid output slots to external extraction. Range Blocker Containment now provides six target modes, and its new public-event-based Silence mode blocks server and client-local positional sounds without mixins or reflection. Live block-entity validation prevents dismantled fields from remaining active; Demagnetization and Silence default to 1 FE/tick.
- **GuideME**: Expanded the bilingual in-game guide with missing machines, upgrades, recipes, images, and structured resource tables.
- **Fixed**: Range previews now use their actual affected and offset areas for render culling, preventing large Entity Suppressor and Range Blocker previews from disappearing after world re-entry or when the machine block is outside the camera frustum.
- **Fixed**: Added the missing Jade installed-upgrades config translation and corrected AE2 add-on acceleration paths that exposed maintenance-only block entity tickers.
- **Fixed**: Restored configured-area execution and mutable target queues for Extended Block Breakers, Block Swappers, Fluid Collectors, Fluid Placers, and Sensors. Advanced Item Collectors now collect existing drops through bounded round-robin scans and bypass an ME Interface buffer only when it cannot accept the complete stack.
- **Usability**: Completed Eclipse Alloy Wrench selections can now be resized one face at a time by looking at a boundary and using Ctrl-scroll, with one-block minimum dimensions and Create-style reversed controls from inside the selection.

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

- **新增**：加入水晶培育机，可调 1-512x 催生或通过超频/创造升级达到 1024x，支持 9 槽自动采收、8 个升级槽、时运 VIII，并通用兼容原版、JDT、AE2、Data Energistics、AE2 附属和 Just Dyna Things 母岩。
- **新增**：加入水晶培育机专用精准升级，沿用原版精准采集逻辑，并与时运升级互斥。
- **新增**：加入温室大棚，提供 4 个可复用且可堆叠的植物模板、4 株植物渲染、16-64 个分页输出槽、通用作物/花朵/树苗支持、时运 III、JEI 配方及 Mystical Agriculture/Agradditions 兼容。
- **新增**：加入生物工厂，支持可复用动物或 Productive Bees 样本、3 个材料输入、4 条独立流体路线、8-32 个物品输出槽、可调 1-32x 或 64x 运行、数据驱动物产物、JEI 配方及客户端缓存生物渲染。
- **新增**：加入生命繁育器，提供繁殖与成长、仅繁殖、仅成长三种模式，4 格饲料输入、8 格掉落物回收输出、可调 1-32x 生物时间推进、超频/创造瞬间结算，并支持范围、红石、自动 I/O、标准 `Animal` 模组生物及有界实体/掉落物处理。
- **生命繁育器**：新增村民繁殖与成长支持、按刷怪蛋实体类型生效的黑白名单过滤、JDT 原生能量条、紧凑的 2x2 饲料和对齐的 4x2 输出布局，并按繁育时间换算生命流体成本；修复调用 JDT 私有容器方法导致的 Shift 崩溃，禁止管道抽取饲料，修复容量升级后的能量条比例，将流体倍率设为 10 倍，并跳过不必要的实体/掉落物扫描、过滤遍历、脏数据保存和客户端同步。
- **生命提取器**：超过 100 生命值后，每增加一个 100 点生命值区间都会应用可配置的边际产率衰减，默认每档降低 10%，同时保持档位边界处总产量连续增长。
- **新增**：加入工厂打包机和便携工厂包裹，可事务化迁移范围内容。UUID 世界压缩记录会保留方块、含物品的方块实体、非玩家实体树及方块/流体计划 Tick；包裹支持缓存式真实方块模型预览、右键定位、Alt 滚轮水平旋转、内部绝对坐标重写、AE2 公开移动策略、有界回滚/重启续作及异步 I/O。
- **工厂打包机**：可配置基础 X/Y/Z 半径由 5 提高到 10，范围升级从新的基础值继续扩展；单轴长度和范围体积的新默认上限分别提高到 128 格与 1,000,000 格。
- **时间加速器**：三档加速器改用共享调度器，支持重叠倍率累加、按区块发现目标、保留虚拟 Tick、可配置 MSPT 余量及 AE2 `IGridTickable` 加速。初级为 16x/32x，高级最高 64x/128x，扩展最高 512x/1024x，时间流体成本固定为 1x/2x/5x 档位。
- **水晶培育机**：普通母岩使用有界的 AE2 晶体催生器式强制随机刻；Just Dyna Things 耗资源母岩会获得其实际所需的 FE 与时间流体。独立缓存与轮询预算保证混合范围公平运行并降低开销。
- **温室大棚**：支持 1-32x 或 64x 运行、成熟方块掉落表主副产物、堆叠密度消耗、产物直接生成到相邻容器、自动 I/O、原生四方向连接模型和有界批量结算。
- **生物工厂**：Productive Bees 兼容遵循高级蜂箱产出、产量与工作条件基因、四档产量升级、Omega 蜜脾块产出，以及精确物品/方块/流体/实体授粉规则。Ribbeet 等实体型蜜蜂可识别带正确组件的琥珀块，并支持反向实体标签。
- **机器调整**：高级炼药机新增烈焰粉输入开关及可选 AE2 样板供应器保护。凝胶发生器完整支持 Just Dyna Things 能量凝胶与创造凝胶，并仅向外部开放有效产物槽抽取。范围屏蔽器围困模式新增六种目标模式，并加入基于公开事件、无需 Mixin 或反射的消音模式，可屏蔽服务端及客户端本地定位声音；实时方块实体校验可防止拆除后旧消音场残留，退磁与消音默认耗能均为 1 FE/tick。
- **GuideME**：补充中英文指南缺失的机器、升级、配方、图片和结构化资源表格。
- **修复**：范围预览现在使用实际作用范围与偏移范围进行渲染裁剪，避免大型实体抑制器和范围屏蔽器的预览在重进世界后或机器方块位于视锥外时消失。
- **修复**：补充 Jade 已安装升级配置翻译，并修正仅暴露维护型方块实体 Ticker 的 AE2 附属机器加速路径。
- **修复**：恢复扩展高级方块破坏器、方块替换器、流体收集器、流体放置器和传感器的设定范围执行及可修改目标队列；高级物品拾取器新增有界轮询收集已有掉落物，并仅在 ME 接口缓冲无法完整接收时绕过缓冲直传网络。
- **易用性**：完成双角点框选后，蚀空合金扳手可对准边界面使用 Ctrl+滚轮单独扩大或缩小该面，范围最小保持 1 格，并沿用 Create 在选区内部反转滚轮方向的交互。


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
