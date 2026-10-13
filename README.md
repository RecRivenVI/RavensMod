# Raven's Models

Minecraft 1.21.4 Fabric 模型方块模组，内部项目名为 `RavensModels`，模组 ID 为 `ravensmodels`。提供镜面、石膏与塑料材质方块，以及球体、G3 圆角、连接板、王座、石膏头像、座椅和黑桃 A 静态展示模型。

## Target

| Target | 加载器版本 | Java |
| --- | --- | --- |
| `1.21.4-fabric` | Fabric 0.19.5 | 21 |

## 快速开始

安装 JDK 25 供 Gradle 守护进程使用；产品编译与运行使用 JDK 21。从仓库根目录执行：

```powershell
.\gradlew.bat spotlessApply
.\gradlew.bat check verifyRelease
.\gradlew.bat :version:1.21.4-fabric:runClient
```

模型姿态、材质与交互需要实际游戏验收。模型与纹理保留各自来源信息；本项目保留所有权利（All Rights Reserved），不授予使用或再分发许可，第三方授权声明见 `NOTICE` 和随资源保留的来源信息。

## 文档

- [安装与使用](documents/usage/installation-basics.md)
- [日常开发流程](gradle/conventions/specification/development/workflow-daily.md)
- [构建与运行](documents/development/build-project.md)
- [本机配置](documents/development/setup-configuration.md)
- [项目规范](documents/AGENTS.md)
- [未发布变更](documents/release/changelog-ravensmodels.md)
