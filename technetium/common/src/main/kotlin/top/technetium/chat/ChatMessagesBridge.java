/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) fishqaq123
 */

package top.technetium.chat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.multiplayer.chat.GuiMessage;
import top.technetium.mixin.ChatComponentWithMessages;

import java.util.List;

/**
 * 聊天消息桥接 - 通过 Mixin 注入的接口获取聊天消息。
 *
 * 由于 Kotlin 无法直接调用 Mixin 注入的方法（带 $ 的方法名），
 * 这里用 Java 做一个桥接。
 */
public final class ChatMessagesBridge {
    private ChatMessagesBridge() {}

    /**
     * 获取聊天消息列表（最新的在最后）。
     */
    public static List<GuiMessage> getMessages() {
        Minecraft client = Minecraft.getInstance();
        ChatComponent chatComponent = client.gui.hud.chat;
        if (chatComponent instanceof ChatComponentWithMessages) {
            return ((ChatComponentWithMessages) chatComponent).technetium$getMessages();
        }
        return java.util.Collections.emptyList();
    }
}