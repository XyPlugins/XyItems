# XyItems 1.0.4

XyItems 是 XY 系列的配置化物品库，面向 Spigot/Paper 1.12.2。v1.0.4 提供带 NBT 身份标识的物品定义、右键随机鉴定、品质与属性渲染，以及供XyForgeCrafting读取的失败/结果最终权重与单次抽取API。

## 运行环境

- Java 8+
- Spigot/Paper 1.12.2
- **XyCore（强制依赖）**

XyItems 不使用 SQL，也不保存玩家数据。它必须依赖 XyCore 的 1.12.2 NBT 标签服务来标识真实物品；未启用 XyCore 或 NBT 服务不可用时，XyItems 会停止启用，避免按名称或 Lore 误识别物品。

## 安装

1. 将 `XyCore` 与 `XyItems-1.0.4.jar` 放入服务器 `plugins/`。
2. 启动服务器一次。
3. 默认示例会释放到：

   ```text
   plugins/XyItems/items/Example/Example.yml
   ```

4. 修改物品配置后执行 `/xyitems reload`。

插件名为 **XyItems**，因此数据目录是 `plugins/XyItems/`。示例目录中实际使用的是复数名称，而不是 `plugins/XyItem/`。

## 指令

| 指令 | 说明 | 权限 |
| --- | --- | --- |
| `/xyitems help` | 查看帮助 | 无 |
| `/xyitem get <物品ID> [数量]` | 玩家为自己取得物品，数量默认 1 | `xyitems.get` |
| `/xyitems list [页码]` | 查看已加载物品 ID | `xyitems.list` |
| `/xyitems info <物品ID>` | 查看物品来源与鉴定状态 | `xyitems.list` |
| `/xyitems give <玩家> <物品ID> [数量]` | 给予未鉴定/基础物品 | `xyitems.give` |
| `/xyitems reload` | 重载 `items/` 中的全部配置 | `xyitems.reload` |

主命令别名：`/xyitem`、`/xyi`，因此也可使用 `/xyi get <物品ID> [数量]`。

## 权限

| 权限 | 默认值 | 说明 |
| --- | --- | --- |
| `xyitems.use` | `true` | 允许右键触发 XyItems 鉴定 |
| `xyitems.list` | `op` | 允许 list 和 info |
| `xyitems.give` | `op` | 允许 give |
| `xyitems.get` | `op` | 允许玩家为自己取得配置物品 |
| `xyitems.reload` | `op` | 允许 reload |

## 配置目录

XyItems 会递归加载 `plugins/XyItems/items/` 下全部 `.yml` 与 `.yaml` 文件。一个 YAML 文件可以定义多个物品；也可以按类别拆分成多个文件，例如：

```text
plugins/XyItems/items/
├─ Example/Example.yml
├─ ForgeItem/ExampleForgeItem.yml
├─ Runes/fire.yml
├─ Weapons/swords.yml
└─ Materials/forge.yml
```

每个文件都使用同一个根节点：

```yml
items:
  example_rune:
    material: NETHER_STAR
    display-name: '&f未鉴定的元素符文'
    lore:
      - '&7右键鉴定。'
    identify:
      enabled: true
      display-name: '<品质.颜色><品质.名称>元素符文'
      lore:
        - '&f✦ 攻击力: &c<damage>'
      qualities:
        普通:
          weight: 1
          color: '&f'
          attributes:
            damage: { min: 3, max: 6, format: '0' }
```

完整的六品质示例及逐项中文注释见 [Example.yml](src/main/resources/items/Example/Example.yml)。

### 关键字段

- `items.<物品ID>`：物品 ID；命令、XyCore 物品库和后续 XY 插件使用它。ID 不可重复。
- `material`：Bukkit 1.12.2 `Material` 名称。
- `data`：旧版耐久值/子类型，通常为 `0`。
- `display-name`、`lore`：基础或未鉴定状态的显示。
- `identify.enabled`：启用主手右键鉴定。
- `identify.display-name/lore`：所有品质共用的成品显示模板；单个品质仍可单独写 `display-name/lore` 覆盖。
- `identify.qualities.<内部ID>`：一个品质结果。内部 ID 可以直接写 `白描`、`萌黄` 等中文名；未写 `name` 时节点名就是品质名。
- `weight`：相对概率权重，不必等于百分比。
- `attributes.<属性ID>`：可用 `{ min, max, format }` 定义随机值，也可直接写固定数值。

## 锻造最终概率

锻造成品在 `identify` 的同级增加一个 `forge.failure`：

```yaml
items:
  example_forge_soul:
    material: NETHER_STAR
    display-name: '&f未鉴定的示例墨魂'
    forge:
      failure:
        weight: 30
        name: '锻造失败'
        color: '&c'
    identify:
      enabled: true
      display-name: '<品质.颜色>示例墨魂'
      lore:
        - '&7品质: <品质.颜色><品质.名称>'
        - '&7攻击力: &c+<damage>'
      qualities:
        白描:
          weight: 19.6
          color: '&f'
          attributes:
            damage: { min: 3, max: 6, format: '0' }
```

`forge.failure.weight` 和所有 `identify.qualities.*.weight` 参加同一次最终抽取。权重不强制写成100，但GUI会按总和归一化显示百分比。

`attributes` 只是随机数字变量，不对接AttributePlus/AP接口。XyItems会把 `<damage>`、`<health>`、`<撕裂>`、`<暴击率>` 等占位符替换成数字并写入Lore；最终属性是否生效由服务器现有的Lore属性规则决定。

