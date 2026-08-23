# JDTE Patchouli 官方手册集成实现计划

> **面向 AI 代理的工作者：** 必需子技能：使用 `superpowers:subagent-driven-development` 或 `superpowers:executing-plans` 逐任务实现此计划。步骤使用复选框（`- [ ]`）语法来跟踪进度。

**目标：** 将 JDTE 的 Patchouli 内容并入 Just Dire Things 官方手册，并移除独立 JDTE 手册入口。

**架构：** 采用 Patchouli 的跨模组资源合并机制，把 JDTE 的 6 个分类和每种语言 34 个条目放到 `justdirethings:justdirethingsbook` 下。条目中的分类引用改用官方手册的 `justdirethings:` 命名空间；JDTE 的物品、方块和配方引用保持 `jdte:` 命名空间。删除独立书籍定义和合成配方，不增加 Java 代码。

**技术栈：** Minecraft 1.21.1、NeoForge、Patchouli JSON 资源、PowerShell、Gradle。

---

### 任务 1：迁移官方手册资源并修正分类引用

**文件：**
- 移动：`src/main/resources/assets/jdte/patchouli_books/jdte_guide/`
- 创建目标目录：`src/main/resources/assets/justdirethings/patchouli_books/justdirethingsbook/`
- 修改：迁移后的 `en_us/entries/jdte/*.json` 和 `zh_cn/entries/jdte/*.json` 中的 `category` 字段

- [ ] **步骤 1：创建官方书籍目录并移动书籍资源**

使用 `git mv` 保留历史记录：

```powershell
$targetParent = 'src/main/resources/assets/justdirethings/patchouli_books'
New-Item -ItemType Directory -Force -Path $targetParent | Out-Null
git mv 'src/main/resources/assets/jdte/patchouli_books/jdte_guide' "$targetParent/justdirethingsbook"
```

移动后，将两个语言目录的条目文件分别放入 `entries/jdte/`：

```powershell
$bookRoot = 'src/main/resources/assets/justdirethings/patchouli_books/justdirethingsbook'
foreach ($locale in @('en_us', 'zh_cn')) {
    $entryRoot = "$bookRoot/$locale/entries"
    New-Item -ItemType Directory -Force -Path "$entryRoot/jdte" | Out-Null
    Get-ChildItem -File "$entryRoot/*.json" | ForEach-Object {
        git mv -- $_.FullName "$entryRoot/jdte/$($_.Name)"
    }
}
```

预期结果：每个语言目录有 6 个分类文件和 34 个条目文件，且条目位于 `entries/jdte/`。

- [ ] **步骤 2：只替换条目的分类命名空间**

对迁移后的 68 个条目文件执行机械替换，将：

```json
"category": "jdte:time_energy"
```

替换为：

```json
"category": "justdirethings:time_energy"
```

替换范围仅限 6 个 `category` 值；`icon`、`item`、`recipe`、`recipe2` 及其他内容中的 `jdte:` ID 不得改变。使用 UTF-8 无 BOM 写回文件，避免修改 JSON 结构和本地化正文。

- [ ] **步骤 3：检查迁移结果**

运行：

```powershell
$root = 'src/main/resources/assets/justdirethings/patchouli_books/justdirethingsbook'
foreach ($locale in @('en_us', 'zh_cn')) {
    $categories = Get-ChildItem -File "$root/$locale/categories/*.json"
    $entries = Get-ChildItem -File "$root/$locale/entries/jdte/*.json"
    if ($categories.Count -ne 6) { throw "$locale category count: $($categories.Count)" }
    if ($entries.Count -ne 34) { throw "$locale entry count: $($entries.Count)" }
}
```

预期：命令成功结束，且官方书籍目标目录下不存在旧的 `jdte_guide` 子目录。

- [ ] **步骤 4：提交资源迁移**

```powershell
git add --all -- 'src/main/resources/assets/jdte/patchouli_books' 'src/main/resources/assets/justdirethings/patchouli_books'
git commit -m "refactor(手册): 迁移 JDTE Patchouli 资源到 JDT 官方书籍"
```

### 任务 2：删除独立手册入口

**文件：**
- 删除：`src/main/resources/data/jdte/patchouli_books/jdte_guide/book.json`
- 删除：`src/main/resources/data/jdte/recipe/jdte_guide.json`
- 修改：`src/main/resources/assets/jdte/lang/en_us.json`
- 修改：`src/main/resources/assets/jdte/lang/zh_cn.json`

