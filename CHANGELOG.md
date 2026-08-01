# XyItems Changelog

## 1.0.4 - 2026-08-01

- 新增重构后的锻造成品短格式：`identify.display-name` 与 `identify.lore` 可作为全部品质的公共模板。
- `identify.qualities` 节点现在可以直接使用中文品质名；未写 `name` 时节点名就是品质显示名。
- 保留旧格式兼容：单个品质仍可单独配置 `name/display-name/lore` 覆盖公共模板。
- `attributes` 继续作为通用数字变量系统，不接入AttributePlus/AP接口；任意变量名只负责替换到最终Lore。
- 默认 `ForgeItem/ExampleForgeItem.yml` 改为8品质短格式，并使用 `##` 中文注释说明锻造失败、品质权重和变量关系。
- 新增共享模板、中文品质键和任意变量回归测试。

## 1.0.3 - 2026-07-30

- `forge.failure.weight` 现在允许等于0，仍拒绝负数、NaN和无穷值。
- `forge.failure.weight` 必须明确写出；完全漏写不会被默认解释为0。
- 支持只有一个品质且失败权重为0的确定性锻造成品，最终概率正确归一化为该品质100%。
- 继续要求至少一个启用品质，并要求每个品质权重大于0，避免全部结果总权重为0。
- 普通右键鉴定行为保持不变；单品质物品每次都得到该品质。
- 新增“失败0、单品质100%”回归测试，并更新README、默认版本信息与AI使用记录。

## 1.0.2 - 2026-07-30

- 新增与 `identify` 同级的 `forge.failure` 配置，用失败和六品质组成一次最终权重抽取。
- 新增不可变 `ForgeOutcomeProfile`，GUI展示和实际抽取读取同一份失败/品质数据结构。
- 新增 `rollForgeOutcome`，抽中品质后立即生成该品质及随机属性，不再二次抽品质。
- 新增 `createIdentifiedItem`，供受信任插件按明确品质创建成品。
- 普通右键鉴定明确忽略锻造失败项，只在品质权重之间归一化。
- 首次启动额外生成 `items/ForgeItem/ExampleForgeItem.yml`，示例为失败30%与六品质70%的最终分配。
- 配置错误继续遵守全量校验和旧注册表保留规则。

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
