# 高级机器设置复制器全量设置实现计划

> **面向 AI 代理的工作者：** 必需子技能：使用 'subagent-driven-development'（推荐）或 'executing-plans' 逐任务实现此计划。步骤使用复选框（'- [ ]'）语法来跟踪进度。

**目标：** 让高级机器设置复制器在同类型 JDT/JDTE 机器之间复制所有界面和设置 payload 可调的配置，同时排除库存、资源、进度、缓存和世界拓扑状态。

**架构：** 在现有 JDT 复制流程外增加版本化 'MachineSettingsSnapshot'，由显式的 Codec 注册表按精确方块实体类型解析机器专用设置。复制流程先完成类型、字段、物品和升级槽校验，再原子扣除升级材料并应用不可变解析结果。

**技术栈：** Java 21、NeoForge 21.1、Minecraft 1.21.1、NBT 'CompoundTag'、'ItemStackHandler'、JUnit 5、Gradle。

---

## 文件清单

### 创建

- 'src/main/java/com/jdte/common/items/machinesettings/MachineSettingsSnapshot.java'：保存已校验的通用字段、机器类型和专用配置 NBT。
- 'src/main/java/com/jdte/common/items/machinesettings/MachineSettingsCodec.java'：定义专用配置的编码、解码和应用契约。
- 'src/main/java/com/jdte/common/items/machinesettings/MachineSettingsCodecRegistry.java'：按精确机器类型查找 Codec，并拒绝类型与 Java 实例不匹配的组合。
- 'src/main/java/com/jdte/common/items/machinesettings/MachineSettingsCodecSupport.java'：集中处理方向、枚举、六方向整数映射、ItemStack 模板和 handler NBT 的严格读写。
- 'src/main/java/com/jdte/common/items/machinesettings/JdtMachineSettingsCodecs.java'：JDT 原生机器的显式 Codec。
- 'src/main/java/com/jdte/common/items/machinesettings/JdteMachineSettingsCodecs.java'：JDTE 机器的显式 Codec。
- 'src/test/java/com/jdte/common/items/machinesettings/MachineSettingsTestFixtures.java'：创建真实 JDT/JDTE BlockEntity 测试对象和手写快照数据。
- 'src/test/java/com/jdte/common/items/machinesettings/MachineSettingsSnapshotTest.java'：版本化数据、边界值和旧数据兼容测试。
- 'src/test/java/com/jdte/common/items/machinesettings/MachineSettingsCodecTest.java'：JDT/JDTE 专用 Codec 的真实对象行为测试。

### 修改

- 'src/main/java/com/jdte/common/items/AdvancedMachineSettingsCopierData.java'：添加 'jdteMachineSettings' schema、通用字段和专用配置读写，同时保留旧节点 API。
- 'src/main/java/com/jdte/common/items/AdvancedMachineSettingsCopierItem.java'：接入快照解析、全量 handler、原子校验、通用字段应用和 Codec 应用。
- 'src/main/java/com/jdte/common/blockentities/AdvancedEnergyTransmitterBE.java'：暴露复制所需的玩家绑定快照和受控应用方法。
- 'src/main/java/com/jdte/common/blockentities/AdvancedPotionBrewerBE.java'：暴露复制锁定配方模板的受控应用方法。
- 'src/test/java/com/jdte/common/items/AdvancedMachineSettingsCopierDataTest.java'：补充新 schema 与旧格式兼容断言。
- 'src/test/java/com/jdte/common/items/AdvancedMachineSettingsCopierItemTest.java'：补充通用字段、专用升级槽、解析失败和原子粘贴测试。

所有实现任务只在上述文件范围内变更；已有的 'UltimateTimeWandEntity' 相关工作区修改不进入该分支。

## 任务 1：建立版本化机器设置快照

**文件：**

- 创建：'src/main/java/com/jdte/common/items/machinesettings/MachineSettingsSnapshot.java'
- 创建：'src/main/java/com/jdte/common/items/machinesettings/MachineSettingsCodecSupport.java'
- 修改：'src/main/java/com/jdte/common/items/AdvancedMachineSettingsCopierData.java'
- 测试：'src/test/java/com/jdte/common/items/machinesettings/MachineSettingsSnapshotTest.java'
- 测试：'src/test/java/com/jdte/common/items/AdvancedMachineSettingsCopierDataTest.java'

- [ ] **步骤 1：编写缺少新 schema 的失败测试**

在 'MachineSettingsSnapshotTest' 中先写出真实契约：

~~~
@Test
void roundTripsCommonSettingsAndCustomSettings() {
    CompoundTag copied = new CompoundTag();
    MachineSettingsSnapshot.write(copied,
            ResourceLocation.parse("justdirethings:clicker_t2"),
            7, 4, new CompoundTag());

    MachineSettingsSnapshot snapshot = MachineSettingsSnapshot.read(copied).orElseThrow();

    assertEquals(1, snapshot.schemaVersion());
    assertEquals(7, snapshot.tickSpeed());
    assertEquals(4, snapshot.direction());
    assertEquals(ResourceLocation.parse("justdirethings:clicker_t2"), snapshot.machineType());
}

@Test
void rejectsInvalidCommonValuesBeforeApplyingAnything() {
    CompoundTag copied = new CompoundTag();
    CompoundTag settings = new CompoundTag();
    settings.putInt("schemaVersion", 1);
    settings.putString("machineType", "justdirethings:clicker_t2");
    settings.putInt("tickSpeed", -1);
    settings.putInt("direction", 6);
    settings.put("custom", new CompoundTag());
    copied.put("jdteMachineSettings", settings);

    assertTrue(MachineSettingsSnapshot.read(copied).isEmpty());
}

@Test
void acceptsLegacyCopiedDataWithoutTheNewSettingsNode() {
    CompoundTag copied = new CompoundTag();
    AdvancedMachineSettingsCopierData.writeMachineType(
            copied, ResourceLocation.parse("justdirethings:clicker_t1"));

    assertTrue(MachineSettingsSnapshot.read(copied).isEmpty());
    assertEquals(Optional.of(ResourceLocation.parse("justdirethings:clicker_t1")),
            AdvancedMachineSettingsCopierData.readMachineType(copied));
}
~~~

这组测试会先因 'MachineSettingsSnapshot' 不存在而编译失败；它点名的是新 schema 的缺失，而不是测试框架行为。

- [ ] **步骤 2：运行测试确认失败原因**

运行：

~~~
.\gradlew.bat test --tests "com.jdte.common.items.machinesettings.MachineSettingsSnapshotTest" --no-daemon --console=plain
~~~

预期：编译失败，错误集中在缺失的 'MachineSettingsSnapshot' 类型或方法。

- [ ] **步骤 3：实现最小快照数据模型**

实现以下公开契约：

~~~
public record MachineSettingsSnapshot(
        int schemaVersion,
        ResourceLocation machineType,
        int tickSpeed,
        int direction,
        CompoundTag custom) {
    public static final int CURRENT_SCHEMA = 1;

    public static void write(CompoundTag copiedData, ResourceLocation machineType,
                             int tickSpeed, int direction, CompoundTag custom);

    public static Optional<MachineSettingsSnapshot> read(CompoundTag copiedData);
}
~~~

