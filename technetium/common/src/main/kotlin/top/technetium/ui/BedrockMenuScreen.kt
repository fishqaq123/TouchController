/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) fishqaq123
 */

package top.technetium.ui

import androidx.compose.runtime.Composable
import net.minecraft.client.Minecraft
import top.fifthlight.combine.core.layout.Alignment
import top.fifthlight.combine.core.layout.Arrangement
import top.fifthlight.combine.core.modifier.Modifier
import top.fifthlight.combine.core.modifier.placement.fillMaxSize
import top.fifthlight.combine.core.modifier.placement.fillMaxWidth
import top.fifthlight.combine.core.modifier.placement.height
import top.fifthlight.combine.core.modifier.placement.padding
import top.fifthlight.combine.core.modifier.placement.width
import top.fifthlight.combine.core.screen.LocalCloseHandler
import top.fifthlight.combine.core.widget.layout.Box
import top.fifthlight.combine.core.widget.layout.Column
import top.fifthlight.combine.core.widget.layout.Row
import top.fifthlight.combine.theme.blackstone.BlackstoneTheme
import top.fifthlight.combine.theme.invoke
import top.fifthlight.combine.widget.Button
import top.fifthlight.combine.widget.Text

/**
 * 仿基岩版主菜单(combine Blackstone 风格 = TC 视觉风格的基础)。
 *
 * 布局(参照基岩版暂停菜单):
 *  - 左列:返回 / 设置 / 返回标题 三个大按钮
 *  - 右侧:在线玩家列表信息框(1 当前玩家 / 2 Player / ...)
 *
 * 说明:combine 是 Compose 风格,按钮点击回调直接写 onClick。
 *      玩家列表当前为占位文本(真实在线玩家读取需额外 authlib/brigadier 编译依赖,后续再加)。
 */
@Composable
fun BedrockMenuScreen() {
    // combine 公开的 Blackstone 主题(TC 视觉风格的基础)。TouchControllerTheme 内部类不可见,故用此。
    BlackstoneTheme {
        val onClose = LocalCloseHandler.current
        Box(
            modifier = Modifier.fillMaxSize(),
            alignment = Alignment.Center,
        ) {
            Row(
                modifier = Modifier.width(300),
                horizontalArrangement = Arrangement.spacedBy(12),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // —— 左侧按钮列 ——
                Column(
                    modifier = Modifier.width(140),
                    verticalArrangement = Arrangement.spacedBy(8),
                ) {
                    MenuButton("返回") {
                        onClose.close() // 关闭本菜单,回到游戏
                    }
                    MenuButton("设置") {
                        // TODO: 打开游戏设置(原版 options),或后续做基岩风格设置页。
                        onClose.close()
                    }
                    MenuButton("返回标题") {
                        // 保存并退出到标题(ClientLevel.disconnect() 保存当前世界并断开返回主菜单)。
                        onClose.close()
                        Minecraft.getInstance().level?.disconnect()
                    }
                }

                // —— 右侧在线玩家列表 ——
                PlayerListBox()
            }
        }
    }
}

/** 基岩风格大按钮封装。 */
@Composable
private fun MenuButton(text: String, enabled: Boolean = true, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth().height(24),
    ) {
        Text(text)
    }
}

/** 右侧:在线玩家列表信息框。(当前为占位文本,真读玩家后续实现。) */
@Composable
private fun PlayerListBox() {
    // 占位:列表内容(真实玩家读取需 authlib/brigadier 编译依赖,单独迭代)。
    val players = listOf("当前玩家", "Player")
    Box(
        modifier = Modifier.width(140).height(120).padding(4),
    ) {
        Column(
            modifier = Modifier.padding(8),
            verticalArrangement = Arrangement.spacedBy(4),
        ) {
            players.forEachIndexed { index, name ->
                Text("${index + 1} $name", modifier = Modifier.fillMaxWidth())
            }
        }
    }
}
