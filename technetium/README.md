# Technetium(子模组)

安卓触摸体验优化模组。基于 **TouchController**,把触摸使用体验向基岩版靠拢:
**仿基岩版主菜单** + Mixin 重定向 TC 顶栏「返回主菜单」按钮。

> 放置位置:本目录整个位于 TouchController 的 **fork**(`fishqaq123/TouchController`)内,
> 是一个可独立构建、可独立发布的子模组(类似 fork 里的 `authproxy`/`armorstand`)。

---

## 已实现功能

### 1. 仿基岩版主菜单(基岩版暂停菜单布局)
- 顶部:MINECRAFT 大 logo;
- 屏幕中间一条灰色辅助线(竖线),右侧铺半透明灰色蒙版;
- 左侧:按钮列(回到游戏 / 设置 / 保存并退出),上下紧凑无间隔;
- **保存并退出** 通过反射调用 `ClientLevel.disconnect(Component)`,保存世界并返回标题。
- 入口:通过 Mixin 重定向 TC 顶栏「返回主菜单」按钮 → 打开我们菜单。

### 2. 聊天命令自动补全 + Tab 虚拟按键
- 点 TC 聊天按钮 → 打开我们自研的 `TechnetiumChatScreen`(命令补全 + Tab 键);
- 输入以 `/` 开头时,用反射调 MC 原版 Brigadier 命令补全,实时显示建议列表;
- Tab 按钮应用选中建议,按钮 `focusable=false` 避免抢走输入框焦点。
- 详见 `common/src/main/kotlin/top/technetium/chat/`。

### 3. 图片显示(硬核方案 · 可复用模板)
> 关键约定:以后凡引用图片,一律用这套**硬核方案**,不走官方/combine 纹理管线(太复杂且坑)。

- **为什么**:combine 的 `texture_lib`/`textureManager.getTexture` 对自定义命名空间资源加载不可靠
  (`Missing resource technetium:...`、expect/actual 崩溃)。
- **做法**:把 PNG 转成 **base64 内嵌**进 Kotlin → 运行时 `Base64.decode` → `NativeImage.read`(解码 PNG)
  → `DynamicTexture(Supplier, image)` 建纹理 → `textureManager.register(id, tex)` 拿 id → 自定义 `Drawable`
  在 combine 的 Canvas 底层 `GuiGraphics` 上原生 `blit` 绘制。
- **模板文件**:
  - `common/.../ui/theme/LogoTexture.kt`(base64 内嵌 + 解码 + 注册)
  - `common/.../ui/theme/LogoDrawable.kt`(自定义 Drawable 原生 blit)
- **以后新增图片**:照这两个文件改 base64 + Identifier + size 即可。

---

## 目录结构与职责

```
technetium/
├─ common/                          # 跨 loader 复用
│   └─ src/main/kotlin/top/technetium/
│       ├─ ui/
│       │   ├─ BedrockMenuScreen.kt # 仿基岩主菜单
│       │   ├─ BedrockMenu.kt       # 打开菜单入口(ScreenFactory 桥接)
│       │   └─ theme/
│       │       ├─ LogoTexture.kt   # base64 内嵌 PNG -> NativeImage -> DynamicTexture -> 注册 id
│       │       └─ LogoDrawable.kt  # 自定义 Drawable 原生 blit(硬核图片模板)
│       ├─ chat/                    # 聊天命令补全 + Tab 虚拟按键
│       │   ├─ screen/  model/  state/  CommandSuggestionProvider.kt  ChatMessageProvider.kt ...
│       └─ mixin/
│           ├─ BuiltinWidgetsPauseRedirectMixin.java  # 重定向 TC 顶栏返回按钮
│           └─ ChatScreenProviderRedirectMixin.java   # 重定向 TC 聊天屏 -> 我们聊天屏
└─ versions/
    ├─ fabric/26.2/                 # Fabric 入口 + fabric.mod.json + mixins.json + BUILD
    └─ neoforge/26.2/               # NeoForge 入口 + mods.toml + mixins.json + BUILD
```

---

## 核心机制:重定向 TC 顶栏「返回主菜单」按钮

已核实(TouchController 源码):
- TC 顶栏按钮 `BuiltinWidgets.pause` → `GameActions.gameMenu` → `GameAction::openGameMenu`
  → `client.pauseGame(false)`(原版 Esc);
- `openGameMenu` **只被触摸控件调用,不被键盘 Esc 调用**。

`BuiltinWidgetsPauseRedirectMixin` 在 `GameActionImpl.openGameMenu` 处重定向(`@Mixin(targets="...")`
字符串形式,避免编译期依赖 TC 内部类):
- 按 TC 顶栏按钮 → 打开我们的**仿基岩主菜单**;
- 虚拟键盘按 Esc → 仍进**原版暂停菜单**(不受影响)。
- 即「双轨互斥」:两套菜单入口互不干扰、底层原版逻辑不动。

---

## 构建(仅 GitHub Actions 编译 · 本机无 Bazel)

本机(Android/Termux)无 Bazel,无法本地编译。**构建全靠 fork 的 CI**:

- fork 自带 workflow `.github/workflows/technetium-build.yml`,push 到 `technetium/**` 变更即触发;
- 已给 fork 各 workflow 加了 `paths:` 过滤,只改 `technetium/` 只触发 `technetium-build`,不烧其它构建配额;
- 产物:GitHub Actions 的 Artifacts(`technetium-neoforge-26.2.jar` / `technetium-fabric-26.2.jar`),
  从 Actions run 页面下载。

---

## 状态 / 已知注意点

- **Mixin 目标**:`GameActionImpl` 是 Kotlin object,用 `@Mixin(targets="...字符串...")` 编译期不需要
  依赖 TC 内部类;运行时应用。
- **common 资源打包**:资源放进 loader 的 `versions/<loader>/.../src/main/resources` 才可靠,
  放 common 层可能因 merge 过程丢失。
- **Mc/EI 类路径**:`Component`/`brigadier` 有时在编译类路径不全,用到时优先反射。

---

## 参考(先行者怎么实现)

- combine 官方示例:`combine/example/hello_world`(Fabric,含 ScreenFactory 用法)
- combine 26.2 `ScreenFactory`:`combine/backend/minecraft/screen/26.2/ScreenFactoryImpl.kt`
- TC neoforge 入口参考:`touchcontroller/versions/neoforge/26.2/TouchController.kt`
- **combine 纹理引用机制(教训)**:`combine/theme/blackstone/texture/BUILD.bazel`
  (`texture_lib` + `combine_theme`,非常复杂,已弃用,改硬核方案)
