# Technetium(子模组)

安卓触摸体验优化模组。基于 **TouchController**,把触摸使用体验向基岩版靠拢:
**仿基岩版主菜单** + Mixin 重定向 TC 顶栏「返回主菜单」按钮。

> 放置位置:本目录未来是整个 TouchController **fork** 内的一个子模组目录
> (类似 fork 里的 `authproxy`/`armorstand`),可独立构建、独立发布。

## 阶段性路线(A → 过渡 NeoForge)

1. **现在(Fabric 26.2 验证)**:先用 combine 官方 hello_world(Fabric)模板跑通
   「仿基岩菜单 + Mixin 重定向」核心逻辑。
2. **过渡(NeoForge 26.2)**:功能验证通过后,补 `versions/neoforge/26.2/` 的入口类
   + `neoforge.mods.toml` + `neoforge.mixins.json` + BUILD,复用 `common/` 的 UI 与 Mixin,
   只改 loader 壳。已按此(common + versions 分层)组织,过渡成本低。
3. 后续:容器关闭键、聊天命令补全(依赖 combine/TC 输入层)。

## 目录结构与职责

```
technetium/
├─ common/                          # 跨 loader 复用
│   └─ src/main/kotlin/top/technetium/
│       ├─ ui/BedrockMenuScreen.kt  # 仿基岩主菜单(combine Compose)
│       ├─ ui/BedrockMenu.kt        # 打开菜单的入口(ScreenFactory 桥接)
│       └─ mixin/BuiltinWidgetsPauseRedirectMixin.java  # 重定向 TC 按钮
└─ versions/
    ├─ fabric/26.2/                 # Fabric 入口 + fabric.mod.json + mixins.json + BUILD
    └─ neoforge/26.2/               # (过渡期新增)NeoForge 入口 + mods.toml + mixins.json + BUILD
```

## 核心机制:重定向 TC 顶栏「返回主菜单」按钮

已核实(TouchController 源码):
- TC 顶栏按钮 `BuiltinWidgets.pause` → `GameActions.gameMenu` → `GameAction::openGameMenu`
  → `client.pauseGame(false)`(原版 Esc)。
- `openGameMenu` **只被触摸控件调用,不被键盘 Esc 调用**。

因此 `BuiltinWidgetsPauseRedirectMixin` 在 `GameActionImpl.openGameMenu` 处重定向:
- 按 TC 顶栏按钮 → 打开我们的**仿基岩主菜单**(combine);
- 虚拟键盘按 Esc → 仍进**原版暂停菜单**(不受影响)。
这实现「双轨互斥」的交互设计:两套菜单入口互不干扰、底层原版逻辑不动。

## 构建(Bazel,在 fork 内)

```bash
# 编译 Fabric 26.2 版 mod jar
bazel build //technetium/versions/fabric/26.2:technetium
# 产物流放在 bazel-bin/technetium/versions/fabric/26.2/technetium.jar
```

## 状态/已知待验证点

- [ ] **BUILD.bazel**:NeoForge 26.2 独立 mod + combine 无官方模板(combine 只有 Fabric 示例),
      依赖图/打包规则是盲写,需在 fork 内编译验证、按 CI 报错修正。
- [ ] **Mixin 目标**:`GameActionImpl` 是 Kotlin object,其组装后字节码/类名需按目标 MC 版本核实;
      Mixin 对 Kotlin object 的 @Inject 需验证。
- [ ] combine/TC 依赖的**类路径可见性**:Mixin 目标类位于 TC 内部(非 public visibility),
      编译期是否可引用见 CI。

## 参考

- combine 官方示例:`combine/example/hello_world`(Fabric,含 ScreenFactory 用法)
- combine 26.2 ScreenFactory 实现:`combine/backend/minecraft/screen/26.2/ScreenFactoryImpl.kt`
- TC neoforge 入口参考:`touchcontroller/versions/neoforge/26.2/TouchController.kt`