'read' 只接受 schema 1、方向 0..5、正的 tick 速度和合法 ResourceLocation；返回的 'custom' 必须是深拷贝。'AdvancedMachineSettingsCopierData' 增加节点常量和清理方法，但继续保留 'jdteAutoIoConfig'、顶层 'upgrades' 和现有读写方法。

- [ ] **步骤 4：运行测试确认通过**

运行：

~~~
.\gradlew.bat test --tests "com.jdte.common.items.machinesettings.MachineSettingsSnapshotTest" --tests "com.jdte.common.items.AdvancedMachineSettingsCopierDataTest" --no-daemon --console=plain
~~~

预期：新增测试和现有数据测试全部通过。

- [ ] **步骤 5：提交**

~~~
git add src/main/java/com/jdte/common/items/machinesettings/MachineSettingsSnapshot.java src/main/java/com/jdte/common/items/machinesettings/MachineSettingsCodecSupport.java src/main/java/com/jdte/common/items/AdvancedMachineSettingsCopierData.java src/test/java/com/jdte/common/items/machinesettings/MachineSettingsSnapshotTest.java src/test/java/com/jdte/common/items/AdvancedMachineSettingsCopierDataTest.java
git commit -m "feat(机器设置复制器): 添加版本化设置快照"
~~~

## 任务 2：实现通用字段 Codec 与类型注册表

**文件：**

- 创建：'src/main/java/com/jdte/common/items/machinesettings/MachineSettingsCodec.java'
- 创建：'src/main/java/com/jdte/common/items/machinesettings/MachineSettingsCodecRegistry.java'
- 修改：'src/main/java/com/jdte/common/items/machinesettings/MachineSettingsSnapshot.java'
- 创建：'src/test/java/com/jdte/common/items/machinesettings/MachineSettingsTestFixtures.java'
- 测试：'src/test/java/com/jdte/common/items/machinesettings/MachineSettingsCodecTest.java'

- [ ] **步骤 1：编写失败测试，证明通用设置会被读取和应用**

先增加一个真实 'BaseMachineBE' 测试夹具，使用公开的 'getTickSpeed'、'setTickSpeed'、'getDirection' 和 'setDirection'，并断言 Codec 应用后的可观察结果：

~~~
@Test
void commonCodecCopiesTickSpeedAndDirection() {
    BaseMachineBE target = MachineSettingsTestFixtures.baseMachine();
    MachineSettingsCodec codec = MachineSettingsCodecRegistry.common();
    CompoundTag encoded = new CompoundTag();
    encoded.putInt("tickSpeed", 9);
    encoded.putInt("direction", 2);

    MachineSettingsCodec.PreparedSettings prepared = codec.decode(encoded, REGISTRIES).orElseThrow();
    prepared.apply(target);

    assertEquals(9, target.getTickSpeed());
    assertEquals(2, target.getDirection());
}

@Test
void registryRejectsATypeIdWhenTheMachineInstanceDoesNotMatch() {
    BaseMachineBE machine = MachineSettingsTestFixtures.baseMachine();

    assertTrue(MachineSettingsCodecRegistry.find(
            ResourceLocation.parse("justdirethings:clicker_t2"), machine).isEmpty());
}
~~~

当前生产代码没有这个 Codec，测试应先编译失败。

- [ ] **步骤 2：运行测试确认失败**

运行：

~~~
.\gradlew.bat test --tests "com.jdte.common.items.machinesettings.MachineSettingsCodecTest.commonCodecCopiesTickSpeedAndDirection" --no-daemon --console=plain
~~~

预期：因 Codec 契约不存在而失败。

- [ ] **步骤 3：实现 Codec 契约和显式注册表**

定义以下接口：

~~~
public interface MachineSettingsCodec {
    CompoundTag encode(BaseMachineBE machine, HolderLookup.Provider registries);
    Optional<PreparedSettings> decode(CompoundTag custom, HolderLookup.Provider registries);

    interface PreparedSettings {
        void apply(BaseMachineBE machine);
    }
}
~~~

'MachineSettingsCodecRegistry' 提供 'common()' 和 'find(ResourceLocation, BaseMachineBE)'。注册表保存精确类型 ID 与 Java 实例谓词，'find' 同时检查两者；未知类型返回空值。通用 Codec 将 tick 速度写入 'common' 节点，将方向写入 'common' 节点，并在解码阶段拒绝 'tickSpeed <= 0' 或方向不在 '0..5' 的数据。

同时在测试夹具中实现以下工厂方法，所有方法都创建真实机器类而不是生产类替身：'baseMachine()'、'clicker()'、'dropper()'、'sensor()'、'inventoryHolder()'、'playerAccessor()'、'paradox()'、'advancedTimeAccelerator()'、'extendedTimeAccelerator()'、'crystalIncubator()'、'greenhouse()'、'largeGreenhouse()'、'bioFactory()'、'lifeBreeder()'、'lifeExtractor()'、'lifeSynthesisVat()'、'mineralExtractor()'、'largeMineralExtractor()'、'advancedGelGenerator()'、'bioCrusher()'、'entitySuppressor()'、'rangeBlocker()'、'timeFreezer()'、'advancedEnergyTransmitter()'、'extendedExperienceHolder()' 和 'potionBrewer()'。夹具只负责构造对象和设置初始值，不包含 Codec 的读写逻辑。

夹具还提供以下测试操作：

~~~
static void applyCodec(BaseMachineBE source, BaseMachineBE target) {
    ResourceLocation type = BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(source.getType());
    MachineSettingsCodec codec = MachineSettingsCodecRegistry.find(type, source).orElseThrow();
    CompoundTag encoded = codec.encode(source, REGISTRIES);
    MachineSettingsCodecRegistry.find(type, target).orElseThrow()
            .decode(encoded, REGISTRIES).orElseThrow().apply(target);
}

static Map<Property<?>, Comparable<?>> blockStateProperties() {
    return Map.of(BlockStateProperties.HORIZONTAL_FACING, Direction.EAST);
}
~~~

这些辅助方法只组合真实生产 API，不能复制 Codec 内部的字段或计算逻辑。测试类还导入 'java.util.function.BiConsumer'、'java.util.function.ToIntFunction'、'java.util.Optional' 和各被测真实 BlockEntity 类型，并提供 'targetOwnerId(AdvancedEnergyTransmitterBE)' 读取夹具预设的放置者 UUID，用于验证玩家绑定复制不会覆盖机器所有者。

- [ ] **步骤 4：运行通用 Codec 测试**

运行：

~~~
.\gradlew.bat test --tests "com.jdte.common.items.machinesettings.MachineSettingsCodecTest" --no-daemon --console=plain
~~~

预期：通用字段复制和错误类型拒绝测试通过。

- [ ] **步骤 5：提交**

~~~
git add src/main/java/com/jdte/common/items/machinesettings/MachineSettingsCodec.java src/main/java/com/jdte/common/items/machinesettings/MachineSettingsCodecRegistry.java src/main/java/com/jdte/common/items/machinesettings/MachineSettingsSnapshot.java src/test/java/com/jdte/common/items/machinesettings/MachineSettingsCodecTest.java
git commit -m "feat(机器设置复制器): 添加通用设置 Codec"
~~~

