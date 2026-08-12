---
navigation:
  title: 温室矩阵
  icon: "jdte:greenhouse_matrix_controller"
  position: 19.7
item_ids:
  - jdte:greenhouse_matrix_controller
  - jdte:greenhouse_matrix_quick_install_upgrade
  - jdte:greenhouse_matrix_casing
  - jdte:greenhouse_matrix_item_input
  - jdte:greenhouse_matrix_item_output
  - jdte:greenhouse_matrix_fluid_input
  - jdte:greenhouse_matrix_energy_input
  - jdte:greenhouse_matrix_speed
  - jdte:greenhouse_matrix_efficiency
  - jdte:greenhouse_matrix_seed
  - jdte:greenhouse_matrix_essence
  - jdte:greenhouse_matrix_auto_crafting
---

# 温室矩阵

<ItemImage id="jdte:greenhouse_matrix_controller" scale="2" />

<ItemGrid>
  <ItemIcon id="jdte:greenhouse_matrix_casing" />
  <ItemIcon id="jdte:greenhouse_matrix_item_input" />
  <ItemIcon id="jdte:greenhouse_matrix_item_output" />
  <ItemIcon id="jdte:greenhouse_matrix_fluid_input" />
  <ItemIcon id="jdte:greenhouse_matrix_energy_input" />
  <ItemIcon id="jdte:greenhouse_matrix_speed" />
  <ItemIcon id="jdte:greenhouse_matrix_efficiency" />
  <ItemIcon id="jdte:greenhouse_matrix_seed" />
  <ItemIcon id="jdte:greenhouse_matrix_essence" />
  <ItemIcon id="jdte:greenhouse_matrix_auto_crafting" />
</ItemGrid>

温室矩阵是可变尺寸的封闭长方体多方块结构，三个方向均可为 **5～18 格**。六个外表面必须完全由矩阵外壳、控制器或端口组成，并且只能有一个控制器。四种端口各至少需要一个。

矩阵外壳会在六个方向自动连接相邻的外壳、控制器和端口，隐藏内部玻璃接缝，只在连续结构玻璃的外边缘保留金属框。

内部可放置普通温室、大型温室（含其结构方块）与矩阵增强块。控制器每 5 秒重新校验一次，也会在打开管理界面时立即校验。界面显示结构尺寸、受管温室数量和增强数量，可统一暂停所有内部温室，也可统一关闭内部作物渲染。

## 端口

- 物品输入端口：只向内部温室的植物模板槽输入。
- 物品输出端口：从控制器的中央长数量产物缓冲区抽取。
- 时间流体输入端口：向所有内部温室分配时间流体。
- 能量输入端口：向所有内部温室分配 FE。

控制器不暴露能力，自动化管道必须连接对应端口。

管理界面的“自动 I/O”默认开启。开启后，各端口会主动与紧贴其矩阵外侧的容器交互：物品输入端口拉取植物模板，物品输出端口推送产物，时间流体和能量输入端口分别主动吸入对应资源。管道仍可直接连接端口，并与自动 I/O 同时使用。

## 集中模拟生产

结构成型后，内部温室不再逐台执行配方解析、资源检查和产出 tick。控制器分批读取它们的模板、倍率和升级，把完全相同的种植槽合并为生产组，再按真实经过的游戏 tick 直接计算整组产量。因此数千台相同配置温室稳定运行时，只需要结算少量生产组。

FE 与时间流体仍实际保存在内部温室中，由所有已加载成员组成统一资源池。新产物直接进入控制器的持久化中央缓冲，不再写入内部温室的旧输出槽；旧槽中已有的物品不会被删除。普通输出端口、主动自动输出和已绑定的 AE 输出升级都从中央缓冲取物，AE 可按长数量批量上传。

真实战利品表或动态作物采用每个生产组有上限的代表性抽样并按整组规模放大，避免一次收获数百万次。种子/精华转换在进入中央缓冲前执行。矩阵暂停、成员区块未加载或游戏关闭期间不会补产；中央缓冲会随世界保存，临时结构失效也不会清空。

## 方块增强

- 速度增强：每个使工作量提高 25%，最高提高 300%。
- 效率增强：每个使 FE 与时间流体消耗降低 10%，最高降低 80%。
- 种子增强：等同于内部每台温室安装种子转精华升级。
- 精华增强：等同于内部每台温室安装精华转化升级；存在多个合成表时仍不转化。

## 自动合成增强

自动合成增强方块可以保存 **16 张 AE 编码合成样板**。每个增强方块对应控制器界面中的一页；结构内安装多个增强时，页面按方块坐标稳定排序，可以用左右按钮翻页。样板实际保存在增强方块中，拆除方块时会全部掉出，不会留在控制器里。

这里只接受 AE **合成样板**，不接受处理样板。为了让矩阵只执行服务端能够证明合法的合成，样板还必须满足全部条件：

- 3×3 合成格中只能出现一种完全相同的输入物品与数据组件。
- 配方不能留下桶、瓶或其他剩余物品。
- 样板声明的主输出必须与服务端重新执行配方得到的物品、数据组件和数量完全一致。
- 输入、输出或配方无法解析时，该槽会在界面中标记为无效并且不参与生产。

矩阵结算时按配方的真实输入数量从中央长数量缓冲扣除原料，再按真实输出数量写回产物；同一次结算不会把刚合成的输出继续递归合成。这样不能通过篡改样板把 1 个精华伪造成 999 个产物。

**种子增强与自动合成增强冲突。** 两种方块同时存在时，结构校验会以 `conflicting_crafting_enhancements` 判定矩阵无效；拆除其中一种后才能重新成型。

## 全局升级安装

控制器与普通机器一样有 8 个升级槽，没有额外的专属槽位。**温室矩阵快速安装升级** 只能用于控制器，可放入这 8 个槽中的任意一个；安装后会解锁另外 8 个可堆叠的全局升级槽。放入全局槽的升级卡会被逐张转移到内部普通温室和大型温室的真实 8 槽升级栏中，而不是提供虚拟效果。

控制器会轮流分配升级卡，并遵守每台温室原有的升级数量限制与冲突规则。当前无法安装的卡会留在全局队列中；队列未清空时不能拆下快速安装升级，以免锁住物品。

<RecipeFor id="jdte:greenhouse_matrix_quick_install_upgrade" />
<RecipeFor id="jdte:greenhouse_matrix_controller" />
<RecipeFor id="jdte:greenhouse_matrix_casing" />
<RecipeFor id="jdte:greenhouse_matrix_item_input" />
<RecipeFor id="jdte:greenhouse_matrix_item_output" />
<RecipeFor id="jdte:greenhouse_matrix_fluid_input" />
<RecipeFor id="jdte:greenhouse_matrix_energy_input" />
<RecipeFor id="jdte:greenhouse_matrix_speed" />
<RecipeFor id="jdte:greenhouse_matrix_efficiency" />
<RecipeFor id="jdte:greenhouse_matrix_seed" />
<RecipeFor id="jdte:greenhouse_matrix_essence" />
<RecipeFor id="jdte:greenhouse_matrix_auto_crafting" />
