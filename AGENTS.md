# 仓库目录与命名规范

本文件适用于整个仓库，定义 Minecraft Multi-Target Mod、非 Mod 组件、运行实例、文档和产品验证的目录职责与命名方式。

## 1. 总体结构

```text
Project/
├── versions/<target>/
│   ├── src/
│   ├── build.gradle.kts
│   └── target.properties
├── instances/<target>/
│   ├── client/
│   ├── client-multiplayer/
│   └── server/
├── components/<component>/
├── documents/
│   ├── usage/
│   ├── development/
│   ├── design/
│   ├── project/
│   ├── reference/
│   └── release/
├── validations/<type>-<topic>/
│   ├── task/
│   ├── instance/
│   ├── result/
│   ├── validation.md
│   └── validation-guide.md
├── README.md
├── AGENTS.md
├── instance.properties
├── local.properties
├── gradle/
│   ├── configuration/              # included build: raven-configuration
│   └── wrapper/
│       ├── gradle-wrapper.jar
│       └── gradle-wrapper.properties
├── gradlew
├── gradlew.bat
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
├── .git/
└── .gitignore
```

目录树表示内容的归属和允许的位置，不要求为展示结构而创建空目录。`<target>`、`<component>`、`<type>`、`<topic>` 和 `<number>` 是命名占位符，使用时替换为实际名称或编号。

## 2. Minecraft Target：versions/

`versions/` 保存 Minecraft Multi-Target Mod 的源码与构建配置。每个 `versions/<target>/` 等价于一个 Minecraft Mod 源码根目录，其内部源码、资源、构建配置和代码风格遵循对应生态。

### Target 命名

格式为 `<minecraft>-<loader>`：

- `<minecraft>` 使用 Minecraft: Java Edition 版本号。
- `<loader>` 使用小写加载器名称，仅允许 `neoforge`、`forge`、`fabric`、`quilt`。
- 示例：`1.20.1-forge`、`1.21.1-neoforge`、`26.1.2-neoforge`、`26.2-fabric`、`26.2-neoforge`。

### Target 文件职责

| 位置 | 职责 |
| --- | --- |
| `src/` | 对应生态的原生源码与资源布局 |
| `build.gradle.kts` | Target 插件、依赖、构建与运行适配 |
| `target.properties` | Minecraft、Loader、Java 等 Target Facts |

Gradle project path 使用单数命名空间：

```text
versions/<target>/ → :version:<target>
```

物理目录仍为复数 `versions/`，由根 `settings.gradle.kts` 显式注册并映射到相应的 Gradle project path。

## 3. Minecraft 实例：instances/

`instances/` 保存 Minecraft Multi-Target Mod 的运行实例。`instances/<target>/` 中的 `<target>` 与对应源码 Target 的名称一致。

| 目录 | 职责与内部布局 |
| --- | --- |
| `client/` | 客户端实例；本目录等价于 `.minecraft/`，直接存放 Minecraft 实例数据 |
| `client-multiplayer/` | 多人游戏客户端实例；本目录等价于 `.minecraft/`，直接存放 Minecraft 实例数据 |
| `server/` | 服务端实例；本目录等价于 NeoForge、Forge、Fabric、Quilt 服务端安装目录 |

客户端实例内部不再套一层 `.minecraft/`；服务端保持相应生态的原生安装目录布局。实例目录是运行数据的位置，不是独立的 Mod 源码模块。

## 4. 非 Mod 组件：components/

`components/` 保存非 Minecraft Mod 的产品组件。每个 `components/<component>/` 是对应生态的传统源码根目录，内部结构与代码风格遵循该生态。

### Component 命名

- 按组件职责命名，使用小写英文字母、数字和单词间的连字符 `-`。
- 连字符用于连接单词，不放在名称首尾，也不连续使用。
- 同一项目内组件名不得重复。
- 避免 `common`、`utils`、`misc` 等职责不明确的名称。
- 示例：`protocol-schema`、`runtime-safety`、`companion`、`web-console`、`config-editor`。

参与 Gradle 构建的组件使用单数命名空间：

```text
components/<component>/ → :component:<component>
```

物理目录仍为复数 `components/`。其他生态组件保留自己的原生构建入口，不因位于 `components/` 就必须成为 Gradle 模块。

## 5. 项目文档：documents/

`documents/` 仅使用以下六个一级分类。各分类下直接放文档，不再创建分类子目录；根目录不放普通项目文档，没有内容的分类不要求创建空目录。

