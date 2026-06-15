# AGENTS.md

## 项目概述

Fabric Minecraft 模组，MC 26.1.2，使用 GeckoLib5 做动画模型。模组 ID：`ravensmod`。

## 构建与验证

```bash
./gradlew build          # 构建 + 打包
./gradlew runClient      # 启动客户端测试
```

**必须使用 Java 25**。MC 26.1+ 需要 Java 25。

Windows 启动方式：
```bat
set JAVA_HOME=C:\Program Files\BellSoft\LibericaJDK-25-Full
gradlew.bat runClient --no-daemon
```

## MC 26.1 迁移要点

MC 26.1 不再使用 remap 工作流。关键变化：
- Loom 插件 ID 从 `net.fabricmc.fabric-loom-remap` 改为 `net.fabricmc.fabric-loom`
- 删除所有 `mappings` 行（26.1 非混淆，不需要 mappings）
- 依赖用 `implementation` 而非 `modImplementation`
- `ResourceLocation` → `Identifier`
- `ItemGroupEvents` → `CreativeModeTabEvents`，`FabricItemGroup` → `FabricCreativeModeTab`
- GeckoLib5 包名：`software.bernie.geckolib.*` → `com.geckolib.*`

## 源码结构

采用 Fabric Loom `splitEnvironmentSourceSets()`，分两个 source set：

- `src/main/` — 服务端 + 通用代码（入口 `RavensMod`）
- `src/client/` — 客户端代码（入口 `RavensModClient`）

关键注册文件在 `src/main/java/com/recrivenvi/ravensmod/register/`：
- `BlocksX` — 方块注册
- `ItemsX` — 物品注册
- `RegisterBlockEntities` — BlockEntity 注册（共 5 个）
- `BlockEntities*Register` — 各 BlockEntity 的详细配置
- `TabsX` — 物品栏标签

客户端模型/渲染在 `src/client/java/com/recrivenvi/ravensmod/client/`：
- `BlockEntitiesModels` — GeckoLib 模型加载
- `BlockEntitiesRenderer` — BlockEntity 渲染器

## 注册顺序

`RavensMod.onInitialize()` 中依次调用：`BlocksX → RegisterBlockEntities → ItemsX`。新增注册必须遵循此顺序。

## 资源文件

- 方块模型/状态/物品 JSON 在 `src/main/resources/assets/ravensmod/`
- GeckoLib 模型（`.geo.json`）在 `src/main/resources/assets/ravensmod/geckolib/models/`
- GeckoLib 动画在 `src/main/resources/assets/ravensmod/geckolib/animations/`
- GeckoLib5 模型路径格式：`Identifier.fromNamespaceAndPath(MOD_ID, "model_name")`（不含 `geckolib/models/` 前缀，GeckoLib5 自动查找）
- Mixin 配置：`ravensmod.mixins.json`（通用）、`ravensmod.client.mixins.json`（客户端）
- 多语言：`en_us.json`、`zh_cn.json`

## 依赖

- Fabric API：`0.151.0+26.1.2`
- GeckoLib：`5.5.1`（Maven: `com.geckolib`）
- Gradle：9.5.1，Fabric Loom：1.17.11

## 已知问题

- `block_entity_2` 到 `block_entity_5` 缺少 `facing` 方向的 blockstate 模型变体（Missing model for variant）
- `recriven_vi` 纹理 2475x2475 不是 2 的幂，mip level 被限制为 0

## 注意事项

- CI 不运行测试，仅有 `build` 任务
- `processResources` 会将版本号注入 `fabric.mod.json`
- 配置缓存已禁用（Loom 兼容性问题）
- LICENSE: CC0-1.0
