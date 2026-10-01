---
navigation:
  title: 实体加速器
  icon: "jdte:advanced_entity_accelerator"
  position: 2.3
item_ids:
  - jdte:advanced_entity_accelerator
  - jdte:extended_entity_accelerator
---

# 实体加速器

实体加速器消耗时间流体与 FE 能量，直接加速选定区域内的生物实体（`LivingEntity`）。

## 高级实体加速器

<BlockImage id="jdte:advanced_entity_accelerator" scale="2" />

消耗时间流体和 FE 能量的基础实体加速版本。

- 可调节倍率：1x - 64x
- 安装超频或创造升级后：128x
- 升级槽位：4 个标准槽
- 支持幽灵刷怪蛋过滤，排除玩家

<RecipeFor id="jdte:advanced_entity_accelerator" />

## 扩展实体加速器

<BlockImage id="jdte:extended_entity_accelerator" scale="2" />

高级实体加速器的扩展版本，拥有 8 个升级槽。可通过扩展升级右键高级实体加速器获得。

- 可调节倍率：1x - 512x
- 安装超频或创造升级后：1024x
- 支持 8 个升级槽，可安装更多升级卡
- 更高的每 tick 实体加速处理上限

<RecipeFor id="jdte:extended_entity_accelerator" />

## 工作机制

- **AI 与位移抑制**：在加速周期内，实体的 AI 寻路、物理移动和碰撞推挤会被自动挂起抑制，防止实体因超高速移动产生异常或卡顿，同时专注于实体的内部 tick（如成长、繁殖冷却、药水效果结算等）。
- **实体过滤**：可在界面过滤槽中放置刷怪蛋标记允许或禁止加速的目标实体类别，支持黑白名单切换。玩家自动排除，不会被加速。
- **资源消耗**：消耗 JDT 时间流体与 FE 能量。当能量或流体不足以支付整个加速批次时，机器不会执行也不会扣除资源。
- **支持升级**：
  - **容量升级**：提升能量与流体容量上限。
  - **流体升级**：进一步倍增时间流体存储量。
  - **范围升级**：扩展加速的作用区域。
  - **过滤升级**：增加过滤页面，支持更多实体类型过滤。
  - **超频升级 / 创造升级**：提升至最高加速倍率，创造升级同时免除 FE 与时间流体消耗。
  - **减频升级**：锁定低速低能耗运行。
  - **AE 合成读取升级**：仅在绑定的 AE 网络有合成任务时工作。