## 任务 3：添加 JDT 原生机器专用 Codec

**文件：**

- 创建：'src/main/java/com/jdte/common/items/machinesettings/JdtMachineSettingsCodecs.java'
- 修改：'src/main/java/com/jdte/common/items/machinesettings/MachineSettingsCodecRegistry.java'
- 修改：'src/main/java/com/jdte/common/items/machinesettings/MachineSettingsCodecSupport.java'
- 测试：'src/test/java/com/jdte/common/items/machinesettings/MachineSettingsCodecTest.java'

- [ ] **步骤 1：编写 JDT 设置失败测试**

使用真实 JDT BlockEntity 类和其公开 setter/字段，添加以下独立测试行为：

~~~
@Test
void clickerCodecCopiesAllFiveClickSettings() {
    ClickerT1BE source = MachineSettingsTestFixtures.clicker();
    source.setClickerSettings(2, 1, true, false, 37);
    ClickerT1BE target = MachineSettingsTestFixtures.clicker();

    MachineSettingsTestFixtures.applyCodec(source, target);

    assertEquals(2, target.clickType);
    assertEquals(1, target.clickTarget.ordinal());
    assertTrue(target.sneaking);
    assertFalse(target.showFakePlayer);
    assertEquals(37, target.maxHoldTicks);
}

@Test
void dropperCodecCopiesDropCountAndPickupDelay() {
    DropperT1BE source = MachineSettingsTestFixtures.dropper();
    source.setDropperSettings(7, 19);
    DropperT1BE target = MachineSettingsTestFixtures.dropper();
    target.setDropperSettings(1, 0);

    MachineSettingsTestFixtures.applyCodec(source, target);

    assertEquals(7, target.dropCount);
    assertEquals(19, target.pickupDelay);
}

@Test
void sensorCodecCopiesValuesAndBlockStateProperties() {
    SensorT1BE source = MachineSettingsTestFixtures.sensor();
    source.setSensorSettings(1, true, 13, 2);
    source.addBlockStateProperty(0, MachineSettingsTestFixtures.blockStateProperties());
    SensorT1BE target = MachineSettingsTestFixtures.sensor();

    MachineSettingsTestFixtures.applyCodec(source, target);

    assertEquals(source.sense_target, target.sense_target);
    assertEquals(13, target.senseAmount);
    assertEquals(2, target.equality);
    assertTrue(target.strongSignal);
    assertEquals(source.saveBlockStateProperties(), target.saveBlockStateProperties());
}

@Test
void inventoryHolderCodecCopiesDisplayAndComparisonSettingsWithoutInventory() {
    InventoryHolderBE source = MachineSettingsTestFixtures.inventoryHolder();
    source.saveSettings(true, true, false, true, false, true, 4);
    InventoryHolderBE target = MachineSettingsTestFixtures.inventoryHolder();
    ItemStack targetStack = new ItemStack(Items.COBBLESTONE, 3);
    target.getInventoryHolderHandler().setStackInSlot(0, targetStack);

    MachineSettingsTestFixtures.applyCodec(source, target);

    assertTrue(target.compareNBT);
    assertTrue(target.filtersOnly);
    assertFalse(target.automatedFiltersOnly);
    assertTrue(target.compareCounts);
    assertFalse(target.automatedCompareCounts);
    assertEquals(4, target.renderedSlot);
    assertTrue(target.renderPlayer);
    assertEquals(targetStack, target.getInventoryHolderHandler().getStackInSlot(0));
}

@Test
void playerAccessorCodecCopiesAllSixSidedInventoryTypes() {
    PlayerAccessorBE source = MachineSettingsTestFixtures.playerAccessor();
    for (Direction direction : Direction.values()) {
        source.updateSidedInventory(direction, direction.ordinal() + 10);
    }
    PlayerAccessorBE target = MachineSettingsTestFixtures.playerAccessor();

    MachineSettingsTestFixtures.applyCodec(source, target);

    for (Direction direction : Direction.values()) {
        assertEquals(direction.ordinal() + 10, target.sidedInventoryTypes.get(direction));
    }
}

@Test
void paradoxCodecCopiesOnlyRenderAndTargetSettings() {
    ParadoxMachineBE source = MachineSettingsTestFixtures.paradox();
    source.setRenderParadox(true, 3);
    ParadoxMachineBE target = MachineSettingsTestFixtures.paradox();
    CompoundTag targetSnapshot = new CompoundTag();
    targetSnapshot.putInt("marker", 91);
    target.snapshotData = targetSnapshot;

    MachineSettingsTestFixtures.applyCodec(source, target);

    assertTrue(target.renderParadox);
    assertEquals(3, target.targetType);
    assertEquals(targetSnapshot, target.snapshotData);
}
~~~

另外增加以下五个真实行为测试，并在每个测试中先给目标写入不同值，再验证复制结果和运行时状态：'blockBreakerCodecCopiesSneaking' 只断言 'sneaking'；'blockSwapperCodecCopiesSwapSettingsButNotBoundPartner' 断言 'swapBlocks' 与 'swap_entity_type' 并断言目标 'boundTo' 不变；'itemCollectorCodecCopiesPickupDelayAndParticles' 断言 'respectPickupDelay' 与 'showParticles'；'experienceHolderCodecCopiesDisplaySettingsButNotStoredExperience' 断言四个设置并断言目标 'exp' 与当前玩家不变；'energyTransmitterCodecCopiesParticlesButNotRuntimeTargets' 断言 'showParticles' 并断言目标发现列表不变。测试期望值使用手写数字和布尔值，不调用 Codec 自己计算期望值。

- [ ] **步骤 2：运行测试确认专用 Codec 缺失**

运行：

~~~
.\gradlew.bat test --tests "com.jdte.common.items.machinesettings.MachineSettingsCodecTest" --no-daemon --console=plain
~~~

预期：JDT 专用测试因 registry 没有对应 Codec 或解码结果为空而失败；通用 Codec 测试继续通过。

- [ ] **步骤 3：实现 JDT Codec**

在 'JdtMachineSettingsCodecs' 中按机器家族显式实现：

- Clicker：'clickType'、'clickTarget'、'sneaking'、'showFakePlayer'、'maxHoldTicks'。
- Dropper：'dropCount'、'pickupDelay'。
- Block Breaker：'sneaking'。
- Block Swapper：'swapBlocks'、'swap_entity_type'；不读取或写入 'boundTo'。
- Sensor：'sense_target'、'strongSignal'、'senseAmount'、'equality'，以及 'saveBlockStateProperties' 返回的每槽方块状态属性。过滤槽物品仍由父过滤器选项控制。
- Item Collector：'respectPickupDelay'、'showParticles'。
- Experience Holder：'targetExp'、'ownerOnly'、'collectExp'、'showParticles'；不复制 'exp' 和当前玩家。
- Energy Transmitter：'showParticles'。
- Inventory Holder：五个比较/过滤布尔值、'renderedSlot'、'renderPlayer'，以及独立 'filterBasicHandler' 的 NBT；不复制实际库存。
- Player Accessor：六个 'Direction' 到整数的映射；缺失方向按目标当前值保留，非法值拒绝整个快照。
- Paradox Machine：'renderParadox'、'targetType'；不复制 'snapshotData'、运行标记和恢复列表。

