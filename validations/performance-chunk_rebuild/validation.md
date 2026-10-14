# 区块重建与持久化网格对照

## 目标

在 Minecraft 1.21.1 NeoForge 与 Sodium 的真实区块编译任务中确认塑料椅和石膏头像返回零面，并比较同一区块段的原生与实例化路径。独立平坦世界在一个 16³ 区块段放置 18 个头像、1 把椅子、1 个王座和其余每种自定义模型各 1 个，覆盖全部 9 种模型；两个阶段不改变几何、位置、UV、PBR 或场景内容。通过 Flywheel 客户端命令切换真实的 `off` 与 `DEFAULT` 后端，不通过产品开关模拟关闭。

## 运行

从仓库根目录执行；本地输入须已登记并可用。探针自行创建验证世界、计时、写出固定路径结果并退出，不操作日常存档。

```powershell
.\gradlew.bat :version:1.21.1-neoforge:runValidationPerformanceChunkRebuildClient
.\gradlew.bat :render_benchmark:verifyBenchmark
```

## 通过标准

`result/benchmark.properties` 的 `passed=true`，原生阶段的模型面数与资产一致，实例化阶段全部自定义模型的区块输出为零，两个阶段各完成 6 次预热和 24 次计时。每次均覆盖场景中的全部模型实例，报告包含实际后端身份和总面数。计时结束后改变 G3 的方向、上下半部和形态，确认 Flywheel 重建了持有新状态的 Visual，记录 `state_update_recreated=true`。`result/samples.csv` 保留每次实际 Sodium `ChunkBuilderMeshingTask.execute` 的 CPU 耗时与返回面数。

此计时不包含排队、主线程上传、GPU 绘制、shader 编译或首次网格创建，不是 FPS 测试。耗时如实报告，不以固定加速倍数作为通过门槛。模型、材质、光照和交互的最终观感由使用者验收。
