# Raven's Models

Minecraft 1.21.4 Fabric 模型方块模组，内部项目名为 `RavensModels`，Mod ID 为 `ravensmodels`。客户端使用 Fabric mesh / baked model 与 PBR 资源；模型姿态、材质和交互效果需要游戏内人工验收。

- [构建与运行](documents/development/build-project.md)：JDK、Gradle 命令、实例配置和验证边界。
- [配置指南](documents/development/guide-configuration.md)：四类 properties、分层覆盖、Loom 接线与配置测试。
- [仓库开发规范](AGENTS.md)：目录职责、配置所有权和产品资源约束。
- 唯一 Target：`versions/1.21.4-fabric/`，Gradle 地址 `:version:1.21.4-fabric`。
- 日常实例：`instances/1.21.4-fabric/`，本机数据不纳入 Git。

模型及纹理包含各自来源的素材；保留资源内来源信息，不将本项目入口视为第三方素材再分发许可。