- [ ] **步骤 1：删除独立书籍定义和合成配方**

使用 `apply_patch` 删除 `book.json` 与 `jdte_guide.json`。不要删除 `src/main/resources/data/jdte/` 下其他配方，也不要修改 JDT 官方书籍的 `book.json`。

- [ ] **步骤 2：删除过时语言键**

从两个语言文件删除以下两行：

```json
"item.jdte.patchouli_guide.name": "...",
"item.jdte.patchouli_guide.landing": "..."
```

保留 `item.jdte.guide`、`item.jdte.guide.tooltip` 以及其他 GuideME 语言键。

- [ ] **步骤 3：确认独立入口已消失且 GuideME 资源仍在**

运行：

```powershell
rg -n -i 'jdte_guide|patchouli_guide' src/main/resources
Test-Path 'src/main/resources/assets/jdte/guideme_guides/guide.json'
Test-Path 'src/main/resources/assets/jdte/guides/jdte/guide/index.md'
```

预期：第一条命令无输出，后两条命令均返回 `True`。

- [ ] **步骤 4：提交独立入口删除**

```powershell
git add --all -- 'src/main/resources/data/jdte' 'src/main/resources/assets/jdte/lang'
git commit -m "docs(手册): 移除 JDTE 独立 Patchouli 入口"
```

### 任务 3：执行资源完整性验证

**文件：**
- 验证：`src/main/resources/assets/justdirethings/patchouli_books/justdirethingsbook/`
- 验证：`src/main/resources/assets/jdte/lang/`

- [ ] **步骤 1：解析所有官方手册 JSON**

运行：

```powershell
$root = 'src/main/resources/assets/justdirethings/patchouli_books/justdirethingsbook'
Get-ChildItem -File $root -Recurse -Filter '*.json' | ForEach-Object {
    Get-Content -Raw -LiteralPath $_.FullName | ConvertFrom-Json | Out-Null
}
```

预期：所有分类和条目 JSON 都能解析，不出现转换异常。

- [ ] **步骤 2：验证分类引用、语言配对和 JDTE ID 保留**

对每种语言的 6 个分类和 34 个条目逐个检查：

```powershell
$root = 'src/main/resources/assets/justdirethings/patchouli_books/justdirethingsbook'
$categoryNames = (Get-ChildItem -File "$root/en_us/categories/*.json").BaseName
foreach ($locale in @('en_us', 'zh_cn')) {
    foreach ($file in Get-ChildItem -File "$root/$locale/entries/jdte/*.json") {
        $json = Get-Content -Raw -LiteralPath $file.FullName | ConvertFrom-Json
        $category = $json.category -replace '^justdirethings:', ''
        if ($category -notin $categoryNames) { throw "Unknown category in $($file.Name): $($json.category)" }
        if ($json.category -like 'jdte:*') { throw "Old category namespace in $($file.FullName)" }
    }
}
if (rg -n '"category"\s*:\s*"jdte:' "$root") { throw 'Old category references remain' }
```

预期：没有未知分类或旧分类命名空间；条目中的其他 `jdte:` 内容保持原样。

- [ ] **步骤 3：检查工作区差异和空白错误**

```powershell
git diff --check
git status --short
```

确认差异只包含本计划的手册资源、语言键、独立配方删除和计划/规格文档；保留工作区原有的 Java、配置和未跟踪文件。

### 任务 4：Gradle 构建验证

**文件：**
- 验证：整个项目构建输出

- [ ] **步骤 1：编译 Java**

运行：`./gradlew compileJava`

预期：任务成功；资源迁移没有引入 Java 编译错误。

- [ ] **步骤 2：构建 JAR**

运行：`./gradlew jar`

预期：任务成功，生成的 JAR 中包含 `assets/justdirethings/patchouli_books/justdirethingsbook/` 下的 JDTE 分类和条目，不包含 `jdte:jdte_guide` 的独立书籍资源或配方。

- [ ] **步骤 3：记录最终验证结果**

再次运行 `git status --short`，确认没有构建任务错误地修改源文件；在交付说明中列出通过的命令和任何与本次改动无关的已有工作区改动。
