# 渲染依赖与测试输入

`1.21.1-neoforge` 的全部自定义模型方块统一使用可选 Flywheel 持久化网格与 GPU 实例化，两个标准立方块保持原版路径。Flywheel 编译 API 的版本由该 Target 的 `target.properties` 定义；依赖来自 [Flywheel 官方 Maven](https://maven.createmod.net/dev/engine-room/flywheel/)。它是客户端可选依赖，不内嵌进发行 JAR；未安装或后端关闭时统一回退到原生烘焙模型，安装本模组不要求安装 Flywheel。Fabric Target 不使用该实验。

## 测试客户端

Sodium、Iris、Flywheel 和 Colorwheel 作为本地输入登记在 `inputs.toml`，具体路径与 NeoForge 第一客户端的 `mods` 列表在被 Git 忽略的 `local.toml` 中。Sodium 0.6.13 是 Iris 1.8.12 正式版声明的配套版本，不与 Sodium 0.8 混用。

Iris 发布页版本为 1.8.12，原始发行 JAR 的元数据版本为 `1.8.12-snapshot+mc1.21.1-local`；本地输入记录原始元数据值，文件哈希与上游发布相同，不改写第三方 JAR。

[Colorwheel](https://github.com/djefrey/Colorwheel) 为 Flywheel 提供 Iris 后端，保留材质、切线与法线、高光纹理通路。Sundial 提供专用 Colorwheel 程序；其他着色器包可能采用兼容回退，画面仍需验收。不要同时安装 Iris Flywheel Compat，两个兼容模组互斥。

## 着色器包

iterationRP Alpha 0.8.31、Sundial Dev Build 2026-10-10 和 Sundial Core Dev Packup 2026-09-28 使用原始 ZIP，哈希登记在 `inputs.toml`，本机路径只写在 `local.toml`。`prepareShaderPacks` 核对输入后复制到 NeoForge 第一客户端的 `shaderpacks`，不解包、不修改、不进入模组 JAR，也不删除使用者的其他着色器包。

## 检查

本机执行 `spotlessApply`、`check`、`verifyRelease` 和 `verifyInputs`。着色器包是本机输入，持续集成不能取得，因此按仓库规范 W-03 执行 `checkRepository`；完整产品构建与发行验证在本机执行。
