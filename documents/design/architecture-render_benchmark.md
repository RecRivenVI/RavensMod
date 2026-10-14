# 区块重建验证工具

`render_benchmark` 是只用于验证的工具组件，不进入产品 JAR。它检查独立游戏探针产生的结果，拒绝失败、不完整或未证明零面输出的报告；组件的 `check` 执行结果拒绝测试。

游戏内计数和计时位于 NeoForge 的 `src/probe/`。Mixin 只在验证运行中挂接 Sodium 的区块编译任务与统一模型入口的五参数 `getQuads`；日常实例不加载探针，不承担逐面诊断开销。实例化对照通过 Flywheel 自己的客户端命令切换实际后端，不修改第三方模组或产品开关。

回退对照实际不加载 Flywheel 和 Colorwheel；普通 BER 由探针在渲染器注册事件中替换空渲染器，仅切换验证世界的几何输出。绘制保持相同的完整原生模型，CPU 绘制与帧间隔单独计时，GPU 时间戳异步读取、不通过等待结果强制同步。工具从本地输入按名称解析 Sundial，核对登记的 SHA-256 后准备独立实例，不读取日常实例配置。

运行命令、场景与证据边界见[区块重建验证](../../validations/performance-chunk_rebuild/validation.md)与[回退对照](../../validations/performance-render_fallback/validation.md)。
