# 本机与实例配置

配置格式与全部取值规则见[仓库配置](../../gradle/conventions/specification/reference/configuration-repository.md)。本项目使用已登记的两个 Target 和三个内置日常实例，不另建解析器或运行适配层。

## 配置所有权

| 文件 | 内容 |
| --- | --- |
| `gradle.properties` | 模组身份、实际发行版本、运行端、作者、许可与描述事实，及 Gradle 原生设置 |
| `versions/1.21.4-fabric/target.properties` | 编译 Loader、最低 Loader、Fabric API、产品 Java 与映射表 |
| `versions/1.21.1-neoforge/target.properties` | NeoForge、产品 Java 与可选 Flywheel 编译 API 版本 |
| `gradle/libs.versions.toml` | 构建插件与共享工具版本 |
| `instances.toml` | 共享玩家名：第一客户端 `DevPlayer`，第二客户端 `DevPlayer2` |
| 可选 `local.toml` | 个人窗口、内存、玩家名、追加参数、本地输入与 EULA；Git 忽略 |
| `inputs.toml` | 本地测试模组与着色器包的元数据、来源与允许哈希，本机路径不进仓库 |

Minecraft 版本与加载器类型来自 Target 名称，不在 properties 中重复定义。实际根 Java 包为 `io.github.recrivenvi.ravensmodels`。

## 个人覆盖

以下内容只是示例，不要求创建配置文件：

```toml
[client]
player-name = "RecRivenVI"
width = 1280
height = 720
memory-max = "4G"
```

本机值先覆盖共享文件中的同表同键，然后按 Target 加实例、Target、实例、全局的顺序选择。数组整体替换；未知、重复或无效的配置报错。

玩家名必须满足 `[A-Za-z0-9_]{3,16}`，两个客户端不能同名。JVM 堆使用 `memory-min` / `memory-max`，不在 `jvm-args` 中重复设置；游戏窗口、玩家名、端口与运行目录也不通过追加参数重复指定。

没有 `local.toml` 是正常状态。服务端 EULA 必须由使用者本人接受，不能由 Agent 自动写入。

## 查看与校验

从仓库根目录执行：

```powershell
.\gradlew.bat verifyConfiguration
.\gradlew.bat printLaunchSettings --target=1.21.4-fabric --variant=client
.\gradlew.bat printLaunchSettings --target=1.21.4-fabric --variant=client-multiplayer
.\gradlew.bat check
```

查询只说明配置解析；没有实际运行不能据此声称 Minecraft 或 PBR 验收通过。
