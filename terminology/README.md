# 天圆地方 GeoCraft 术语表

收录天圆地方及镍 API（Nickel API）创造并定义的概念，给出中英术语对照，供文档、翻译与开发时查证使用。

## 文件划分

| 文件 | 范围 |
| --- | --- |
| general.yaml | 框架级基础概念（层级、地理属性、地理状态等） |
| atmosphere.yaml | 大气系统 |
| fluidphysics.yaml | 流体物理（基础概念、各模式独有概念、压强系统，文件内以注释分段） |
| fluid-host.yaml | 载流方块（分层流体承载方块及其单位、机制与典型案例雪族） |
| soil.yaml | 土壤系统 |
| commands.yaml | GeoCraft 的命令与命令参数名 |
| nickel.yaml | 镍 API 底层框架（参数类型名、节点等） |
| nickel-nbt.yaml | 镍 API 的 NBT 系统 |
| misc.yaml | 跨系统难以归类的概念（待定区），以及不属于任何游戏系统的概念（如测试框架） |

## 收录判据

看概念的出生地：本模组文档中创造并定义的词才收录（纯文档定义的上层现象也算，无需代码实体）；物理学、地理学等领域已有的词，仅被借用来描述现象的（如虹吸效应）则不收录。配置项命名不作为术语依据。

## 条目格式

```yaml
- 中文: "示例术语"
  英文: "Example Term"
  别称:
    - ["中文别称"]
    - ["English Alias"]
  旧称:
    - ["中文旧称"]
    - ["Old English Name"]
  定义: "一句话定义。"
  出处:
    - "https://www.mcmod.cn/item/xxxxxx.html"
    - "https://github.com/QGMoe/GeoCraft/releases/tag/vX.Y.Z"
```

## 规则

- **英文** 为 null = 尚未决定英文命名，文档与翻译中不得擅自命名。
- 一个概念有多个正式英文名时，**英文** 可为列表，并以 `#` 注释注明各名的适用语境。
- **别称** 为可选字段，值为两个列表：第一个列表为中文别称，第二个为英文别称（含简称、对应类名）。
- **旧称** 为可选字段，结构同别称（先中文后英文），记录历史名称/曾用名。
- 暂定（未正式定名）的名字放入别称，主名字段记 null。
- **定义** 为可选字段，缺省时请见出处文档。
- **出处** 为必填字段，值为列表，且必须为精确链接：仓库内文件指向具体 commit 的 blob/tree 链接，release note 指向对应 release 页，语言文件另注明键名。

## 数据来源

MC 百科资料页（[游戏设定](https://www.mcmod.cn/item/list/22470-10.html)与[方块/物品](https://www.mcmod.cn/item/list/22470-1.html)分类）、语言文件（assets/geocraft/lang、assets/nickelapi/lang）、双语 README 与主仓库 release note。

## 维护

新概念随引入它的提交一同入册；命名、定义或归类发生变化时同步更新对应条目。