| 分类 | 主要职责 | 推荐的 `<type>` |
| --- | --- | --- |
| `usage/` | 产品安装、配置、操作与使用问题处理 | `installation`、`configuration`、`guide`、`troubleshooting`、`compatibility`、`limitation`、`faq` |
| `development/` | 开发环境、构建、协作、测试、调试与移植 | `setup`、`build`、`workflow`、`testing`、`debugging`、`porting`、`contribution`、`compatibility` |
| `design/` | 系统概念、架构、机制、模型、流程、生命周期与安全设计 | `overview`、`concept`、`architecture`、`mechanism`、`model`、`flow`、`lifecycle`、`security` |
| `project/` | 产品范围、提案、计划、状态、里程碑、技术债与阶段记录 | `overview`、`scope`、`proposal`、`plan`、`status`、`milestone`、`debt` |
| `reference/` | 接口、协议、结构、命令、配置、属性、事件、格式与术语的查阅资料 | `api`、`protocol`、`schema`、`command`、`configuration`、`property`、`event`、`format`、`version`、`glossary`、`example` |
| `release/` | 发布说明、变更、升级、迁移、支持与弃用 | `release`、`changelog`、`upgrade`、`migration`、`support`、`deprecation` |

普通文档统一使用 `<type>-<topic>.md`。另有两种专用格式：

```text
documents/design/adr-<number>-<topic>.md
documents/project/phase-<number>-<topic>.md
```

- `adr`：架构或设计决策记录。
- `phase`：阶段目标、实施与历史记录。

### 文档命名要素

- `<type>` 使用单个简短的小写英文词，表示整份文档的主要形式或职责。
- 推荐词只是优先词汇，不构成白名单；没有合适推荐词时，允许根据实际职责选择新的 `<type>`。
- 同一个 `<type>` 可以出现在不同分类中，不要求全局唯一；分类决定主要职责，`<type>` 只在所在分类语境内解释。
- `<topic>` 使用小写英文字母、数字和连字符；不允许首尾连字符或连续连字符，可以由字母或数字开头、结尾。
- `<number>` 使用自然数编号，允许 `0`；可在编号后使用点号连接子编号，区分同编号下的子文档，例如 `1`、`2`、`2.1`、`2.10`。它表示编号层级，不按数学小数合并不同编号。

这些规则约束文档位置和文件名，不规定正文的固定章节或格式。按整份文档的主要职责选择分类与 `<type>`，允许正文同时涉及多个相关主题；不要为了追求分类纯度或覆盖推荐类型而拆分、创建文档。

### 容易混淆的职责边界

| 文档位置 | 主要职责 |
| --- | --- |
| `usage/configuration-*` / `reference/configuration-*` | 前者讲用户如何配置；后者提供配置项的准确、完整定义 |
| `usage/compatibility-*` / `development/compatibility-*` | 前者面向用户说明环境、Mod、硬件等能否正常使用；后者面向开发和集成过程说明兼容要求 |
| `design/overview-*` / `project/overview-*` | 前者说明系统内部如何组成和运行；后者说明产品或项目整体是什么、范围与方向是什么 |
| `development/porting-*` | 源码跨 Minecraft 版本、Loader、平台或 API 的移植 |
| `release/upgrade-*` | 用户或部署方升级版本的说明 |
| `release/migration-*` | 版本变化引起的数据、配置、接口或部署迁移 |
| `project/plan-*` | 已确定方向的实施计划，也可包含尚未实施的内部迁移方案 |

### 标准文件与现有内容

`documents/` 的治理规则不禁止 GitHub、许可证、构建工具或其他生态约定要求放在仓库根目录的标准文件，例如 `README.md`、`LICENSE`、`CONTRIBUTING.md`、`SECURITY.md`、`CODE_OF_CONDUCT.md`。这些文件保留其标准名称和位置，不因文档目录规范而擅自移动，也不要求套用 `<type>-<topic>.md`。

规则允许的位置与类型不等于必须创建的文件清单。规范调整本身不授权创建、拆分、移动现有文档或清理旧目录；实际整理应根据用户明确的任务范围处理。

## 6. 产品验证：validations/

`validations/` 用于产品级验收、回归、集成、兼容、跨 Target 一致性、协议符合性、性能、稳定性、安全性以及长期机器证据。它不等同于普通单元测试、JUnit 或 GameTest；普通源码测试仍属于对应源码和构建体系，不为统一目录而搬入这里。

每项产品验证按 `validations/<type>-<topic>/` 集中组织，将任务、运行实例、结果与说明放在同一个验证项目下。

### 验证项目命名

