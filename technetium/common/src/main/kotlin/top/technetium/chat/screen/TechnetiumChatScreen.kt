/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) fishqaq123
 */

package top.technetium.chat.screen

import androidx.compose.runtime.*
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.Screen
import top.fifthlight.combine.core.data.Text
import top.fifthlight.combine.core.input.interaction.MutableInteractionSource
import top.fifthlight.combine.core.layout.Alignment
import top.fifthlight.combine.core.layout.Arrangement
import top.fifthlight.combine.core.modifier.Modifier
import top.fifthlight.combine.core.modifier.drawing.background
import top.fifthlight.combine.core.modifier.focus.FocusInteraction
import top.fifthlight.combine.core.modifier.focus.FocusRequester
import top.fifthlight.combine.core.modifier.focus.focusRequester
import top.fifthlight.combine.core.modifier.placement.fillMaxHeight
import top.fifthlight.combine.core.modifier.placement.fillMaxSize
import top.fifthlight.combine.core.modifier.placement.fillMaxWidth
import top.fifthlight.combine.core.modifier.placement.height
import top.fifthlight.combine.core.modifier.placement.width
import top.fifthlight.combine.core.modifier.scroll.verticalScroll
import top.fifthlight.combine.core.paint.Colors
import top.fifthlight.combine.core.screen.LocalCloseHandler
import top.fifthlight.combine.core.screen.ScreenFactoryFactory
import top.fifthlight.combine.core.widget.layout.Column
import top.fifthlight.combine.core.widget.layout.Row
import top.fifthlight.combine.theme.invoke
import top.fifthlight.combine.theme.vanilla.VanillaTheme
import top.fifthlight.combine.widget.Button
import top.fifthlight.combine.widget.EditText
import top.fifthlight.combine.widget.Text
import top.technetium.chat.ChatMessagesBridge
import top.technetium.chat.model.TechnetiumChatScreenModel

/**
 * Technetium 聊天界面。
 *
 * 基于 TC 的 ChatScreen 重写，使用 combine 公开 API + VanillaTheme：
 * - 新增命令补全建议列表（当输入 / 开头时显示）
 * - 新增 Tab 虚拟按键（用于快速补全）
 * - 消息获取通过 ChatComponentMixin 注入到 MC ChatComponent
 */
@Composable
fun TechnetiumChatScreen() {
    val screenModel = remember { TechnetiumChatScreenModel() }
    DisposableEffect(screenModel) {
        onDispose {
            screenModel.onDispose()
        }
    }

    VanillaTheme {
        val uiState by screenModel.uiState.collectAsState()
        val onClose = LocalCloseHandler.current

        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            // 顶部标题栏
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(20)
                    .background(Colors.GRAY),
                horizontalArrangement = Arrangement.spacedBy(4),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Button(
                    onClick = { onClose.close() },
                    modifier = Modifier.height(16),
                ) {
                    Text("< 退出")
                }
                Text(
                    text = "聊天与命令",
                    modifier = Modifier.weight(1f),
                )
            }

            // 消息列表区域
            var messages by remember { mutableStateOf(emptyList<String>()) }
            LaunchedEffect(Unit) {
                while (true) {
                    withFrameMillis { _ ->
                        messages = ChatMessagesBridge.getMessageTexts().reversed()
                    }
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(true)
                    .background(Colors.TRANSPARENT_BLACK),
                verticalArrangement = Arrangement.spacedBy(0, Alignment.Bottom),
            ) {
                for (message in messages) {
                    Text(
                        text = message,
                        color = uiState.textColor,
                    )
                }
            }

            // 命令补全建议列表
            if (uiState.isSuggestionVisible && uiState.suggestions.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Colors.TRANSPARENT_BLACK),
                ) {
                    for ((index, suggestion) in uiState.suggestions.withIndex()) {
                        val isSelected = index == uiState.selectedSuggestionIndex
                        Button(
                            onClick = { screenModel.applySuggestion(index) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(16),
                        ) {
                            Text(
                                text = if (isSelected) {
                                    "> ${suggestion.text}"
                                } else {
                                    suggestion.text
                                }
                            )
                        }
                    }
                }
            }

            // 底部输入栏
            val bottomBarHeight = 32
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(bottomBarHeight),
            ) {
                val focusRequester = remember { FocusRequester() }
                val interactionSource = remember { MutableInteractionSource() }
                var focused by remember { mutableStateOf(false) }
                LaunchedEffect(interactionSource) {
                    interactionSource.interactions.collect {
                        when (it) {
                            FocusInteraction.Blur -> {
                                focused = false
                            }

                            FocusInteraction.Focus -> {
                                focused = true
                            }
                        }
                    }
                }

                // Tab 虚拟按键 - 用于命令补全
                Button(
                    onClick = {
                        screenModel.applySelectedSuggestion()
                    },
                    modifier = Modifier
                        .width(bottomBarHeight)
                        .fillMaxHeight(),
                ) {
                    Text("Tab")
                }

                // 输入框
                EditText(
                    interactionSource = interactionSource,
                    modifier = Modifier
                        .focusRequester(focusRequester)
                        .weight(1f)
                        .fillMaxHeight(),
                    value = uiState.text,
                    onValueChanged = screenModel::updateText,
                    onEnter = screenModel::sendText,
                )

                // 发送按钮
                Button(
                    onClick = screenModel::sendText,
                    modifier = Modifier
                        .width(64)
                        .fillMaxHeight(),
                ) {
                    Text("发送")
                }
            }
        }
    }
}

/**
 * 打开 Technetium 聊天界面的入口。
 */
object TechnetiumChatScreen {
    fun openFor(client: Minecraft) {
        val parent = client.gui.screen()
        val screen = ScreenFactoryFactory.of().getScreen(
            parent = parent,
            renderBackground = false,
            title = Text.literal("聊天与命令"),
        ) {
            TechnetiumChatScreen()
        } as Screen
        client.gui.setScreen(screen)
    }
}