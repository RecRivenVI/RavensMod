# 无 Flywheel 的渲染回退对照

## 目标

在 Minecraft 1.21.1 NeoForge、Sodium、Iris 和 Sundial 下，实际不安装 Flywheel 或 Colorwheel，比较原生烘焙区块路径与普通方块实体渲染。小场景放置每种自定义模型各 1 个，密集场景各 4 个；两种路径的模型、UV、PBR、状态、位置、镜头、分辨率与着色器一致。

## 运行

从仓库根目录执行；着色器仅从已登记的本地输入复制到本验证实例。探针创建独立平坦世界，不读取日常实例，不使用 GUI 自动化。

```powershell
.\gradlew.bat :render_benchmark:prepareFallback
.\gradlew.bat :version:1.21.1-neoforge:runValidationPerformanceRenderFallbackClient
.\gradlew.bat :render_benchmark:verifyFallback
```

## 通过标准

`result/fallback.properties` 的 `passed=true`，模组列表不含 Flywheel 与 Colorwheel。每组完成 3 次重建预热、12 次重建计时、30 帧绘制预热和 90 帧计时；烘焙路径返回完整地形面数，BER 路径返回零地形面并在绘制阶段实际提交模型。逐次数据保留在 `result/rebuilds.csv` 与 `result/frames.csv`。

重建耗时为 Sodium 编译任务历时；CPU 绘制耗时覆盖渲染帧事件，帧间隔包含等待与交换，GPU 耗时由异步时间戳查询取得。通过不要求某条路径达到固定加速比；选择回退方案时同时考虑稳定绘制成本和区块更新尖峰。光照算法属于两条原生路径的差异，资产与材质输入不变；画面与交互仍由使用者验收。