- `<type>` 表示验证的主要目的，使用单个简短的小写英文词。
- 推荐：`feature`、`regression`、`integration`、`compatibility`、`parity`、`conformance`、`performance`、`stability`、`migration`、`security`。
- 推荐词只是优先词汇，不构成白名单；没有合适推荐词时，允许根据验证的实际目的选择新的 `<type>`。
- `<topic>` 表示被验证的能力、问题或边界，使用与文档主题相同的字符规则。

以下三种验证目的应明确区分，不混用：

| `<type>` | 要证明什么 |
| --- | --- |
| `compatibility` | 不同环境、Mod、组件等能否共同工作 |
| `parity` | 不同 Target / Loader 的行为是否一致 |
| `conformance` | 是否符合明确的协议、Schema 或合同 |

### 验证项目内容

| 位置 | 职责 |
| --- | --- |
| `task/` | 验证程序、断言、输入与辅助工具；不限制内部目录结构 |
| `instance/` | 可选的验证专用实例；保存实例运行内容，以及不值得长期保存的验证结果与机器证据 |
| `result/` | 从 `instance/` 中提取的值得长期保存的验证结果与机器证据；不限制内部目录结构 |
| `validation.md` | 面向开发者和 Agent 的完整测试目标、测试内容、操作说明与验收说明 |
| `validation-guide.md` | 可选的简易版说明，面向一般用户介绍测试目标、测试内容、操作与验收；尽可能简洁易懂，并提供可直接从仓库根目录运行的命令 |

`validation.md` 和 `validation-guide.md` 直接放在验证项目根目录，不放进 `task/`，也不额外套一层 `documents/`。两份说明有不同的读者定位，不要求固定正文模板。

### 验证实例的 Git 策略

`validations/<type>-<topic>/instance/` 默认忽略运行内容，同时允许 Git 遍历子目录；通过 `.gitignore` 的显式 negation 白名单放行指定文件。

不要将父目录整体排除到无法重新包含指定文件。长期结果放入 `result/`，运行数据和临时证据留在默认忽略的 `instance/`。

## 7. 根文件与基础设施

| 文件或目录 | 职责 |
| --- | --- |
| `README.md` | 面向人的项目说明与使用入口 |
| `AGENTS.md` | Coding Agent 的仓库开发规范 |
| `instance.properties` | 仓库共享的实例运行默认值 |
| `local.properties` | 本机工具路径与实例覆盖；Git ignored |
| `gradle/` | Gradle 基础设施 |
| `gradle/configuration/` | properties 配置插件、项目 Loom 适配及原生测试；不是产品组件 |
| `gradle/wrapper/` | 官方 Gradle Wrapper |
| `gradle/wrapper/gradle-wrapper.jar` | Wrapper 启动与发行包下载程序 |
| `gradle/wrapper/gradle-wrapper.properties` | Gradle 版本、下载地址与校验值 |
| `gradlew` | Unix Gradle Wrapper 入口 |
| `gradlew.bat` | Windows Gradle Wrapper 入口 |
| `build.gradle.kts` | 仓库共同构建协调 |
| `settings.gradle.kts` | 显式注册分层 Gradle 模块及其目录映射 |
| `gradle.properties` | 项目身份、版本与运行环境 Profile |
| `.git/` | Git 元数据 |
| `.gitignore` | 构建输出、本机配置与实例内容的忽略规则 |

## 8. RavensModels 项目约束

共享模板和工作区父级规范只读，不能从本项目任务修改。

### 当前真实边界

- 当前只有 `versions/1.21.4-fabric/` 一个正式 Mod Target，Gradle 地址为 `:version:1.21.4-fabric`。
- 根 settings 必须同时将中间 descriptor `:version` 映射到 `versions/`，再显式映射 Target；不自动扫描目录，也不创建虚假 Target。
- 当前没有非 Mod 业务组件或仓库内产品验证项目，不为示意树创建 `components/`、`validations/` 或空文档分类。
- `src/main` 为双端安全源码，`src/client` 为客户端源码和资源；保持 Loom environment source-set split，二者属于同一个 Mod。
- 不为模板示意树补建空文件、许可证或 Workflow；文档只记录实际项目内容。

### 配置所有权

