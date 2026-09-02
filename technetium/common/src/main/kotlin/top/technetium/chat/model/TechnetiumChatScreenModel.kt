/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) fishqaq123
 */

package top.technetium.chat.model

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import net.minecraft.util.StringUtil
import org.apache.commons.lang3.StringUtils
import top.fifthlight.combine.core.paint.Colors
import top.fifthlight.combine.core.util.dispatcher.GameDispatcherProviderFactory
import top.technetium.chat.ChatMessageProvider
import top.technetium.chat.CommandSuggestionProvider
import top.technetium.chat.state.TechnetiumChatScreenState

/**
 * Technetium 聊天界面 Model。
 *
 * 基于 TC 的 ChatScreenModel 重写：
 * - 不依赖 TC 内部类（不继承 TouchControllerScreenModel）
 * - 消息获取/发送通过自己的 ChatMessageProvider 实现
 */
class TechnetiumChatScreenModel {
    private val gameDispatcher = GameDispatcherProviderFactory.of().gameDispatcher
    private val coroutineScope = CoroutineScope(SupervisorJob() + gameDispatcher)
    
    private val chatMessageProvider = ChatMessageProvider
    
    private val _uiState: MutableStateFlow<TechnetiumChatScreenState> = MutableStateFlow(TechnetiumChatScreenState())
    val uiState = _uiState.asStateFlow()

    fun updateText(newText: String) {
        _uiState.getAndUpdate { state ->
            val suggestions = if (newText.startsWith("/")) {
                CommandSuggestionProvider.getSuggestionEntries(newText)
            } else {
                emptyList()
            }
            state.copy(
                text = newText,
                suggestions = suggestions,
                selectedSuggestionIndex = 0,
                isSuggestionVisible = suggestions.isNotEmpty(),
            )
        }
    }

    fun sendText() {
        val text = uiState.value.text
        val trimmed = StringUtil.trimChatMessage(StringUtils.normalizeSpace(text.trim()))
        if (trimmed.isNotEmpty()) {
            chatMessageProvider.sendMessage(trimmed)
        }
        updateText("")
    }

    /**
     * 应用指定索引的建议到输入框。
     */
    fun applySuggestion(index: Int) {
        val state = uiState.value
        if (state.suggestions.isEmpty() || index !in state.suggestions.indices) return
        
        val suggestion = state.suggestions[index]
        val newText = CommandSuggestionProvider.applySuggestion(
            input = state.text,
            suggestion = suggestion.text,
            rangeStart = suggestion.rangeStart,
            rangeEnd = suggestion.rangeEnd,
        )
        updateText(newText)
    }

    /**
     * 应用当前选中的建议（Tab 按键行为）。
     */
    fun applySelectedSuggestion() {
        val state = uiState.value
        if (state.suggestions.isEmpty()) return
        applySuggestion(state.selectedSuggestionIndex)
    }

    /**
     * 选中下一个建议。
     */
    fun selectNextSuggestion() {
        _uiState.getAndUpdate { state ->
            if (state.suggestions.isEmpty()) return@getAndUpdate state
            val next = (state.selectedSuggestionIndex + 1) % state.suggestions.size
            state.copy(selectedSuggestionIndex = next)
        }
    }

    /**
     * 选中上一个建议。
     */
    fun selectPreviousSuggestion() {
        _uiState.getAndUpdate { state ->
            if (state.suggestions.isEmpty()) return@getAndUpdate state
            val prev = (state.selectedSuggestionIndex - 1 + state.suggestions.size) % state.suggestions.size
            state.copy(selectedSuggestionIndex = prev)
        }
    }

    fun openSettingsDialog() {
        _uiState.getAndUpdate { it.copy(settingsDialogOpened = true) }
    }

    fun closeSettingsDialog() {
        _uiState.getAndUpdate { it.copy(settingsDialogOpened = false) }
    }

    fun resetSettings() {
        _uiState.getAndUpdate {
            it.copy(
                lineSpacing = 0,
                textColor = Colors.WHITE,
            )
        }
    }

    fun onDispose() {
        coroutineScope.cancel()
    }
}