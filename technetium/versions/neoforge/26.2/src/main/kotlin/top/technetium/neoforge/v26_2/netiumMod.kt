/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) fishqaq123
 */

package top.technetium.fabric.v26_2

import net.fabricmc.api.ClientModInitializer
import top.technetium.mixin.BuiltinWidgetsPauseRedirectMixin

/**
 * Technetium — Fabric 26.2 客户端入口。
 *
 * 结构参考 combine 官方示例 hello_world(versions/26.2/HelloWorldMod.kt)。
 *
 * 当前作用(阶段 1 Fabric 验证):
 *  - 注册为 client mod。
 *  - 核心功能(仿基岩菜单 + Mixin 重定向 TC 顶栏按钮)由 Mixin 完成:
 *    BuiltinWidgetsPauseRedirectMixin 把 TC「返回主菜单」按钮的 openGameMenu 重定向到
 *    我们的仿基岩菜单(用 combine 渲染),而虚拟键盘 Esc 仍进原版暂停菜单。
 */
class TechnetiumMod : ClientModInitializer {
    override fun onInitializeClient() {
        // 阶段 1:无需额外初始化。Mixin 已挂载。
        // 若需要按键绑定测开菜单(与 Mixin 解耦),可仿 hello_world 加一个 KeyMapping。
    }
}