- `gradle.properties`：Raven's Models / RavensModels / ravensmodels 的 `mod_*` 产品事实；`org.gradle.*` 保留 Gradle 原生消费，不进入 Project Facts 或实例 Cascade。`mod_environment=both` 仅提供部署建议，不自动部署。
- `versions/1.21.4-fabric/target.properties`：Minecraft、Fabric Loader、Fabric API、Java 版本事实；Target 构建脚本读取这些值。
- Loom 插件及 official Mojang mappings 的选择保留在 Target 构建脚本中。结构调整不是依赖升级授权。
- `instance.properties`：共享 `instance.*` 默认值；`local.properties` 可选，提供本机工具路径和最终实例覆盖，必须被 Git ignore。唯一解析入口为根扩展 `repositoryConfig`，不在 Target 再写 properties 解析器。
- 每个文件分别按 Target+Variant > Target > Variant > Global 选第一个存在的键；匹配的 local 值总覆盖共享值。Target 用方括号。空 extra/`[]` 清空追加值；其他选中无效值报错，不回退。所有属性遵循同一规则。
- `extraJvmArgs` / `extraGameArgs` 支持多行字面参数块，兼容旧单行 JSON 数组；分别追加在 Loom 原生/项目必需参数后，不经 shell、空格拆分或逐层拼接。块内每个非空行一个参数，仅 trim 首尾，保留内部空格、反斜杠、Unicode 和引号，不解释注释、转义或续行。
- 两项分别完成 Cascade 后，最终 JVM 列表的唯一有效 `-Xmx` 优先于 memory（共享列表也可优先于 local memory）；无 `-Xmx` 或清空列表时回落到 memory。重复/无效 `-Xmx` 报错。适配器以 `effectiveMemory()` 设置原生堆，只追加 `jvmArgumentsWithoutMaxHeap()`，避免发出两个 `-Xmx`。游戏 window/player/directory 仍不允许通过 extra 定义。
- 玩家名用 `[A-Za-z0-9_]{3,16}` 校验；第一客户端仅检查自身，多人客户端检查双方并拒绝同名。Target 独立 build/test 和 server 不消费无关客户端字段；根 verifyConfiguration/check/build 则严格检查所有已声明值，可能因坏客户端配置失败。
- `java.<major>.home` 使用时校验 JDK，缺省使用 Target java_version 的 Gradle toolchain；不更换 Gradle 宿主 JVM。`launcher.prism.path` 仅为可查询工具路径，不自动启动。
- 日常运行数据位于 `instances/1.21.4-fabric/{client,client-multiplayer,server}/`。专项验证实例按实际职责单独组织，不能混进三个日常 variant。

### 产品行为与资源

- registry ID、Java package、方块状态、碰撞、放置、含水、座椅交互、模型、UV、法线和 PBR 的修改必须属于当次产品任务范围。
- 模型姿态以用户实际验收结果为准；不能只凭类名、资源名、JSON 角度或构建成功推断视觉正确。
- G3 的物品显示覆盖与其他方块分开；黑桃 A 的物品显示参数独立。左右手使用现有右手配置和 Minecraft 镜像语义，不混入旧预览中的左手值。
- `_n` / `_s` 贴图会被 shader 按约定发现；没有显式 Java/JSON 引用不代表可以删除。
- Minecraft 1.21.4 不支持 `on_shelf`；未来版本备忘不能作为新增 Target 或向当前 JSON 写入该槽位的授权。
- 不重新引入 GeckoLib、Loader 兼容框架、无职责共享层或第三方 Mod 自动管理系统。

### 工作与验证边界

- 操作前检查当前 Git 根、分支、HEAD 和工作树；保留其他未提交工作。不沿用旧任务的 commit、push、部署或启动授权。
- 游戏启动、重启和 JAR 投放需要当次用户明确授权；无游戏检查不能操作既有游戏进程。
- 构建使用 JDK 21，保留现有 Wrapper 和依赖版本。仓库根的常用无游戏检查为：

  - `.\gradlew.bat projects`
  - `.\gradlew.bat :version:1.21.4-fabric:tasks --all`
  - `.\gradlew.bat :version:1.21.4-fabric:build --no-daemon`
  - `.\gradlew.bat build --no-daemon`
- 普通单元测试、JUnit、GameTest 应位于 Target 的原生测试体系；产品验证才使用第 6 节目录。不得将没有执行的验证写成 PASS。
- 不自动搬入或修改仓库外的历史临时测试、参考素材、导出文件或 Prism 实例。其他任务的仓库和进程不属于本项目操作范围。
- 运行数据迁移前核实进程、文件占用、路径冲突、重解析点和内容归属。未知或使用中的实例保持原状；不能覆盖、删除用户世界或配置。
- 当前开发入口与验证边界见[构建与运行](documents/development/build-project.md)。本仓库目前没有自动布局检查器或 Workflow；不能将人工目录检查描述为自动治理。
- 配置规则、实际 Loader 接线与无启动验证见[配置指南](documents/development/guide-configuration.md)。配置库测试不等于 Minecraft 运行验收。
