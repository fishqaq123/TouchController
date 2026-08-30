/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) fishqaq123
 */

package top.technetium.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
 *  - 右侧:在线玩家列表信息框(1 当前玩家 / 2 player / ...)
 *
 * 说明:combine 是 Compose 风格,按钮点击回调直接写 onClick。
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
                        // TODO: 真正断开回标题。MVP 先占位,后面接原版回标题逻辑。
                        onClose.close()
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

/** 右侧:在线玩家列表信息框。读取真实在线玩家(multiplayer 连接)。 */
@Composable
private fun PlayerListBox() {
    val players = remember {
        readOnlinePlayers()
    }
    Box(
        modifier = Modifier
            .width(140)
            .height(120)
            .padding(4),
    ) {
        Column(
            modifier = Modifier.padding(8),
            verticalArrangement = Arrangement.spacedBy(4),
        ) {
            if (players.isEmpty()) {
                Text("暂无在线玩家", modifier = Modifier.fillMaxWidth())
            } else {
                players.forEachIndexed { index, name ->
                    Text("${index + 1} $name", modifier = Modifier.fillMaxWidth())
                }
            }
        }
    }
}

/** 从 Minecraft 客户端连接读取在线玩家名字列表(真实读取)。 */
private fun readOnlinePlayers(): List<String> {
    val player = Minecraft.getInstance().player ?: return emptyList()
    val connection = player.connection
    return if (connection != null) {
        connection.getOnlinePlayers().map { it.profile.name }
    } else {
        listOf(player.name.string)
    }
}
