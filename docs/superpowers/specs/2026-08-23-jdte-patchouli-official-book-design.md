# JDTE Patchouli 手册并入 JDT 官方手册设计规格

## 背景

JDTE 当前维护一套独立的 Patchouli 书籍 `jdte:jdte_guide`，包含 6 个分类、每种语言 34 个条目，并通过 `jdte_guide` 配方单独获得。Just Dire Things 的官方手册标识为 `justdirethings:justdirethingsbook`；Just Dyna Things 通过向该手册的资源路径追加分类和条目实现集成。

## 目标

- 让 JDTE 的 Patchouli 内容直接显示在 Just Dire Things 官方手册中。
- 保留 JDTE 当前 6 个分类和条目顺序，避免内容重排或丢失。
- 采用与 Just Dyna Things 相同的官方书籍资源合并方式。
- 删除 JDTE 独立 Patchouli 书籍及其合成配方。

## 非目标

- 不重写现有 Patchouli 条目正文、页面类型、配方引用或本地化文本。
- 不把 JDTE 内容重新映射到 JDT 的 `Machines`、`Items`、`Upgrades` 等既有分类。
- 不修改当前 GuideME 文档资源；GuideME 与本次 Patchouli 资源迁移保持独立。
- 不增加运行时 Java 注册代码；该集成只依赖资源合并。

## 方案

将原有资源从 JDTE 自己的书籍路径迁移到 JDT 官方书籍路径：

```text
原路径：assets/jdte/patchouli_books/jdte_guide/<locale>/categories/*.json
目标路径：assets/justdirethings/patchouli_books/justdirethingsbook/<locale>/categories/*.json

原路径：assets/jdte/patchouli_books/jdte_guide/<locale>/entries/*.json
目标路径：assets/justdirethings/patchouli_books/justdirethingsbook/<locale>/entries/jdte/*.json
```

分类文件继续使用现有文件名：

- `upgrades_tools.json`
- `time_energy.json`
- `logistics_automation.json`
- `greenhouses_resources.json`
- `biology_life.json`
- `control_special.json`

由于资源命名空间从 `jdte` 变为 `justdirethings`，所有条目中的分类引用从 `jdte:<category>` 更新为 `justdirethings:<category>`。分类图标、条目图标、物品 ID、方块 ID、配方 ID 和实体/流体 ID 仍保持 `jdte:` 命名空间。

官方 JDT 的 `book.json` 不做修改；它继续提供官方手册标题、封面和入口。JDTE 只追加官方手册资源，因此在安装 JDTE 后，官方手册会出现 JDTE 的 6 个分类。

## 删除内容

以下独立书籍资源全部删除：

- `src/main/resources/data/jdte/patchouli_books/jdte_guide/book.json`
- `src/main/resources/assets/jdte/patchouli_books/jdte_guide/`
- `src/main/resources/data/jdte/recipe/jdte_guide.json`

同时删除 `en_us.json` 和 `zh_cn.json` 中仅服务于独立 Patchouli 书籍的以下语言键：

- `item.jdte.patchouli_guide.name`
- `item.jdte.patchouli_guide.landing`

GuideME 使用的 `item.jdte.guide`、`item.jdte.guide.tooltip` 和相关 `guideme_guides`/`guides` 资源保留不变。

## 数据流与兼容性

Patchouli 在加载资源时会合并不同模组提供的同一本书资源。JDTE 使用 `assets/justdirethings/...` 作为资源命名空间，因而能追加到 JDT 官方书籍，而不需要依赖 JDT 的内部 Java 类或反射。

删除独立配方不会影响存档数据：当前独立书籍是 Patchouli 的书籍组件，不是 JDTE 注册物品，也没有需要迁移的方块实体或玩家数据。玩家之后通过 JDT 官方手册入口访问 JDTE 内容。

## 验证方案

实现后执行以下检查：

1. 解析所有迁移后的中英文 JSON，确保 JSON 语法有效。
2. 检查 6 个分类和全部中英文条目均位于 `justdirethingsbook` 路径。
3. 检查所有条目的分类引用都能对应目标分类，且不再存在 `jdte:<category>` 引用。
4. 检查仓库中不再存在 `jdte_guide` 或 `patchouli_guide` 的独立书籍/配方引用。
5. 运行 `./gradlew compileJava` 和 `./gradlew jar`，确认资源处理和项目编译通过。

## 验收标准

- 在安装 Patchouli、JDT 和 JDTE 的客户端中，打开 `justdirethings:justdirethingsbook` 可看到 6 个 JDTE 分类及其全部条目。
- 英文和简体中文条目均可加载，且图标、配方页面和页面链接仍指向 JDTE 内容。
- 不再生成或提供 `jdte:jdte_guide` 独立书籍配方。
- 现有 GuideME 手册功能不受影响。