`forge.failure.weight` 必须明确写出，可以填写 `0`，表示该物品锻造时不会抽中失败；完全漏写会被配置校验拒绝。品质仍然至少需要一个，并且每个品质权重必须大于0。例如只有一个“传说”品质时：

```yaml
forge:
  failure:
    weight: 0
    name: '锻造失败'
    color: '&c'
identify:
  enabled: true
  display-name: '&6传说之剑'
  lore:
    - '&7必定锻造成功。'
  qualities:
    传说:
      weight: 1
      color: '&6'
```

最终归一化概率为失败 `0%`、传说 `100%`。普通鉴定同样只会得到传说结果。

当失败概率为30%，原六品质比例为 `28/22/18/14/10/8` 时，最终权重可以写成：

```text
失败 30
白描 19.6
萌黄 15.4
气象 12.6
归元 9.8
传神 7
浮世 5.6
```

总和正好为100。普通右键鉴定会忽略 `forge.failure`，只在六个品质的70权重之间重新归一化，因此普通鉴定仍保持原来的 `28/22/18/14/10/8` 品质比例。

首次启动会额外生成：

```text
plugins/XyItems/items/ForgeItem/ExampleForgeItem.yml
```

该文件包含可直接读取的完整示例。不要把锻造配方写到这里；材料、金币、图纸和失败退款写在XyForgeCrafting的 `ForgeRecipe` 中。

## Lore 占位符与 AP

XyItems 不硬编码 `damage`、`health`、`防御力` 的游戏含义。它只从 `attributes` 取值并替换到最终 Lore，因此 AttributePlus/AP 可以继续按照服务器既有的 Lore 规则读取属性。

支持以下占位符形式：

- 品质：`<品质.颜色>`、`<品质.名称>`、`<quality.color>`、`<quality.name>`
- 任意属性键 `damage`：`<damage>`、`%damage%`、`<%damage%>`
- 中文属性键也可直接使用，例如 `<防御力>`

例如配置 `attributes.damage` 后，`&f✦ 攻击力: &c<%damage%>` 会在物品生成时写成最终数值。AP 读取的是这条最终 Lore，不需要 XyItems 再额外注册一套属性定义。

## 严格背包容量规则

所有由 XyItems 玩家流程产生物品的路径都遵循同一条前置规则：**主背包 36 格中必须至少有一个空格，才能执行下一步。**

- 右键鉴定前先检查空格；没有空格时不扣除未鉴定物品，也不随机品质。
- `/xyitem get` 与 `/xyitems give` 都会先检查容纳全部目标堆叠所需的空格；空间不足时不部分发放、不掉落到地面。
- 提示统一读取 XyCore 的 `messages.prefix`，默认效果为：`[XyCore]您的背包已满无法容纳多余物品。`
- 后续 XyForge、XyEnhance、XyExchange 应调用 XyItems API 的 `hasDeliverySpace` 与 `deliverItems`，不要直接 `Inventory#addItem`。

这条规则也适用于手中只剩一件待鉴定物品的情况，保证所有 XyItems 产物流程行为一致。

## XyCore 物品库接入

启动后，XyItems 会在 XyCore 中注册物品提供器 `xyitems`。其他 XY 插件可通过：

```text
xyitems:<物品ID>
```

创建基础物品，例如 `xyitems:example_rune`。XyCore 负责 NBT 标签底层实现，XyItems 使用这些标签记录物品 ID、未鉴定状态和鉴定品质，名称或 Lore 相同的仿制物不会被误判为 XyItems 物品。鉴定时实际随机出的属性也会写入 NBT，后续插件可通过 `getRolledAttributes` 读取，不需要反向解析 Lore。

### Java API

```java
XyItemsApi api = XyItems.get();
Optional<ItemStack> item = api.createItem("example_rune", 1);

if (api.hasDeliverySpace(player, 1) && item.isPresent()) {
    api.deliverItems(player, Collections.singletonList(item.get()));
}
```

`createItem` 只构造物品，不会修改玩家背包；真正交付必须使用 `deliverItems`，以维持严格容量规则。已鉴定物品的原始随机值可通过 `api.getRolledAttributes(item)` 读取。

锻造插件使用以下API：

```java
Optional<ForgeOutcomeProfile> profile = api.getForgeOutcomeProfile("example_forge_soul");
ForgeRollResult roll = api.rollForgeOutcome("example_forge_soul");

if (roll.isSuccess()) {
    ItemStack identified = roll.getItem().get();
}
```

`rollForgeOutcome` 只进行一次最终抽取。抽中品质后，返回值已经携带按该品质生成并写入品质、随机属性NBT的物品，不允许调用方再次随机品质。`createIdentifiedItem(itemId, qualityId, amount)` 只用于明确指定品质的受信任流程。

## 重载与错误处理

`/xyitems reload` 会先完整校验所有配置文件。只要任一 YAML 或物品定义存在错误，本次重载就会失败并保留当前正在工作的配置，不会出现半加载状态。控制台会输出具体文件和节点。

## 构建

项目附带 1.12.2 编译期 API JAR，避免旧版 Maven 快照失效导致无法构建：

```powershell
.\gradlew.bat clean build --no-daemon
```

产物位于：

```text
build/libs/XyItems-1.0.4.jar
```

## 后续方向

- 锻造、强化、兑换插件统一使用 `XyItemsApi` 的身份识别与库存交付。
- 在保留 NBT 物品 ID 和品质标签的前提下，增加强化等级、锻造词条和绑定状态标签。
- 增加更丰富的物品外观、条件和动作配置，但继续保持事件监听的按需过滤模式。
