# XyItems Changelog

## 1.0.1 - 2026-07-28

- 新增玩家自取指令 `/xyitem get <物品ID> [数量]`，数量默认 `1`。
- 新增 `xyitems.get` 权限，默认仅 OP，避免普通玩家任意生成物品。
- 新增主命令缩写 `/xyi`，完整支持 `/xyi get <物品ID> [数量]`。
- `get` 与管理员 `give` 共用严格容量检查；空间不足时完全不发放。
- 更新插件版本、帮助、README、AI 使用记录与构建产物名称至 `1.0.1`。

## 1.0 - 2026-07-25

- 初始化 XyItems Gradle 工程，版本号为 `1.0`。
- 强制依赖 XyCore，并接入其 NBT 物品标签与统一物品库 API。
- 注册 `xyitems:<物品ID>` 提供器，供后续 XY 系列插件创建基础物品。
- 新增 `/xyitems give`、`/xyitems list`、`/xyitems info`、`/xyitems reload` 和帮助指令。
- 新增递归 `items/` 配置加载；一个 YAML 可定义多个物品，支持按目录分类。
- 新增可堆叠未鉴定物品、主手右键鉴定、加权品质和随机属性 Lore 渲染。
- 新增与 AttributePlus/AP Lore 读取兼容的通用属性占位符，包括 `<%damage%>`。
- 将鉴定时实际随机出的属性写入 XyCore NBT，供后续强化与锻造插件稳定读取。
- 新增统一的严格背包容量交付层：空间不足时不消耗输入、不部分发放、不掉落产物。
- 新增默认配置 `plugins/XyItems/items/Example/Example.yml`，含六品质符文和普通材料示例。
- 新增 README、AI 使用记录与离线可复现的 1.12.2 构建依赖。
