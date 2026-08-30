/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) fishqaq123
 */

package top.technetium.ui

import androidx.compose.runtime.Composable
import top.fifthlight.combine.core.layout.Alignment
import top.fifthlight.combine.core.layout.Arrangement
import top.fifthlight.combine.core.modifier.Modifier
import top.fifthlight.combine.core.modifier.placement.fillMaxSize
import top.fifthlight.combine.core.modifier.placement.fillMaxWidth
import top.fifthlight.combine.core.modifier.placement.height
import top.fifthlight.combine.core.modifier.placement.padding
import top.fifthlight.combine.core.modifier.placement.width
import top.fifthlight.combine.core.widget.layout.Box
import top.fifthlight.combine.core.widget.layout.Column
import top.fifthlight.combine.core.screen.LocalCloseHandler
import top.fifthlight.combine.theme.invoke
import top.fifthlight.combine.theme.vanilla.VanillaTheme
import top.fifthlight.combine.widget.Button
import top.fifthlight.combine.widget.Text

/**
 * 仿基岩版主菜单。
 *
 * 说明:这是 MVP 阶段最简单的「大按钮菜单」—— 用 combine 的 VanillaTheme + Row/Column + Button
 * 堆出手机友好的、基岩风格的大按钮布局。
 * 后续可按需:换成 TouchControllerTheme(BLACKSTONE)、加背景纹理、加游戏设置子页面等。
 *
 * 注意:combine 是 Compose 风格,按钮点击回调直接可写 onClick。
 */
@Composable
fun BedrockMenuScreen() {
    VanillaTheme {
        val onClose = LocalCloseHandler.current
        Box(
            modifier = Modifier.fillMaxSize(),
            alignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier
                    .width(220)
                    .padding(top = 24),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8),
            ) {
                // 标题
                Text("Technetium", modifier = Modifier.padding(bottom = 16))

                // —— 基岩版风格大按钮 ——
                MenuButton("继续游戏") {
                    onClose.close()  // 关闭本菜单(回到游戏)
                }
                MenuButton("设置") {
                    // TODO: 打开游戏设置(原版 options),或后续做基岩风格设置页。
                    onClose.close()
                }
                MenuButton("世界", enabled = false) {
                    // 占位:基岩版有 世界/服务器 大按钮。MVP 先禁用占位。
                }
                MenuButton("退出到主菜单") {
                    // 这里应该真正断开回标题;MVP 先占位,后面接原版回标题逻辑。
                    onClose.close()
                }
            }
        }
    }
}

/** 床岩风格大按钮封装。 */
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
