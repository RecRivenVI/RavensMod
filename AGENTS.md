# AGENTS.md

## 项目概述

Minecraft 模组，使用 Stonecutter 支持 Fabric / NeoForge 多版本构建。模组 ID：`ravensmod`，LICENSE：CC0-1.0。

当前目标：
- `fabric-1.21.4` — Fabric Loader 0.16.14，Fabric API 0.119.4+1.21.4，GeckoLib 4.8.5，Java 21，remap 工作流
- `fabric-26.2` — Fabric Loader 0.19.3，Fabric API 0.152.2+26.2，GeckoLib 5.5.2，Java 25，modern 非 remap 工作流
- `neoforge-1.21.4` — NeoForge 21.4.157，GeckoLib 4.8.5，Java 21
- `neoforge-26.2` — NeoForge 26.2.0.1-beta，GeckoLib 5.5.2，Java 25

## 构建与验证

```bash
./gradlew build
./gradlew :fabric-26.2:build
./gradlew :fabric-1.21.4:build
./gradlew :neoforge-1.21.4:build
./gradlew :neoforge-26.2:build
```

客户端启动：

```bash
./gradlew :fabric-26.2:runClient
./gradlew :fabric-1.21.4:runClient
./gradlew :neoforge-1.21.4:runClient
./gradlew :neoforge-26.2:runClient
```

Windows 建议始终用 JDK 21 启动 Gradle daemon，Java toolchain 会自动下载/选择 Java 25 编译 26.2：

```powershell
$env:JAVA_HOME='C:\Program Files\BellSoft\LibericaJDK-21-Full'
.\gradlew.bat :fabric-26.2:build --no-daemon
```

本机环境可能存在 `HTTP_PROXY` / `HTTPS_PROXY` 指向本地 socks 代理，NeoForge / Mojang 依赖下载遇到 TLS handshake 失败时，先用同一 PowerShell 进程清掉代理再重跑：

```powershell
Remove-Item Env:HTTP_PROXY,Env:HTTPS_PROXY,Env:http_proxy,Env:https_proxy -ErrorAction SilentlyContinue
```

## Stonecutter 架构

核心文件：
- `settings.gradle.kts` — `versions(mapOf(...))` 定义 `fabric-1.21.4`、`fabric-26.2`、`neoforge-1.21.4`、`neoforge-26.2`
- `stonecutter.gradle.kts` — 当前活跃版本，默认 `fabric-26.2`
- `build.gradle.kts` — 根据 `sc.current.project` 区分 Fabric / NeoForge，根据 `sc.current.parsed` 区分 MC API / Java 版本
- `versions/*/gradle.properties` — 各目标依赖版本

Stonecutter 常量：
- `fabric`：当前项目名以 `fabric` 开头
- `neoforge`：当前项目名以 `neoforge` 开头
- `>=26.1` / `<26.1`：处理 MC 26.x 与 1.21.4 API 差异

条件注释必须写完整块，避免只切第一行：

```java
//? >=26.1 {
import net.minecraft.resources.Identifier;
//?} else {
/*import net.minecraft.resources.ResourceLocation;*/
//?}
```

## 源码结构

```
src/main/java/com/recrivenvi/ravensmod/
├── RavensMod.java
├── compat/                         # GeckoLib v4/v5 公共适配
├── fabric/FabricRavensMod.java     # Fabric 主入口
├── neoforge/NeoForgeRavensMod.java # NeoForge 主入口
├── platform/PlatformRegistries.java# 跨加载器注册适配
├── register/                       # Blocks / Items / Tabs / BlockEntity
└── utils/RegisterBlockEntities.java

src/client/java/com/recrivenvi/ravensmod/
├── client/ClientInit.java
├── client/fabric/FabricRavensModClient.java
├── client/neoforge/NeoForgeRavensModClient.java
└── compat/                         # GeckoLib 客户端渲染适配
```

不要把 Stonecutter 需要预处理的共享类只放在 `src/fabric/java` 或 `src/neoforge/java`，非活跃版本生成源码时可能不会纳入。共享调用点优先放在 `src/main/java` / `src/client/java`，再用 Stonecutter 常量切分差异。

## 关键差异

构建：
- Fabric 1.21.4 使用 `fabric-loom-remap`、`loom.officialMojangMappings()`、`modImplementation`
- Fabric 26.2 使用 `fabric-loom`、不声明 mappings、用 `implementation`
- NeoForge 使用 `net.neoforged.moddev`
- `mavenCentral()` 必须保留，NeoForge 工具链会解析 ASM / LWJGL 等通用依赖

MC API：
- 26.2 用 `Identifier` / `Identifier.fromNamespaceAndPath()`
- 1.21.4 用 `ResourceLocation` / `ResourceLocation.fromNamespaceAndPath()`
- 注册 `Block` / `Item` 两个版本都需要 `ResourceKey` + `setId()`
- `BaseEntityBlock.codec()` 两个版本都要实现
- `BlockEntityRenderState` 泛型参数仅 26.1+

GeckoLib：
- GeckoLib 4 包名是 `software.bernie.geckolib.*`
- GeckoLib 5 包名是 `com.geckolib.*`
- GeckoLib 4 `GeoBlockRenderer` 构造器是 `(GeoModel)`，模型路径用 `assets/ravensmod/geo/*.geo.json`
- GeckoLib 5 `GeoBlockRenderer` 构造器是 `(Context, GeoModel)`，模型路径用 `assets/ravensmod/geckolib/models/*.geo.json`
- GeckoLib 4 的 `GeoAnimatable` 需要 `getTick(Object)`

## 资源文件

- Fabric 元数据：`src/main/resources/fabric.mod.json`
- NeoForge 元数据：`src/main/resources/META-INF/neoforge.mods.toml`
- 两者都用 Gradle `processResources` 展开 `${version}` / `${mc_version}` 等变量
- GeckoLib 模型同时维护：
  - `src/main/resources/assets/ravensmod/geckolib/models/`
  - `src/main/resources/assets/ravensmod/geo/`
- 方块模型、物品模型、blockstates、lang、textures 都在 `src/main/resources/assets/ravensmod/`

## 注册顺序

加载器入口中保持：

```text
BlocksX.initialize() -> RegisterBlockEntities.initialize() -> ItemsX.initialize()
```

创造模式标签内容由各加载器入口分别挂事件：Fabric 用 Fabric API 事件，NeoForge 用 `BuildCreativeModeTabContentsEvent`。

## 注意事项

- 不要在 26.2 使用 `new ResourceLocation()`。
- 不要在 Fabric 26.2 使用 `modImplementation` 或 `mappings`。
- 客户端渲染类保持在 `src/client/java`。
- `block_entity_1`、`2`、`3`、`5` 是水平朝向，`block_entity_4` 是垂直朝向。
- 修改渲染后尽量跑对应 `runClient`，因为仅 build 无法验证视觉方向和中心原点。
- CI 主要产出 build artifacts，不依赖测试源码。
