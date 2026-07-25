# AI 使用记录

## 使用范围

本仓库在服务器服主的需求下，使用 AI 辅助完成 XyItems v1.0 的设计、实现、配置示例、文档和构建验证。目标是为 XY 插件生态提供轻量、配置化的 Spigot/Paper 1.12.2 物品库。

## 已确认需求

- 插件名称为 `XyItems`，主指令为 `/xyitems`，版本号为 `1.0`。
- 包名跟随已有 XY 仓库：`org.xyplugin.xyitems`。
- 强制依赖 XyCore，并使用其 NBT 物品标签与物品提供器接口。
- 属性由 Lore 供 AttributePlus/AP 读取。`damage` 是服务器自定义的 Lore 数值，不在 XyItems 内部强行定义战斗含义。
- 未鉴定物品允许堆叠；右键鉴定一次只生成一件随机结果，剩余未鉴定物品保留。
- 所有 XyItems 玩家产物流程在开始前必须检查背包空格。空间不足时不消耗输入、不执行后续步骤、不掉落或部分发放物品，提示使用 XyCore 前缀。
- 配置要适合新手，并在 `items/Example/Example.yml` 提供实际可运行、逐项中文注释的示例。

## AI 辅助决策

- 采用 XyCore 的公开 `ItemTagService` 标记物品，而不是按名称或 Lore 判断，避免同名物品误触发鉴定。
- 在 XyCore 物品库注册 `xyitems` 提供器，后续插件可用统一的 `xyitems:<item-id>` 格式创建基础物品。
- 采用递归 YAML 加载和全量校验。任一文件错误时，重载保留上一次有效注册表，避免半加载。
- 将背包空位校验与物品交付收敛到 `InventoryDeliveryService`，并通过公开 API 提供给未来锻造、强化、兑换插件复用。
- 鉴定物品的随机属性除了渲染到 Lore 外，也序列化写入 XyCore NBT；后续模块可以稳定读取，避免反向解析展示文本。
- 监听器只处理主手右键事件；与 XyItems 无关的物品只进行一次 NBT 查询后立即返回。
- 为保证旧版 1.12.2 构建可复现，随项目保留编译期 API JAR。历史 Spigot 快照依赖的 `bungeecord-chat:1.12-SNAPSHOT` 已无法稳定从公共仓库获取。

## 验证记录

- 执行 `./gradlew.bat clean build --no-daemon` 成功，生成 `build/libs/XyItems-1.0.jar`。
- 检查 JAR 内容，确认未把 XyCore API stub、Spigot/Paper API 或编译期依赖打入最终插件。
- 使用 Bukkit 的 `YamlConfiguration` 实际加载默认示例，结果为成功、无错误、加载 2 个物品 ID：`example_rune`、`example_forge_crystal`。
- 验证 `<%damage%>`、`%damage%`、`<damage>` 三种属性占位符均可渲染为相同最终数值。

## 仍需人工联机验证

- 在生产用 Spigot/Paper 1.12.2 与部署的 XyCore JAR 上启用插件。
- 将示例 Lore 标签与服务器最终 AttributePlus/AP 规则对齐后，再向玩家正式发放物品。
- 后续 XyForge、XyEnhance、XyExchange 应调用 `XyItemsApi#hasDeliverySpace` 和 `XyItemsApi#deliverItems`，不要直接调用 `Inventory#addItem`。
