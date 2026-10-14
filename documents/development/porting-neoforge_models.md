# NeoForge 模型移植

`1.21.1-neoforge` 使用 NeoForge 原生几何加载器与烘焙四边形展示与 `1.21.4-fabric` 相同的产品内容。两个 Target 各自持有 Minecraft 相关源码；参考 Target 保持 `1.21.4-fabric`。

## 构建与入口

NeoForge 使用 `net.neoforged.moddev` 插件、Java 21 与 Mojang 映射。加载器版本从 `target.properties` 读取；`DeferredRegister` 在模组事件总线上按注册时机创建方块、物品、座椅实体类型和创造模式标签。入口的模组 ID 仍由 `src/main/templates/` 展开。

客户端类位于 `src/main/java/` 的 `client` 包，事件订阅限制为 `Dist.CLIENT`。独立服务端不加载几何加载器、烘焙模型或座椅的无形渲染器。注销与服务端关闭时通过 NeoForge 事件解除座椅骑乘。

## 模型与材质

`ModelGeometryLoader` 通过 `ModelEvent.RegisterGeometryLoaders` 登记，读取保留的 mesh JSON 与王座 Geo。各模型把原始位置、UV、顶点法线和状态变换写入 `QuadBakingVertexConsumer`，输出原生 `BakedQuad`；三角形仍通过重复末顶点提交四点。

每次资源重载重新建立几何缓存，模型按状态返回预烘焙的四边形。物品显示和渲染层由 NeoForge 的几何烘焙上下文提供，黑桃 A 使用 cutout，其余 mesh 使用 solid。模型资产、贴图与 `_n` / `_s` 内容不变。

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
