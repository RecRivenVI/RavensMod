# 构建与运行

## 环境

唯一正式 Target 是 Minecraft 1.21.4 Fabric，Gradle 地址为 `:version:1.21.4-fabric`。Gradle 守护进程使用 JDK 25，产品编译与运行使用 JDK 21；版本来源分别是 `gradle/gradle-daemon-jvm.properties` 和 Target 的 `target.properties`。

Gradle Wrapper 与构建插件版本取自 `gradle/libs.versions.toml` 和 Wrapper 配置。Fabric API 保持 `0.119.4+1.21.4`，映射表使用 Mojang 官方映射。Fabric Loader 编译版本为 0.19.5，发布元数据最低要求为 0.19.3。

## 构建检查

从仓库根目录执行：

```powershell
.\gradlew.bat projects
.\gradlew.bat :version:1.21.4-fabric:tasks --all
.\gradlew.bat spotlessApply
.\gradlew.bat check
.\gradlew.bat verifyRelease
.\gradlew.bat build --no-daemon
git diff --check
```

发行 JAR 命名为 `ravensmodels-1.21.4-fabric-<mod_version>.jar`，生成于 Target 的 `build/libs/`，`verifyRelease` 将发行构件汇总到根 `build/release/<mod_version>/`。产品版本目前是 `1.0.0+1.21.4`。

根 `check` 聚合配置、合规、文档、格式和模板组件测试。Target 目前没有 JUnit、GameTest 或探针源码；仓库没有产品验证项目。配置库测试不是 Minecraft 游戏验收。

## 日常运行

| Gradle 任务（前缀 `:version:1.21.4-fabric:`） | 仓库内实例目录 |
| --- | --- |
| `runClient` | `instances/1.21.4-fabric/client/` |
| `runClientMultiplayer` | `instances/1.21.4-fabric/client-multiplayer/` |
| `runServer` | `instances/1.21.4-fabric/server/` |

玩家名共享预设为 `DevPlayer` 和 `DevPlayer2`，本机设置见[配置指南](setup-configuration.md)。服务端只有使用者在 `local.toml` 明确接受 Minecraft EULA 后才启动。

日常运行始终通过开发源码集加载产品，不把自己的发行 JAR 再放进开发实例的 `mods/`。不要覆盖已有世界、配置或第三方模组；不得从本项目任务读写仓库之外的游戏实例。

## 产品边界

`src/main` 保存双端代码与数据，`src/client` 保存客户端模型与资源；Loom 将二者识别为同一模组。入口常量由 `src/main/templates/` 生成。

`_n` / `_s` 贴图由 shader 按约定发现；缺少 Java 或 JSON 显式引用不代表资源无用。王座的 Geo 资源由原生 mesh 模型读取，并不需要 GeckoLib。构建和启动成功不能证明方向、碰撞、含水、座椅交互、左右手展示或 PBR 视觉正确。