所有枚举用稳定名称保存，读取时通过 'MachineSettingsCodecSupport.readEnum' 严格拒绝未知名称；数值字段按原界面允许的范围校验。

- [ ] **步骤 4：运行 JDT Codec 测试**

运行：

~~~
.\gradlew.bat test --tests "com.jdte.common.items.machinesettings.MachineSettingsCodecTest" --no-daemon --console=plain
~~~

预期：JDT 全部专用 Codec 测试通过，且运行时库存、经验存量、快照、伙伴链接仍保持目标值。

- [ ] **步骤 5：提交**

~~~
git add src/main/java/com/jdte/common/items/machinesettings/JdtMachineSettingsCodecs.java src/main/java/com/jdte/common/items/machinesettings/MachineSettingsCodecRegistry.java src/main/java/com/jdte/common/items/machinesettings/MachineSettingsCodecSupport.java src/test/java/com/jdte/common/items/machinesettings/MachineSettingsCodecTest.java
git commit -m "feat(机器设置复制器): 支持 JDT 原生机器设置"
~~~

## 任务 4：添加 JDTE 机器专用 Codec 和受控复制 API

**文件：**

- 创建：'src/main/java/com/jdte/common/items/machinesettings/JdteMachineSettingsCodecs.java'
- 修改：'src/main/java/com/jdte/common/items/machinesettings/MachineSettingsCodecRegistry.java'
- 修改：'src/main/java/com/jdte/common/blockentities/AdvancedEnergyTransmitterBE.java'
- 修改：'src/main/java/com/jdte/common/blockentities/AdvancedPotionBrewerBE.java'
- 测试：'src/test/java/com/jdte/common/items/machinesettings/MachineSettingsCodecTest.java'

- [ ] **步骤 1：编写 JDTE 专用设置失败测试**

添加按机器家族分开的真实行为测试：

~~~
@Test
void multiplierCodecCopiesEveryAdjustableMultiplier() {
    assertMultiplierCopied(MachineSettingsTestFixtures.advancedTimeAccelerator(),
            MachineSettingsTestFixtures.advancedTimeAccelerator(), 23,
            AdvancedTimeAcceleratorBE::setMultiplier, AdvancedTimeAcceleratorBE::getMultiplier);
    assertMultiplierCopied(MachineSettingsTestFixtures.extendedTimeAccelerator(),
            MachineSettingsTestFixtures.extendedTimeAccelerator(), 23,
            ExtendedTimeAcceleratorBE::setMultiplier, ExtendedTimeAcceleratorBE::getMultiplier);
    assertMultiplierCopied(MachineSettingsTestFixtures.crystalIncubator(),
            MachineSettingsTestFixtures.crystalIncubator(), 23,
            CrystalIncubatorBE::setMultiplier, CrystalIncubatorBE::getMultiplier);
    assertMultiplierCopied(MachineSettingsTestFixtures.greenhouse(),
            MachineSettingsTestFixtures.greenhouse(), 23,
            GreenhouseBE::setMultiplier, GreenhouseBE::getMultiplier);
    assertMultiplierCopied(MachineSettingsTestFixtures.largeGreenhouse(),
            MachineSettingsTestFixtures.largeGreenhouse(), 23,
            LargeGreenhouseBE::setMultiplier, LargeGreenhouseBE::getMultiplier);
    assertMultiplierCopied(MachineSettingsTestFixtures.bioFactory(),
            MachineSettingsTestFixtures.bioFactory(), 23,
            BioFactoryBE::setMultiplier, BioFactoryBE::getMultiplier);
    assertMultiplierCopied(MachineSettingsTestFixtures.lifeBreeder(),
            MachineSettingsTestFixtures.lifeBreeder(), 23,
            LifeBreederBE::setMultiplier, LifeBreederBE::getMultiplier);
    assertMultiplierCopied(MachineSettingsTestFixtures.lifeSynthesisVat(),
            MachineSettingsTestFixtures.lifeSynthesisVat(), 23,
            LifeSynthesisVatBE::setMultiplier, LifeSynthesisVatBE::getMultiplier);
    assertMultiplierCopied(MachineSettingsTestFixtures.mineralExtractor(),
            MachineSettingsTestFixtures.mineralExtractor(), 23,
            MineralExtractorBE::setMultiplier, MineralExtractorBE::getMultiplier);
    assertMultiplierCopied(MachineSettingsTestFixtures.largeMineralExtractor(),
            MachineSettingsTestFixtures.largeMineralExtractor(), 23,
            LargeMineralExtractorBE::setMultiplier, LargeMineralExtractorBE::getMultiplier);
}

private static <T extends BaseMachineBE> void assertMultiplierCopied(
        T source, T target, int expected, BiConsumer<T, Integer> setter, ToIntFunction<T> getter) {
    setter.accept(source, expected);
    MachineSettingsTestFixtures.applyCodec(source, target);
    assertEquals(expected, getter.applyAsInt(target));
}

@Test
void modeCodecCopiesBreederExtractorCrusherSuppressorAndRangeBlockerModes() {
    LifeBreederBE breeder = MachineSettingsTestFixtures.lifeBreeder();
    breeder.setMode(2);
    LifeBreederBE breederTarget = MachineSettingsTestFixtures.lifeBreeder();
    MachineSettingsTestFixtures.applyCodec(breeder, breederTarget);
    assertEquals(LifeBreederBE.Mode.GROW_ONLY, breederTarget.getMode());

    LifeExtractorBE extractor = MachineSettingsTestFixtures.lifeExtractor();
    extractor.setMode(LifeExtractorBE.MODE_ALL);
    LifeExtractorBE extractorTarget = MachineSettingsTestFixtures.lifeExtractor();
    MachineSettingsTestFixtures.applyCodec(extractor, extractorTarget);
    assertEquals(LifeExtractorBE.MODE_ALL, extractorTarget.getMode());

    BioCrusherBE crusher = MachineSettingsTestFixtures.bioCrusher();
    crusher.setMode(BioCrusherBE.MODE_FRIENDLY);
    BioCrusherBE crusherTarget = MachineSettingsTestFixtures.bioCrusher();
    MachineSettingsTestFixtures.applyCodec(crusher, crusherTarget);
    assertEquals(BioCrusherBE.MODE_FRIENDLY, crusherTarget.getMode());

    EntitySuppressorBE suppressor = MachineSettingsTestFixtures.entitySuppressor();
    suppressor.setSettings(EntitySuppressorBE.Mode.DISABLE_ENTITY_RENDERING.ordinal(),
            EntitySuppressorBE.Target.NON_LIVING.ordinal(), true);
    EntitySuppressorBE suppressorTarget = MachineSettingsTestFixtures.entitySuppressor();
    MachineSettingsTestFixtures.applyCodec(suppressor, suppressorTarget);
    assertEquals(EntitySuppressorBE.Mode.DISABLE_ENTITY_RENDERING, suppressorTarget.getMode());
    assertEquals(EntitySuppressorBE.Target.NON_LIVING, suppressorTarget.getTarget());
    assertTrue(suppressorTarget.isBlacklist());

    RangeBlockerBE blocker = MachineSettingsTestFixtures.rangeBlocker();
    blocker.setSettings(RangeBlockerBE.Mode.SILENCE.ordinal(),
            EntitySuppressorBE.Target.SELECTED_TYPES.ordinal(), true);
    RangeBlockerBE blockerTarget = MachineSettingsTestFixtures.rangeBlocker();
    MachineSettingsTestFixtures.applyCodec(blocker, blockerTarget);
    assertEquals(RangeBlockerBE.Mode.SILENCE, blockerTarget.getMode());
    assertEquals(EntitySuppressorBE.Target.SELECTED_TYPES, blockerTarget.getTarget());
    assertTrue(blockerTarget.isBlacklist());
}

