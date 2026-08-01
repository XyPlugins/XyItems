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

## 1.0.1 维护记录

- 根据服主要求新增 `/xyitem get <物品ID> [数量]` 玩家自取指令及 `/xyi` 主命令缩写。
- AI 辅助将 `get` 与既有 `give` 发放过程收敛到同一实现，避免数量校验、NBT 创建和背包容量规则发生差异。
- 出于安全考虑新增独立权限 `xyitems.get`，默认值为 `op`；服务器可按实际的可信指令绑定或权限组进行授权。
- 执行 `./gradlew.bat clean build --no-daemon` 成功，并核对成品 `plugin.yml` 的版本、别名与权限声明。

## 1.0.2 维护记录

- AI根据服主确认的XyForgeCrafting概率模型实现 `forge.failure`：失败与六品质只抽取一次，配方文件不再重复配置成功率。
- 将GUI展示用的最终概率封装为不可变快照，并让实际抽取使用同一结果结构，减少展示与结算逻辑漂移。
- 成功抽中品质时直接生成该品质物品并写入品质、随机属性NBT；调用方不再进行第二次品质随机。
- 保留普通右键鉴定的旧行为：忽略失败权重，只在六品质间按比例抽取。
- 新增 `ForgeItem/ExampleForgeItem.yml`，使用失败30和品质 `19.6/15.4/12.6/9.8/7/5.6` 的最终权重示例。
- 所有实现继续锁定Java 8与Paper/Spigot 1.12.2，没有引入现代API或跨版本兼容层。
- `gradlew.bat clean build --no-daemon` 已成功；测试确认默认配置加载3个物品、锻造快照含失败和六品质、总权重为100且失败最终概率为30%。
- 已核对 `XyItems-1.0.2.jar` 未打入XyCore编译桩或Paper API。

## 1.0.3 维护记录

- 根据服主确认的确定性锻造需求，允许 `forge.failure.weight: 0`，但继续拒绝负权重和无效数值。
- 要求 `forge.failure.weight` 节点明确存在，避免漏写字段被YAML默认值静默解释为必定成功。
- 保留至少一个启用品质且每个品质权重大于0的约束，因此最终抽取总权重始终有效。
- 单个品质配合零失败权重时，最终快照仍保留失败项0%，唯一品质归一化为100%；XyForgeCrafting可以隐藏0%行并显示“传说: 100%”。
- 普通右键鉴定继续忽略锻造失败项，单品质配置自然得到100%的唯一品质结果。
- 新增回归测试直接构造零失败、单品质定义，验证结果数量、总权重、失败0%和传说100%。
- 版本提升至1.0.3，并同步README、CHANGELOG、默认帮助文本和插件描述版本。

## 1.0.4 维护记录

- 根据服主后续批量配置需求，AI辅助将锻造成品示例改为短格式：公共 `identify.display-name/lore` 只写一次，品质节点只写权重、颜色和变量范围。
- 解析层保持向后兼容；旧配置中每个品质单独写 `name/display-name/lore` 仍然有效，且优先覆盖公共模板。
- 品质节点允许直接使用中文名，例如 `白描`、`萌黄`、`群青`；未写 `name` 时节点名就是 `<品质.名称>`。
- 明确 `attributes` 不是AP接口，而是XyItems内部的通用数字变量。AI没有引入AttributePlus依赖，变量最终只替换进Lore，由服务器已有Lore属性规则读取。
- 默认 `ForgeItem/ExampleForgeItem.yml` 使用 `##` 中文注释，说明失败权重、品质权重、共享Lore和自定义变量写法。
- 新增测试覆盖共享模板继承、中文品质键、`damage/health/撕裂/暴击率` 等任意变量读取。
- 版本提升至1.0.4，并同步README、CHANGELOG、默认帮助文本和插件描述版本。
