# Changelog

### English

#### v0.5.3 (Current)
- **Build system overhaul**: Refactored `build.gradle` — extracted `dependencies.gradle`, added `localRuntime`/`clientLocalRuntime` configurations, `clientRuntime` source set, `ExpandPropertiesAction`, sources jar, and jar manifest. All runtime dev dependencies from WCWT mod added.
- **JDT dependency**: Changed from local jar file to CurseMaven coordinate `curse.maven:just-dire-things-1002348:7463040`.
- **GUI Redesign Plan**: Created `GUI_REDESIGN.md` documenting all 37 machines with full component/upgrade analysis, overlap detection, and proposed embedded (popup-free) GUI layouts with calculated dimensions.
- **Upgrade panel rework**: All machines now use fixed embedded upgrade panels (no popups). 4-slot machines have a single panel right of the inventory (1×4 vertical). 8-slot machines have two panels — one right (slots 0–3) and one left of the inventory (slots 4–7). All slot positions are soft-coded in `assets/jdte/gui_layout.json`. Created `GuiUpgradeLayoutConfig` for runtime config loading.
- **Upgrade slot tooltip**: Empty upgrade slots now show available upgrade types for the machine with current count/max limits. Machine-incompatible upgrades and speed-upgrade conflicts (overclock/underclock) are dimmed.
- **Glue Activator GUI**: Advanced and Extended Glue Activator screens now use `extraWidth=60` (matching ClickerT2 screen size).
- **Gel Generator GUI final alignment**: GEL/FOOD slot X aligned with the first filter slot (x=8), input/output item columns at x=44/x=116, and separate input/output fluid bars at x=62/x=134. Progress arrow and speed button are centered between the item columns; Blacklist/MatchNBT/Redstone/Auto-balance buttons are moved to x=170. All coordinates are soft-coded through `assets/jdte/gui_layout.json` and `GuiUpgradeLayoutConfig`.
- **Gel Generator auto-balance tooltip**: Dynamic tooltip showing "Auto-balance: On"/"Auto-balance: Off" depending on state. Updated via `setTooltip()` in `toggleAutoBalance()`.
- **Fluid Sender GUI**: All three tiers (Basic/Advanced/Extended) now use `extraWidth=60` and `getFluidBarOffset()=204`, matching the Extended Time Accelerator layout for fluid tank and energy bar positioning.
- **Advanced/Extended Item Sender GUI**: Screen size matches Extended Time Accelerator (`extraWidth=60`). Blacklist/MatchNBT/Redstone/RenderArea buttons soft-coded via `item_sender_buttons` section in `gui_layout.json`, moved up 40px to y=42. Speed button X aligned 2px left of 5th filter slot (x=78), soft-coded, moved down 4px to y=44. 9 item storage slots soft-coded via `item_sender_slots`, moved up 18px to y=36, first slot X aligned with first filter slot (x=8). Area range (X/Y/Z radius) and offset adjustment buttons fully restored — `valueButtonsDoubleList`/`valueButtonsList` widgets properly registered via `addRenderableWidget()`. `addAreaButtons()` uses `UpgradeHelper.getMaxAreaRadius()` and `getMaxAreaOffset()` for dynamic max values.
- **Basic Item Sender GUI**: All buttons (Blacklist/MatchNBT/Redstone/RenderArea/Speed) and 9 item storage slots now use the same positions as Advanced/Extended, soft-coded via `basic_item_sender_buttons`/`basic_item_sender_slots` in `gui_layout.json`. Added `addMachineSlots()` override in container. Overrode `addFilterButtons()`, `addRedstoneButtons()`, `addAreaButtons()`, `addTickSpeedButton()` in screen.
- **Item slot tooltip**: All three tiers (Basic/Advanced/Extended Item Sender) now show "Item Slot" tooltip when hovering over empty machine storage slots, via `renderTooltip()` override.
- **Fluid Receiver GUI**: All three tiers (Basic/Advanced/Extended) now use `extraWidth=60` and `getFluidBarOffset()=204`, matching the Extended Time Accelerator layout for fluid tank and energy bar positioning.
- **Advanced/Extended Fluid Stabilizer GUI**: Screen size changed to `extraWidth=60`, energy bar positioned correctly.
- **Item Receiver GUI**: All three tiers (Basic/Advanced/Extended) now match their Item Sender counterparts. Buttons and slots soft-coded via `item_receiver_buttons`/`item_receiver_slots`/`basic_item_receiver_buttons`/`basic_item_receiver_slots` in `gui_layout.json`. Added "Item Slot" tooltip.
- **Life Extractor mode button**: Replaced sprite-sheet with JDT mob scanner/glowing/nightvision icons, added mode tooltips (hostile/friendly/all), soft-coded via `life_extractor_buttons` section in `gui_layout.json`. Fixed coordinate reference from `topSectionLeft` to `leftPos` for correct positioning.
- **Bio Crusher mode button**: Same redesign as Life Extractor — JDT icons, tooltips, soft-coded via `bio_crusher_buttons` section in `gui_layout.json`.
- **Fixed**: Extended Sensor GUI crash — MenuType passed JDT's `SensorT2_Container`; `instanceof SensorT2BE` check in `SensorT2Screen` caused NPE; API version mismatch corrected (JDT jar 1.5.7 uses old API).
- **Changed**: Extended Sensor filter slots increased to 9 (matching JDT Advanced Sensor).
- **New**: Gel Generator (Advanced/Extended) machine slot tooltips — shows slot type (Gel/Food/Input/Output) on hover.
- **Fixed**: Auto-balance button tooltip text changed from "Do not auto-balance" to "Auto-balance: Off".
- **Changed**: Filter pagination buttons scaled to match range button size (`PoseStack.scale 0.75`), added tooltips.
- **Fixed**: Filter page content not updating on page switch — `DynamicFilterSlot` overrides all `SlotItemHandler` methods using `this.index` to use `getSlotIndex()` instead.
- **Tweak**: Filter next button moved 4px left, both pagination buttons moved 2px down.
- **Tweak**: Filter page number text color changed to white. Later changed back to `0xFF404040` to match area label style.
- **Tweak**: Filter page number text moved from 14px to 2px right of the next button.
- **Fixed**: Removing FILTER upgrade while on a non-default filter page no longer causes filter slots to disappear — page auto-resets to 0 when current page exceeds max page.
- **New**: JEI recipe category for Advanced/Extended Gel Generator — dynamically reads JDT `GooSpreadRecipe`/`GooSpreadRecipeTag` entries and current goo-revive food tags. Recipes are split by gel tier while valid foods rotate in the food slot. The gel slot is marked as a non-consumed catalyst and now shows a "Not Consumed" tooltip. Item recipes draw the original four input/output slots with ingredients only in the first slot, move the progress arrow/output column left by 18px, add an animated energy-cost bar next to the output column, and use a compact page size. Fluid recipes use the same input/output/progress/energy column positions and also show the animated energy-cost bar.
- **Fixed**: Added JDT Fluid Placer-style right-click fluid container transfer for JDTE fluid machines. Buckets and compatible single-item fluid containers can now fill/drain Time Accelerators, Gel Generators, Fluid Senders/Receivers, Bio Crushers, Life Extractors, Infusion Machines, and Extended Fluid Collector/Placer blocks. Extended Fluid Collector/Placer also now expose `Capabilities.FluidHandler.BLOCK`.

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