@Test
void toggleCodecCopiesFreezerGelGeneratorAndEnergyTransmitterSettings() {
    TimeFreezerBE freezer = MachineSettingsTestFixtures.timeFreezer();
    freezer.setTimeFreezeEnabled(false);
    freezer.setWeatherFreezeEnabled(true);
    TimeFreezerBE freezerTarget = MachineSettingsTestFixtures.timeFreezer();
    freezerTarget.setTimeFreezeEnabled(true);
    freezerTarget.setWeatherFreezeEnabled(false);
    MachineSettingsTestFixtures.applyCodec(freezer, freezerTarget);
    assertFalse(freezerTarget.isTimeFreezeEnabled());
    assertTrue(freezerTarget.isWeatherFreezeEnabled());

    AdvancedGelGeneratorBE gelGenerator = MachineSettingsTestFixtures.advancedGelGenerator();
    gelGenerator.setAutoBalanceInputs(true);
    AdvancedGelGeneratorBE gelGeneratorTarget = MachineSettingsTestFixtures.advancedGelGenerator();
    MachineSettingsTestFixtures.applyCodec(gelGenerator, gelGeneratorTarget);
    assertTrue(gelGeneratorTarget.isAutoBalanceInputs());

    AdvancedEnergyTransmitterBE transmitter = MachineSettingsTestFixtures.advancedEnergyTransmitter();
    transmitter.setShowParticles(false);
    AdvancedEnergyTransmitterBE transmitterTarget = MachineSettingsTestFixtures.advancedEnergyTransmitter();
    MachineSettingsTestFixtures.applyCodec(transmitter, transmitterTarget);
    assertFalse(transmitterTarget.isShowingParticles());
}

@Test
void extendedExperienceCodecCopiesSettingsWithoutStoredExperience() {
    ExtendedExperienceHolderBE source = MachineSettingsTestFixtures.extendedExperienceHolder();
    source.targetExp = 37;
    source.ownerOnly = true;
    source.collectExp = true;
    source.showParticles = false;
    source.exp = 99;

    ExtendedExperienceHolderBE target = MachineSettingsTestFixtures.extendedExperienceHolder();
    target.targetExp = 4;
    target.ownerOnly = false;
    target.collectExp = false;
    target.showParticles = true;
    target.exp = 7;

    MachineSettingsTestFixtures.applyCodec(source, target);

    assertEquals(37, target.targetExp);
    assertTrue(target.ownerOnly);
    assertTrue(target.collectExp);
    assertFalse(target.showParticles);
    assertEquals(7, target.exp);
}

@Test
void potionBrewerCodecCopiesLockedTemplatesAndFuelInputWithoutBrewingRuntime() {
    AdvancedPotionBrewerBE source = MachineSettingsTestFixtures.potionBrewer();
    NonNullList<ItemStack> templates = NonNullList.withSize(AdvancedPotionBrewerBE.TOTAL_SLOTS, ItemStack.EMPTY);
    templates.set(0, new ItemStack(Items.POTION));
    source.applyCopiedRecipeLock(true, templates);
    source.setFuelInputEnabled(false);

    AdvancedPotionBrewerBE target = MachineSettingsTestFixtures.potionBrewer();
    target.setFuelInputEnabled(true);
    target.brewerData.set(0, 8);
    target.brewerData.set(2, 17);

    MachineSettingsTestFixtures.applyCodec(source, target);

    assertTrue(target.isRecipeLocked());
    assertEquals(templates.get(0), target.getLockedRecipeTemplate(0));
    assertFalse(target.isFuelInputEnabled());
    assertEquals(8, target.getBrewProgress());
    assertEquals(17, target.getFuel());
}

@Test
void advancedEnergyTransmitterCodecCopiesPlayerBindingAndParticles() {
    UUID boundPlayer = UUID.fromString("11111111-1111-1111-1111-111111111111");
    AdvancedEnergyTransmitterBE source = MachineSettingsTestFixtures.advancedEnergyTransmitter();
    source.applyCopiedPlayerBinding(Optional.of(boundPlayer), "BoundPlayer");
    source.setShowParticles(false);

    AdvancedEnergyTransmitterBE target = MachineSettingsTestFixtures.advancedEnergyTransmitter();
    UUID targetOwner = MachineSettingsTestFixtures.targetOwnerId(target);
    MachineSettingsTestFixtures.applyCodec(source, target);

    assertEquals(Optional.of(boundPlayer), target.getBoundPlayerIdForCopy());
    assertEquals("BoundPlayer", target.getBoundPlayerNameForCopy());
    assertFalse(target.isShowingParticles());
    assertEquals(targetOwner, MachineSettingsTestFixtures.targetOwnerId(target));
}
~~~

- [ ] **步骤 2：运行测试确认 JDTE Codec 缺失**

运行：

~~~
.\gradlew.bat test --tests "com.jdte.common.items.machinesettings.MachineSettingsCodecTest" --no-daemon --console=plain
~~~

预期：新增 JDTE 测试失败，失败原因是对应 Codec 或复制辅助 API 尚未实现。

- [ ] **步骤 3：添加最小的 JDTE 复制辅助 API**

在 'AdvancedEnergyTransmitterBE' 增加只服务于配置复制的公开契约：

~~~
public Optional<UUID> getBoundPlayerIdForCopy();
public String getBoundPlayerNameForCopy();
public void applyCopiedPlayerBinding(Optional<UUID> playerId, String playerName);
~~~

应用方法只更新绑定字段、同步字段和客户端脏标记，不改变 'placedByUUID'，并清空失效的目标发现缓存。

在 'AdvancedPotionBrewerBE' 增加：

~~~
public void applyCopiedRecipeLock(boolean locked, NonNullList<ItemStack> templates);
~~~

方法复制模板列表的独立副本并刷新配方校验缓存；不能调用会清零 'brewProgress' 的完整用户交互方法，也不修改水、时间流体、燃料、输入物品、FE 或正在运行的 brew sequence。

- [ ] **步骤 4：实现 JDTE Codec**

在 'JdteMachineSettingsCodecs' 中显式实现：

- 倍率：Advanced/Extended Time Accelerator、Crystal Incubator、Greenhouse、Large Greenhouse、Bio Factory、Life Breeder、Life Synthesis Vat、Mineral Extractor 和 Large Mineral Extractor。
- 模式：Life Breeder、Life Extractor、Bio Crusher、Entity Suppressor、Range Blocker。
- 开关：Time Freezer 的时间/天气冻结、Gel Generator 的自动平衡、Energy Transmitter 的粒子显示。
- Advanced Energy Transmitter：粒子显示和玩家绑定 UUID/名称。
- Extended Experience Holder：目标经验、仅所有者、收集经验和粒子显示。
- Advanced Potion Brewer：配方锁定、锁定模板和燃料输入。

