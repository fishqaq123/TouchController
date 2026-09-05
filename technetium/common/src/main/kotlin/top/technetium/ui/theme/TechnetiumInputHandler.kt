/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) fishqaq123
 */

package top.technetium.ui.theme

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import net.minecraft.client.Minecraft
import top.fifthlight.combine.core.input.text.InputHandler
import top.fifthlight.combine.core.input.text.TextInputState
import top.fifthlight.data.IntRect

/**
 * Technetium 自己的 InputHandler 实现。
 *
 * 直接调用 MC 的文本输入管理（TextInputManager），实现键盘呼出。
 */
object TechnetiumInputHandler : InputHandler {
    private val eventsFlow = MutableSharedFlow<TextInputState>()
    
    override val events: SharedFlow<TextInputState> = eventsFlow.asSharedFlow()
    
    private var haveState = false

    override fun updateInputState(textInputState: TextInputState?, cursorRect: IntRect?, areaRect: IntRect?) {
        val client = Minecraft.getInstance()
        val textInputManager = client.textInputManager()
        
        if (!haveState && textInputState != null) {
            textInputManager.startTextInput()
        } else if (haveState && textInputState == null) {
            textInputManager.stopTextInput()
        }
        
        haveState = textInputState != null
    }

    override fun tryShowKeyboard() {
        val client = Minecraft.getInstance()
        val textInputManager = client.textInputManager()
        textInputManager.startTextInput()
    }

    override fun tryHideKeyboard() {
        val client = Minecraft.getInstance()
        val textInputManager = client.textInputManager()
        textInputManager.stopTextInput()
    }
}