#### v0.5.3（当前）
- **构建系统重构**：重写 `build.gradle`，提取 `dependencies.gradle` 分离依赖管理；新增 `localRuntime`/`clientLocalRuntime` 配置和 `clientRuntime` 源集；新增 `ExpandPropertiesAction` 处理资源替换；生成 sources jar 和 jar manifest。添加 WCWT 模组的所有开发运行时依赖。
- **JDT 依赖**：从本地 jar 文件改为 CurseMaven 坐标 `curse.maven:just-dire-things-1002348:7463040`。
- **GUI 重新设计方案**：创建 `GUI_REDESIGN.md`，详细分析全部 37 台机器的组件和升级配置，检测重叠问题，提出嵌入式的无弹窗 GUI 布局，含精确尺寸计算。
- **升级面板改造**：所有机器升级面板均改为嵌入式固定面板，去除弹窗。4 槽机器在物品栏右侧有一个纵向 1×4 面板。8 槽机器有两个面板——右侧（槽位 0–3）和物品栏左侧（槽位 4–7）。所有槽位坐标软编码到 `assets/jdte/gui_layout.json`，新增 `GuiUpgradeLayoutConfig` 运行时读取配置。
- **升级槽 tooltip**：空升级槽现在显示该机器可用的升级类型及当前限制（数量/最大数量），未兼容的升级和速度升级冲突（超频/降频）以灰色标记。
- **粘胶激活器 GUI**：高级和扩展粘胶激活器屏幕现在使用 `extraWidth=60`，与 ClickerT2 屏幕尺寸一致。
- **凝胶发生器 GUI 最终对齐**：GEL/FOOD 槽 X 坐标与第一个过滤槽对齐（x=8），输入/输出物品列位于 x=44/x=116，输入/输出流体条拆成两列并位于 x=62/x=134。进度箭头和速度按钮居中于物品列之间；黑名单/匹配NBT/红石控制/自动均分按钮移动到 x=170。所有坐标通过 `assets/jdte/gui_layout.json` 和 `GuiUpgradeLayoutConfig` 软编码。
- **凝胶发生器均分按钮 tooltip**：tooltip 根据开关状态动态显示"自动均分"/"不自动均分"，在 `toggleAutoBalance()` 中通过 `setTooltip()` 更新。
- **流体放置器 GUI**：三个等级（初级/高级/扩展）均使用 `extraWidth=60` 和 `getFluidBarOffset()=204`，与扩展时间加速器的流体罐和能量条布局对齐。
- **高级/扩展物品放置器 GUI**：界面大小匹配扩展时间加速器（`extraWidth=60`）。黑名单/匹配NBT/红石控制/渲染区域按钮通过 `item_sender_buttons` 节软编码，上移 40px 至 y=42。速度按钮软编码，相对第五个过滤槽左偏 2px（x=78），下移 4px 至 y=44。9 个物品存储槽通过 `item_sender_slots` 节软编码，上移 18px 至 y=36，第一个槽 X 坐标与第一个过滤槽对齐（x=8）。区域范围（X/Y/Z 半径）和偏移调节按钮完全恢复——`valueButtonsDoubleList`/`valueButtonsList` 控件通过 `addRenderableWidget()` 正确注册。`addAreaButtons()` 使用 `UpgradeHelper.getMaxAreaRadius()` 和 `getMaxAreaOffset()` 获取动态最大值。
- **初级物品放置器 GUI**：黑名单/匹配NBT/红石控制/渲染区域/速度按钮和 9 个物品存储槽均采用与高级/扩展版本相同的位置，通过 `basic_item_sender_buttons`/`basic_item_sender_slots` 节软编码。容器新增 `addMachineSlots()` 覆盖，屏幕覆盖 `addFilterButtons()`、`addRedstoneButtons()`、`addAreaButtons()`、`addTickSpeedButton()`。
- **物品槽 tooltip**：三个等级（初级/高级/扩展）的物品放置器在悬停于空的机器存储槽时显示"物品槽"提示，通过 `renderTooltip()` 覆盖实现。
- **流体接收器 GUI**：三个等级（初级/高级/扩展）均使用 `extraWidth=60` 和 `getFluidBarOffset()=204`，与扩展时间加速器的流体罐和能量条布局对齐。
- **高级/扩展流体稳定器 GUI**：界面大小改为 `extraWidth=60`，能量槽位置正确对齐。
- **物品接收器 GUI**：三个等级（初级/高级/扩展）现在与对应的物品放置器布局一致。按钮和槽位通过 `item_receiver_buttons`/`item_receiver_slots`/`basic_item_receiver_buttons`/`basic_item_receiver_slots` 软编码。增加"物品槽"tooltip。
- **生命提取器模式按钮**：将精灵表替换为 JDT 生物扫描/生物透视/夜视图标，增加模式 tooltip（敌对/友好/全部），通过 `life_extractor_buttons` 节软编码。修复坐标参考系问题（`topSectionLeft` → `leftPos`）以正确定位。
- **生物粉碎机模式按钮**：与生命提取器相同改造 — JDT 图标、tooltip、通过 `bio_crusher_buttons` 节软编码。
- **修复**：扩展传感器 GUI 崩溃 — MenuType 注册传入 JDT 的 `SensorT2_Container`；`SensorT2Screen` 的 `instanceof SensorT2BE` 检查导致 NPE；JDT jar 1.5.7 为旧版 API，修正 API 版本不匹配。
- **变更**：扩展传感器过滤槽改为 9 个（与 JDT 高级传感器一致）。
- **新增**：凝胶发生器（高级/扩展）机器槽位 tooltip — 鼠标悬停时显示槽位类型（凝胶/食物/输入/输出）。
- **修复**：自动均分按钮 tooltip 文本从"不自动均分"改为"取消均分"。
- **变更**：过滤翻页按钮缩放至与范围按钮相同大小（`PoseStack.scale 0.75`），并添加 tooltip。
- **修复**：翻页后过滤槽内容不更新 — `DynamicFilterSlot` 重写 `SlotItemHandler` 中所有使用 `this.index` 的方法，改为调用 `getSlotIndex()`。
- **调整**：过滤下一页按钮左移 4px，两翻页按钮下移 2px。
- **调整**：过滤页码文字颜色改为白色，后改为 `0xFF404040` 以匹配区域标签样式。
- **调整**：过滤页码文字从下一页按钮右侧 14px 处移至 2px 处。
- **修复**：移除过滤升级后非默认页的过滤槽不再消失 — 当前页超出最大页时自动重置到第 0 页。
- **新增**：高级/扩展凝胶发生器的 JEI 配方界面 — 动态读取 JDT `GooSpreadRecipe`/`GooSpreadRecipeTag` 和当前凝胶复活食物标签。配方按凝胶等级拆分，同一凝胶等级的可用食物在食物槽内轮换。凝胶槽标记为不消耗的催化剂，并显示“不消耗”tooltip；物品配方绘制原来的四个输入/输出槽，但只在第一个槽显示物品，并将进度箭头和输出列左移 18px，在输出列右侧增加动态能耗条，页面尺寸改为更紧凑；流体配方使用与物品页相同的输入/输出/进度/能量列位置，并同样显示动态能耗条。
- **修复**：为 JDTE 流体机器加入类似 JDT 流体放置器的右键流体容器转移逻辑。水桶和兼容的单物品流体容器现在可对时间加速器、凝胶发生器、流体放置/接收器、生物粉碎机、生命提取器、灌注机、扩展流体收集器/放置器进行填充或抽取。扩展流体收集器/放置器同步补充 `Capabilities.FluidHandler.BLOCK` 能力。

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
- **新增**：6 种自动化机器族 — 粘胶激活器、凝胶发生器、流体稳定器、物品/流体放置器、物品/流体接收器。
- **新增**：升级和过滤悬浮弹窗 — 可独立显示、拖动、位置持久化。
- **新增**：动态过滤槽 — 根据过滤升级数量动态增加。
- **文档**：同步更新 README 和 AGENTS.md。

#### v0.1.0
- **初始发布**：核心升级系统。
- **新增**：初级/高级时间加速器。
- **新增**：8 种扩展 JDT T2 机器。
- **新增**：GuideME 游戏内文档页面。