每个 'setMultiplier' 或 'setMode' 应用后调用该类已有的缓存/管理器刷新路径；不直接写入运行字段。倍率、枚举和绑定名称均在解码时限制长度和范围。

- [ ] **步骤 5：运行 JDTE Codec 测试**

运行：

~~~
.\gradlew.bat test --tests "com.jdte.common.items.machinesettings.MachineSettingsCodecTest" --no-daemon --console=plain
~~~

预期：JDTE 设置测试通过，且配方进度、机器资源和目标缓存保持目标值。

- [ ] **步骤 6：提交**

~~~
git add src/main/java/com/jdte/common/items/machinesettings/JdteMachineSettingsCodecs.java src/main/java/com/jdte/common/items/machinesettings/MachineSettingsCodecRegistry.java src/main/java/com/jdte/common/blockentities/AdvancedEnergyTransmitterBE.java src/main/java/com/jdte/common/blockentities/AdvancedPotionBrewerBE.java src/test/java/com/jdte/common/items/machinesettings/MachineSettingsCodecTest.java
git commit -m "feat(机器设置复制器): 支持 JDTE 专用机器设置"
~~~

## 任务 5：扩展全部升级槽并保持原子材料扣除

**文件：**

- 创建：'src/main/java/com/jdte/common/items/machinesettings/MachineUpgradeHandlers.java'
- 修改：'src/main/java/com/jdte/common/items/AdvancedMachineSettingsCopierData.java'
- 修改：'src/main/java/com/jdte/common/items/AdvancedMachineSettingsCopierItem.java'
- 修改：'src/test/java/com/jdte/common/items/AdvancedMachineSettingsCopierItemTest.java'

- [ ] **步骤 1：编写专用升级槽失败测试**

增加真实行为测试：

~~~
@Test
void bioCrusherCopiesLootingAndSharpnessHandlers() {
    BioCrusherBE source = MachineSettingsTestFixtures.bioCrusher();
    source.getLootingHandler().setStackInSlot(0, new ItemStack(JDTEItems.LOOTING_UPGRADE.get()));
    source.getSharpnessHandler().setStackInSlot(0, new ItemStack(JDTEItems.SHARPNESS_UPGRADE.get()));

    CompoundTag copiedData = new CompoundTag();
    MachineUpgradeHandlers.write(copiedData, source, REGISTRIES);

    BioCrusherBE target = MachineSettingsTestFixtures.bioCrusher();
    MachineUpgradeHandlers.prepare(copiedData, target, REGISTRIES).orElseThrow().apply(target);

    assertEquals(JDTEItems.LOOTING_UPGRADE.get(),
            target.getLootingHandler().getStackInSlot(0).getItem());
    assertEquals(JDTEItems.SHARPNESS_UPGRADE.get(),
            target.getSharpnessHandler().getStackInSlot(0).getItem());
}

@Test
void missingDedicatedUpgradePreventsPasteWithoutConsumingOtherCards() {
    BioCrusherBE source = MachineSettingsTestFixtures.bioCrusher();
    source.getLootingHandler().setStackInSlot(0, new ItemStack(JDTEItems.LOOTING_UPGRADE.get()));
    source.getSharpnessHandler().setStackInSlot(0, new ItemStack(JDTEItems.SHARPNESS_UPGRADE.get()));
    UpgradeHelper.getUpgradeHandler(source).setStackInSlot(0,
            new ItemStack(JDTEItems.CAPACITY_UPGRADE.get()));
    UpgradeHelper.getUpgradeHandler(source).setStackInSlot(1,
            new ItemStack(JDTEItems.CAPACITY_UPGRADE.get()));

    CompoundTag copiedData = new CompoundTag();
    MachineUpgradeHandlers.write(copiedData, source, REGISTRIES);
    ItemStack copierStack = new ItemStack(Items.STICK);
    copierStack.set(JustDireDataComponents.COPIED_MACHINE_DATA, CustomData.of(copiedData));

    Inventory inventory = new Inventory(null);
    inventory.setItem(0, new ItemStack(JDTEItems.LOOTING_UPGRADE.get()));
    inventory.setItem(1, new ItemStack(JDTEItems.SHARPNESS_UPGRADE.get()));
    inventory.setItem(2, new ItemStack(JDTEItems.CAPACITY_UPGRADE.get()));
    TrackingCopierItem copier = newTrackingCopier();
    copier.inventory = inventory;

    assertFalse(copier.consumeUpgradeCost(null, MachineSettingsTestFixtures.bioCrusher(),
            copierStack, null));
    assertEquals(1, inventory.getItem(0).getCount());
    assertEquals(1, inventory.getItem(1).getCount());
    assertEquals(1, inventory.getItem(2).getCount());
}

@Test
void legacyStandardUpgradeTagStillLoads() {
    TrackingCopierItem copier = newTrackingCopier();
    ItemStack stack = new ItemStack(Items.STICK);
    TestMachine target = new TestMachine("justdirethings:clicker_t1", 2, 0, 0, false);
    CompoundTag copiedData = new CompoundTag();
    AdvancedMachineSettingsCopierData.writeMachineType(copiedData,
            ResourceLocation.parse("justdirethings:clicker_t1"));
    ItemStackHandler legacyHandler = new ItemStackHandler(1);
    legacyHandler.setStackInSlot(0, new ItemStack(JDTEItems.CAPACITY_UPGRADE.get()));
    AdvancedMachineSettingsCopierData.writeUpgrades(copiedData,
            legacyHandler.serializeNBT(REGISTRIES));
    stack.set(JustDireDataComponents.COPIED_MACHINE_DATA, CustomData.of(copiedData));

    copier.loadSettings(null, target, stack);

    assertEquals(JDTEItems.CAPACITY_UPGRADE.get(),
            UpgradeHelper.getUpgradeHandler(target).getStackInSlot(0).getItem());
}
~~~

测试使用两个不同槽位的相同升级物品，验证重复物品需求按槽位计数，而不是按物品种类去重。

- [ ] **步骤 2：运行测试确认专用升级槽缺失**

运行：

~~~
.\gradlew.bat test --tests "com.jdte.common.items.AdvancedMachineSettingsCopierItemTest" --no-daemon --console=plain
~~~

预期：Bio Crusher 专用槽 round-trip 测试失败，现有标准升级测试保持通过。

- [ ] **步骤 3：实现升级 handler 集合和兼容格式**

新增 'MachineUpgradeHandlers'，为每台机器返回有稳定名称的 handler 集合：

~~~
public record NamedHandler(String name, ItemStackHandler handler) {}

public static List<NamedHandler> all(BaseMachineBE machine);

public record PreparedHandlers(List<NamedHandler> handlers, List<ItemStack> requiredUpgrades) {
    public void apply(BaseMachineBE machine);
}

public static void write(CompoundTag copiedData, BaseMachineBE machine,
                         HolderLookup.Provider registries);
