# 配置系统

根 settings 显式 include `gradle/configuration/`，名称 `raven-configuration`；根 build 应用 `raven.repository-configuration`。公共解析实现沿用模板，RavensModels 的 Loom 消费适配独立于解析器。

## 配置事实与默认值

| 文件 | 内容 |
| --- | --- |
| `gradle.properties` | `mod_id=ravensmodels`、`mod_name=Raven's Models`、`mod_group=io.github.recrivenvi`、`mod_artifact_name=ravensmodels`、`mod_version=1.0.0`、`mod_environment=both`，及作者、真实仓库 URL |
| `versions/1.21.4-fabric/target.properties` | Minecraft 1.21.4、`loader=fabric`、Loader 0.19.3、Fabric API 0.119.4+1.21.4、Java 21 |
| `instance.properties` | `instance.client.playerName=DevPlayer`、`instance.client-multiplayer.playerName=DevPlayer2` |
| 可选根 `local.properties` | 实例最终覆盖、`java.<major>.home`、`launcher.prism.path`；Git ignored |

原有 `org.gradle.jvmargs=-Xmx1G`、`org.gradle.parallel=true` 保留，由 Gradle 原生消费，配置插件不将其返回为产品事实。不能用实例 memory 改 Gradle daemon 的堆，也不能用 `-Pmod_*` 另建产品事实权威。Loom 1.17.19 和 official Mojang mappings 的选择仍在 Target build 中。

文件使用 UTF-8 和 Java properties 语法，重复键报错。本机路径建议正斜杠，相对路径以仓库根为准。local 可以完全不存在；没有默认注入窗口大小、游戏堆或其他模板示例值，未配置时保留生态默认。

## 统一覆盖规则

在每个文件内，按以下顺序选第一个存在的键：

```properties
instance.[<target>].<variant>.<property>
instance.[<target>].<property>
instance.<variant>.<property>
instance.<property>
```

先独立求 `instance.properties`，再独立求 `local.properties`；任何匹配 local 值覆盖共享结果，即使 local 更宽泛。不能先合并两个文件，也不能合并各层参数数组。

支持 memory（正整数 K/M/G）、width/height（正整数像素）、playerName（3–16 个 ASCII 字母、数字、下划线）、extraJvmArgs/extraGameArgs，以及额外扁平运行属性。server 不消费窗口或玩家字段。

```properties
# local.properties 示例，不要求创建该文件
instance.client.playerName=RecRivenVI
instance.[1.21.4-fabric].client.memory=2G
instance.extraJvmArgs=["-Dmessage=hello world"]
instance.extraGameArgs=[]
```

extra 支持以下多行字面参数块，兼容原有单行 JSON 字符串数组。开始行的值仅为 `[`，独占一行 `]` 结束；每个非空行 trim 首尾后成为一个完整参数，内部空格、反斜杠、Unicode 和引号原样保留。块内不解释注释、转义或续行，不需要逗号/包裹引号。单行 JSON 仍按 properties + JSON 两层转义，可表示空字符串。

```properties
# 示例，不是项目默认值
instance.extraJvmArgs=[
    -Dmessage=hello world
    -ea
]
```

空字段、`[]` 或空块清除共享追加值，但不清除 Loader 原有参数。所有参数不经 shell 或二次拆分。JVM 列表与 memory 先分别做 Cascade，再让最终列表中唯一有效的 `-Xmx` 优先于 memory，因此共享列表中的 `-Xmx` 也能优先于本机 memory。清空列表或没有 `-Xmx` 时恢复选中的 memory。重复/无效 `-Xmx` 报错；其他 JVM 参数（如 `-Xms`、GC 选项）由 JVM 解释，不保证任意组合都兼容。

游戏 `--width`、`--height`、`--username`、`--gameDir` 仍通过专用属性与目录模型提供，禁止在 extra 中重复定义。选中空/错误的 typed 值报错，不静默回退。

## Loom 接入边界

Target 通过 `repositoryConfig` 消费身份、版本、metadata 和编译 Java toolchain。`both` 映射 Fabric metadata 的 `*`；三个 variant 的建议都是 INSTALL，但不会自动复制 JAR。

Loom 原生 `RunConfiguration.runDirectory` 继续管理 `instances/1.21.4-fabric/{client,client-multiplayer,server}`，并保留 main/client source sets、开发入口、classpath 和原生参数提供器。client-multiplayer 仍继承 client 配置，但玩家和 extras 在各自任务中追加，不污染继承的基础配置。

`LoomRunAdapter` 在已有 Loom argument providers 后追加 `jvmArgumentsWithoutMaxHeap()` 和游戏参数，以 `effectiveMemory()` 设置原生最大堆并检查客户端身份；确保 `-Xmx` 只发一次。原 `memory()` / `extraJvmArgs()` 保留诊断含义，查询另外显示 effectiveMemory。工作目录及创建目录仍由 `AbstractRunTask.exec()` 处理，没有调用普通 JavaExecAdapter 去覆盖 Loom 的目录/参数链。

`java.21.home` 使用时检查 java/javac/release 版本；在运行任务配置时选择 executable，以便 Gradle 推导匹配的 launcher。缺省使用 Java 21 toolchain，不要求 Gradle 宿主版本与其相同。编译 toolchain 使用 java_version；本地路径不会自动修改全局 JDK 设置。Prism 路径仅查询/校验，不启动 Launcher。

项目没有专用验证实例或场景覆盖层。未来固定场景输入应是任务约束，而不是第五级 Cascade；已解析身份冲突时应启动前报错，不能悄悄覆盖日常配置。

## 无游戏验证

```powershell
.\gradlew.bat verifyConfiguration
.\gradlew.bat printInstanceConfiguration --target=1.21.4-fabric --variant=client
.\gradlew.bat -p gradle/configuration test validatePlugins --no-daemon
.\gradlew.bat build --no-daemon
```

`verifyConfiguration` 检查所有声明值与本机工具；查询显示最终值及来源，不打印 extra 内容。根 check/build 聚合严格配置校验及配置插件测试，因此坏 client 值会使根聚合失败。Target 独立 build/test 与 server 只消费自己所需字段。多人客户端另检查两个玩家名不同。

配置文件通过 Provider 读取，配置缓存跟踪 local 的缺失、新增、修改和撤除。TestKit 使用独立临时工程，不注册成正式 Target；输入保留，不永久删除。可传 `-PtestFixturesDirectory=<已有临时目录>` 指定测试位置，`-PalternateJdkHome=<不同版本JDK>` 执行额外跨 JDK JavaExec 检查，未传时这项额外检查会跳过。

测试覆盖 Cascade、JSON 参数、缓存失效、普通 JavaExec 进程与实际 Loom 配置/任务参数链。Loom 探针只执行适配器的准备动作，读取原生参数/目录，然后交给独立普通 Java 程序；不执行 Loom 的游戏启动动作。通过这些检查不等于 Minecraft、DLSS、shader、PBR、网络或人工视觉验收通过。
