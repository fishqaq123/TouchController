/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) fishqaq123
 */

package top.technetium.chat

import net.minecraft.client.Minecraft

/**
 * 聊天消息提供者 - 通过 MC 原版 API 实现消息发送。
 *
 * 不依赖 TC 内部类，直接使用 MC 原版 API：
 * - 消息以 / 开头时作为命令发送
 * - 否则作为聊天消息发送
 */
object ChatMessageProvider {
    private val client = Minecraft.getInstance()

    /**
     * 发送消息。
     * 消息以 / 开头时作为命令发送，否则作为聊天消息发送。
     */
    fun sendMessage(message: String) {
        if (message.startsWith("/")) {
            client.player?.connection?.sendCommand(message.substring(1))
        } else {
            client.player?.connection?.sendChat(message)
        }
    }
}