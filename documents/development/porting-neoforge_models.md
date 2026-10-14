# NeoForge 模型移植

`1.21.1-neoforge` 使用 NeoForge 原生几何加载器与烘焙四边形展示与 `1.21.4-fabric` 相同的产品内容。两个 Target 各自持有 Minecraft 相关源码；参考 Target 保持 `1.21.4-fabric`。

## 构建与入口

NeoForge 使用 `net.neoforged.moddev` 插件、Java 21 与 Mojang 映射。加载器版本从 `target.properties` 读取；`DeferredRegister` 在模组事件总线上按注册时机创建方块、物品、座椅实体类型和创造模式标签。入口的模组 ID 仍由 `src/main/templates/` 展开。

客户端类位于 `src/main/java/` 的 `client` 包，事件订阅限制为 `Dist.CLIENT`。独立服务端不加载几何加载器、烘焙模型或座椅的无形渲染器。注销与服务端关闭时通过 NeoForge 事件解除座椅骑乘。

## 模型与材质

`ModelGeometryLoader` 通过 `ModelEvent.RegisterGeometryLoaders` 登记，读取保留的 mesh JSON 与王座 Geo。各模型把原始位置、UV、顶点法线和状态变换写入 `QuadBakingVertexConsumer`，输出原生 `BakedQuad`；三角形仍通过重复末顶点提交四点。

每次资源重载重新建立几何缓存，模型按状态返回预烘焙的四边形。物品显示和渲染层由 NeoForge 的几何烘焙上下文提供，黑桃 A 使用 cutout，其余 mesh 使用 solid。模型资产、贴图与 `_n` / `_s` 内容不变。

NeoForge 除镜面方块和白色方块两个标准立方块外，全部 9 种模型方块使用同一个 `RoutedBakedModel` 入口。实现原生 `EntityBlock` 的 `ModelBlock` 标记同时决定渲染锚点与接入名单，注册阶段据此取得模型方块集合，不另维护客户端产品清单。

Flywheel 是可选客户端优化：后端可用时，全部自定义模型使用持久化 GPU 网格与实例化；未安装、关闭或没有可用后端时，统一返回原生烘焙四边形。路径直接读取 Flywheel 状态，不提供第二套启用开关，不强制开启第三方后端。普通逐帧 BER 只在验证探针中实现，不进入产品回退路径。

同一个无自有数据、无 tick 的 `DecorativeModelBlockEntity` 实现提供加载、卸载、状态和破坏覆层生命周期，保留已有的 `chair_model`、`david_bust_model` 身份，其余模型使用 `model_block`。每个实际使用的完整模型状态缓存一份 GPU 网格，同状态的所有位置共享；含水状态不重复分配网格。原生烘焙模型已包含形态与方向，因此实例只施加世界位置，不再重复旋转。物品展示、碰撞、放置、含水与座椅交互不变。客户端加载已有区块时为缺少渲染锚点的模型补建原生 BlockEntity，不改方块状态或资产。

资源重载由 Flywheel 清理模型与实例，方块状态改变由其原生生命周期重建 Visual 并选择新状态网格。实例化光照使用 GPU 空间采样，不沿用逐方块 CPU AO，光照与 PBR 需要人工比较。区块路径通过 `ModelData` 中的原生渲染锚点决定是否返回零面，虚拟模型捕获与物品仍返回完整网格；验证覆盖真实 Flywheel 后端切换与无 Flywheel 的两种候选回退，见[区块重建验证](../../validations/performance-chunk_rebuild/validation.md)和[回退对照](../../validations/performance-render_fallback/validation.md)。

该原型只在 NeoForge Target 验证性能渲染接口，不改变参考 Fabric Target 的产品行为。测试依赖和着色器输入见[渲染依赖](../reference/dependency-rendering.md)。

两个 Target 保持相同的 PBR 资源配置：`assets/minecraft/atlases/blocks.json` 从普通方块图集中排除本模组的 `_n` / `_s` 贴图，`assets/minecraft/optifine/texture.properties` 声明材质格式与透明裁切阈值。过滤不删除材质文件，shader 仍可按约定读取。

Minecraft 1.21.1 直接从 `models/item/` 读取物品模型，不使用 1.21.4 的 `items/` 定义。NeoForge 物品模型引用同一方块模型与展示父模型；黑桃 A 保留独立物品几何及展示参数。G3 继续保留独立展示参数和楼梯形态变换。

## 方块与座椅 API

Minecraft 1.21.1 没有 1.21.4 的 Block / Item `setId` 构造要求，实体类型构建使用字符串 ID。含水的 `updateShape` 使用 `LevelAccessor` 安排水流更新；碰撞箱按全部 BlockState 建立不可变映射。

座椅保留尺寸、坐高、单乘客限制、视角约束、安全下椅和不写入存档的语义。伤害入口使用 1.21.1 的 `hurt`，服务端成功交互使用 `InteractionResult.CONSUME`。

## 验证

从仓库根目录执行：

```powershell
.\gradlew.bat spotlessApply
.\gradlew.bat check verifyRelease
.\gradlew.bat :version:1.21.1-neoforge:runClient
```

游戏中的模型方向、UV、PBR、G3 各形态碰撞、含水与椅子交互仍需人工验收。PBR 效果需要测试实例中的 shader 支持；保留材质文件不等于已验证 shader 表现。独立服务端只在使用者已于 `local.toml` 接受 EULA 后启动。
