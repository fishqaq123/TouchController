/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) fishqaq123
 */

package top.technetium.chat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.multiplayer.chat.GuiMessage;
import top.technetium.chat.ChatComponentWithMessages;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 聊天消息桥接 - 通过 Mixin 注入的接口获取聊天消息。
 *
 * 由于 Kotlin 无法直接调用 Mixin 注入的方法（带 $ 的方法名），
 * 且 MC 的 Component 类型继承自 Brigadier 的 Message（编译期不可访问），
 * 这里用 Java + 反射做桥接并直接返回字符串列表。
 */
public final class ChatMessagesBridge {
    private ChatMessagesBridge() {}

    /**
     * 获取聊天消息文本列表（最新的在最后）。
     */
    public static List<String> getMessageTexts() {
        Minecraft client = Minecraft.getInstance();
        try {
            // 通过反射获取 Hud.chat 私有字段
            Field chatField = client.gui.hud.getClass().getDeclaredField("chat");
            chatField.setAccessible(true);
            Object chatComponentObj = chatField.get(client.gui.hud);

            if (chatComponentObj instanceof ChatComponentWithMessages) {
                List<GuiMessage> messages = ((ChatComponentWithMessages) chatComponentObj).technetium$getMessages();
                List<String> result = new ArrayList<>(messages.size());
                for (GuiMessage message : messages) {
                    // 通过反射获取 content().getString()
                    // 避免直接访问 Component（继承自 Brigadier 的 Message，编译期不可访问）
                    Object content = message.content();
                    Method getStringMethod = content.getClass().getMethod("getString");
                    String text = (String) getStringMethod.invoke(content);
                    result.add(text);
                }
                return result;
            }
        } catch (Exception e) {
            // 忽略异常，返回空列表
        }
        return Collections.emptyList();
    }
}