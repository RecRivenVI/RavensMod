# 更新日志

本日志记录 Minecraft 1.21.4 Fabric 与 1.21.1 NeoForge 版 Raven's Models 的变更。

## [未发布]

### 新增

- 增加 Minecraft 1.21.1 NeoForge Target，使用原生烘焙模型移植全部方块、材质资源、展示参数与座椅交互。

### 变更

- 接入统一的构建、配置、合规、格式与发行校验基础设施。
- 日常实例配置使用 TOML，保留三个运行入口和原玩家名预设。
- Gradle 使用 JDK 25，产品继续使用 Java 21 与 Mojang 官方映射。
- 编译使用 Fabric Loader 0.19.5，发布元数据最低要求保持 0.19.3；Fabric API 保持现有版本。
- 模组身份常量由源码模板生成，产品注册、资源、碰撞、状态和交互不变。

### 修复

- Minecraft 1.21.1 NeoForge 补齐与 Fabric 相同的 PBR 材质格式声明和法线、高光贴图的普通图集过滤配置。
