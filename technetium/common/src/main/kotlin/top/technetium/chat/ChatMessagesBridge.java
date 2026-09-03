/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) fishqaq123
 */

package top.technetium.chat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.multiplayer.chat.GuiMessage;
import top.technetium.mixin.ChatComponentWithMessages;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 聊天消息桥接 - 通过 Mixin 注入的接口获取聊天消息。
 *
 * 由于 Kotlin 无法直接调用 Mixin 注入的方法（带 $ 的方法名），
 * 且 MC 的 Component 类型继承自 Brigadier 的 Message（编译期不可访问），
 * 这里用 Java 做桥接并直接返回字符串列表。
 */
public final class ChatMessagesBridge {
    private ChatMessagesBridge() {}

    /**
     * 获取聊天消息文本列表（最新的在最后）。
     */
    public static List<String> getMessageTexts() {
        Minecraft client = Minecraft.getInstance();
        ChatComponent chatComponent = client.gui.hud.chat;
        if (chatComponent instanceof ChatComponentWithMessages) {
            List<GuiMessage> messages = ((ChatComponentWithMessages) chatComponent).technetium$getMessages();
            List<String> result = new ArrayList<>(messages.size());
            for (GuiMessage message : messages) {
                result.add(message.content().getString());
            }
            return result;
        }
        return Collections.emptyList();
    }
}