# XyItems Changelog

## 1.0.7 - 2026-08-19

- 新增可选的品质内强度系统：属性独立随机后按配置权重计算 `0.0%` 到 `100.0%` 的强度。
- 新增 `<strength.percent>` 与 `<strength.bar>` Lore 占位符，同时支持中文别名 `<强度.百分比>`、`<强度.条>`。
- 强度条长度、填充字符、空白字符、百分比精度和属性权重均可在 `identify.strength` 自定义。
- 强度和实际属性滚动值写入 XyCore NBT；配置重载不会改变已经生成的物品。
- 新增每件物品的 `identify.action-name`，支持将“鉴定”改成“净化”“拯救”“重构”等；未鉴定 Lore 和成功提示均可引用该名称。
- 新增带中文注释的 `items/Example/Chiyamopo.yml` 赤牙墨魄示例。
- 公共 API 新增 `getStrengthPercent`，供锻造、强化等后续插件读取。
- 版本、README、AI 使用记录和默认配置同步提升至1.0.7。

## 1.0.6 - 2026-08-02

- 按服主最终确认重构聊天前缀语义：玩家实际获得物品、背包满等玩法结果走 XyCore `messages.prefix`。
- `/xyitems help/list/info/reload/get/give` 的权限不足、参数错误、物品不存在、管理员给予反馈等管理/排错提示保留 XyItems 自身前缀。
- 新增 `sendPlayer` 与 `sendLocal` 两类发送入口，避免简单按发送者是否为玩家判断导致 help/报错混入系统提示。
- 同步提升依赖说明至 XyCore 0.3.12，并更新 README、AI 使用记录和版本号。

## 1.0.5 - 2026-08-02

- 玩家聊天提示前缀改为通过 `XyCoreApi#getMessagePrefix()` 统一读取 XyCore `messages.prefix`。
- 启动时检查 XyCore 0.3.11+ 的前缀API，避免旧Core在玩家收到提示时才发生运行期错误。
- 控制台日志继续保留 `[XyItems]` 插件名，不切换为统一玩家前缀。
- 同步更新 `config.yml` 注释、README、AI使用记录和插件版本。

## 1.0.4 - 2026-08-01

- 新增重构后的锻造成品短格式：`identify.display-name` 与 `identify.lore` 可作为全部品质的公共模板。
- `identify.qualities` 节点现在可以直接使用中文品质名；未写 `name` 时节点名就是品质显示名。
- 保留旧格式兼容：单个品质仍可单独配置 `name/display-name/lore` 覆盖公共模板。
- `attributes` 继续作为通用数字变量系统，不接入AttributePlus/AP接口；任意变量名只负责替换到最终Lore。
- 新增 `unbreakable` 与 `hide-unbreakable` 物品根节点配置，支持武器、防具生成原版无限耐久标记。
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
- 修复强度条渲染：默认十格使用 `l`，已填充和未填充颜色分别为 `&c`、`&7`；连续段只输出一次颜色码，例如 `&clllllll&7lll`。
- 强度大于 0 时至少显示一格红色，1% 会显示为 `&cl&7lllllllll`；0% 才显示十格灰色。
- 简化强度条配置：`filled: '&c'`、`empty: '&7'` 只配置颜色，条码字符自动使用 `l`；旧的 `&cl`、`&7l` 写法仍兼容。
