# Technetium 子模组搭建蓝图(A2 · fork 内子模组)

> 状态:设计定稿 · 目标:在 `fishqaq123/TouchController` fork 内,把 Technetium 建成一个像
> `authproxy` / `armorstand` 那样**可独立发布的 fabric mod 子模组**。
> 写法模板:**authproxy**(最小 Mixin mod,文件极少,正好匹配"用 Mixin 改行为"的思路)。

---

## 一、为什么用"fork 内子模组"(A2)

TC 是 monorepo,内部工具链(`zig_cc` 等)为"TC 在仓库根/自己的工程"设计。
实测多次证明:**把 TC 作为外部 git_override 依赖消费,会在 zig_sdk 等内部环节失败**。
因此改为:
- fork `TouchController/TouchController` -> `fishqaq123/TouchController`(已完成,head=上游 98f2134)
- Technetium 作为 fork 里的一个子目录(子模组),像 authproxy 一样**单独构建、独立发布**。

### 注意(当前设备约束)
本执行环境 `github.com:443` 无法直连,无法 `git clone/push` 到 fork;只能走
`api.github.com`(REST API)。因此:
- **代码先写在本地/本仓库(Technetium 私人仓),再由你在能连 GitHub 的环境并入 fork**。
- 不在这台设备上往 fork 硬推代码。

---

## 二、Technetium 要做什么(三件套,按优先级)

| UI 改造 | 手段 | 编译期依赖 |
|---|---|---|
| ① 顶栏「返回主菜单」按钮 → 打开**仿基岩版主菜单** | **Mixin 重定向** TC 内置 `pause` 按钮触发动作 | 不依赖 TC 编译目标(字符串/运行时) |
| ② 容器/箱子 GUI 加**虚拟关闭键**(手机友好) | 后续,可 Mixin 或 combine 自绘 | — |
| ③ 聊天框输入加**命令自动补全 + 提示** | 后续,依赖 TC 聊天输入层/combine | 需 combine |

**核心 MVP 是 ①:仿基岩主菜单 + Mixin 重定向 TC pause 按钮。**

---

## 三、可见性(已核实 — 决定能不能编译)

| 目标 | visibility | Technetium 能否依赖 |
|---|---|---|
| `//combine:combine` | `//visibility:public` | ✅ **可以(画仿基岩菜单用这个)** |
| `//touchcontroller`(主) | 默认 private | ❌ 不能跨目录编译期依赖 |
| `//touchcontroller/api` | `//touchcontroller:__subpackages__` | ❌ 非 public |
| TC 内部实现类(`BuiltinWidgets` 等) | —(Mixin 目标) | ✅ **Mixin 用字符串引用,不需要编译期依赖** |

**结论:** Technetium 编译期只需依赖 `combine`(写 UI)+ Mixin 框架;碰 TC 内部类全部走 Mixin(字符串引用),不撞 visibility 限制。

---

## 四、子模组结构(参照 authproxy)

```
technetium/
├─ BUILD.bazel            # 构建;deps = combine + mixin + minecraft client
├─ LICENSE
├─ resources/
│   ├─ fabric.mod.json    # id=technetium, mixins 指向 technetium.mixins.json, depends
│   ├─ technetium.mixins.json
│   └─ icon.png
└─ src/main/java/top/technetium/
    ├─ mixin/
    │   └─ BuiltinWidgetsMixin.java   # ★ Mixin 重定向 TC pause 按钮(核心)
    ├─ ui/
    │   └─ BedrockMenuScreen.kt       # 仿基岩版主菜单(用 combine / Compose 风格)
    └─ TechnetiumClient.java           # 客户端入口(fabric entrypoint)
```

---

## 五、核心 Mixin:重定向 TC 顶栏 pause 按钮

参考 `authproxy/MainMixin.java` 的 `@Redirect` 写法 + `touchcontroller/versions/fabric/*/mixin/KeyBindingMixin.java` 的样式。

目标(在 TC 源码中核实过):
- TC 顶栏按钮: `Common/control/builtin/BuiltinWidgets.pause` 触发 `GameActions.gameMenu`
- `GameActions.gameMenu` -> `GameAction::openGameMenu` -> `client.pauseGame(false)`(原版 Esc)
- 我们重定向:让 `pause`(返回主菜单)按钮打开**我们的仿基岩菜单**,而非原版。

Mixin 目标方向:
- `@Mixin(BuiltInWidgets.class)` 重定向 `pause` 的 getter/动作(方案 A),或
- `@Mixin(BuiltInWidget.class)` 拦截 `get(TextureSet)` 返回我们的 widget(方案 B)
- 具体 target 类名/方法签名需按 TC 组装后字节码确定(TC 有 multijar 打包,需对目标 MC 版本)。

---

## 六、关键决策点(推进前需定)

- [ ] **目标 MC 版本 / modloader**:fabric 1.21.x 还是 26.x?(决定 fabric.mod.json depends、mixin compatibilityLevel、依赖 @minecraft//ver 坐标)
- [ ] **仿基岩菜单的 combine 版本**:combine 是 public,但写它需要 combine 的 Compose 风格 API —— 确认从 `//combine:combine` 能拿到写 Screen 的全部能力
- [ ] **Mixin target**:需本地有对应 MC 版本的 TC 组装产物才能确定精确签名(这台设备难做,可在你有环境的机器上生成)

---

## 七、即时可做、不受环境约束的部分

即使没有 MC 环境,现在就能写:
1. `fabric.mod.json`(id/name/description/depends 框架)
2. `technetium.mixins.json`(package/空 client 列表占位)
3. 仿基岩菜单 UI 的**combine/Compose 代码**(参考 TC `ChatScreen.kt` 结构)
4. Mixin 骨架(类名/方法签名按 TC 源码先写,具体 target 后续校准)

→ 这些都存进 `Technetium` 私人仓,等你网络环境好再并入 fork。
