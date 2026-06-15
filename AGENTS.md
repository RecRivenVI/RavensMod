# AGENTS.md

## 项目概述

Fabric Minecraft 模组，使用 Stonecutter 支持多版本构建。模组 ID：`ravensmod`。

支持版本：
- **1.21.4** — GeckoLib 4.8.5，Java 21，remap 工作流
- **26.1.2** — GeckoLib 5.5.1，Java 25，modern 非 remap 工作流

## 构建与验证

```bash
./gradlew build                    # 构建所有版本
./gradlew :26.1.2:runClient        # 启动 26.1.2 客户端
./gradlew :1.21.4:runClient        # 启动 1.21.4 客户端
```

**Gradle daemon 用 JDK 21 运行**，Java toolchain 自动下载正确版本编译。CI 通过 foojay-resolver 自动获取 JDK。

Windows 启动方式：
```powershell
$env:JAVA_HOME='C:\Program Files\BellSoft\LibericaJDK-21-Full'
.\gradlew.bat :26.1.2:runClient --no-daemon
```

## Stonecutter 多版本架构

使用 Stonecutter 0.9.5 插件管理多版本。核心文件：

- `settings.gradle.kts` — 定义版本列表 `versions("1.21.4", "26.1.2")`
- `stonecutter.gradle.kts` — 活跃版本声明 `stonecutter active "26.1.2"`
- `build.gradle.kts` — 构建逻辑，用 `sc.current.parsed` 做版本判断
- `versions/1.21.4/gradle.properties` — 1.21.4 版本属性
- `versions/26.1.2/gradle.properties` — 26.1.2 版本属性

切换活跃版本：修改 `stonecutter.gradle.kts` 中的 `stonecutter active` 行，然后运行 `./gradlew Set active project to <version>`。

## 版本差异处理

### 构建配置差异（build.gradle.kts）

| 项目 | 1.21.4 | 26.1.2 |
|------|--------|--------|
| Loom 插件 | `fabric-loom-remap` | `fabric-loom` |
| Mappings | `loom.officialMojangMappings()` | 无 |
| 依赖方式 | `modImplementation` | `implementation` |
| Java 版本 | 21 | 25 |

### MC API 差异（Stonecutter 条件注释）

在 Java 源码中用 `//? >=26.1` 条件注释处理 API 差异：

```java
//? >=26.1 {
import net.minecraft.resources.Identifier;
//?} else {
/*import net.minecraft.resources.ResourceLocation;*/
//?}
```

主要 API 差异：
- `Identifier`（26.1）vs `ResourceLocation`（1.21.4）
- `Identifier.fromNamespaceAndPath()` vs `ResourceLocation.fromNamespaceAndPath()`
- `CreativeModeTabEvents` vs `ItemGroupEvents`
- `FabricCreativeModeTab` vs `FabricItemGroup`
- `entries.accept()` vs `entries.accept()`（1.21.4 的 FabricItemGroupEntries 也用 accept）
- `ResourceKey` + `setId()` 注册（两个版本都需要）
- `BaseEntityBlock.codec()`（两个版本都需要实现）
- `BlockEntityRenderState` 泛型参数（仅 26.1）

### GeckoLib v4 vs v5 差异（compat 适配层）

GeckoLib 版本差异太大，用 `compat/` 包做适配：

| 项目 | GeckoLib 4（1.21.4） | GeckoLib 5（26.1.2） |
|------|---------------------|---------------------|
| 包名 | `software.bernie.geckolib.*` | `com.geckolib.*` |
| AnimatableManager | `software.bernie.geckolib.animation.AnimatableManager` | `com.geckolib.animatable.manager.AnimatableManager` |
| GeoModel | `GeoModel<T>`，方法签名为 `(T, GeoRenderer<T>)` | `GeoModel<T>`，方法签名为 `(GeoRenderState)` |
| GeoBlockRenderer | 构造器 `(GeoModel)` | 构造器 `(Context, GeoModel)` |
| 模型路径 | `geo/model.geo.json`（必须在 `assets/<modid>/geo/`） | `model_name`（自动查找 `geckolib/models/`） |
| GeoAnimatable | 需要实现 `getTick(Object)` | 不需要 |

适配层文件：
- `src/main/java/.../compat/CompatBlockEntitiesRegister.java` — 基础 BlockEntity 适配
- `src/client/java/.../compat/CompatBlockEntitiesModels.java` — 模型适配
- `src/client/java/.../compat/CompatBlockEntitiesRenderer.java` — 渲染器适配

## 源码结构

```
src/main/java/com/recrivenvi/ravensmod/
├── RavensMod.java              # 主入口
├── compat/                     # GeckoLib v4/v5 适配层
│   └── CompatBlockEntitiesRegister.java
├── register/                   # 注册文件（含 Stonecutter 条件注释）
│   ├── BlocksX.java
│   ├── ItemsX.java
│   ├── TabsX.java
│   ├── BlockEntities.java      # BaseEntityBlock 子类
│   ├── BlockEntitiesRegister.java
│   └── BlockEntity*Register.java (1-5)
└── utils/
    └── RegisterBlockEntities.java

src/client/java/com/recrivenvi/ravensmod/client/
├── RavensModClient.java        # 客户端入口
└── compat/                     # 客户端适配层
    ├── CompatBlockEntitiesModels.java
    └── CompatBlockEntitiesRenderer.java
```

## 资源文件

- GeckoLib 模型同时放在两个位置：
  - `src/main/resources/assets/ravensmod/geckolib/models/` — GeckoLib 5 自动查找
  - `src/main/resources/assets/ravensmod/geo/` — GeckoLib 4 要求的路径
- 方块模型/状态/物品 JSON 在 `src/main/resources/assets/ravensmod/`
- `fabric.mod.json` 用 `${version}`、`${mc_version}`、`${loader_version}` 动态替换

## 注册顺序

`RavensMod.onInitialize()` 中依次调用：`BlocksX → RegisterBlockEntities → ItemsX`。新增注册必须遵循此顺序。

## 依赖

| 依赖 | 1.21.4 | 26.1.2 |
|------|--------|--------|
| Minecraft | 1.21.4 | 26.1.2 |
| Fabric Loader | 0.16.14 | 0.19.3 |
| Fabric API | 0.119.4+1.21.4 | 0.151.0+26.1.2 |
| GeckoLib | 4.8.5（`software.bernie.geckolib`） | 5.5.1（`com.geckolib`） |
| Java | 21 | 25 |
| Gradle | 9.5.1 | — |
| Fabric Loom | 1.17.11 | — |
| Stonecutter | 0.9.5 | — |

## 注意事项

- **不要在 26.1.2 使用 `modImplementation`**，用 `implementation`
- **不要在 26.1.2 使用 `mappings`**，非混淆版本不需要
- **不要在 26.1.2 使用 `new ResourceLocation()`**，用 `Identifier.fromNamespaceAndPath()`
- **GeckoLib 4 模型必须在 `geo/` 目录**，GeckoLib 5 在 `geckolib/models/`
- **`codec()` 方法两个版本都要实现**，否则 1.21.4 报抽象方法未实现
- **`setId()` 两个版本都需要**，否则报 "Block id not set"
- CI 不运行测试，仅有 `build` 任务
- 配置缓存已禁用（Loom 兼容性问题）
- LICENSE: CC0-1.0