public static Optional<PreparedHandlers> prepare(CompoundTag copiedData, BaseMachineBE machine,
                                                  HolderLookup.Provider registries);
~~~

集合至少覆盖标准 handler、Bio Crusher 的 'lootingHandler'/'sharpnessHandler'；已有自定义 'UpgradeItemStackHandler'（如 Bio Factory、Loot Fabricator、Potion Brewer）只出现一次。保存时继续写旧的顶层 'upgrades'，并额外写 'jdteUpgradeHandlers' 命名 compound；读取时先识别新命名格式，缺失时回退到旧标准格式。

解码阶段为每个 handler 创建独立的临时 handler，验证 'Size'、槽位数量、物品 NBT 和目标 handler 的物品合法性；任一 handler 失败就拒绝完整快照。'PreparedHandlers.requiredUpgrades()' 按 handler 顺序为每个非空槽生成一份 count 为一的深拷贝物品，供粘贴前统一检查材料。

- [ ] **步骤 4：实现跨所有 handler 的原子材料统计**

将 'readRequiredUpgrades' 改为遍历 'MachineUpgradeHandlers.all(machine)'，对每个非空槽生成一份 count 为一的物品需求；沿用现有 'consumed[]' 计数数组，先完整扫描背包再统一 'removeItem'。材料检查期间不调用目标 handler 的 deserialize。

- [ ] **步骤 5：运行升级测试**

运行：

~~~
.\gradlew.bat test --tests "com.jdte.common.items.AdvancedMachineSettingsCopierItemTest" --no-daemon --console=plain
~~~

预期：标准和专用升级槽测试全部通过；材料不足时目标槽位和玩家背包均保持原值。

- [ ] **步骤 6：提交**

~~~
git add src/main/java/com/jdte/common/items/machinesettings/MachineUpgradeHandlers.java src/main/java/com/jdte/common/items/AdvancedMachineSettingsCopierData.java src/main/java/com/jdte/common/items/AdvancedMachineSettingsCopierItem.java src/test/java/com/jdte/common/items/AdvancedMachineSettingsCopierItemTest.java
git commit -m "fix(机器设置复制器): 补齐专用升级槽复制"
~~~

## 任务 6：接入保存、解析、原子粘贴和自动 I/O

**文件：**

- 修改：'src/main/java/com/jdte/common/items/AdvancedMachineSettingsCopierItem.java'
- 修改：'src/main/java/com/jdte/common/items/AdvancedMachineSettingsCopierData.java'
- 测试：'src/test/java/com/jdte/common/items/AdvancedMachineSettingsCopierItemTest.java'
- 测试：'src/test/java/com/jdte/common/items/machinesettings/MachineSettingsSnapshotTest.java'

- [ ] **步骤 1：编写完整复制流程失败测试**

覆盖以下可观察行为：

~~~
@Test
void saveAndLoadCopiesCommonCustomAutoIoAndAllUpgradesForSameType() {
    TrackingCopierItem copier = newTrackingCopier();
    ItemStack stack = new ItemStack(Items.STICK);
    TestMachine source = new TestMachine("justdirethings:clicker_t1", 7,
            0b11_1111, 0b10_1010, true);
    source.setTickSpeed(9);
    source.setDirection(2);
    UpgradeHelper.getUpgradeHandler(source).setStackInSlot(0,
            new ItemStack(JDTEItems.CAPACITY_UPGRADE.get()));
    TestMachine target = new TestMachine("justdirethings:clicker_t1", 2,
            0b00_0001, 0b00_0010, true);

    copier.saveSettings(null, source, stack);
    copier.loadSettings(null, target, stack);

    assertEquals(7, target.parentSetting);
    assertEquals(9, target.getTickSpeed());
    assertEquals(2, target.getDirection());
    assertEquals(0b11_1111, target.inputMask);
    assertEquals(0b10_1010, target.outputMask);
    assertEquals(JDTEItems.CAPACITY_UPGRADE.get(),
            UpgradeHelper.getUpgradeHandler(target).getStackInSlot(0).getItem());
    assertTrue(MachineSettingsSnapshot.read(copiedData(stack)).isPresent());
}

@Test
void typeMismatchLeavesBaseCustomAutoIoAndUpgradesUntouched() {
    TrackingCopierItem copier = newTrackingCopier();
    ItemStack stack = new ItemStack(Items.STICK);
    TestMachine source = new TestMachine("justdirethings:clicker_t1", 7,
            0b11_1111, 0b10_1010, true);
    source.setTickSpeed(9);
    TestMachine target = new TestMachine("justdirethings:block_placer_t1", 2,
            0b00_0001, 0b00_0010, true);
    target.setTickSpeed(3);

    copier.saveSettings(null, source, stack);
    copier.loadSettings(null, target, stack);

    assertEquals(0, copier.baseLoadCalls);
    assertEquals(2, target.parentSetting);
    assertEquals(3, target.getTickSpeed());
    assertEquals(0b00_0001, target.inputMask);
    assertEquals(0b00_0010, target.outputMask);
}

@Test
void malformedCustomSettingsDoNotPartiallyApplyParentSettings() {
    TrackingCopierItem copier = newTrackingCopier();
    ItemStack stack = new ItemStack(Items.STICK);
    CompoundTag copiedData = new CompoundTag();
    AdvancedMachineSettingsCopierData.writeMachineType(copiedData,
            ResourceLocation.parse("justdirethings:clicker_t1"));
    CompoundTag settings = new CompoundTag();
    settings.putInt("schemaVersion", MachineSettingsSnapshot.CURRENT_SCHEMA);
    settings.putString("machineType", "justdirethings:clicker_t1");
    settings.putInt("tickSpeed", 0);
    settings.putInt("direction", 1);
    settings.put("custom", new CompoundTag());
    copiedData.put("jdteMachineSettings", settings);
    copiedData.putInt(TrackingCopierItem.PARENT_SETTING_KEY, 91);
    stack.set(JustDireDataComponents.COPIED_MACHINE_DATA, CustomData.of(copiedData));
    TestMachine target = new TestMachine("justdirethings:clicker_t1", 2, 0, 0, true);

    copier.loadSettings(null, target, stack);

    assertEquals(0, copier.baseLoadCalls);
    assertEquals(2, target.parentSetting);
}

@Test
void copierFlagsStillControlAreaOffsetFilterAndRedstone() {
    ClickerT1BE source = MachineSettingsTestFixtures.clicker();
    ClickerT1BE target = MachineSettingsTestFixtures.clicker();
    MachineSettingsTestFixtures.setParentSettings(source,
            MachineSettingsTestFixtures.nonDefaultParentSettings());
    MachineSettingsTestFixtures.setParentSettings(target,
            MachineSettingsTestFixtures.targetParentSettings());
    ParentSettings before = MachineSettingsTestFixtures.parentSettings(target);
    ItemStack stack = new ItemStack(Items.STICK);
    MachineSettingsCopier.setSettings(stack, false, false, false, false);
    AdvancedMachineSettingsCopierItem copier = new AdvancedMachineSettingsCopierItem();

    copier.saveSettings(null, source, stack);
    copier.loadSettings(null, target, stack);

    assertEquals(before, MachineSettingsTestFixtures.parentSettings(target));
}

