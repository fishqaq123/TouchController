/*
 * SPDX-License-Identifier: LGPL-3.0-or-later
 * Copyright (C) fishqaq123
 */

package top.technetium.mixin;

import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.multiplayer.chat.GuiMessage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import top.technetium.chat.ChatComponentWithMessages;

import java.util.List;

/**
 * 注入 MC ChatComponent，获取消息列表。
 *
 * 与 TC 的实现类似，但使用 technetium 前缀避免冲突。
 */
@Mixin(ChatComponent.class)
public abstract class ChatComponentMixin implements ChatComponentWithMessages {
    @Shadow
    @Final
    private List<GuiMessage> allMessages;

    @Override
    public List<GuiMessage> technetium$getMessages() {
        return allMessages;
    }
}