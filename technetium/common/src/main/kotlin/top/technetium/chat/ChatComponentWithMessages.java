/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) fishqaq123
 */

package top.technetium.chat;

import net.minecraft.client.multiplayer.chat.GuiMessage;

import java.util.List;

/**
 * 获取 MC ChatComponent 中的消息列表接口。
 * 
 * 通过 Mixin 注入到 net.minecraft.client.gui.components.ChatComponent，
 * 获取其 allMessages 字段（与 TC 的实现一致）。
 */
public interface ChatComponentWithMessages {
    List<GuiMessage> technetium$getMessages();
}