@Test
void oldCopiedDataStillLoadsParentUpgradesAndAutoIo() {
    TrackingCopierItem copier = newTrackingCopier();
    ItemStack stack = new ItemStack(Items.STICK);
    TestMachine target = new TestMachine("justdirethings:clicker_t1", 2,
            0b00_0001, 0b00_0010, true);
    CompoundTag copiedData = new CompoundTag();
    AdvancedMachineSettingsCopierData.writeMachineType(copiedData,
            ResourceLocation.parse("justdirethings:clicker_t1"));
    AdvancedMachineSettingsCopierData.writeMasks(copiedData, 0b11_1111, 0b10_1010);
    ItemStackHandler upgrades = new ItemStackHandler(1);
    upgrades.setStackInSlot(0, new ItemStack(JDTEItems.CAPACITY_UPGRADE.get()));
    AdvancedMachineSettingsCopierData.writeUpgrades(copiedData,
            upgrades.serializeNBT(REGISTRIES));
    stack.set(JustDireDataComponents.COPIED_MACHINE_DATA, CustomData.of(copiedData));

    copier.loadSettings(null, target, stack);

    assertEquals(1, copier.baseLoadCalls);
    assertEquals(0b11_1111, target.inputMask);
    assertEquals(0b10_1010, target.outputMask);
    assertEquals(JDTEItems.CAPACITY_UPGRADE.get(),
            UpgradeHelper.getUpgradeHandler(target).getStackInSlot(0).getItem());
    assertTrue(MachineSettingsSnapshot.read(copiedData).isEmpty());
}
~~~

上述集成测试补充导入 'com.direwolf20.justdirethings.common.items.MachineSettingsCopier'、'com.direwolf20.justdirethings.common.blockentities.ClickerT1BE'、'com.jdte.setup.JDTEItems'、'net.neoforged.neoforge.items.ItemStackHandler'，并静态导入任务 2 中夹具提供的 'MachineSettingsTestFixtures.ParentSettings' 类型。

- [ ] **步骤 2：运行测试确认集成缺口**

运行：

~~~
.\gradlew.bat test --tests "com.jdte.common.items.AdvancedMachineSettingsCopierItemTest" --no-daemon --console=plain
~~~

预期：新流程测试因 'AdvancedMachineSettingsCopierItem' 尚未读取/应用快照而失败。

- [ ] **步骤 3：实现保存流程**

在 'saveSettings' 中按以下顺序处理：

1. 调用父类保存用户选中的区域、偏移、过滤器和红石数据。
2. 写入精确的 BlockEntityType ID。
3. 用通用 Codec 和类型 Codec 生成 'custom'，并写入 tick 速度、方向和 schema。
4. 写入自动 I/O 掩码；只对 'AutoIoConfigHelper' 识别的实际接口保存面配置。
5. 用 'MachineUpgradeHandlers.all' 写入标准和专用升级节点。

当源机器没有专用 Codec 时仍保留当前父复制能力，但对当前登记的 JDT/JDTE BaseMachineBE 类型全部注册 Codec；不会通过全量 BE NBT 兜底。

- [ ] **步骤 4：实现粘贴前解析和原子应用**

加入内部解析步骤：

~~~
private Optional<PreparedPaste> preparePaste(Level level, BaseMachineBE machine, ItemStack copierStack);
~~~

'PreparedPaste' 包含已解析的 'MachineSettingsSnapshot'、专用 'PreparedSettings'、全部 handler 临时值和升级需求。'useOn' 先调用 'preparePaste'，再检查和扣除材料，最后一次性调用 'PreparedPaste.apply'。'loadSettings' 也调用同一解析路径，但跳过玩家材料扣除。

应用顺序固定为：父区域/偏移/过滤器/红石、升级 handler、tick 速度、方向、专用 Codec、自动 I/O。任何解析错误都在第一步之前返回；应用阶段只使用已经校验过的整数、枚举、ItemStack 和深拷贝 NBT。

- [ ] **步骤 5：刷新机器状态并保留 JDT 选项语义**

应用完成后调用 'UpgradeHelper.syncCapacities'、'markDirtyClient' 和各机器 Codec 中已有的 manager/cache refresh。'copyArea'、'copyOffset'、'copyFilter'、'copyRedstone' 仍由 JDT 物品组件决定；传感器属性和 Inventory Holder 独立过滤器只在 'copyFilter' 为真时应用。

为使选项测试不依赖生产替身，'MachineSettingsTestFixtures' 额外提供真实 JDT 机器的 'ParentSettings' 记录、'nonDefaultParentSettings()'、'targetParentSettings()'、'setParentSettings(BaseMachineBE, ParentSettings)' 和 'parentSettings(BaseMachineBE)'；这些方法只调用 Area/Offset/Filter/Redstone 的公开接口，'copierFlagsStillControlAreaOffsetFilterAndRedstone' 用四个复制开关均为 false 验证目标设置保持不变。

- [ ] **步骤 6：运行集成测试**

运行：

~~~
.\gradlew.bat test --tests "com.jdte.common.items.AdvancedMachineSettingsCopierItemTest" --tests "com.jdte.common.items.machinesettings.MachineSettingsSnapshotTest" --no-daemon --console=plain
~~~

预期：完整同类型复制、旧数据兼容、类型拒绝、选项开关和无部分修改测试全部通过。

- [ ] **步骤 7：提交**

~~~
git add src/main/java/com/jdte/common/items/AdvancedMachineSettingsCopierItem.java src/main/java/com/jdte/common/items/AdvancedMachineSettingsCopierData.java src/test/java/com/jdte/common/items/AdvancedMachineSettingsCopierItemTest.java src/test/java/com/jdte/common/items/machinesettings/MachineSettingsSnapshotTest.java
git commit -m "fix(机器设置复制器): 复制全部机器可调设置"
~~~

## 任务 7：全量回归验证和最终整理

**文件：**

- 修改：任务 1-6 中列出的实现文件，仅在测试暴露真实重复或命名问题时重构。
- 测试：任务 1-6 中列出的测试文件。

- [ ] **步骤 1：运行复制器专项测试**

运行：

~~~
.\gradlew.bat test --tests "com.jdte.common.items.AdvancedMachineSettingsCopierDataTest" --tests "com.jdte.common.items.AdvancedMachineSettingsCopierItemTest" --tests "com.jdte.common.items.machinesettings.*" --no-daemon --console=plain
~~~

预期：专项测试全部通过，输出中没有失败或错误。

- [ ] **步骤 2：运行完整测试**

运行：

~~~
.\gradlew.bat test --no-daemon --console=plain
~~~

预期：'BUILD SUCCESSFUL'，既有项目测试没有回归失败。

- [ ] **步骤 3：运行编译和打包验证**

运行：

~~~
.\gradlew.bat compileJava jar --no-daemon --console=plain
~~~

预期：Java 编译和 jar 任务成功；仅允许出现项目已有的弃用警告，不允许新增编译错误。

- [ ] **步骤 4：执行差异和污染检查**

运行：

~~~
git diff --check
git status --short
git log --oneline -8
~~~

预期：没有空白错误；工作区只包含本功能分支的实现/测试变更；主工作区的两处用户修改仍不在该 worktree 中。
