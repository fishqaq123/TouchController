/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) fishqaq123
 */

package top.technetium.chat.state

import top.fifthlight.combine.core.paint.Color
import top.fifthlight.combine.core.paint.Colors
import top.technetium.chat.SuggestionEntry

/**
 * Technetium 聊天界面状态。
 *
 * 在 TC 的 ChatScreenState 基础上新增：
 * - suggestions: 命令建议列表
 * - selectedSuggestionIndex: 当前选中的建议索引
 * - isSuggestionVisible: 建议列表是否可见
 */
data class TechnetiumChatScreenState(
    val text: String = "",
    val lineSpacing: Int = 0,
    val textColor: Color = Colors.WHITE,
    val settingsDialogOpened: Boolean = false,
    // 新增：命令补全相关
    val suggestions: List<SuggestionEntry> = emptyList(),
    val selectedSuggestionIndex: Int = 0,
    val isSuggestionVisible: Boolean = false,
)