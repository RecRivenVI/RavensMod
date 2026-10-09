# 构建与运行

## 环境与配置

从仓库根运行 Wrapper，使用 JDK 21。当前 Target 为 Minecraft 1.21.4 / Fabric，Gradle 9.7.1、Loom 1.17.19、Fabric Loader 0.19.3、Fabric API 0.119.4+1.21.4，使用 official Mojang mappings。

- 根 `gradle.properties`：产品身份与发布版本、全局 Gradle 配置。
- `versions/1.21.4-fabric/target.properties`：Minecraft、Loader、API、Java 版本事实。
- Target `build.gradle.kts`：插件、mappings、依赖、main/client split 与运行适配。
- `instance.properties`：共享玩家名，默认 `DevPlayer` 和 `DevPlayer2`。
- 根 `local.properties`：可选且被 Git 忽略，只覆盖实际提供的键。例如 `instance.client.playerName=RecRivenVI` 不改变第二客户端的默认名字。规则见[配置指南](guide-configuration.md)。

玩家名须满足 `[A-Za-z0-9_]{3,16}`。第一客户端只校验自身；第二客户端校验两者且拒绝同名。Target 独立 build/test 和 server 不消费无关客户端字段；根 `verifyConfiguration`、`check`、`build` 会严格校验全部已声明配置。

## 无游戏检查

先将 `JAVA_HOME` 指向实际安装的 JDK 21，再执行：

```powershell
.\gradlew.bat projects
.\gradlew.bat verifyConfiguration
.\gradlew.bat :version:1.21.4-fabric:tasks --all
.\gradlew.bat :version:1.21.4-fabric:build --no-daemon
.\gradlew.bat build --no-daemon
.\gradlew.bat :version:1.21.4-fabric:runClient :version:1.21.4-fabric:runClientMultiplayer :version:1.21.4-fabric:runServer --dry-run --no-daemon
git diff --check
```

根 `build` 聚合唯一 Target。JAR 位于 `versions/1.21.4-fabric/build/libs/`，命名为 `ravensmodels-<mod_version>+1.21.4.jar`；当前发布版本对应 `ravensmodels-1.0.0+1.21.4.jar`。`.gradle/` 和 `build/` 是正常的本机构建输出与缓存。

## 日常实例

以下任务会实际启动游戏，只在需要且获得授权时执行；不要为了构建检查启动或关闭游戏。

| Gradle 任务（前缀 `:version:1.21.4-fabric:`） | 仓库根下的游戏目录 |
| --- | --- |
| `runClient` | `instances/1.21.4-fabric/client/` |
| `runClientMultiplayer` | `instances/1.21.4-fabric/client-multiplayer/` |
| `runServer` | `instances/1.21.4-fabric/server/` |

多人客户端继承 client run configuration，但拥有独立目录与玩家名。Loom 在任务执行时应用运行目录，不以配置阶段 `JavaExec.workingDir` 的默认值判断最终目录。RavensModels 通过开发 source sets 加载，不要再把自己的 JAR 放入开发实例 `mods/` 造成重复加载。不要覆盖用户世界、配置或第三方 Mod。

## 验证边界

Mod Target 目前没有普通 JUnit / GameTest 测试源码，其 `test` 为 `NO-SOURCE`。`gradle/configuration/` 有配置单元测试及 Gradle TestKit 功能测试，由根 `check/build` 聚合；测试包括普通 Java 探针，不是 Minecraft 启动。没有仓库内产品验证项目、自动布局检查器或 Workflow。不为目录规范创建空测试、组件或 Target。

构建成功只证明编译和打包；dry-run 只检查任务图，不证明客户端或服务端启动成功。模型方向、碰撞、含水、座椅交互、左右手展示及 shader/PBR 效果需要真实游戏验收；跨 Loader、跨版本与整合包兼容性不能由单 Target 构建推导。

`src/main` 保持双端安全，`src/client` 保存客户端源码和资源。`_n` / `_s` 贴图按 shader 约定发现，不能依据缺少显式引用判定无用。保留模型及素材来源信息，尤其不能按 `geo`、`experiment` 等文件名自动删除仍被使用的资源。
